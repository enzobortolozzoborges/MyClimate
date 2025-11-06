# 🌤 MyClimate:Aesthetic Weather App

An Android application built with *Kotlin* and *Jetpack Compose* that displays current weather information and forecasts in a simple and modern way.  
The app allows users to *search for cities, **save favorite locations, and **get weather data automatically via GPS*.

---

## 🧩 Features

- Search current weather by city name  
- Display temperature, humidity, wind speed, and condition  
- Automatically fetch weather based on GPS location  
- Save and manage favorite cities  
- Modern interface using Material Design 3  

---

## ⚙ Technologies Used

- *Kotlin*  
- *Jetpack Compose*   
- *Room (Local Database)*  
- *Material 3*  

---

## 🛠 Setup

### Main Dependencies (Gradle)

---

## 📍 GPS Weather

The app can automatically fetch weather data based on the user’s current location.

- Uses *FusedLocationProviderClient* from Google Play Services  
- Requests location permission using rememberLauncherForActivityResult  
- Once granted, calls fetchWeatherByGps() in the ViewModel  
- A GPS icon button in the UI allows refreshing weather by location  

---

## 🖼 User Interface

- Search bar for entering city names  
- Search and GPS icon buttons  
- Weather displayed using *Cards*  
- List of saved cities with delete options  
- Clean, modern, and responsive layout  

---

## 🧠 Architecture Overview

- *ViewModel:* handles app logic and state  
- *Repository:* manages API and database operations  
- *Room:* stores favorite cities locally  
- *Retrofit:* connects to the OpenWeatherMap API  
- *Compose:* builds the entire UI declaratively  

---

## 📸 Example Usage

1. Type a city name to check the current weather  
2. Tap the GPS icon to get the weather for your current location  
3. Save your favorite cities for quick access later  

---

## 👨‍💻 Author

*Enzo Bortolozzo Borges*  
*Davi Henrique Moreira*

Developed as part of the *Mobile Development Android* subject
On *Information Systems* course 
Pontifical Catholic University of Paraná (PUC-PR)
