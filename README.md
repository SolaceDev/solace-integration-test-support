# Solace Integration Test Support

## Overview

The support suite for testing Solace integration projects.

## Table of Contents
* [Repository Contents](#repository-contents)
* [Usage](#usage)
* [Release Process](#release-process)
---

## Repository Contents
These are the projects contained within this repository:
* [Solace SEMP V2 Client](./semp-client)

## Usage

At the time of writing, this project isn't released into Maven Central, so you will have to install this project locally.

### Integrate It Into Your Project (Recommended)

The method will integrate this project into your own. This is the include-and-forget way to use this project.

#### Prerequisites

* Have `git` installed and be accessible in your environment.

#### Initial Setup

1. First, we'll shallowly add this project as a submodule to the root of your project:
    ```shell script
    git submodule add --depth 1 https://github.com/SolaceDev/solace-integration-test-support.git
    ```
   **NOTE:** **DO NOT TOUCH** this submodule. It will be automatically managed and updated by the `exec-maven-plugin` in one of the next few steps.
1. In your POM file, add these properties to your `pom.xml`:
    ```xml
   <properties>
       <solace.integration.test.support.version>0.1.0</solace.integration.test.support.version>
       <solace.integration.test.support.install.skip>true</solace.integration.test.support.install.skip>
   </properties>
    ```
   The `solace.integration.test.support.version` property should specify the release-tag/version of this project that you want to use. This is also what you should use for your dependencies' `<version>` tags.
1. Next, we'll add a plugin to automatically manage this submodule.

    At Maven compile-time, this will:
    * Initialize and update all your Git submodules
    * Fetch and checkout the tag specified by the `solace.integration.test.support.version` property
    * Install the solace-integration-test-support submodule into your local repository

    Now add this plugin to your `pom.xml`:
    ```xml
   <build>
   <plugins>
           <plugin>
               <groupId>org.codehaus.mojo</groupId>
               <artifactId>exec-maven-plugin</artifactId>
               <version>1.6.0</version>
               <inherited>false</inherited>
               <executions>
                   <execution>
                       <id>git-submodule-update</id>
                       <phase>initialize</phase>
                       <goals>
                           <goal>exec</goal>
                       </goals>
                       <configuration>
                           <executable>git</executable>
                           <arguments>
                               <argument>submodule</argument>
                               <argument>update</argument>
                               <argument>--init</argument>
                               <argument>--recursive</argument>
                           </arguments>
                       </configuration>
                   </execution>
                   <execution>
                       <id>solace-integration-test-support_fetch</id>
                       <phase>initialize</phase>
                       <goals>
                           <goal>exec</goal>
                       </goals>
                       <configuration>
                           <skip>${solace.integration.test.support.install.skip}</skip>
                           <executable>git</executable>
                           <arguments>
                               <argument>-C</argument>
                               <argument>${basedir}/solace-integration-test-support/</argument>
                               <argument>fetch</argument>
                               <argument>--depth</argument>
                               <argument>1</argument>
                               <argument>origin</argument>
                               <argument>refs/tags/${solace.integration.test.support.version}:refs/tags/${solace.integration.test.support.version}</argument>
                           </arguments>
                       </configuration>
                   </execution>
                   <execution>
                       <id>solace-integration-test-support_checkout</id>
                       <phase>initialize</phase>
                       <goals>
                           <goal>exec</goal>
                       </goals>
                       <configuration>
                           <skip>${solace.integration.test.support.install.skip}</skip>
                           <executable>git</executable>
                           <arguments>
                               <argument>-C</argument>
                               <argument>${basedir}/solace-integration-test-support/</argument>
                               <argument>checkout</argument>
                               <argument>${solace.integration.test.support.version}</argument>
                           </arguments>
                       </configuration>
                   </execution>
                   <execution>
                       <id>solace-integration-test-support_install</id>
                       <phase>generate-resources</phase>
                       <goals>
                           <goal>exec</goal>
                       </goals>
                       <configuration>
                           <skip>${solace.integration.test.support.install.skip}</skip>
                           <executable>mvn</executable>
                           <arguments>
                               <argument>clean</argument>
                               <argument>install</argument>
                               <argument>-f</argument>
                               <argument>${basedir}/solace-integration-test-support/</argument>
                               <argument>-DskipTests</argument>
                               <argument>-q</argument>
                           </arguments>
                       </configuration>
                   </execution>
               </executions>
           </plugin>
       </plugins>
   </build>
    ```
1. Add this profile to your `pom.xml` so that we only install this submodule if it's not yet locally-installed:
    ```xml
   <profiles>
       <profile>
           <id>solace-integration-test-support_install</id>
           <activation>
               <file>
                   <missing>${settings.localRepository}/com/solace/test/integration/solace-integration-test-support/${solace.integration.test.support.version}</missing>
               </file>
           </activation>
           <properties>
               <solace.integration.test.support.install.skip>false</solace.integration.test.support.install.skip>
           </properties>
       </profile>
   </profiles>
    ```

#### Upgrading

Just update `solace.integration.test.support.version` to point to the version that you want to use then do a `mvn install`.

If you had correctly followed the setup steps, this will auto-magically update and install this submodule. 

### Install It Directly (Not Recommended)

The trivial way to use this is to just directly clone and install this project directly:

```shell script
git clone https://github.com/SolaceDev/solace-integration-test-support.git
cd solace-integration-test-support
mvn install
```

The main drawback to this method is that you will have to manually manage and release the artifacts yourself. So this is not the recommended way to use this project.

## Release Process

Since we're not releasing this project to Maven Central, this command will only tag the release and bump the project to it's next development version.

```shell script
mvn -B release:prepare release:perform -DreleaseVersion="${releaseVersion}" -Dtag="${releaseVersion}"
```
