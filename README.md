[![Actions Status](https://github.com/SolaceDev/solace-integration-test-support/workflows/build/badge.svg)](https://github.com/SolaceDev/solace-integration-test-support/actions/workflows/build-test.yml)

# Solace Integration Test Support

## Overview

The support suite for testing Solace integration projects.

## Table of Contents
* [Repository Contents](#repository-contents)
* [Usage](#usage)
* [Testing](#testing)
* [Release Process](#release-process)
---

## Repository Contents
These are the projects contained within this repository:
* [Solace Integration Test Support BOM](./bom)
* [Solace PubSub+ JUnit Jupiter Utility](./junit-jupiter)
* [Solace SEMP V2 Client](./semp-client)
* [Solace PubSub+ Testcontainer](./testcontainer)

## Usage

### Adding the GitHub Packages Repository

Follow https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token

### Import BOM

Import the BOM so that you don't have to specify the versions for each project from this repository:

```xml
<dependencyManagement>
     <dependencies>
         <dependency>
             <groupId>com.solace.test.integration</groupId>
             <artifactId>solace-integration-test-support-bom</artifactId>
             <version>${solace.integration.test.support.version}</version>
             <type>pom</type>
             <scope>import</scope>
         </dependency>
     </dependencies>
</dependencyManagement>
```

## Testing

### Prerequisites

* Docker must be installed

### Running the Tests

To run the tests:
```shell
./mvnw clean verify
```

To skip the integration tests:
```shell
./mvnw clean verify -DskipITs
```
