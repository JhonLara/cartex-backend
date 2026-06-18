package com.cartex.application.dto.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditsByMaturityReportDto {

    private boolean success;
    private int status;
    private Variables variables;
    private List<CreditItem> data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Variables {
        @JsonProperty("total_cartera")
        private Double totalCartera;
        @JsonProperty("total_vencido")
        private Double totalVencido;
        @JsonProperty("total_vencido_loan_list")
        private Double totalVencidoLoanList;
        @JsonProperty("sum_monto_ini")
        private Double sumMontoIni;
        @JsonProperty("sum_saldo_cap")
        private Double sumSaldoCap;
        @JsonProperty("sum_monto_ini_loan_list")
        private Double sumMontoIniLoanList;
        @JsonProperty("sum_saldo_cap_loan_list")
        private Double sumSaldoCapLoanList;
        @JsonProperty("sum_canceladas_loan_list")
        private Double sumCanceladasLoanList;
        @JsonProperty("porc_venc")
        private Double porcVenc;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CreditItem {
        private String identif;
        private String nombres;
        @JsonProperty("num_cred")
        private String numCred;
        @JsonProperty("nom_tipocred")
        private String nomTipocred;
        @JsonProperty("monto_ini")
        private Double montoIni;
        private String sucursal;
        private Integer plazo;
        @JsonProperty("saldo_cap")
        private Double saldoCap;
        @JsonProperty("fecha_venc")
        private String fechaVenc;
        @JsonProperty("fecha_desembolso")
        private String fechaDesembolso;
        @JsonProperty("vr_cuota")
        private Double vrCuota;
        @JsonProperty("fecha_ult_pago")
        private String fechaUltPago;
        @JsonProperty("val_ult_pago")
        private Double valUltPago;
        @JsonProperty("valor_hoy")
        private Double valorHoy;
        @JsonProperty("capital_vencido")
        private Double capitalVencido;
        @JsonProperty("interes_vencido")
        private Double interesVencido;
        @JsonProperty("seguro_vencido")
        private Double seguroVencido;
        @JsonProperty("otros_vencido")
        private Double otrosVencido;
        @JsonProperty("mora_total")
        private Double moraTotal;
        @JsonProperty("total_vencido")
        private Double totalVencido;
        @JsonProperty("cap_vencido_30")
        private Double capVencido30;
        @JsonProperty("cap_vencido_31_60")
        private Double capVencido31_60;
        @JsonProperty("cap_vencido_61_90")
        private Double capVencido61_90;
        @JsonProperty("cap_vencido_91_180")
        private Double capVencido91_180;
        @JsonProperty("cap_vencido_181_360")
        private Double capVencido181_360;
        @JsonProperty("cap_vencido_361")
        private Double capVencido361;
        @JsonProperty("capital_no_vencido")
        private Double capitalNoVencido;
        @JsonProperty("cuotas_vencidas")
        private Integer cuotasVencidas;
        private Integer pendientes;
        @JsonProperty("dias_mora")
        private Integer diasMora;
        private String calificacion;
        private String etapa;
        private String ciudad;
        private String direccion;
        private String telefono;
        private String celular;
        @JsonProperty("fecha_nac")
        private String fechaNac;
        private String sexo;
        private String email;
        private String grupo;
    }
}
