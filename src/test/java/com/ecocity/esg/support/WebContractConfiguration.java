package com.ecocity.esg.support;

import com.ecocity.esg.adapter.in.web.*;
import com.ecocity.esg.adapter.in.web.mapper.*;
import com.ecocity.esg.adapter.in.web.exception.GlobalExceptionHandler;
import com.ecocity.esg.adapter.in.web.config.OpenApiConfig;
import com.ecocity.esg.adapter.in.web.config.RequestIdFilter;
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
@Import({ApplicationConfiguration.class, OpenApiConfig.class, GlobalExceptionHandler.class,
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
    EnergyConsumptionRepositoryPort energyConsumptionRepository() {
        var port = mock(EnergyConsumptionRepositoryPort.class);
        var data = new Records<EnergyConsumption>(EnergyConsumption::getId,
                (item, id) -> item.toBuilder().id(id).build());
        when(port.save(any())).thenAnswer(call -> data.save(call.getArgument(0)));
        when(port.findById(anyString())).thenAnswer(call -> data.find(call.getArgument(0)));
        when(port.findAll(anyInt(), anyInt())).thenAnswer(call -> data.page(call.getArgument(0), call.getArgument(1)));
        when(port.existsById(anyString())).thenAnswer(call -> data.find(call.getArgument(0)).isPresent());
        doAnswer(call -> { data.remove(call.getArgument(0)); return null; }).when(port).deleteById(anyString());
        return port;
    }

    @Bean
    WasteCollectionRepositoryPort wasteCollectionRepository() {
        var port = mock(WasteCollectionRepositoryPort.class);
        var data = new Records<WasteCollection>(WasteCollection::getId,
                (item, id) -> item.toBuilder().id(id).build());
        when(port.save(any())).thenAnswer(call -> data.save(call.getArgument(0)));
        when(port.findById(anyString())).thenAnswer(call -> data.find(call.getArgument(0)));
        when(port.findAll(anyInt(), anyInt())).thenAnswer(call -> data.page(call.getArgument(0), call.getArgument(1)));
        when(port.existsById(anyString())).thenAnswer(call -> data.find(call.getArgument(0)).isPresent());
        doAnswer(call -> { data.remove(call.getArgument(0)); return null; }).when(port).deleteById(anyString());
        return port;
    }

    @Bean
    CarbonEmissionRepositoryPort carbonEmissionRepository() {
        var port = mock(CarbonEmissionRepositoryPort.class);
        var data = new Records<CarbonEmission>(CarbonEmission::getId,
                (item, id) -> item.toBuilder().id(id).build());
        when(port.save(any())).thenAnswer(call -> data.save(call.getArgument(0)));
        when(port.findById(anyString())).thenAnswer(call -> data.find(call.getArgument(0)));
        when(port.findAll(anyInt(), anyInt())).thenAnswer(call -> data.page(call.getArgument(0), call.getArgument(1)));
        when(port.existsById(anyString())).thenAnswer(call -> data.find(call.getArgument(0)).isPresent());
        doAnswer(call -> { data.remove(call.getArgument(0)); return null; }).when(port).deleteById(anyString());
        return port;
    }

    @Bean
    DiversityReportRepositoryPort diversityReportRepository() {
        var port = mock(DiversityReportRepositoryPort.class);
        var data = new Records<DiversityReport>(DiversityReport::getId,
                (item, id) -> item.toBuilder().id(id).build());
        when(port.save(any())).thenAnswer(call -> data.save(call.getArgument(0)));
        when(port.findById(anyString())).thenAnswer(call -> data.find(call.getArgument(0)));
        when(port.findAll(anyInt(), anyInt())).thenAnswer(call -> data.page(call.getArgument(0), call.getArgument(1)));
        when(port.existsById(anyString())).thenAnswer(call -> data.find(call.getArgument(0)).isPresent());
        doAnswer(call -> { data.remove(call.getArgument(0)); return null; }).when(port).deleteById(anyString());
        return port;
    }

    @Bean
    EnvironmentalLicenseRepositoryPort environmentalLicenseRepository() {
        var port = mock(EnvironmentalLicenseRepositoryPort.class);
        var data = new Records<EnvironmentalLicense>(EnvironmentalLicense::getId,
                (item, id) -> item.toBuilder().id(id).build());
        when(port.save(any())).thenAnswer(call -> data.save(call.getArgument(0)));
        when(port.findById(anyString())).thenAnswer(call -> data.find(call.getArgument(0)));
        when(port.findAll(anyInt(), anyInt())).thenAnswer(call -> data.page(call.getArgument(0), call.getArgument(1)));
        when(port.existsById(anyString())).thenAnswer(call -> data.find(call.getArgument(0)).isPresent());
        doAnswer(call -> { data.remove(call.getArgument(0)); return null; }).when(port).deleteById(anyString());
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

        void remove(String id) {
            records.remove(id);
        }
    }
}
