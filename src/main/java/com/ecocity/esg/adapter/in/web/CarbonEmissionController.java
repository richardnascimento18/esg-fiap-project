package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.adapter.in.web.dto.request.CarbonEmissionRequest;
import com.ecocity.esg.adapter.in.web.dto.response.CarbonEmissionResponse;
import com.ecocity.esg.adapter.in.web.mapper.CarbonEmissionWebMapper;
import com.ecocity.esg.application.port.in.CarbonEmissionUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/carbon-emissions")
@Tag(name = "Carbon Emission", description = "Monitoramento de emissao de carbono e compensação ambiental (pilar Governanca)")
public class CarbonEmissionController {

    private final CarbonEmissionUseCase useCase;
    private final CarbonEmissionWebMapper mapper;

    public CarbonEmissionController(CarbonEmissionUseCase useCase, CarbonEmissionWebMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @Operation(summary = "Cadastrar emissão de carbono")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CarbonEmissionResponse create(@Valid @RequestBody CarbonEmissionRequest request) {
        return mapper.toResponse(useCase.create(mapper.toDomain(request)));
    }

    @Operation(summary = "Listar registros de emissão de carbono",
            description = "Retorna uma lista JSON. A primeira página é 0; o tamanho padrão é 20.")
    @GetMapping
    public List<CarbonEmissionResponse> findAll(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return useCase.findAll(page, size).stream().map(mapper::toResponse).toList();
    }

    @Operation(summary = "Consultar emissão de carbono pelo identificador")
    @GetMapping("/{id}")
    public CarbonEmissionResponse findById(@PathVariable String id) {
        return mapper.toResponse(useCase.findById(id));
    }

    @Operation(summary = "Atualizar emissão de carbono",
            description = "Substitui os campos editáveis do registro e aplica as regras de negócio.")
    @PutMapping("/{id}")
    public CarbonEmissionResponse update(@PathVariable String id, @Valid @RequestBody CarbonEmissionRequest request) {
        return mapper.toResponse(useCase.update(id, mapper.toDomain(request)));
    }

    @Operation(summary = "Excluir emissão de carbono")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        useCase.delete(id);
    }
}
