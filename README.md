# Book Application API Automation Tests

This repository contains an automated API test framework for the Simple Book Application. The framework is built using Java, Maven, RestAssured, and JUnit 5, following a layered architecture for enhanced maintainability, scalability, and readability.

## 🌟 Project Overview

This framework aims to ensure the quality and reliability of the Book Application's RESTful APIs. It covers various API functionalities, including user management (signup, login) and core book operations (create, retrieve, update, delete).

## ✨ Features

* **Comprehensive API Testing:** Covers functional testing for book and user management endpoints.
* **Authentication Handling:** Manages user signup and login to obtain and use Bearer tokens for authenticated API calls.
* **Layered Architecture:** Clear separation of concerns (POJOs, API Helpers, API Factories, Test Classes) for improved maintainability and reusability.
* **Data-Driven Capabilities:** Utilizes `javafaker` for dynamic test data generation, enabling unique test scenarios.
* **Robust Assertions:** Employs AssertJ for fluent and expressive test assertions.
* **Asynchronous Operation Handling:** Configured with Awaitility for robust polling and waiting for API states (if applicable for future async operations).
* **Detailed Reporting:** Integrates Allure Report for rich, interactive test results, aiding in debugging and analysis.
* **CI/CD Integration:** Automatically runs tests via GitHub Actions on code pushes and Pull Requests, ensuring continuous quality feedback.
* **Secure Configuration:** Externalizes sensitive data (e.g., base URL) to a properties file, keeping credentials out of source code.

## 🏗️ Architecture Overview

The framework follows a layered design pattern:

* **Data Models (POJOs):** Plain Old Java Objects representing API request and response payloads (e.g., `Book`, `User`, `AuthResponse`). Uses Lombok for boilerplate reduction and Jackson for JSON serialization/deserialization.
* **Request Utility:** A central class (`RequestHelper`) to build common `RequestSpecification` objects, manage authentication tokens (Bearer), and handle base URI configuration.
* **API Interaction Layer (Service Helper):** Contains low-level methods for direct API calls (e.g., `BookApiServiceHelper`), encapsulating `RestAssured` syntax, logging, and initial status code assertions.
* **Business Logic / Orchestration Layer (Factory):** Provides high-level, scenario-driven methods (`BookApiFactory`) that orchestrate multiple API calls via the Service Helper to achieve complex test workflows (e.g., "create a book and verify it exists").
* **Test Execution Layer:** JUnit 5 test classes (`BookApiTests`) that define the actual test scenarios, leveraging the Factory methods and AssertJ for validations.
* **Base Test Layer:** An abstract base class (`TestBaseSimplified`) for common setup (e.g., global RestAssured configuration, Awaitility defaults) shared across all test classes.

## 📋 Prerequisites

Before you can run these tests, ensure you have the following installed:

* **Java Development Kit (JDK) 17 or higher:**
    * Verify with: `java -version`
    * Recommended: Adoptium (Eclipse Temurin)
* **Apache Maven 3.6.0 or higher:**
    * Verify with: `mvn -v`
* **Allure Commandline Tool:** (Required for generating and viewing Allure reports locally)
    * Verify with: `allure --version`
    * Installation instructions: [Allure Docs](https://allurereport.org/docs/gettingstarted-installation/)
* **Book Application API Running:** The target API application should be running and accessible at the URL configured in `src/test/resources/config.properties`.
    * Default local URL: `http://localhost:8000`

## ⚙️ Setup Instructions (Local)

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/Gaddale/bookstore-api-tests
    cd bookstore-api-tests
    ```
2.  **Configure `config.properties`:**
    * Open `src/test/resources/config.properties`.
    * Ensure the `base.url` matches the address where your Book Application API is running.
        ```properties
        # config.properties
        base.url=http://localhost:8000 # Change if your API runs on a different host/port
        ```
3.  **Import into IntelliJ IDEA (or other IDE):**
    * Open IntelliJ IDEA (Ultimate Edition recommended for best Lombok/Maven integration).
    * Select `File > Open...` and choose the `pom.xml` file at the root of the cloned repository. IntelliJ should automatically detect and import it as a Maven project.
    * **Ensure Annotation Processing is Enabled:** Go to `File > Settings/Preferences > Build, Execution, Deployment > Compiler > Annotation Processors` and check "Enable annotation processing".
    * **Install Lombok Plugin:** (If using IntelliJ) Go to `File > Settings/Preferences > Plugins`, search for "Lombok Plugin" in the Marketplace, install it, and restart the IDE.
    * **Verify Project SDK:** Go to `File > Project Structure > Project` and ensure your Project SDK is set to JDK 17 and the Language level is 17.
    * **Invalidate Caches & Restart (Recommended):** `File > Invalidate Caches / Restart... > Invalidate and Restart`. This resolves many common IDE-related issues.

## ▶️ Running Tests

All tests are designed to run using Maven.

1.  **Open your Terminal/Command Prompt** and navigate to the project's root directory.
2.  **Clean and Install Dependencies:** (First time, or after `pom.xml` changes)
    ```bash
    mvn clean install
    ```
3.  **Run All Tests:**
    ```bash
    mvn test
    ```
    This command will execute all JUnit 5 tests located in `src/test/java` and generate raw Allure results in the `target/allure-results` directory.

## 📊 Viewing Allure Reports (Locally)

After running `mvn test`, you can generate and view the interactive Allure report:

1.  **Generate and Serve Report:**
    ```bash
    allure serve target/allure-results
    ```
    This command will process the raw results, generate an HTML report, start a local web server (usually on `http://localhost:8080`), and automatically open the report in your default web browser.

## 🔄 CI/CD Integration (GitHub Actions)

The framework is integrated with GitHub Actions for continuous testing.

* **Workflow File:** The CI pipeline is defined in `.github/workflows/api-tests.yml`.
* **Triggers:** The workflow automatically runs on:
    * Pushes to the `master` (or `main`) and `develop` branches.
    * Creation, updates, and reopens of Pull Requests targeting `master` (or `main`) and `develop` branches.
* **Execution Environment:** Tests run on an `ubuntu-latest` GitHub Actions runner with JDK 17 installed.
* **Artifacts:** After execution, the raw Allure results (`target/allure-results`) are uploaded as a GitHub Actions artifact named `allure-results-raw`.

**To access CI results from GitHub:**

1.  Go to your GitHub repository.
2.  Click on the **"Actions"** tab.
3.  Select a workflow run.
4.  In the "Artifacts" section at the bottom, download the `allure-results-raw.zip` file.
5.  Unzip the file locally and then run `allure serve <unzipped_folder_name>` (e.g., `allure serve allure-results-raw`) in your terminal to view the report.

## 🐛 Troubleshooting

* **`Connection refused` / `ConnectException`:**
    * **Cause:** The Book Application API is not running or is not accessible at the configured `base.url`.
    * **Fix:** Ensure your application is running on `http://localhost:8000` (or the correct configured URL).
* **Compilation Errors (related to Lombok, `getId()`, `getName()` etc.):**
    * **Cause:** Lombok annotation processor not running correctly.
    * **Fix:**
        1.  Ensure Lombok plugin is installed and "Enable annotation processing" is checked in IntelliJ settings.
        2.  Perform `File > Invalidate Caches / Restart... > Invalidate and Restart`.
        3.  Run `mvn clean install` from the terminal after any code or `pom.xml` changes.
        4.  Verify JDK version (must be 17 for this project).
* **GitHub Actions Failure (for `localhost`):**
    * **Cause:** GitHub Actions runners cannot access `localhost` on your machine.
    * **Fix:** Change `base.url` in `config.properties` to a publicly accessible URL where your Book Application API is deployed (e.g., a test environment URL).

---