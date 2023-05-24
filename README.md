# ApigeeMetricsReporter

## Description

Lightweight Java ETL capability for populating Apigee Metrics in AppDynamics Events Service

* Exports from Apigee Metrics API
* Transforms into Events Service Records
* Loads into AppDynamics Events Service

## Requirements

1. AppDynamics Analytics API Key and Global Account Name
2. Apigee API Username / Password
3. Java 17
4. Maven

## Configuration

Configuration files reside within the src/main/resources directory
* apigee_api.properties - Sets required properties for source Apigee Metrics API
* events_service.properties - Sets required properties for target AppDynamics Event Service
* options.properties - Sets tuning options for capability
* logging.properties - java.util.logging config

For full details of the configuration options, please see provided documentation.

## Build

After configuration, run a maven build from the root of the project using 'mvn clean install'

## Run

Grab the produced target/ApigeeMetricsReporter-1.0-jar-with-dependencies.jar file and deploy on host / VM.<br /><br />Run using 'java -jar ApigeeMetricsReporter-1.0-jar-with-dependencies.jar'

## Repeat

For additional deployments repeat the above process with differing configuration