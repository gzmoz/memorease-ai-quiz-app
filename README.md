# MemorEase 

MemorEase is an Android application developed with Kotlin, designed to help users preserve and share their memories — with the help of AI-generated questions. The app features a unique dual-user structure: individuals can save their memories, while relatives can contribute and view them with a dedicated interface. Firebase services and Hugging Face AI models are used for memory management and smart quiz generation.

<p align="center">
  <img src="app/src/main/res/drawable/logowname.png" width="180"/>
</p>

MemorEase is an Android application that allows users to preserve and share their memories in text, voice, and image formats. The app features two distinct user flows — one for individuals and another for relatives. While individuals can upload and review their memories, relatives are also encouraged to contribute, fostering intergenerational interaction and shared remembrance.

A key feature of the app is its AI-powered question generation system. Instead of using a generic model, a custom fine-tuned version of the FLAN-T5 model was trained specifically for this project to generate context-aware, memory-based quiz questions. This approach enhances cognitive stimulation and encourages users to recall and reflect on their past experiences through gamified prompts.

The backend utilizes Firebase services for secure authentication, cloud storage, and real-time data handling. Meanwhile, the fine-tuned Hugging Face model enables intelligent quiz generation based on user-submitted memories. With modern UI components, PDF reporting, and leaderboard tracking, MemorEase transforms memory sharing into an engaging, personalized, and socially connected experience.

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
- **Backend:** Firebase Firestore, Firebase Auth, Cloudinary
- **AI Integration:** Hugging Face API (FLAN-T5 model)
- **Media Handling:** Glide for image loading, MediaPlayer for voice playback

---


## 📸 Screenshots

###🔐 User & Relative Login
<p align="left">
  <img src="screenshots/main.png" width="250"/>
  <img src="screenshots/signinuser.png" width="250"/>
  <img src="screenshots/signinrelative.png" width="250"/>
</p>

### 📝 Home Screen / Upload & Review Memory
<p align="left">
  <img src="screenshots/home.png" width="250"/>
  <img src="screenshots/uploadmemory.png" width="250"/>
  <img src="screenshots/reviewmemory.png" width="250"/>
</p>

---

### ❓ Generate Questions
<p float="left">
  <img src="screenshots/generate1.png" width="250"/>
  <img src="screenshots/generate2.png" width="250"/>
  <img src="screenshots/generate3.png" width="250"/>
  <img src="screenshots/generate4.jpg" width="250"/>
  <img src="screenshots/generate5.jpg" width="250"/>
  <img src="screenshots/generate6.jpg" width="250"/>
</p>

---

### 🧠 Quiz Screen
<p float="left">
  <img src="screenshots/quiz1.png" width="250"/>
  <img src="screenshots/quiz2.png" width="250"/>
   <img src="screenshots/quiz3.png" width="250"/>
   <img src="screenshots/quiz4.png" width="250"/>
   <img src="screenshots/quiz5.jpg" width="250"/>
   <img src="screenshots/quiz6.jpg" width="250"/>
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



