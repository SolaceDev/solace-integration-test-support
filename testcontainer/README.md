# Solace PubSub+ Testcontainer

## Overview

Basic Testcontainer implementation for Solace PubSub+.

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
        <artifactId>pubsubplus-testcontainer</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Using It In Your Application

```java
// Create and start the PubSub+ container
PubSubPlusContainer container = new PubSubPlusContainer();
container.start();

// Get SEMP admin credentials
container.getAdminUsername();
container.getAdminPassword();

// Get the origin for a given PubSub+ port
container.getOrigin(Port port);
```

See https://www.testcontainers.org for more info.
