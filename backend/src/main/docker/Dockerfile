FROM openjdk:11-jre-slim
COPY . /app
COPY version /app/version

RUN sh -c 'touch /app/@backendArtifactName@'

COPY ./docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
