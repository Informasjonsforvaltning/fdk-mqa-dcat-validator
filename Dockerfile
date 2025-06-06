FROM maven:3-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
COPY kafka ./kafka
RUN mvn clean package --no-transfer-progress -DskipTests
RUN mvn versions:display-dependency-updates --no-transfer-progress

FROM eclipse-temurin:17-jre-jammy
ENV TZ=Europe/Oslo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /app
RUN addgroup --gid 1001 --system app && \
  adduser --uid 1001 --system app --gid 1001 && \
  chown -R app:app /app && \
  chmod 770 /app
USER app:app
COPY --chown=app:app --from=build /app/target/fdk-mqa-dcat-validator.jar ./

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -Xss1m"

CMD ["sh", "-c", "java -jar fdk-mqa-dcat-validator.jar"]
