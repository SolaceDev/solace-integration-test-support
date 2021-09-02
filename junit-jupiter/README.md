# Solace PubSub+ JUnit Jupiter Utility

## Overview

Utility for using Solace PubSub+ in JUnit Jupiter.

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
        <artifactId>pubsubplus-junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Using It In Your Application

This project provides a number of JUnit extensions:

* [ExecutorServiceExtension](src/main/java/com/solace/test/integration/junit/jupiter/extension/ExecutorServiceExtension.java)
* [LogCaptorExtension](src/main/java/com/solace/test/integration/junit/jupiter/extension/LogCaptorExtension.java)
* [PubSubPlusExtension](src/main/java/com/solace/test/integration/junit/jupiter/extension/PubSubPlusExtension.java)

