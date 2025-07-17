package com.bookapp.tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.parsing.Parser;
import io.restassured.path.json.mapper.factory.DefaultJackson2ObjectMapperFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.junit.jupiter.api.BeforeAll;
import org.awaitility.Awaitility; // Import Awaitility
import java.time.Duration; // Import Duration

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public abstract class TestBaseSimplified {

    static {
        RestAssured.defaultParser = Parser.JSON;

        RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                        (cls, charset) -> {
                            ObjectMapper om = new DefaultJackson2ObjectMapperFactory().create(cls, charset);
                            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                            return om;
                        }
                ));
        Awaitility.setDefaultPollDelay(100, MILLISECONDS);
        Awaitility.setDefaultPollInterval(1, SECONDS);
        Awaitility.setDefaultTimeout(Duration.ofSeconds(15));
    }

    @BeforeAll
    static void globalSetupForTests() {
        System.out.println("Global Test Base Setup: RestAssured and Awaitility configured.");
    }
}