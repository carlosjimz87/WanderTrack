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

Contributions are welcome! If you have suggestions for improving WanderTrack, please open an issue or submit a pull request.

Please ensure your contributions adhere to the following guidelines:

-   **Code Style:** Follow the existing Kotlin coding conventions and project's code style.
-   **Tests:** Add or update tests for any new or changed functionality to maintain high code quality.
-   **Commit Messages:** Write clear and concise commit messages that explain the purpose of your changes.

### How to Contribute

1.  **Fork the repository.**
2.  **Create a new branch** for your feature or bug fix: `git checkout -b feature/your-feature-name` or `bugfix/your-bug-fix-name`.
3.  **Make your changes and commit them.**
4.  **Push your branch** to your forked repository.
5.  **Open a pull request** to the `main` branch of the original repository.

---

## License

WanderTrack is released under the MIT License. See the [LICENSE](LICENSE) file for more details.

---