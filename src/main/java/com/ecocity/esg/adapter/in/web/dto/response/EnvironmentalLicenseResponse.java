package com.ecocity.esg.adapter.in.web.dto.response;

import com.ecocity.esg.domain.model.LicenseStatus;
import com.ecocity.esg.domain.model.LicenseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentalLicenseResponse {

    private String id;
    private String licenseNumber;
    private String facility;
    private LicenseType licenseType;
    @Schema(description = "Estado efetivo calculado na consulta; pode mudar com o tempo sem alterar a versão do documento MongoDB.", accessMode = Schema.AccessMode.READ_ONLY)
    private LicenseStatus status;
    private Instant issueDate;
    private Instant expirationDate;
    private String issuingAuthority;
    private Map<String, Object> additionalRequirements;
}
