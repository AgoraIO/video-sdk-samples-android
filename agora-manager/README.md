# AgoraManager

The `AgoraManager` class encapsulates the basic logic to setup the Agora SDK, join, and leave a channel. All projects in this repository extend the `AgoraManager` class to add specific functionality for that project.

Additionally, the `agora_manager` folder includes the essential UI components for the quickstart project. All projects within this repository build upon the quickstart UI layout to incorporate additional UI elements required to showcase specific functionalities.

## The `config.json` file

This section provides information about the configuration file [`config.json`](./src/main/res/raw/config.json) used in the project. The `AgoraManager` class loads the configuration data from this file. The file has the following structure:

```json
{
    "uid": "0",
    "appId": "<--Your app Id-->",
    "channelName": "demo",
    "rtcToken": "<--Authentication token from Agora Console-->",
    "serverUrl": "<--Token server url-->",
    "tokenExpiryTime": "600"
}
```

### Properties

- `uid`: The user ID associated with the application.
- `appId`: The unique ID for the application obtained from https://console.agora.io.
- `channelName`: The name of the channel to join.
- `rtcToken`: The RTC (Real-Time Communication) token generated for authentication.
- `serverUrl`: The URL for the token generator.
- `tokenExpiryTime`: The time in seconds after which a token expires.


Please ensure that the [`config.json`](./src/main/res/raw/config.json) file is correctly populated with the required values before running the application.