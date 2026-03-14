# 🩺 MedAssist — Advanced Health & Medicine Management Application

![Android](https://img.shields.io/badge/Platform-Android-green)
![Java](https://img.shields.io/badge/Language-Java-blue)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple)
![Firebase](https://img.shields.io/badge/Backend-Firebase-orange)
![Gemini](https://img.shields.io/badge/AI-Google%20Gemini-blueviolet)
![ML Kit](https://img.shields.io/badge/OCR-Google%20ML%20Kit-red)
![Release](https://img.shields.io/github/v/release/Its-Nahid/MedAssist)
![Repo Size](https://img.shields.io/github/repo-size/Its-Nahid/MedAssist)
![License](https://img.shields.io/badge/License-MIT-yellow)

**MedAssist** is a modern Android health management application designed to simplify medication tracking and health assistance.

Built natively using **Android Studio**, the app allows users to:

- Manage medicines
- Track medicine stock
- Scan medicine labels using AI OCR
- Consult an AI-powered health assistant

The latest version integrates **cloud storage, machine learning, and AI assistance** to provide a smarter personal healthcare tool.

---

# 📥 Download

Download the latest release:

[![Download Latest Release](https://img.shields.io/badge/Download-Latest%20APK-brightgreen?style=for-the-badge&logo=android)](https://github.com/Its-Nahid/MedAssist/releases/tag/v4.0.0)

Browse all releases:

👉 https://github.com/Its-Nahid/MedAssist/releases

---

# 📱 Screenshots

*(Screenshots will be added soon)*

| Login | Dashboard | Scanner | AI Assistant |
|------|------|------|------|
| ![Login](screenshots/login.png) | ![Dashboard](screenshots/dashboard.png) | ![Scanner](screenshots/scanner.png) | ![AI](screenshots/ai.png) |

Create this folder later:

```
screenshots/
login.png
dashboard.png
scanner.png
ai.png
```

---

# ✨ Key Features

## 🛡 Authentication & Security

- Firebase Email & Password Authentication
- Secure user login and registration
- Personalized user dashboard

---

## 💊 Medicine & Stock Management

- Add and manage medicines
- Real-time cloud synchronization using **Firebase Firestore**
- Medicine stock tracker
- Reminder system for medicine schedules

---

## 🤖 AI & Smart Features

### 📷 Smart Medicine Scanner
- Camera-based label scanning
- OCR using **Google ML Kit**
- Extract medicine information directly from packaging

### 💬 Gemini AI Health Assistant
- Ask health-related questions
- AI-generated responses using **Google Gemini API**

### 🔊 Accessibility Support
- Text-to-Speech for reading medicine instructions

---

## 🧑⚕ Additional Utilities

- Doctor directory
- Clean modern Android UI
- Built with native Android XML layouts

---

# ⚙️ Tech Stack

| Category | Technology |
|--------|--------|
| Language | Java / Kotlin |
| Framework | Android SDK |
| Backend | Firebase Authentication |
| Database | Firebase Firestore |
| AI | Google Gemini API |
| OCR | Google ML Kit |
| Camera | Android CameraX |
| Networking | OkHttp |
| Architecture | Gradle Android Project |
| IDE | Android Studio |

---

# 🚀 Getting Started

## Clone the repository

```bash
git clone https://github.com/Its-Nahid/MedAssist.git
```

Open the project in **Android Studio**.

---

## Configure Firebase

1. Create a Firebase project
2. Add the file:

```
app/google-services.json
```

3. Enable:

* Email/Password Authentication
* Firestore Database

---

## Configure Gemini API

Create a `local.properties` file:

```
GEMINI_API_KEY=your_actual_api_key_here
```

This prevents API keys from being committed to the repository.

---

## Run the Project

Build and run the application on:

* Android Emulator
* Physical Android Device

---

# 🧾 Roadmap

| Version | Description                               | Status     |
| ------- | ----------------------------------------- | ---------- |
| v1      | Base UI implementation                    | ✅ Done     |
| v2      | Firebase authentication                   | ✅ Done     |
| v3      | Firestore + stock tracker + reminders     | ✅ Done     |
| v4      | OCR scanner + Gemini AI assistant         | ✅ Done     |
| v5      | Multi-language + health metrics + Wear OS | 🚀 Planned |

---

# 🐛 Known Issues

* OCR may struggle with **faded or stylized medicine labels**
* Notification timing may vary due to **Android battery optimization**

---

# 🧠 What I Learned

### AI API Integration

Implemented secure API calls using **OkHttp** and protected API keys via `local.properties`.

### On-device Machine Learning

Used **Google ML Kit OCR** with **CameraX** for real-time medicine scanning.

### Firebase Backend

Built a real-time cloud database using **Firestore**.

### Android Development

Managed multiple activities, async operations, and custom RecyclerViews.

### Accessibility

Integrated **Text-to-Speech** to improve usability.

---

# 📂 Project Structure

```
MedAssist
│
├── app
├── screenshots
│
├── README.md
├── LICENSE
└── .gitignore
```

---

# 👨💻 Author

**Nahid**

GitHub
[https://github.com/Its-Nahid](https://github.com/Its-Nahid)

---

⭐ If you like the project, please consider **starring the repository** on GitHub.
