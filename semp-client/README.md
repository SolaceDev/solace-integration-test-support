# Solace SEMP V2 Client

## Overview

Generates the Solace SEMP V2 API and also provides a wrapper class to use it.

## Usage

### Updating Your Build

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

<dependencies>
    <dependency>
        <groupId>com.solace.test.integration</groupId>
        <artifactId>solace-semp-v2-client</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Using It In Your Application

This project has a wrapper class from which you can call any SEMPv2 command:

```java
// Authenticate
SempV2Api sempV2Api = new SempV2Api(mgmtHost, mgmtUsername, mgmtPassword);

// Call Config API Commands
sempV2Api.config().getAboutApi(null);

// Call Monitor API Commands
sempV2Api.config().getAboutApi(null);

// Call Config API Commands
sempV2Api.config().getAboutApi(null, null);
```

