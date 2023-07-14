## Access token generator for FCM HTTP v1 API

### Steps to get access token

- go to Service account tab in Firebase projects settings
- generate new private key
- save ***.json file
- run *./gradlew :shadowJar* to create Java archive file
- run *java -jar GoogleAccessToken-1.0-SNAPSHOT-all.jar <\*\*\*.json file>* to generate access token

### Build

```bash
$ ./gradlew :shadowJar
```

### Usage

```bash
$ java -jar GoogleAccessToken-1.0-SNAPSHOT-all.jar *path to service account json*
```
Add access token to Authorization header as Bearer token (https://fcm.googleapis.com/v1/projects/<YOUR_PROJECT_NUMBER>/messages:send)
