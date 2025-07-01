# WanderTrack

WanderTrack is a modern Android app to visually track the countries and cities you’ve visited on an interactive world map. Users can mark visited countries and cities, save their trips with Firebase backend, and enjoy a sleek Material You UI with smooth map interactions.

---

## Features

- Interactive world map with country and city selection
- Highlight visited countries and cities with customizable colors
- Bottom sheet with detailed country info and city visit toggles
- User authentication with email/password and Google Sign-In (Firebase Auth)
- Real-time data sync and persistence via Firebase Firestore
- Clean architecture using Kotlin, Jetpack Compose, and Koin for DI
- Material You theming with light/dark modes and custom color palette
- Splash screen with Lottie animation and smooth transition to login
- Scalable for freemium monetization and multi-language support

---

## Mockups

![Mockup 1](https://i.imgur.com/wYHF0Va.png)

---

## Setup & Installation

### Prerequisites

- Android Studio Bumblebee or newer
- Android SDK 31+
- Firebase project with Auth and Firestore configured
- Google Maps API key

### Configuration

1. Clone this repository:
   ```bash
   git clone https://github.com/carlosjimz87/WanderTrack.git

2. Add your Firebase config (google-services.json) to the app/ directory.
   
3. Create a `secrets.keystore` file at the root of the project (this file should be gitignored) containing your sensitive keys:

    - `GOOGLE_MAPS_KEY`: Your Google Maps API key.
    - `GOOGLE_CLIENT_ID`: Your OAuth client ID for Google Sign-In.

   The file format should be:
   - GOOGLE_MAPS_KEY=your_google_maps_api_key_here
   - GOOGLE_CLIENT_ID=your_google_oauth_client_id_here

4. Sync your Gradle project to ensure all dependencies are downloaded.

---

## Usage

- Launch the app to enjoy the splash animation showcasing the animated planet and app logo.
- Authenticate via Google Sign-In or email/password.
- Navigate the interactive map, mark countries and cities you have visited.
- Access detailed trip information in the bottom sheet.
- All data is saved and synchronized with Firebase in real-time.

---

## Architecture Overview

- **MVVM** architecture with `MapViewModel` and `AuthViewModel`.
- Backend powered by **Firebase Firestore** for realtime data sync.
- UI built with **Jetpack Compose** and **Google Maps Compose**.
- Dependency injection managed by **Koin**.
- Asynchronous programming and UI state management via **Kotlin Coroutines** and **StateFlow**.
- Modern theming following **Material You** guidelines, supporting light and dark modes.

---

## Contributing

> This repository and its contents are proprietary and intended for personal use only. Unauthorized use, copying, modification, or distribution is strictly prohibited.

## License

> This project is proprietary. No license is granted for usage, redistribution, or modification. All rights reserved.
---