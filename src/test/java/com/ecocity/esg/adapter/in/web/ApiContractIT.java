package com.ecocity.esg.adapter.in.web;

import com.ecocity.esg.support.MongoIntegrationTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiContractIT extends AbstractApiContractTest {
    @DynamicPropertySource
    static void mongo(DynamicPropertyRegistry registry) {
        MongoIntegrationTest.mongoProperties(registry);
    }
}
