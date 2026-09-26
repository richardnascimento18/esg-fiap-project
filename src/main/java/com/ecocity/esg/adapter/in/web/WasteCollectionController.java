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
import com.ecocity.esg.adapter.in.web.config.RequestFingerprint;
import com.ecocity.esg.application.port.in.IdempotencyUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/waste-collections")
@Tag(name = "Waste Collection", description = "Rastreamento de coleta seletiva e reciclagem (pilar Ambiental)")
public class WasteCollectionController {

    private final WasteCollectionUseCase useCase;
    private final WasteCollectionWebMapper mapper;
    private final IdempotencyUseCase idempotency;
    private final ObjectMapper objectMapper;

    public WasteCollectionController(WasteCollectionUseCase useCase, WasteCollectionWebMapper mapper,
                              IdempotencyUseCase idempotency, ObjectMapper objectMapper) {
        this.useCase = useCase;
        this.mapper = mapper;
        this.idempotency = idempotency;
        this.objectMapper = objectMapper;
    }

    @Operation(summary = "Cadastrar coleta de resíduos")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<WasteCollectionResponse> create(@Valid @RequestBody WasteCollectionRequest request,
                                         @RequestHeader(value = "Idempotency-Key", required = false) String key) {
        var domain = mapper.toDomain(request);
        domain.validate();
        String id = idempotency.reserve("waste-collections", key, RequestFingerprint.of(request, objectMapper));
        var created = id == null ? useCase.create(domain) : useCase.createWithId(domain, id);
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
    public void delete(@PathVariable String id,
                       @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        useCase.delete(id, EntityTags.requiredVersion(ifMatch));
    }
}
