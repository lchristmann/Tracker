# Tracker - An Android App

<img style="float: right; margin-left: 12px;" src="app/src/main/res/mipmap-xhdpi/ic_launcher.png">

This is an Open Source Android Application, that lets you

- track your location with your Android phone and 
- send that data over the Internet to a remote API

The app is embedded in a larger system, that can receive and visualize the location data in near real-time.

![System Architecture](system-architecture.drawio.svg)

The project for the API can be found here: [tracker-api-aws-lambda-function](https://github.com/lchristmann/tracker-api-aws-lambda-function) (left side of diagram)

The project for the website can be found here: &lt;tbd&gt; (right side of diagram)

## The App's Architecture

The app uses the [MVVM (Model View ViewModel) Architecture Pattern in Android](https://www.geeksforgeeks.org/mvvm-model-view-viewmodel-architecture-pattern-in-android/) for structure (that's the top half of the diagram).

The database part is implemented with the recommended [Room database persistency library (using the Repository pattern)](https://medium.com/swlh/basic-implementation-of-room-database-with-repository-and-viewmodel-android-jetpack-8945b364d322) that [provides a powerful abstraction layer over SQLite](https://developer.android.com/training/data-storage/room) (that's the bottom half).

![App Architecture](app-architecture.drawio.svg)

## How does the Location Tracking work

The location tracking works by utilizing the [Fused Location Provider API](https://developers.google.com/location-context/fused-location-provider) for Android.

![Fused Location Provider Funktionsweise](https://heise.cloudimg.io/v7/_www-heise-de_/imgs/18/3/6/9/6/1/2/4/MicrosoftTeams-image__6_-fe0435ee6265148b.png?force_format=avif%2Cwebp%2Cjpeg&org_if_sml=1&q=85&width=610)<br>Image source: heise online Artikel "[50 Jahre Notruf: So ortet die Leitstelle Ihr Mobiltelefon](https://www.heise.de/hintergrund/Notruf-112-So-ortet-die-Leitstelle-Ihr-Mobiltelefon-7490400.html?seite=5)".

As shown by default this uses Google Play Services, but for the de-googled operating system eOS, this is replaced by [MicroG](https://community.e.foundation/t/discover-microg-and-what-it-is-used-for/43418, which is an open source implementation of the Google Play Services.

## Details

The app tracks the location every 10 minutes. (For development purposes, it is currently set to "every second".)

### App Icon

The app icon is currently configured [like this](https://icon.kitchen/i/H4sIAAAAAAAAAzWQMY%2FDIAyF%2F4tvzcI1WbJ2uPWk63Y6nSA2BJXEKZBWVdX%2FXjttFzAffu%2BBb3C2aaUC%2FQ3Q5uNhpImg9zYVasCFPSfO0MPHznfGddCAD%2FsUF5urSgrJBkjerqnKZRx4FuAn%2FA%2FMCHftP1wXsYThJVPbFwrZYqT5yb7eB%2FEdNNZobmvQeC8NG%2FpU5HDXehJk55DEpu22mG%2BLGOeg8soL9KZtIMcwiqGWjmvl6Vkn8hsVnQs%2Fo90eU05rzIMYCp0Y16RT%2BZUQzBxRP8dF1gs5%2BLs%2FAFGiSo84AQAA) thanks to the [IconKitchen App Icon Generator](https://icon.kitchen).

## Development Notes

- use a new Android Studio with the new UI (made by JetBrains)
- use Logcat, Build and App Inspection to get insights (bottom left bar)
  - under App Inspection there's Database Inspection
    - have the "Keep database connections open" option selected there, so you can keep inspecting the database when the app has closed
  - under App Inspection there's Background Task Inspector -> view my WorkManager Tasks and their status
- when changing the LocationEntity (or in general the database schema), go into the Emulator device > Settings > Tracker (App) > Storage > Delete all storage (else the Room database will crash again and again, because it can't cope with the inconsistency of a changed schema)
- for debugging SharedPreferences: you can go in the top right bar to "Device Manager" and then 3 dots > Open in Device Explorer > /data/data/com.lchristmann.tracker/shared_prefs/tracking_prefs.xml and download that file (there you can see the current value of `isTracking`)
- `./gradlew clean build`, `./gradlew --version`, `./gradlew --warning-mode all`, `./gradlew build -stacktrace`

## Next Steps

- sync app data with API when Internet connection there
- build the website to track (with authentication)