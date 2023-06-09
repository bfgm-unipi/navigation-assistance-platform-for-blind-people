# Navigation Assistance Platform for Blind People (VIBA)
![Android](https://img.shields.io/badge/-Android-3DDC84?logo=android&logoColor=white&style=flat-square) ![Kotlin](https://img.shields.io/badge/-Kotlin-7F52FF?logo=kotlin&logoColor=white&style=flat-square) ![Gradle](https://img.shields.io/badge/-Gradle-02303A?logo=gradle&logoColor=white&style=flat-square) ![Java](https://img.shields.io/badge/Java-%23ED8B00.svg?style=flat-square&logo=openjdk&logoColor=white) ![Google ARCore](https://img.shields.io/badge/-Google%20ARCore-4285F4?logo=Google&logoColor=white&style=flat-square)

University project for **Mobile and Social Sensing Systems** course (MSc Computer Engineering at University of Pisa, A.Y. 2022-23)

**VIBA** is an **Android Application** aimed at assisting visually impaired and blind **(VIB) people** in everyday life by **detecting obstacles in an outdoor environment**, in order to warn the user and help him/her to move safely.  
More specifically, it is designed to work in combination with traditional tools such as a walking cane, guide dogs, and human assistant.  
The system uses a **Depth Map** generated by **Google's ARCore Depth Lab API** that is used to **compute the distance between camera and obstacles**. If the user gets too close to them, he/she is **alerted via audio and vibration feedback**.



# Structure of the repository 

```
navigation-assistance-platform-for-blind-people
|
├── src
│   ├── app
│   │   └── src/main
│   |       ├── assets
|   │       |   ├── models
|   │       |   └── shaders
|   │       |   
│   |       ├── java/com/google/ar/core/vibaplatform
│   |       |   ├── java
|   │       |   └── kotlin
│   |       |       ├── common
│   |       |       └── viba
|   │       |
|   |       └── res   
|   |           ├── drawable
|   |           ├── layout
|   |           ├── menu
|   |           └── values
│   └── gradle
| 
└── docs
    ├── documentation
    └── presentation
```

## Authors
- [Biagio Cornacchia](https://github.com/biagiocornacchia)
- [Gianluca Gemini](https://github.com/yolly98)
- [Fabrizio Lanzillo](https://github.com/FabrizioLanzillo)
- [Matteo Abaterusso](https://github.com/MatteoAba)