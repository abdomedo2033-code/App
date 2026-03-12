# MedPop Quiz - Medical Quiz App

A medical quiz Android app that helps you study medical terms by showing popups when you unlock your phone. Uses spaced repetition to optimize your learning.

## Features

### Core Features
- **Popup on Unlock**: Shows a quiz popup every time you unlock your phone
- **Easy/Hard Buttons**: 
  - **Hard** → Reviews the term sooner/more frequently
  - **Easy** → Reviews the term later/less frequently
- **Term List Management**: Add, edit, and delete medical terms
- **Categories**: Organize terms by Labs, Anatomy, Drugs, Numbers, Pathology, Procedures
- **Random or Sequential**: Choose how terms are displayed
- **Daily Review**: Optional scheduled quizzes throughout the day
- **Statistics**: Track your progress and identify weak spots

### Spaced Repetition Algorithm
The app uses an intelligent spaced repetition system:
- **Easy responses**: Interval increases (1 → 3 → 7 → 14 → 30 → 60 → 90 days)
- **Hard responses**: Interval decreases or resets to review sooner

## Technical Setup

### Platform
- **Platform**: Android (API 24+)
- **Language**: Kotlin
- **Storage**: SQLite with Room ORM
- **Architecture**: MVVM pattern

### Key Components
- **ScreenUnlockReceiver**: Listens for SCREEN_ON/USER_PRESENT events
- **PopupMonitorService**: Foreground service for persistent popup monitoring
- **QuizPopupActivity**: Shows quiz with Easy/Hard buttons
- **Room Database**: Stores terms, categories, and study sessions

## Installation

### Building from Source

1. Clone the repository:
```bash
git clone <repository-url>
cd MedPopQuiz
```

2. Open in Android Studio or build with Gradle:
```bash
./gradlew assembleDebug
```

3. Install the APK:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Required Permissions
- `RECEIVE_BOOT_COMPLETED`: Start service on boot
- `FOREGROUND_SERVICE`: Run persistent monitoring service
- `SYSTEM_ALERT_WINDOW`: Show popups on lock screen
- `POST_NOTIFICATIONS`: Show notification when service is active (Android 13+)

## Usage

### First Launch
1. Open the app
2. Grant necessary permissions (notifications, overlay)
3. Enable "Popup on Unlock" toggle
4. The app comes with pre-loaded medical terms

### Adding Terms
1. Tap "Manage Terms" from main screen
2. Tap the + button
3. Enter question and answer
4. Select category and difficulty
5. Save

### Studying
1. Lock your phone
2. Unlock your phone
3. A quiz popup will appear
4. Tap "Show Answer" to reveal the answer
5. Tap "Easy" or "Hard" based on how well you knew it

### Categories
Default categories include:
- **Labs**: Lab values and reference ranges
- **Anatomy**: Body structures and systems
- **Drugs**: Medications and pharmacology
- **Numbers**: Vital signs and measurements
- **Pathology**: Diseases and conditions
- **Procedures**: Medical procedures and protocols

### Settings
- **Popup on Unlock**: Enable/disable popups
- **Random Order**: Show terms randomly or sequentially
- **Daily Review**: Scheduled quizzes at specific times
- **Minimum Interval**: Set minimum time between popups

## Default Terms Included

### Labs
- Normal Hemoglobin range (male): 13.5 - 17.5 g/dL
- Normal WBC count: 4,500 - 11,000 /μL
- Normal Platelet count: 150,000 - 400,000 /μL
- Normal Creatinine: 0.7 - 1.3 mg/dL
- Normal Potassium (K+): 3.5 - 5.0 mEq/L
- Normal Sodium (Na+): 135 - 145 mEq/L
- Normal Glucose (fasting): 70 - 100 mg/dL
- Normal pH: 7.35 - 7.45
- Normal pCO2: 35 - 45 mmHg

### Anatomy
- Largest organ: Skin
- Adult bones: 206
- Vertebrae in spine: 33
- Largest artery: Aorta
- Smallest blood vessels: Capillaries

### Drugs
- Anaphylaxis drug: Epinephrine
- Status epilepticus: Lorazepam/Diazepam
- Opioid overdose antidote: Naloxone
- Acetaminophen overdose antidote: N-acetylcysteine
- Beta-blocker suffix: -olol

### Numbers
- Normal heart rate: 60 - 100 bpm
- Normal respiratory rate: 12 - 20 breaths/min
- Normal blood pressure: < 120/80 mmHg
- Normal body temperature: 98.6°F (37°C)
- Normal MAP: 70 - 100 mmHg

## Architecture

```
com.medpopquiz/
├── data/
│   ├── entity/          # Room entities (Term, Category, StudySession)
│   ├── dao/             # Data Access Objects
│   ├── AppDatabase.kt   # Room database
│   └── Converters.kt    # Type converters
├── ui/
│   ├── main/            # MainActivity
│   ├── popup/           # QuizPopupActivity
│   ├── terms/           # TermList, AddEditTerm
│   ├── categories/      # CategoryList
│   ├── stats/           # Statistics
│   └── settings/        # Settings
├── receiver/
│   ├── ScreenUnlockReceiver.kt
│   └── BootReceiver.kt
├── service/
│   └── PopupMonitorService.kt
├── worker/
│   └── DailyReviewWorker.kt
└── utils/
    └── PreferenceManager.kt
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

MIT License - See LICENSE file for details

## Acknowledgments

- Material Design 3 components
- Room Persistence Library
- MPAndroidChart for statistics
