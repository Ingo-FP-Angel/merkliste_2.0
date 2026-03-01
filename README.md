# Repo archiviert

Seit dem Relaunch der Bücherhallen-Webseite 2025 funktioniert diese App nicht mehr.
Insbesondere gibt es jetzt ein Captcha gegen Bots.
Dagegen möchte ich nicht anprogrammieren und versetze das Repo in den Ruhestand.
Stattdessen verwende ich jetzt ein User-Script im Browser mit reduzierter Funktionalität.

# Merkliste 2.0

Diese App zeigt alle aktuell verfügbaren Medien, die man auf seiner Merkliste bei den Hamburger Bücherhallen hat.

## Lokales Testen

Um nicht immer gegen die echte buecherhallen.de Webseite zu testen, kann man einen wiremock container starten, der ein paar wenige Medien auf der Merkliste hat:

```
docker-compose -f backend/src/test/resources/docker-compose.yml up -d
```

Dann noch die URL für das Backend in der `application.yml` ändern:

```
merkliste:
  baseUrl: http://localhost:9090
```
