package com.cartex.infrastructure.rest.controller;

import com.cartex.application.dto.report.CreditsByMaturityReportDto;
import com.cartex.application.dto.report.LoanListReportDto;
import com.cartex.application.dto.report.PaymentsDetailsReportDto;
import com.cartex.application.usecase.ReportUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportUseCase reportUseCase;

    @GetMapping("/credits-by-maturity")
    public ResponseEntity<CreditsByMaturityReportDto> getCreditsByMaturity(
            @RequestParam String cutoffDate,
            @RequestParam(required = false) String creditType,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String clientGroup) {
        CreditsByMaturityReportDto result = reportUseCase.getCreditsByMaturity(cutoffDate, creditType, branch, clientGroup);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/loan-list")
    public ResponseEntity<LoanListReportDto> getLoanList(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String creditType,
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) String clientGroup) {
        LoanListReportDto result = reportUseCase.getLoanList(startDate, endDate, creditType, branch, clientGroup);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/payments-details")
    public ResponseEntity<PaymentsDetailsReportDto> getPaymentsDetails(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) String branch) {
        PaymentsDetailsReportDto result = reportUseCase.getPaymentsDetails(startDate, endDate, branch);
        return ResponseEntity.ok(result);
    }
}
