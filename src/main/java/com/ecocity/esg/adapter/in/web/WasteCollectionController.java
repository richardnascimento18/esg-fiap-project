package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.adapter.in.web.dto.request.WasteCollectionRequest;
import com.ecocity.esg.adapter.in.web.dto.response.WasteCollectionResponse;
import com.ecocity.esg.adapter.in.web.mapper.WasteCollectionWebMapper;
import com.ecocity.esg.application.port.in.WasteCollectionUseCase;
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
    public ResponseEntity<WasteCollectionResponse> create(@Valid @RequestBody WasteCollectionRequest request) {
        var created = useCase.create(mapper.toDomain(request));
        return ResponseEntity.status(HttpStatus.CREATED).eTag(EntityTags.forVersion(created.getVersion())).body(mapper.toResponse(created));
    }

    @Operation(summary = "Listar registros de coleta de resíduos",
            description = "Retorna uma lista JSON. A primeira página é 0; o tamanho padrão é 20.")
    @GetMapping
    public List<WasteCollectionResponse> findAll(@RequestParam(defaultValue = "0") @Min(0) int page,
                                                  @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return useCase.findAll(page, size).stream().map(mapper::toResponse).toList();
    }

    @Operation(summary = "Consultar coleta de resíduos pelo identificador")
    @GetMapping("/{id}")
    public ResponseEntity<WasteCollectionResponse> findById(@PathVariable String id) {
        var found = useCase.findById(id);
        return ResponseEntity.ok().eTag(EntityTags.forVersion(found.getVersion())).body(mapper.toResponse(found));
    }

    @Operation(summary = "Atualizar coleta de resíduos",
            description = "Substitui os campos editáveis do registro e aplica as regras de negócio.")
    @PutMapping("/{id}")
    public ResponseEntity<WasteCollectionResponse> update(@PathVariable String id, @Valid @RequestBody WasteCollectionRequest request,
                                               @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        var updated = useCase.update(id, mapper.toDomain(request), EntityTags.requiredVersion(ifMatch));
        return ResponseEntity.ok().eTag(EntityTags.forVersion(updated.getVersion())).body(mapper.toResponse(updated));
    }

    @Operation(summary = "Excluir coleta de resíduos")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        useCase.delete(id);
    }
}
