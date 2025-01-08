FROM eclipse-temurin:17-jdk-jammy

COPY ./src src/
RUN pwd && ls -la
COPY gradle/wrapper/ gradle/wrapper/
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle
COPY gradlew gradlew

RUN chmod +x gradlew && \
    ./gradlew bootJar && \
    ls -la build/

FROM eclipse-temurin:17-jre-jammy
COPY --from=0 ./build/libs/generator-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8079
ENTRYPOINT [ "java", "-jar", "app.jar" ]