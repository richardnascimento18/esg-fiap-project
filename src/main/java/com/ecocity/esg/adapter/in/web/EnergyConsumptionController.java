package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.adapter.in.web.dto.request.EnergyConsumptionRequest;
import com.ecocity.esg.adapter.in.web.dto.response.EnergyConsumptionResponse;
import com.ecocity.esg.adapter.in.web.mapper.EnergyConsumptionWebMapper;
import com.ecocity.esg.application.port.in.EnergyConsumptionUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/energy-consumptions")
@Tag(name = "Energy Consumption", description = "Monitoramento de consumo de energia e alertas automáticos de limite (pilar Ambiental)")
public class EnergyConsumptionController {

    private final EnergyConsumptionUseCase useCase;
    private final EnergyConsumptionWebMapper mapper;

    public EnergyConsumptionController(EnergyConsumptionUseCase useCase, EnergyConsumptionWebMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @Operation(summary = "Cadastrar consumo de energia")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnergyConsumptionResponse create(@Valid @RequestBody EnergyConsumptionRequest request) {
        return mapper.toResponse(useCase.create(mapper.toDomain(request)));
    }

    @Operation(summary = "Listar registros de consumo de energia",
            description = "Retorna uma lista JSON. A primeira página é 0; o tamanho padrão é 20.")
    @GetMapping
    public List<EnergyConsumptionResponse> findAll(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return useCase.findAll(page, size).stream().map(mapper::toResponse).toList();
    }

    @Operation(summary = "Consultar consumo de energia pelo identificador")
    @GetMapping("/{id}")
    public EnergyConsumptionResponse findById(@PathVariable String id) {
        return mapper.toResponse(useCase.findById(id));
    }

    @Operation(summary = "Atualizar consumo de energia",
            description = "Substitui os campos editáveis do registro e aplica as regras de negócio.")
    @PutMapping("/{id}")
    public EnergyConsumptionResponse update(@PathVariable String id, @Valid @RequestBody EnergyConsumptionRequest request) {
        return mapper.toResponse(useCase.update(id, mapper.toDomain(request)));
    }

    @Operation(summary = "Excluir consumo de energia")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        useCase.delete(id);
    }
}
