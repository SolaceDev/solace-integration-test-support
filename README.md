[![Actions Status](https://github.com/SolaceDev/solace-integration-test-support/workflows/build/badge.svg)](https://github.com/SolaceDev/solace-integration-test-support/actions/workflows/build-test.yml)

# Solace Integration Test Support

## Overview

The support suite for testing Solace integration projects.

## Table of Contents
* [Repository Contents](#repository-contents)
* [Usage](#usage)
* [Testing](#testing)
---

## Repository Contents
These are the projects contained within this repository:
* [Solace Integration Test Support BOM](./bom)
* [Solace PubSub+ JUnit Jupiter Utility](./junit-jupiter)
* [Solace SEMP V2 Client](./semp-client)
* [Solace PubSub+ Testcontainer](./testcontainer)

## Usage

### Configuring Maven to Pull the Artifacts

This project's artifacts are published to GitHub Packages. To build projects that depend on these test utilities, you need to configure Maven to authenticate with GitHub Packages.

#### Prerequisites

- A GitHub account
- GitHub Personal Access Token with `read:packages` scope (see [here for more info](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry#authenticating-with-a-personal-access-token))

#### Maven Configuration

Add the following to your Maven `~/.m2/settings.xml` file. In particular you need to add the `github-solace-integration-test-support` `<repository>` and `<server>` to your active `<profile>`:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                           http://maven.apache.org/xsd/settings-1.0.0.xsd">

<activeProfiles>
 <activeProfile>github</activeProfile>
</activeProfiles>

<profiles>
 <profile>
   <id>github</id>
   <repositories>
     <repository>
       <id>central</id>
       <url>https://repo.maven.apache.org/maven2</url>
     </repository>
     <repository>
       <id>github-solace-integration-test-support</id>
       <url>https://maven.pkg.github.com/solacedev/solace-integration-test-support</url>
     </repository>
   </repositories>
 </profile>
</profiles>

<servers>
 <server>
   <id>github-solacedev</id>
   <username>YOUR_GITHUB_USERNAME</username>
   <password>YOUR_PERSONAL_ACCESS_TOKEN</password>
 </server>
</servers>

</settings>
```

Replace `YOUR_GITHUB_USERNAME` with your GitHub username and `YOUR_PERSONAL_ACCESS_TOKEN` with your token.

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
