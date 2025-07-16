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

    // This static block runs once when the class is loaded
    static {
        // Set default parser for RestAssured responses to JSON
        RestAssured.defaultParser = Parser.JSON;

        // Configure RestAssured to use Jackson for object mapping
        // and ignore unknown properties during deserialization
        RestAssured.config = RestAssuredConfig.config()
                .objectMapperConfig(new ObjectMapperConfig().jackson2ObjectMapperFactory(
                        (cls, charset) -> {
                            // Create a default Jackson ObjectMapper
                            ObjectMapper om = new DefaultJackson2ObjectMapperFactory().create(cls, charset);
                            // Configure it to not fail if JSON contains properties not defined in the POJO
                            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                            return om;
                        }
                ));
        // Common Awaitility defaults for polling asynchronous operations
        Awaitility.setDefaultPollDelay(100, MILLISECONDS);
        Awaitility.setDefaultPollInterval(1, SECONDS);
        Awaitility.setDefaultTimeout(Duration.ofSeconds(15)); // Default timeout for Awaitility calls
    }

    @BeforeAll
    static void globalSetupForTests() {
        // Any global setup before all tests run, if not handled in the static block.
        // For example, if you had a global TestSetup object that loaded properties
        // from a secrets file, it would be initialized here or by a JUnit Extension.
        System.out.println("Global Test Base Setup: RestAssured and Awaitility configured.");
    }
}