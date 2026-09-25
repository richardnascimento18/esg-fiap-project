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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/environmental-licenses")
@Tag(name = "Environmental License", description = "Controle de licenças ambientais e alertas de renovação (pilar Governanca)")
public class EnvironmentalLicenseController {

    private final EnvironmentalLicenseUseCase useCase;
    private final EnvironmentalLicenseWebMapper mapper;

    public EnvironmentalLicenseController(EnvironmentalLicenseUseCase useCase, EnvironmentalLicenseWebMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @Operation(summary = "Cadastrar licença ambiental")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnvironmentalLicenseResponse create(@Valid @RequestBody EnvironmentalLicenseRequest request) {
        return mapper.toResponse(useCase.create(mapper.toDomain(request)));
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
    public EnvironmentalLicenseResponse findById(@PathVariable String id) {
        return mapper.toResponse(useCase.findById(id));
    }

    @Operation(summary = "Atualizar licença ambiental",
            description = "Substitui os campos editáveis do registro e aplica as regras de negócio.")
    @PutMapping("/{id}")
    public EnvironmentalLicenseResponse update(@PathVariable String id, @Valid @RequestBody EnvironmentalLicenseRequest request) {
        return mapper.toResponse(useCase.update(id, mapper.toDomain(request)));
    }

    @Operation(summary = "Excluir licença ambiental")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        useCase.delete(id);
    }
}
