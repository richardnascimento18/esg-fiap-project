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
    public DiversityReportResponse create(@Valid @RequestBody DiversityReportRequest request) {
        return mapper.toResponse(useCase.create(mapper.toDomain(request)));
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
    public DiversityReportResponse findById(@PathVariable String id) {
        return mapper.toResponse(useCase.findById(id));
    }

    @Operation(summary = "Atualizar relatório de diversidade",
            description = "Substitui os campos editáveis do registro e aplica as regras de negócio.")
    @PutMapping("/{id}")
    public DiversityReportResponse update(@PathVariable String id, @Valid @RequestBody DiversityReportRequest request) {
        return mapper.toResponse(useCase.update(id, mapper.toDomain(request)));
    }

    @Operation(summary = "Excluir relatório de diversidade")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        useCase.delete(id);
    }
}
