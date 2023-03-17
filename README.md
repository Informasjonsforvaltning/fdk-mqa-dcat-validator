# fdk-mqa-dcat-validator

This service is part of the Metadata Quality Assessment stack. This service listens to dataset harvested events (Kafka) and 
validates if the dataset RDF is DCAT AP compliant. Results are stored in a DQV metrics model which is
stored in the MQA event topic (Kafka).

## Requirements
- maven
- java 17
- docker
- docker-compose

## Run tests
```
mvn test
```

## Run locally
```
docker-compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=develop
```
