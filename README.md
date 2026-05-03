SKYba Vision: Demo

📌 About the Project
SKYba Vision is a professional eye-care assistant designed for individuals who spend significant time in front of screens. 
The app automates work-rest cycles, helping users implement the healthy 20-20-20 habit to prevent digital eye strain.

Important: This repository is a showcase demo created to demonstrate the architectural decisions and technical stack of a real-world product.

🛠 Technical Stack & Features
The project showcases the implementation of modern Android development standards:

Architecture: Clean Architecture + MVVM + Unidirectional Data Flow (UDF).

UI Layer: Jetpack Compose (Material 3).

Background Engine: Utilizes Foreground Services to ensure uninterrupted timer operation, even when the app is minimized or the device is locked.

Data Layer: Room Persistence Library for local storage of sessions and statistics.

Custom Visualization:

Implemented a Custom Scroll Column Chart.

Based on the io.github.ehsannarmani:compose-charts library, which was significantly modified and extended to support horizontal scrolling 
and specific logic for displaying hourly activity.

🚀 Key Features (Demo)
Fully Automated Cycles: Set it once, and the app manages work and break intervals throughout the day.

Interactive Notifications: Pause, skip, or restart intervals directly from the notification shade.

Deep Analytics: Detailed activity chart for the current day to track break consistency.

Customizable Timer: Flexible adjustment of work and break durations.

Sound & Volume Control: Personalized notification sounds and volume settings.

Author: Artem Skyba

This project was created as part of a developer portfolio.
