package com.cartex.application.usecase;

import com.cartex.application.dto.report.CreditsByMaturityReportDto;
import com.cartex.application.dto.report.LoanListReportDto;
import com.cartex.application.dto.report.PaymentsDetailsReportDto;
import com.cartex.application.dto.report.SAdminReportRequestDto;
import com.cartex.domain.port.ExternalApiPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportUseCase {

    private final ExternalApiPort externalApiPort;

    @Value("${sadmin.unomasuno.username:}")
    private String unoMasUnoUsername;

    @Value("${sadmin.unomasuno.password:}")
    private String unoMasUnoPassword;

    private String toSAdminDate(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) return isoDate;
        // SAdmin/SQL Server accepts ISO yyyy-MM-dd universally regardless of locale
        // Do NOT convert to MM/dd/yyyy as it causes "string to date conversion error"
        // in Spanish locale (es-co) where month/day are ambiguous
        return isoDate;
    }

    public CreditsByMaturityReportDto getCreditsByMaturity(String cutoffDate, String creditType, String branch, String clientGroup) {
        String sadminDate = toSAdminDate(cutoffDate);
        log.info("Fetching CREDITS_BY_MATURITY for cutoffDate={} (SAdmin format: {})", cutoffDate, sadminDate);

        String token = externalApiPort.authenticateAndGetToken(unoMasUnoUsername, unoMasUnoPassword);

        // Calculate first day of year from cutoffDate for LOAN_LIST range
        LocalDate cutoff = LocalDate.parse(sadminDate);
        String startOfYear = cutoff.withDayOfYear(1).toString();

        // 1. Fetch LOAN_LIST to get the set of credit numbers to consider
        SAdminReportRequestDto loanListRequest = SAdminReportRequestDto.builder()
                .reportid("LOAN_LIST")
                .startDate(startOfYear)
                .endDate(sadminDate)
                .balanceType(1)
                .creditType(creditType != null ? creditType : "ALL")
                .branch(branch != null ? branch : "ALL")
                .clientGroup(clientGroup != null ? clientGroup : "ALL")
                .advisorid("ALL")
                .investorid("ALL")
                .build();

        LoanListReportDto loanListResult = externalApiPort.executeWithToken(
                "/generate_report",
                loanListRequest,
                LoanListReportDto.class,
                token
        );

        Set<String> loanNumCreds = new HashSet<>();
        if (loanListResult != null && loanListResult.getData() != null) {
            loanNumCreds = loanListResult.getData().stream()
                    .map(LoanListReportDto.LoanItem::getNumCred)
                    .filter(n -> n != null && !n.isBlank())
                    .collect(Collectors.toSet());
        }
        log.info("LOAN_LIST returned {} distinct credit numbers", loanNumCreds.size());
        if (!loanNumCreds.isEmpty()) {
            log.info("LOAN_LIST sample numCreds: {}", loanNumCreds.stream().limit(5).collect(Collectors.toList()));
        }

        // 2. Fetch CREDITS_BY_MATURITY (reporte por edades)
        SAdminReportRequestDto request = SAdminReportRequestDto.builder()
                .reportid("CREDITS_BY_MATURITY")
                .cutoffDate(sadminDate)
                .balanceType(-1)
                .creditType(creditType != null ? creditType : "ALL")
                .advisorid("ALL")
                .branch(branch != null ? branch : "ALL")
                .clientGroup(clientGroup != null ? clientGroup : "ALL")
                .excludeRestrictedList("NONE")
                .numberOfDecimals(2)
                .identif("ALL")
                .names("")
                .build();

        CreditsByMaturityReportDto result = externalApiPort.executeWithToken(
                "/generate_report",
                request,
                CreditsByMaturityReportDto.class,
                token
        );

        if (result != null && result.getData() != null) {
            log.info("CREDITS_BY_MATURITY returned {} items", result.getData().size());
            List<String> sampleNumCreds = result.getData().stream()
                    .map(CreditsByMaturityReportDto.CreditItem::getNumCred)
                    .filter(n -> n != null && !n.isBlank())
                    .limit(5)
                    .collect(Collectors.toList());
            log.info("CREDITS_BY_MATURITY sample numCreds: {}", sampleNumCreds);
        }

        postProcessCreditsByMaturity(result, loanNumCreds);
        return result;
    }

    private void postProcessCreditsByMaturity(CreditsByMaturityReportDto result, Set<String> loanNumCreds) {
        if (result == null || result.getData() == null || result.getVariables() == null) {
            log.warn("CREDITS_BY_MATURITY response or data/variables is null, skipping post-processing");
            return;
        }

        List<CreditsByMaturityReportDto.CreditItem> items = result.getData();

        long nullNumCredCount = items.stream().filter(i -> i.getNumCred() == null).count();
        log.info("CREDITS_BY_MATURITY items with null numCred: {}/{} ({}%)",
                nullNumCredCount, items.size(), items.size() > 0 ? (nullNumCredCount * 100 / items.size()) : 0);

        double sumMontoIni = items.stream()
                .mapToDouble(item -> item.getMontoIni() != null ? item.getMontoIni() : 0.0)
                .sum();
        double sumSaldoCap = items.stream()
                .mapToDouble(item -> item.getSaldoCap() != null ? item.getSaldoCap() : 0.0)
                .sum();
        double sumTotalVencidoAll = items.stream()
                .mapToDouble(item -> item.getTotalVencido() != null ? item.getTotalVencido() : 0.0)
                .sum();
        long matchedCount = items.stream()
                .filter(item -> item.getNumCred() != null && loanNumCreds.contains(item.getNumCred()))
                .count();
        double sumTotalVencidoLoanList = items.stream()
                .filter(item -> item.getNumCred() != null && loanNumCreds.contains(item.getNumCred()))
                .mapToDouble(item -> item.getTotalVencido() != null ? item.getTotalVencido() : 0.0)
                .sum();
        double sumMontoIniLoanList = items.stream()
                .filter(item -> item.getNumCred() != null && loanNumCreds.contains(item.getNumCred()))
                .mapToDouble(item -> item.getMontoIni() != null ? item.getMontoIni() : 0.0)
                .sum();
        double sumSaldoCapLoanList = items.stream()
                .filter(item -> item.getNumCred() != null && loanNumCreds.contains(item.getNumCred()))
                .mapToDouble(item -> item.getSaldoCap() != null ? item.getSaldoCap() : 0.0)
                .sum();
        double sumCanceladasLoanList = items.stream()
                .filter(item -> item.getNumCred() != null && loanNumCreds.contains(item.getNumCred()))
                .filter(item -> item.getTotalVencido() == null || item.getTotalVencido() == 0.0)
                .mapToDouble(item -> item.getMontoIni() != null ? item.getMontoIni() : 0.0)
                .sum();

        double sadminTotalCartera = result.getVariables().getTotalCartera() != null ? result.getVariables().getTotalCartera() : 0.0;
        double sadminTotalVencido = result.getVariables().getTotalVencido() != null ? result.getVariables().getTotalVencido() : 0.0;

        log.info("CREDITS_BY_MATURITY validation - SAdmin total_cartera={} vs sum saldo_cap={} (diff={})",
                sadminTotalCartera, sumSaldoCap, sadminTotalCartera - sumSaldoCap);
        log.info("CREDITS_BY_MATURITY recalculation - SAdmin total_vencido={} vs sum all total_vencido={} (diff={})",
                sadminTotalVencido, sumTotalVencidoAll, sadminTotalVencido - sumTotalVencidoAll);
        log.info("CREDITS_BY_MATURITY crossed - matched {} credits from LOAN_LIST, sum total_vencido={}",
                matchedCount, sumTotalVencidoLoanList);

        // total_vencido: all credits (for Cartera Activa tab)
        result.getVariables().setTotalVencido(sumTotalVencidoAll);

        // total_vencido_loan_list: only credits present in LOAN_LIST (for Colocacion Total tab)
        result.getVariables().setTotalVencidoLoanList(sumTotalVencidoLoanList);
        log.info("CREDITS_BY_MATURITY -> Colocacion vencida (crossed LOAN_LIST) = {}", sumTotalVencidoLoanList);

        // New sums for Colocacion Total tab
        result.getVariables().setSumMontoIni(sumMontoIni);
        result.getVariables().setSumSaldoCap(sumSaldoCap);
        result.getVariables().setSumMontoIniLoanList(sumMontoIniLoanList);
        result.getVariables().setSumSaldoCapLoanList(sumSaldoCapLoanList);
        result.getVariables().setSumCanceladasLoanList(sumCanceladasLoanList);
        log.info("CREDITS_BY_MATURITY -> sum_monto_ini={}, sum_saldo_cap={}", sumMontoIni, sumSaldoCap);
        log.info("CREDITS_BY_MATURITY -> sum_monto_ini_loan_list={}, sum_saldo_cap_loan_list={}", sumMontoIniLoanList, sumSaldoCapLoanList);
        log.info("CREDITS_BY_MATURITY -> sum_canceladas_loan_list={} (credits without arrears from LOAN_LIST)", sumCanceladasLoanList);

        // porc_venc based on all credits (Cartera Activa)
        if (sadminTotalCartera > 0) {
            double porcVenc = (sumTotalVencidoAll / sadminTotalCartera) * 100.0;
            result.getVariables().setPorcVenc(porcVenc);
            log.info("CREDITS_BY_MATURITY recalculated porc_venc={}", porcVenc);
        }
    }

    public LoanListReportDto getLoanList(String startDate, String endDate, String creditType, String branch, String clientGroup) {
        String sadminStart = toSAdminDate(startDate);
        String sadminEnd = toSAdminDate(endDate);
        log.info("Fetching LOAN_LIST from {} to {} (SAdmin: {} to {})", startDate, endDate, sadminStart, sadminEnd);

        String token = externalApiPort.authenticateAndGetToken(unoMasUnoUsername, unoMasUnoPassword);

        SAdminReportRequestDto request = SAdminReportRequestDto.builder()
                .reportid("LOAN_LIST")
                .startDate(sadminStart)
                .endDate(sadminEnd)
                .balanceType(1)
                .creditType(creditType != null ? creditType : "ALL")
                .branch(branch != null ? branch : "ALL")
                .clientGroup(clientGroup != null ? clientGroup : "ALL")
                .advisorid("ALL")
                .investorid("ALL")
                .build();

        return externalApiPort.executeWithToken(
                "/generate_report",
                request,
                LoanListReportDto.class,
                token
        );
    }

    public PaymentsDetailsReportDto getPaymentsDetails(String startDate, String endDate, String branch) {
        String sadminStart = toSAdminDate(startDate);
        String sadminEnd = toSAdminDate(endDate);
        log.info("Fetching PAYMENTS_DETAILS from {} to {} (SAdmin: {} to {})", startDate, endDate, sadminStart, sadminEnd);

        String token = externalApiPort.authenticateAndGetToken(unoMasUnoUsername, unoMasUnoPassword);

        SAdminReportRequestDto request = SAdminReportRequestDto.builder()
                .reportid("PAYMENTS_DETAILS")
                .startDate(sadminStart)
                .endDate(sadminEnd)
                .branch(branch != null ? branch : "ALL")
                .investorid("ALL")
                .build();

        PaymentsDetailsReportDto result = externalApiPort.executeWithToken(
                "/generate_report",
                request,
                PaymentsDetailsReportDto.class,
                token
        );

        int count = result != null && result.getData() != null ? result.getData().size() : 0;
        log.info("PAYMENTS_DETAILS returned {} payment records, total_recaudos={}",
                count, result != null && result.getVariables() != null ? result.getVariables().getTotalRecaudos() : 0);
        return result;
    }
}
