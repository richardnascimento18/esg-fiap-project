package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.adapter.in.web.dto.request.WasteCollectionRequest;
import com.ecocity.esg.adapter.in.web.dto.response.WasteCollectionResponse;
import com.ecocity.esg.adapter.in.web.mapper.WasteCollectionWebMapper;
import com.ecocity.esg.application.port.in.WasteCollectionUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/waste-collections")
@Tag(name = "Waste Collection", description = "Rastreamento de coleta seletiva e reciclagem (pilar Ambiental)")
public class WasteCollectionController {

    private final WasteCollectionUseCase useCase;
    private final WasteCollectionWebMapper mapper;

    public WasteCollectionController(WasteCollectionUseCase useCase, WasteCollectionWebMapper mapper) {
        this.useCase = useCase;
        this.mapper = mapper;
    }

    @Operation(summary = "Cadastrar coleta de resíduos")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WasteCollectionResponse create(@Valid @RequestBody WasteCollectionRequest request) {
        return mapper.toResponse(useCase.create(mapper.toDomain(request)));
    }

    @Operation(summary = "Listar registros de coleta de resíduos",
            description = "Retorna uma lista JSON. A primeira página é 0; o tamanho padrão é 20.")
    @GetMapping
    public List<WasteCollectionResponse> findAll(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return useCase.findAll(page, size).stream().map(mapper::toResponse).toList();
    }

    @Operation(summary = "Consultar coleta de resíduos pelo identificador")
    @GetMapping("/{id}")
    public WasteCollectionResponse findById(@PathVariable String id) {
        return mapper.toResponse(useCase.findById(id));
    }

    @Operation(summary = "Atualizar coleta de resíduos",
            description = "Substitui os campos editáveis do registro e aplica as regras de negócio.")
    @PutMapping("/{id}")
    public WasteCollectionResponse update(@PathVariable String id, @Valid @RequestBody WasteCollectionRequest request) {
        return mapper.toResponse(useCase.update(id, mapper.toDomain(request)));
    }

    @Operation(summary = "Excluir coleta de resíduos")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        useCase.delete(id);
    }
}
