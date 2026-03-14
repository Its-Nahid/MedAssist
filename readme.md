# 🩺 MedAssist — Advanced Health & Medicine Management Application

**MedAssist** is a modern, comprehensive health management solution built natively with **Android Studio**. Designed to streamline personal healthcare, this application helps users manage medications, track stock, scan medicine labels using AI, and consult a smart health assistant.

This version introduces powerful upgrades including **Google ML Kit** for OCR scanning, **Firebase Firestore** for cloud synchronization, and full **Gemini AI** integration for intelligent assistance.

---

### ✨ Key Features

#### 🛡️ Authentication & Security
* 🔐 **Firebase Authentication**: Secure Email & Password Login / Signup.
* 🧭 **Private Dashboard**: Secure, personalized access for authenticated users.

#### 💊 Medication & Stock Management
* 📦 **Medicine Cloud Database**: Add, view, and manage medicines, synced in real-time with **Firebase Firestore**.
* 📊 **Stock Tracker**: Keep track of available medicines and receive alerts for low stock.
* ⏰ **Active Reminders**: Never miss a dose with the built-in reminder system.

#### 🤖 AI & Smart Features (New in v3)
* 📷 **Smart Medicine Scanner (OCR)**: Instantly scan medicine labels using **CameraX** and **Google ML Kit** Text Recognition.
* 💬 **Gemini AI Health Assistant**: Ask health-related queries and get instant advice powered by the **Google Gemini API**.
* 🔊 **Text-to-Speech Accessibility**: Built-in voice utility for reading out medicine details and instructions.

#### 🧑‍⚕️ Additional Utilities
* 🏥 **Doctor Directory**: Browse and find doctors easily right from the app.
* 🎨 **Modern & Responsive UI**: Clean, accessible, and user-friendly interface constructed with native Android XML.

---

### ⚙️ Tech Stack

* **Language:** Java / Kotlin
* **Framework:** Android SDK
* **Cloud & Backend:** Firebase Authentication, Firestore
* **Artificial Intelligence:** Google Gemini API, Google ML Kit (Text Recognition)
* **Camera:** Android CameraX
* **Networking:** OkHttp, JSON
* **Architecture:** Gradle-based Android project structure
* **IDE:** Android Studio

---

### 🚀 Getting Started

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Its-Nahid/MedAssist.git
   ```
2. **Open the project** in **Android Studio**.
3. **Configure Firebase:**
   * Create a Firebase project and add your `google-services.json` to the `app/` directory.
   * Enable Email/Password authentication and Firestore database in the Firebase Console.
4. **Configure Gemini API:**
   * Create a `local.properties` file in the root directory (if it does not exist).
   * Add your Gemini API key: 
     ```properties
     GEMINI_API_KEY=your_actual_api_key_here
     ```
5. **Build and Run** the app on an emulator or physical Android device.

---

### 🧾 Roadmap & Versions

| Version | Description | Status |
| :--- | :--- | :--- |
| **v1** | Base UI Implementation | ✅ Done |
| **v2** | Firebase Email Authentication + Dashboard integration | ✅ Done |
| **v3** | Firestore Integration, Stock Tracker, Reminders & Push Notifications | ✅ Done |
| **v4** | Smart Medicine Scanner (ML Kit) & AI Chatbot Assistant (Gemini) | ✅ Done |
| **v5** | Multi-language support, Advanced Health Metrics Dashboard, and Wear OS integration | 🚀 Planned |

---

### 🐛 Current Known Bugs

*   *(Add any known bugs or UI glitches here as you find them)*
*   Sometimes the ML Kit scanner might struggle with highly stylized or faded medicine labels.
*   Push notification exact timing can vary depending on Android OS battery optimization settings.

---

### 🧠 What I Learned From This Project

Building **MedAssist** was an incredible journey that significantly leveled up my Android development skills. Key takeaways include:

1. **Integrating AI APIs**: Learned how to effectively integrate third-party REST APIs (Google Gemini) securely using `OkHttp` and hiding sensitive API keys safely using `local.properties` and Gradle `BuildConfig`.
2. **Machine Learning on Device**: Gained hands-on experience with **Google ML Kit** and **CameraX** to perform live optical character recognition (OCR) natively on mobile devices.
3. **Advanced Firebase Concepts**: Transitioned from basic Firebase Auth to implementing robust, real-time NoSQL database structures with **Firebase Firestore**.
4. **Complex UI & State Management**: Better understood how to manage multiple activities, handle asynchronous operations, and map custom models (like `Medicine` and `Doctor`) to custom Recycler View adapters.
5. **Accessibility Integration**: Incorporated Text-to-Speech tools to make the application more accessible and inclusive for a wider range of users.

---

### 👨‍💻 Author

**Nahid**
🔗 [GitHub @Its-Nahid](https://github.com/Its-Nahid)

⭐ *If you find this project useful or interesting, please consider giving it a star on GitHub! It highly encourages further development!*
