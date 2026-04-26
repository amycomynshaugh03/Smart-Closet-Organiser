# Smart Closet Organiser

A smart wardrobe management Android app built with Kotlin.

**By:** Amy Comyns Haugh  
**Student Number:** 20103780

## About

Smart Closet Organiser helps users manage their wardrobe digitally. Users can track clothing items, build outfits, get AI-powered style suggestions, plan donations, and get weather-based outfit recommendations, all backed by Firebase.


## Features

### Clothing Management
- Add, edit, and delete clothing items with photos
- Store title, description, colour/pattern, size, season, and category
- AI-powered image scanning via **Gemini** to auto-fill clothing details
- Background removal using the **remove.bg API**
- Track the last worn date per item

### Outfit Management
- Create outfits by combining clothing items from your closet
- View, edit, and delete saved outfits
- Virtual try-on screen to preview outfit combinations

### AI Stylist
- Get personalised outfit suggestions powered by **Gemini AI**
- Suggestions are based on your wardrobe, current weather, and your mood input
- Built-in fashion rules engine to ensure colour-coordinated, weather-appropriate recommendations

### Weather Integration
- Real-time weather data via the **Open-Meteo API**
- Weather displayed on the home screen
- AI suggestions factor in current temperature and conditions
- User can set their location via the settings screen

### Outfit Calendar
- Assign outfits to specific dates
- Add personal notes to calendar entries
- View your outfit history at a glance

### Donation Planner
- Flags clothing items that haven't been worn within a configurable threshold
- Search for nearby charity shops and clothing bins with **Google Places API**
- Schedule donation drop-offs with date and location
- Receive reminder notifications on the day of your donation
- Track donation stats and favourite locations

### Authentication
- Email/password sign-in and registration
- Google Sign-In support
- Password reset via email
- User profile editing with photo upload

### Firebase Integration
- Firestore for cloud storage of clothing, outfits, calendar, and donation data
- Firebase Storage for clothing item images
- Offline support via local SQLite backup

### Documentation
- Full KDoc documentation on every Kotlin source file
- Generated HTML docs via **Dokka**, see the latest release for the downloadable docs site



## Tech Stack

| Technology | Usage |
|---|---|
| Kotlin | Primary language |
| Android SDK | Mobile framework |
| Firebase Firestore | Cloud database |
| Firebase Auth | Authentication |
| Firebase Storage | Image storage |
| Hilt | Dependency injection |
| Gemini AI | Image analysis & AI styling |
| Open-Meteo API | Weather data |
| Google Places API | Donation location search |
| remove.bg API | Background removal |
| Retrofit | HTTP networking |
| Dokka | KDoc documentation generation |
| WorkManager | Donation reminder notifications |
| DataStore | User preferences |
| SQLite | Local offline backup |


## Architecture

The app uses a hybrid **MVP + MVVM** architecture:
- **Presenters** handle legacy view logic (clothing, outfits, main add/edit screens)
- **ViewModels** with StateFlow handle reactive UI (home screen, AI stylist, calendar, donations, settings, auth)
- **Hilt** provides dependency injection throughout
- **Repository pattern** separates data sources (Firestore, SQLite, DataStore)

