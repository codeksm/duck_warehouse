# ---- build stage: full JDK + Maven, thrown away after the jar is built ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
# copy only the pom first so the dependency layer is cached until pom.xml changes
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
# unit tests run with `mvn test` (see README); the image build just packages
RUN mvn -q -B -DskipTests package

# ---- runtime stage: JRE only, non-root ----
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --no-create-home duck
COPY --from=build /build/target/warehouse-*.jar /app/app.jar
USER duck
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
