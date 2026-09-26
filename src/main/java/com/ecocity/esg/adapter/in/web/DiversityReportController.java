package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.adapter.in.web.dto.request.DiversityReportRequest;
import com.ecocity.esg.adapter.in.web.dto.response.DiversityReportResponse;
import com.ecocity.esg.adapter.in.web.mapper.DiversityReportWebMapper;
import com.ecocity.esg.application.port.in.DiversityReportUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.ecocity.esg.adapter.in.web.config.EntityTags;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/diversity-reports")
@Tag(name = "Diversity Report", description = "Indicadores de diversidade e inclusao corporativa (pilar Social)")
public class DiversityReportController {

    private final DiversityReportUseCase useCase;
    private final DiversityReportWebMapper mapper;

    public DiversityReportController(DiversityReportUseCase useCase, DiversityReportWebMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @Operation(summary = "Cadastrar relatório de diversidade")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<DiversityReportResponse> create(@Valid @RequestBody DiversityReportRequest request) {
        var created = useCase.create(mapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).eTag(EntityTags.forVersion(created.getVersion())).body(mapper.toResponse(created));
    }

    @Operation(summary = "Listar registros de relatório de diversidade",
            description = "Retorna uma lista JSON. A primeira página é 0; o tamanho padrão é 20.")
    @GetMapping
    public List<DiversityReportResponse> findAll(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                  @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return useCase.findAll(page, size).stream().map(mapper::toResponse).toList();
    }

    @Operation(summary = "Consultar relatório de diversidade pelo identificador")
    @GetMapping("/{id}")
    public ResponseEntity<DiversityReportResponse> findById(@PathVariable String id) {
        var found = useCase.findById(id);
        return ResponseEntity.ok().eTag(EntityTags.forVersion(found.getVersion())).body(mapper.toResponse(found));
    }

    @Operation(summary = "Atualizar relatório de diversidade",
            description = "Substitui os campos editáveis do registro e aplica as regras de negócio.")
    @PutMapping("/{id}")
    public ResponseEntity<DiversityReportResponse> update(@PathVariable String id, @Valid @RequestBody DiversityReportRequest request,
                                               @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        var updated = useCase.update(id, mapper.toDomain(request), EntityTags.requiredVersion(ifMatch));
        return ResponseEntity.ok().eTag(EntityTags.forVersion(updated.getVersion())).body(mapper.toResponse(updated));
    }

    @Operation(summary = "Excluir relatório de diversidade")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        useCase.delete(id);
    }
}
