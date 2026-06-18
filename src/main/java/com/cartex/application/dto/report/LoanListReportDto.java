package com.cartex.application.dto.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanListReportDto {

    private boolean success;
    private int status;
    private Variables variables;
    private List<LoanItem> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Variables {
        @JsonProperty("number_of_credits")
        private Integer numberOfCredits;
        @JsonProperty("total_amount")
        private Double totalAmount;
        @JsonProperty("total_balance_today")
        private Double totalBalanceToday;
        @JsonProperty("total_balance_cutoff")
        private Double totalBalanceCutoff;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoanItem {
        @JsonProperty("num_cred")
        private String numCred;
        private String identif;
        private String nombres;
        @JsonProperty("nom_tipocred")
        private String nomTipocred;
    }
}
