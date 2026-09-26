package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.adapter.in.web.dto.request.EnvironmentalLicenseRequest;
import com.ecocity.esg.adapter.in.web.dto.response.EnvironmentalLicenseResponse;
import com.ecocity.esg.adapter.in.web.mapper.EnvironmentalLicenseWebMapper;
import com.ecocity.esg.application.port.in.EnvironmentalLicenseUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.ecocity.esg.adapter.in.web.config.EntityTags;
import com.ecocity.esg.adapter.in.web.config.RequestFingerprint;
import com.ecocity.esg.application.port.in.IdempotencyUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/environmental-licenses")
@Tag(name = "Environmental License", description = "Controle de licenças ambientais e alertas de renovação (pilar Governanca)")
public class EnvironmentalLicenseController {

    private final EnvironmentalLicenseUseCase useCase;
    private final EnvironmentalLicenseWebMapper mapper;
    private final IdempotencyUseCase idempotency;
    private final ObjectMapper objectMapper;

    public EnvironmentalLicenseController(EnvironmentalLicenseUseCase useCase, EnvironmentalLicenseWebMapper mapper,
                              IdempotencyUseCase idempotency, ObjectMapper objectMapper) {
        this.useCase = useCase;
        this.mapper = mapper;
        this.idempotency = idempotency;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Cadastrar licença ambiental")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<EnvironmentalLicenseResponse> create(@Valid @RequestBody EnvironmentalLicenseRequest request,
                                         @RequestHeader(value = "Idempotency-Key", required = false) String key) {
        var domain = mapper.toDomain(request);
        domain.validate();
        String id = idempotency.reserve("environmental-licenses", key, RequestFingerprint.of(request, objectMapper));
        var created = id == null ? useCase.create(domain) : useCase.createWithId(domain, id);
        return ResponseEntity.status(HttpStatus.CREATED).eTag(EntityTags.forLicense(created)).body(mapper.toResponse(created));
    }

    @Operation(summary = "Listar registros de licença ambiental",
            description = "Retorna uma lista JSON. A primeira página é 0; o tamanho padrão é 20.")
    @GetMapping
    public List<EnvironmentalLicenseResponse> findAll(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                       @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return useCase.findAll(page, size).stream().map(mapper::toResponse).toList();
    }

    @Operation(summary = "Consultar licença ambiental pelo identificador")
    @GetMapping("/{id}")
    public ResponseEntity<EnvironmentalLicenseResponse> findById(@PathVariable String id) {
        var found = useCase.findById(id);
        return ResponseEntity.ok().eTag(EntityTags.forLicense(found)).body(mapper.toResponse(found));
    }

    @Operation(summary = "Atualizar licença ambiental",
            description = "Substitui os campos editáveis do registro e aplica as regras de negócio.")
    @PutMapping("/{id}")
    public ResponseEntity<EnvironmentalLicenseResponse> update(@PathVariable String id, @Valid @RequestBody EnvironmentalLicenseRequest request,
                                               @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        var replacement = mapper.toDomain(request);
        replacement.validate();
        long expectedVersion = EntityTags.requiredLicenseVersion(ifMatch);
        EntityTags.requireLicenseMatch(ifMatch, useCase.findById(id));
        var updated = useCase.update(id, replacement, expectedVersion);
        return ResponseEntity.ok().eTag(EntityTags.forLicense(updated)).body(mapper.toResponse(updated));
    }

    @Operation(summary = "Excluir licença ambiental")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id,
                       @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        long expectedVersion = EntityTags.requiredLicenseVersion(ifMatch);
        EntityTags.requireLicenseMatch(ifMatch, useCase.findById(id));
        useCase.delete(id, expectedVersion);
    }
}
