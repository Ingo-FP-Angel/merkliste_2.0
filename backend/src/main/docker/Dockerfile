FROM eclipse-temurin:17-jre-alpine
COPY . /app
COPY version /app/version

RUN sh -c 'touch /app/@backendArtifactName@'

COPY ./docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
