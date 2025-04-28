# FDK MQA DCAT Validator

This service is part of the Metadata Quality Assessment stack. This service listens to dataset harvested events (Kafka)
and validates if the dataset RDF is DCAT AP compliant. Results are stored in a DQV metrics model which is stored in the
MQA event topic (Kafka).

For a broader understanding of the system’s context, refer to
the [architecture documentation](https://github.com/Informasjonsforvaltning/architecture-documentation) wiki. For more
specific context on this application, see the **Metadata Quality** subsystem section.

## Requirements

- Java 17
- Maven
- Docker

### Running locally

#### Clone the repository

```sh
git clone https://github.com/Informasjonsforvaltning/fdk-mqa-dcat-validator.git
cd fdk-mqa-dcat-validator
```

#### Generate sources

Kafka messages are serialized using Avro. Avro schemas are located in ```kafka/schemas```. To generate sources from Avro
schema, run the following command:

```sh
mvn generate-sources    
```

#### Start Kafka cluster and setup topics/schemas

Topics and schemas are set up automatically when starting the Kafka cluster. Docker compose uses the scripts
```create-topics.sh``` and ```create-schemas.sh``` to set up topics and schemas.

```sh
docker-compose up -d
```

If you have problems starting kafka, check if all health checks are ok. Make sure number at the end (after 'grep')
matches desired topics.

#### Start application

```sh
mvn spring-boot:run -Dspring-boot.run.profiles=develop
```

#### Produce messages

Check if schema id is correct in the script. This should be 1 if there is only one schema in your registry.

```sh
sh ./kafka/produce-messages.sh
```

### Running tests

```sh
mvn verify
```
