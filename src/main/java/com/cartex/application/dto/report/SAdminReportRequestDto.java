package com.cartex.application.dto.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SAdminReportRequestDto {

    private String reportid;

    @JsonProperty("cutoff_date")
    private String cutoffDate;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("balance_type")
    private Integer balanceType;

    @JsonProperty("credit_type")
    private String creditType;

    private String advisorid;

    private String branch;

    @JsonProperty("client_group")
    private String clientGroup;

    @JsonProperty("exclude_restricted_list")
    private String excludeRestrictedList;

    @JsonProperty("number_of_decimals")
    private Integer numberOfDecimals;

    private String identif;

    private String names;

    private String investorid;
}
