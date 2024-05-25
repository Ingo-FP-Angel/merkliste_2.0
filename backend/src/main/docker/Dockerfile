FROM eclipse-temurin:17-jre-alpine

RUN mkdir /opt/app
COPY . /opt/app
COPY version /opt/app
CMD ["java", "-jar", "/opt/app/@backendArtifactName@"]
