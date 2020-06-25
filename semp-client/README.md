# Solace SEMP V2 Client

## Overview

Generates the Solace SEMP V2 API and also provides a wrapper class to use it.

## Usage

### Updating Your Build

```xml
<dependency>
    <groupId>com.solace.test.integration</groupId>
    <artifactId>solace-semp-v2-client</artifactId>
    <version>${solace.integration.test.support.version}</version>
    <scope>test</scope>
</dependency>
```

### Using It In Your Application

This project has a wrapper class from which you can call any SEMPv2 command:

```java
// Authenticate
SempV2Api sempV2Api = new SempV2Api(mgmtHost, mgmtUsername, mgmtPassword);

// Call Config API Commands
sempV2Api.config().getAboutApi();

// Call Monitor API Commands
sempV2Api.config().getAboutApi();

// Call Config API Commands
sempV2Api.config().getAboutApi();
```

