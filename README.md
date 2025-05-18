# MemorEase 

MemorEase is an Android application developed with Kotlin, designed to help users preserve and share their memories — with the help of AI-generated questions. The app features a unique dual-user structure: individuals can save their memories, while relatives can contribute and view them with a dedicated interface. Firebase services and Hugging Face AI models are used for memory management and smart quiz generation.

<p align="center">
  <img src="app/src/main/res/drawable/logowname.png" width="180"/>
</p>

---

## 🎯 Features

- 🔐 **User & Relative Login** – Separate flows with secure Firebase authentication
- 📝 **Upload Memories** – Add text, voice, and image memories
- 🤖 **AI-Powered Questions** – Automatically generate quiz questions from memories using a Hugging Face model
- 📊 **Weekly Reports** – Get visual performance feedback on memory sharing
- 🏆 **Leaderboard** – Track and compare memory sharing scores
- 📄 **PDF Export** – Generate printable reports with memory statistics
- 🎨 **Modern UI** – Material Design + image assets for clean and interactive experience

---

## 🛠️ Tech Stack

- **Language:** Kotlin
- **Architecture:** MVVM
- **UI:** XML layouts, RecyclerViews, Fragments
- **Backend:** Firebase Firestore, Firebase Auth, Firebase Storage
- **AI Integration:** Hugging Face API (FLAN-T5 model)
- **PDF Creation:** Android `PdfDocument`
- **Media Handling:** Glide for image loading, MediaPlayer for voice playback

---


## 📸 Screenshots

### 📝 Upload Memory
<p align="left">
  <img src="screenshots/uploadmemory.png" width="300"/>
</p>

---

### ❓ Generate Questions
<p float="left">
  <img src="screenshots/generate1.png" width="250"/>
  <img src="screenshots/generate2.png" width="250"/>
  <img src="screenshots/generate3.png" width="250"/>
</p>

---

### 🧠 Quiz Screen
<p float="left">
  <img src="screenshots/quiz1.png" width="250"/>
  <img src="screenshots/quiz2.png" width="250"/>
</p>

---

### 🏆 Leaderboard
<p align="left">
  <img src="screenshots/leaderboard.png" width="300"/>
</p>

---

### 📊 Weekly Report
<p float="left">
  <img src="screenshots/generalreport.png" width="250"/>
  <img src="screenshots/weeklyreports.png" width="250"/>
  <img src="screenshots/weeklydetail.png" width="250"/>
</p>



 

---

## 👤 Developed By

**Gizem Öz**  
📧 ozgzm2001@gmail.com  
🌐 [linkedin.com/in/gizem-oz](https://linkedin.com/in/gizem-oz)

---

## 📜 License

This project is licensed under the MIT License.  



