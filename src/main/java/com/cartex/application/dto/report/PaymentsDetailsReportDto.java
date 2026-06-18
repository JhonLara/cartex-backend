package com.cartex.application.dto.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentsDetailsReportDto {

    private boolean success;
    private int status;
    private Variables variables;
    private List<PaymentItem> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Variables {
        @JsonProperty("total_recaudos")
        private Double totalRecaudos;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentItem {
        private String fecha;
        private String identif;
        private String nombre;
        private String numdoc;
        private String tipo;
        private String detalle;
        @JsonProperty("vr_pago")
        private Double vrPago;
        @JsonProperty("tasa_iva")
        private Double tasaIva;
        @JsonProperty("num_cred")
        private String numCred;
        @JsonProperty("cod_tipocred")
        private String codTipocred;
        @JsonProperty("nom_tipocred")
        private String nomTipocred;
        @JsonProperty("num_externo")
        private String numExterno;
    }
}
