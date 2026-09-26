package com.ecocity.esg.support;

import com.ecocity.esg.adapter.in.web.*;
import com.ecocity.esg.adapter.in.web.mapper.*;
import com.ecocity.esg.adapter.in.web.exception.GlobalExceptionHandler;
import com.ecocity.esg.adapter.in.web.config.OpenApiConfig;
import com.ecocity.esg.adapter.in.web.config.RequestIdFilter;
import com.ecocity.esg.adapter.in.web.config.SecurityConfig;
import com.ecocity.esg.application.port.out.*;
import com.ecocity.esg.domain.model.*;
import com.ecocity.esg.infrastructure.config.ApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Real web adapters and use cases, with test doubles only at the output ports. */
@Configuration(proxyBeanMethods = false)
@Profile("web-contract")
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class,
        MongoRepositoriesAutoConfiguration.class})
@Import({ApplicationConfiguration.class, SecurityConfig.class, OpenApiConfig.class, GlobalExceptionHandler.class,
        RequestIdFilter.class,
        EnergyConsumptionController.class, EnergyConsumptionWebMapper.class,
        WasteCollectionController.class, WasteCollectionWebMapper.class,
        CarbonEmissionController.class, CarbonEmissionWebMapper.class,
        DiversityReportController.class, DiversityReportWebMapper.class,
        EnvironmentalLicenseController.class, EnvironmentalLicenseWebMapper.class})
public class WebContractConfiguration {

    @Bean
    LicenseRenewalAlertPort alerts() {
        return mock(LicenseRenewalAlertPort.class);
    }

    @Bean
    LicenseScanCoordinationPort coordination() {
        return task -> { task.run(); return true; };
    }

    @Bean
    IdempotencyReservationPort reservations() {
        Map<String, String> fingerprints = new java.util.concurrent.ConcurrentHashMap<>();
        return new IdempotencyReservationPort() {
            @Override
            public String reserve(String resource, String key, String fingerprint) {
                String id = "idem-" + resource + "-" + key;
                String previous = fingerprints.putIfAbsent(id, fingerprint);
                if (previous != null && !previous.equals(fingerprint)) {
                    throw new com.ecocity.esg.domain.exception.IdempotencyConflictException();
                }
                return id;
            }

            @Override
            public void markDeleted(String resourceId) {
                fingerprints.remove(resourceId);
            }
        };
    }

    @Bean
    EnergyConsumptionRepositoryPort energyConsumptionRepository() {
        var port = mock(EnergyConsumptionRepositoryPort.class);
        var data = new Records<EnergyConsumption>(EnergyConsumption::getId,
                (item, id) -> item.toBuilder().id(id).version(item.getVersion() == null ? 0L : item.getVersion() + 1).build());
        when(port.save(any())).thenAnswer(call -> data.save(call.getArgument(0)));
        when(port.findById(anyString())).thenAnswer(call -> data.find(call.getArgument(0)));
        when(port.findAll(anyInt(), anyInt())).thenAnswer(call -> data.page(call.getArgument(0), call.getArgument(1)));
        doAnswer(call -> { data.removeValue(call.getArgument(0)); return null; }).when(port).delete(any());
        return port;
    }

    @Bean
    WasteCollectionRepositoryPort wasteCollectionRepository() {
        var port = mock(WasteCollectionRepositoryPort.class);
        var data = new Records<WasteCollection>(WasteCollection::getId,
                (item, id) -> item.toBuilder().id(id).version(item.getVersion() == null ? 0L : item.getVersion() + 1).build());
        when(port.save(any())).thenAnswer(call -> data.save(call.getArgument(0)));
        when(port.findById(anyString())).thenAnswer(call -> data.find(call.getArgument(0)));
        when(port.findAll(anyInt(), anyInt())).thenAnswer(call -> data.page(call.getArgument(0), call.getArgument(1)));
        doAnswer(call -> { data.removeValue(call.getArgument(0)); return null; }).when(port).delete(any());
        return port;
    }

    @Bean
    CarbonEmissionRepositoryPort carbonEmissionRepository() {
        var port = mock(CarbonEmissionRepositoryPort.class);
        var data = new Records<CarbonEmission>(CarbonEmission::getId,
                (item, id) -> item.toBuilder().id(id).version(item.getVersion() == null ? 0L : item.getVersion() + 1).build());
        when(port.save(any())).thenAnswer(call -> data.save(call.getArgument(0)));
        when(port.findById(anyString())).thenAnswer(call -> data.find(call.getArgument(0)));
        when(port.findAll(anyInt(), anyInt())).thenAnswer(call -> data.page(call.getArgument(0), call.getArgument(1)));
        doAnswer(call -> { data.removeValue(call.getArgument(0)); return null; }).when(port).delete(any());
        return port;
    }

    @Bean
    DiversityReportRepositoryPort diversityReportRepository() {
        var port = mock(DiversityReportRepositoryPort.class);
        var data = new Records<DiversityReport>(DiversityReport::getId,
                (item, id) -> item.toBuilder().id(id).version(item.getVersion() == null ? 0L : item.getVersion() + 1).build());
        when(port.save(any())).thenAnswer(call -> data.save(call.getArgument(0)));
        when(port.findById(anyString())).thenAnswer(call -> data.find(call.getArgument(0)));
        when(port.findAll(anyInt(), anyInt())).thenAnswer(call -> data.page(call.getArgument(0), call.getArgument(1)));
        doAnswer(call -> { data.removeValue(call.getArgument(0)); return null; }).when(port).delete(any());
        return port;
    }

    @Bean
    EnvironmentalLicenseRepositoryPort environmentalLicenseRepository() {
        var port = mock(EnvironmentalLicenseRepositoryPort.class);
        var data = new Records<EnvironmentalLicense>(EnvironmentalLicense::getId,
                (item, id) -> item.toBuilder().id(id).version(item.getVersion() == null ? 0L : item.getVersion() + 1).build());
        when(port.save(any())).thenAnswer(call -> data.save(call.getArgument(0)));
        when(port.findById(anyString())).thenAnswer(call -> data.find(call.getArgument(0)));
        when(port.findAll(anyInt(), anyInt())).thenAnswer(call -> data.page(call.getArgument(0), call.getArgument(1)));
        doAnswer(call -> { data.removeValue(call.getArgument(0)); return null; }).when(port).delete(any());
        return port;
    }

    private static class Records<T> {
        private final Map<String, T> records = new LinkedHashMap<>();
        private final Function<T, String> getId;
        private final BiFunction<T, String, T> withId;

        Records(Function<T, String> getId, BiFunction<T, String, T> withId) {
            this.getId = getId;
            this.withId = withId;
        }

        T save(T value) {
            String id = Optional.ofNullable(getId.apply(value)).orElseGet(() -> UUID.randomUUID().toString());
            if (id.startsWith("idem-") && records.containsKey(id)) return records.get(id);
            T stored = withId.apply(value, id);
            records.put(id, stored);
            return stored;
        }

        Optional<T> find(String id) {
            return Optional.ofNullable(records.get(id));
        }

        List<T> page(int page, int size) {
            return records.values().stream().skip((long) page * size).limit(size).toList();
        }

        void removeValue(T item) {
            remove(getId.apply(item));
        }

        void remove(String id) {
            records.remove(id);
        }
    }
}
