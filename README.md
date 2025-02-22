# Tracker - An Android App

This is an Open Source Android Application, that lets you

- track your location with your Android phone and 
- send that data over the Internet to a remote API

The app is embedded in a larger system, that can receive and visualize the location data in real-time.

![System Architecture](system-architecture.drawio.svg)

## How does the Location Tracking work

The location tracking works by utilizing the [Fused Location Provider API](https://developers.google.com/location-context/fused-location-provider) for Android.

![Fused Location Provider Funktionsweise](https://heise.cloudimg.io/v7/_www-heise-de_/imgs/18/3/6/9/6/1/2/4/MicrosoftTeams-image__6_-fe0435ee6265148b.png?force_format=avif%2Cwebp%2Cjpeg&org_if_sml=1&q=85&width=610)<br>Image source: heise online Artikel "[50 Jahre Notruf: So ortet die Leitstelle Ihr Mobiltelefon](https://www.heise.de/hintergrund/Notruf-112-So-ortet-die-Leitstelle-Ihr-Mobiltelefon-7490400.html?seite=5)".

As shown by default this uses Google Play Services, but for the de-googled operating system eOS, this is replaced by [MicroG](https://community.e.foundation/t/discover-microg-and-what-it-is-used-for/43418, which is an open source implementation of the Google Play Services.

## The Details

The app tracks the location every 10 minutes. (For development purposes, it is currently set to "every second".)

## Next Steps

- start stop button for tracking
- room database implementation
- build the API
- move the location measurements into the background
- sync app data with API when Internet connection there
- build the website to track (with authentication)