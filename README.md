# 🐔 Chicken Farm Management System

> A desktop application for managing chicken farm operations built with Java and JavaFX

[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-25-blue.svg)](https://openjfx.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE.txt)

---

## 👥 Team Members

This project was developed as part of a university Java course by the following team:

| Name                     | Role                                  | GitHub |
|--------------------------|---------------------------------------|--------|
| ELFADILI MOHAMED YACINE  | Chef de Projet (Project Lead)        | [@Medfadili20Dev](https://github.com/Medfadili20Dev) |
| HAMMOU MOHAMED           | Développeur Backend / Base de Données | [@Hmou05](https://github.com/Hmou05) |
| ANSSEM HAFID             | Développeur Frontend / JavaFX         | [@ANSS77](https://github.com/ANSS77) |
| HAIFI MOHAMED AMINE      | Testeur / Documentateur               | [@Mohamadaminehaifi](https://github.com/Mohamadaminehaifi) |
| OUCHRAA ISMAIL           | Architecte Logiciel / Design Patterns | [@ismailouchraa](https://github.com/ismailouchraa) |

### 👨‍🏫 Supervising Professors

| Name               | GitHub Profile                               |
|--------------------|----------------------------------------------|
| Youssef ES-SAADY   | [@essaady](https://github.com/essaady)       |
| Abderrahmane SADIQ | [@AbSadiqHub](https://github.com/AbSadiqHub) |
| AICHA DAKIR        | [@ProfessorGitHub3](#)                       |

---

## 📋 Project Description

The **Chicken Farm Management System** is a comprehensive desktop application designed to streamline and optimize chicken farm operations. Built with modern Java technologies, this system provides farm managers with powerful tools to track chicken inventory across multiple houses, monitor egg production, manage storage resources, and oversee personnel.

### What Problems Does It Solve?

- **Manual Record Keeping**: Eliminates error-prone paper-based tracking
- **Data Fragmentation**: Centralizes all farm data in one application
- **Production Monitoring**: Provides real-time insights into egg production and chicken health
- **Resource Management**: Tracks feed, medications, and equipment inventory
- **Task Coordination**: Organizes daily tasks and assigns them to workers

### Target Users

- Farm managers and owners
- Farm workers and supervisors
- Agricultural operations coordinators

### Key Benefits

✅ Real-time production tracking  
✅ Automated data calculations  
✅ Simplified inventory management  
✅ Task assignment and monitoring  
✅ Historical data analysis  
✅ Improved decision-making capabilities

---

## 🎯 Development Approach

We are building this application using an **MVP (Minimum Viable Product)** approach. This means we start by creating a simplified but fully functional version with core features, then iteratively add more advanced functionality based on testing and feedback.

### Why MVP?

- ✅ **Faster Initial Delivery** - Get a working product quickly
- ✅ **Early User Feedback** - Test with real users sooner
- ✅ **Focus on Core Functionality** - Build what's essential first
- ✅ **Iterative Improvement** - Add features based on actual needs
- ✅ **Better Risk Management** - Identify issues early

### Timeline Overview

**Sprint Duration**: 5 Days  
**Target**: Functional MVP with 7 core pages  
**Approach**: Agile development with daily standups

---

## 🚀 MVP Version - Feature Set

The initial MVP release includes the following pages and functionalities:

### 🔐 **1. Login Page**

**Features:**
- Email and password text inputs
- Simple login button with validation
- Pre-configured admin account

**How It Works:**
- User enters credentials
- System validates against database
- Successful login redirects to Dashboard

**Default Credentials:**
- Email: `admin@farm.ma`
- Password: `admin123`

**Not Included:** Sign up, Forgot password, Remember me

---

### 📊 **2. Dashboard**

**Features:**
- **3 Metric Cards:**
    - Total chickens count across all houses
    - Eggs produced today (H2 + H3)
    - Active alerts count
- **7-Day Egg Production Bar Chart** for trend visualization

**How Data is Shown:**
- Auto-queries database on page load
- Optional auto-refresh every 30 seconds
- Real-time calculations from database

---

### 🏠 **3. Chicken Bay**

**Features:**
- **4 House Cards:**
    - **House 1**: Day-old chicks (0-168 days)
    - **House 2**: Female egg layers (169+ days)
    - **House 3**: Female meat chickens (545+ days)
    - **House 4**: Male meat chickens (169+ days)

**Each Card Displays:**
- House name and chicken type
- Current count and average age
- Health status (Good/Fair/Poor)
- Days until transfer to next house

**Mortality Tracking Card:**
- Deaths today
- Deaths this week
- Deaths this month

**Actions:**
- **Add Chickens**: Select house, enter quantity and arrival date
- **Record Death**: Select house, enter deaths and cause

---

### 🥚 **4. Eggs Bay**

**Features:**
- **House 2 Card** (Egg layers):
    - Eggs collected today
    - Dead chickens today
- **House 3 Card** (Meat females):
    - Eggs collected today
    - Dead chickens today
- **Total Eggs Inventory Card**

**Actions:**
- **Record Collection**: Select house, enter collected eggs, cracked eggs, and dead chickens
- **Remove Eggs**: Deduct quantity for sales or usage

---

### 📦 **5. Storage**

**Features:**

**Feed Card:**
- Day-old chick feed (kg)
- Egg layer feed (kg)
- Meat growth feed (kg)

**Medications Card:**
- Total medication types in stock
- Number of low-stock medications

**Equipment Table:**
- Equipment name, quantity, and status (Good/Fair/Broken)

**Actions:**
- **Feed**: Add or use feed (updates stock)
- **Medications**: Add or use medications
- **Equipment**: Add new equipment or update status

---

### ✅ **6. Tasks**

**Features:**
- List view with status badges:
    - ✓ Done
    - ○ Pending
    - ✗ Missed
- Each task displays:
    - Task description
    - Cracked eggs count (if applicable)
    - Assigned worker name

**Example Task:**  
*"Collect eggs from H2 - Cracked: 15, Assigned to: Ahmed Elbagi"*

**Actions:**
- **Add Task**: Enter description, assign worker, set due date
- **Mark as Done**: Update task status
- **Edit/Delete**: Modify or remove tasks

---

### 👨‍🌾 **7. Personnel**

**Features:**
- Grid of personnel cards showing:
    - Full name and age
    - Phone number and email
    - Job title (Worker or Tracker only)

**Actions:**
- **Add Worker**: Enter name, age, phone, email, and role
- **Edit Worker**: Update information
- **Delete Worker**: Remove personnel record

**Not Included in MVP:** Salary, CNSS, contracts, attendance tracking

---

## 🛠️ Technologies Stack

- **Language:** Java 17+
- **UI Framework:** JavaFX 25
- **Build Tool:** Maven
- **Database:** SQLite 3.44.1 with DAO pattern
- **Architecture:** MVC (Model-View-Controller)
- **Testing:** JUnit 5
- **Version Control:** Git & GitHub
- **IDE:** IntelliJ IDEA

---

## 🎨 UML Diagrams (MVP Version)

### 📥 Download Diagrams
[Download ZIP file here (Mega link)](https://mega.nz/file/5i0xQSaD#5nWTCpLmXBYek9CQGhTECigcV6bra0bZX3PW9EZYgUw)

### 📊 Class Diagram - System Architecture
![General Class Diagram](Diagram_Uml_MVP/Classe/Images/Class_Diagram_General.png)

### 🎯 Use Case Diagram - System Operations
![General Use Case Diagram](Diagram_Uml_MVP/UseCase/Images/UC_Diagram_General.png)

### 📈 Sequence Diagrams - Key Workflows
The sequence diagrams cover the following flows:
1. Login & Authentication
2. View Dashboard
3. Add Chicken Batch
4. Record Egg Collection
5. Add Feed to Storage
6. Create & Assign Task
7. Add Personnel

*See `Diagram_Uml_MVP/Séquences/` for all sequence diagrams.*



## 🏗️ Architecture

This project follows the **MVC (Model-View-Controller)** architectural pattern:

- **Model**: Entity classes representing database tables (User, House, Chicken, EggProduction, etc.)
- **View**: FXML files defining the UI layout for each page
- **Controller**: JavaFX controller classes handling user interactions and business logic
- **DAO (Data Access Objects)**: Provides a clean interface to database operations

### Architecture Benefits

- Clear separation of concerns
- Easy to maintain and test
- Scalable for future enhancements
- Follows industry best practices

---

## 📁 Folder Structure (MVP Version)

```txt
Chicken_Farm_Management_System/
│
├── database/
│   └── farm.db
│
├── Diagram_Uml_MVP/
│   ├── Classe/
│   │   ├── Images/
│   │   │   └── Chicken_Farm_Class_Diagram_General.png
│   │   └── SourceCode/
│   │       ├── Chicken Farm Class Diagram 1.plantuml
│   │       ├── Chicken Farm Class Diagram 2.plantuml
│   │       ├── Chicken Farm Class Diagram 3.plantuml
│   │       ├── Chicken Farm Class Diagram 4.plantuml
│   │       └── Chicken Farm Class Diagram General.plantuml
│   │
│   ├── Séquences/
│   │   ├── Images/
│   │   └── SourceCode/
│   │       ├── Chicken Farm Sequence Diagram 1.plantuml
│   │       ├── Chicken Farm Sequence Diagram 2.plantuml
│   │       ├── Chicken Farm Sequence Diagram 3.plantuml
│   │       ├── Chicken Farm Sequence Diagram 4.plantuml
│   │       ├── Chicken Farm Sequence Diagram 5.plantuml
│   │       ├── Chicken Farm Sequence Diagram 6.plantuml
│   │       └── Chicken Farm Sequence Diagram 7.plantuml
│   │
│   └── UseCase/
│       ├── Images/
│       │   └── UC_Diagram_General.png
│       └── SourceCode/
│           ├── UC_Diagram_1_System_Overview.plantuml
│           ├── UC_Diagram_2_Livestock_Operations.plantuml
│           ├── UC_Diagram_3_Storage_Inventory.plantuml
│           ├── UC_Diagram_4_Personnel_Tasks.plantuml
│           └── UC_Diagram_General.plantuml
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ma/
│   │   │       └── farm/
│   │   │           ├── controller/
│   │   │           │   ├── dialogs/
│   │   │           │   │   ├── AddEditEquipmentItemDialogController.java
│   │   │           │   │   ├── AddEditFeedDialogController.java
│   │   │           │   │   ├── AddEditMedicationDialogController.java
│   │   │           │   │   ├── AddEditPersonnelDialogController.java
│   │   │           │   │   ├── AddEditTaskDialogController.java
│   │   │           │   │   ├── AddEquipmentCategoryDialogController.java
│   │   │           │   │   ├── AddHouseDialogController.java
│   │   │           │   │   ├── ConfigHousesDialogController.java
│   │   │           │   │   ├── DistributeChicksDialogController.java
│   │   │           │   │   ├── EditEggProductionDialogController.java
│   │   │           │   │   ├── EditHouseDialogController.java
│   │   │           │   │   ├── ImportChicksDialogController.java
│   │   │           │   │   ├── ManageEquipmentItemsDialogController.java
│   │   │           │   │   ├── PersonnelDetailDialogController.java
│   │   │           │   │   ├── RecordEggCollectionDialogController.java
│   │   │           │   │   ├── RecordMortalityDialogController.java
│   │   │           │   │   ├── SellChickensDialogController.java
│   │   │           │   │   ├── SellEggsDialogController.java
│   │   │           │   │   ├── TransferChickensDialogController.java
│   │   │           │   │   ├── UseFeedDialogController.java
│   │   │           │   │   └── UseMedicationDialogController.java
│   │   │           │   ├── ChickenBayController.java
│   │   │           │   ├── DashboardController.java
│   │   │           │   ├── EggsBayController.java
│   │   │           │   ├── LoginController.java
│   │   │           │   ├── MainWindowController.java
│   │   │           │   ├── PersonnelController.java
│   │   │           │   ├── SidebarController.java
│   │   │           │   ├── SignUpController.java
│   │   │           │   ├── StorageController.java
│   │   │           │   └── TasksController.java
│   │   │           │
│   │   │           ├── dao/
│   │   │           │   ├── ChickenDAO.java
│   │   │           │   ├── DatabaseConnection.java
│   │   │           │   ├── EggProductionDAO.java
│   │   │           │   ├── EquipmentDAO.java
│   │   │           │   ├── FeedDAO.java
│   │   │           │   ├── HouseDAO.java
│   │   │           │   ├── MedicationDAO.java
│   │   │           │   ├── MortalityDAO.java
│   │   │           │   ├── PersonnelDAO.java
│   │   │           │   ├── TaskDAO.java
│   │   │           │   └── UserDAO.java
│   │   │           │
│   │   │           ├── model/
│   │   │           │   ├── Chicken.java
│   │   │           │   ├── EggProduction.java
│   │   │           │   ├── Equipment.java
│   │   │           │   ├── Feed.java
│   │   │           │   ├── House.java
│   │   │           │   ├── Medication.java
│   │   │           │   ├── Mortality.java
│   │   │           │   ├── Personnel.java
│   │   │           │   ├── Task.java
│   │   │           │   └── User.java
│   │   │           │
│   │   │           ├── util/
│   │   │           │   ├── DateUtil.java
│   │   │           │   ├── NavigationUtil.java
│   │   │           │   └── ValidationUtil.java
│   │   │           │
│   │   │           └── App.java
│   │   │
│   │   └── resources/
│   │       ├── css/
│   │       │   └── style.css
│   │       │
│   │       ├── database/
│   │       │   └── schema.sql
│   │       │
│   │       └── fxml/
│   │           ├── dialogs/
│   │           │   ├── AddEditEquipmentItemDialog.fxml
│   │           │   ├── AddEditFeedDialog.fxml
│   │           │   ├── AddEditMedicationDialog.fxml
│   │           │   ├── AddEditPersonnelDialog.fxml
│   │           │   ├── AddEditTaskDialog.fxml
│   │           │   ├── AddEquipmentCategoryDialog.fxml
│   │           │   ├── AddHouseDialog.fxml
│   │           │   ├── ConfigHousesDialog.fxml
│   │           │   ├── DistributeChicksDialog.fxml
│   │           │   ├── EditEggProductionDialog.fxml
│   │           │   ├── EditHouseDialog.fxml
│   │           │   ├── ImportChicksDialog.fxml
│   │           │   ├── ManageEquipmentItemsDialog.fxml
│   │           │   ├── PersonnelDetailDialog.fxml
│   │           │   ├── RecordEggCollectionDialog.fxml
│   │           │   ├── RecordMortalityDialog.fxml
│   │           │   ├── SellChickensDialog.fxml
│   │           │   ├── SellEggsDialog.fxml
│   │           │   ├── TransferChickensDialog.fxml
│   │           │   ├── UseFeedDialog.fxml
│   │           │   └── UseMedicationDialog.fxml
│   │           ├── ChickenBayView.fxml
│   │           ├── DashboardView.fxml
│   │           ├── EggsBayView.fxml
│   │           ├── LoginView.fxml
│   │           ├── MainWindow.fxml
│   │           ├── PersonnelView.fxml
│   │           ├── Sidebar.fxml
│   │           ├── SignUpView.fxml
│   │           ├── StorageView.fxml
│   │           └── TasksView.fxml
│   │
│   └── test/
│
├── .gitignore
├── LICENSE.txt
├── pom.xml
└── README.md                     
```
---

### Expandability Notes

- **For full version:** Add `supplier/`, `customer/`, `financial/`, `document/` packages
- **For PostgreSQL:** Replace `DatabaseConnection.java` with connection pooling
- **For multi-user:** Add `role/` and `permission/` packages
- **For documents:** Add `report/` and `pdf/` packages

---

## 💻 Complete Development Environment Setup

### Prerequisites Checklist

Before you begin, make sure you have:

- [ ] Git installed
- [ ] Java JDK 17+ installed
- [ ] Maven installed
- [ ] IntelliJ IDEA installed
- [ ] JavaFX SDK downloaded
- [ ] GitHub account created

---

### A. Installing Git

**For Windows:**

1. Download Git from: https://git-scm.com/download/windows
2. Run the installer with default options
3. Open Command Prompt and verify:
   ```bash
   git --version
   ```
   You should see something like: `git version 2.43.0`

**For macOS:**

```bash
# Install using Homebrew
brew install git

# Verify installation
git --version
```

**For Linux:**

```bash
# For Ubuntu/Debian
sudo apt-get update
sudo apt-get install git

# Verify installation
git --version
```

---

### B. Installing Java JDK 17+

**For Windows:**

1. Download Oracle JDK 17+ from: https://www.oracle.com/java/technologies/downloads/#java17
    - Or download OpenJDK from: https://adoptium.net/
2. Run the installer
3. Set `JAVA_HOME` environment variable:
    - Right-click "This PC" → Properties → Advanced system settings
    - Click "Environment Variables"
    - Under "System variables", click "New"
    - Variable name: `JAVA_HOME`
    - Variable value: `C:\Program Files\Java\jdk-17` (your installation path)
4. Add to PATH:
    - Edit "Path" variable
    - Add: `%JAVA_HOME%\bin`
5. Verify installation:
   ```bash
   java -version
   javac -version
   ```

**For macOS:**

```bash
# Install using Homebrew
brew install openjdk@17

# Add to PATH (add to ~/.zshrc or ~/.bash_profile)
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc

# Verify
java -version
```

**For Linux:**

```bash
# For Ubuntu/Debian
sudo apt-get update
sudo apt-get install openjdk-17-jdk

# Verify
java -version
javac -version
```

---

### C. Installing Maven

**For Windows:**

1. Download Maven from: https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Apache\maven`
3. Set `MAVEN_HOME` environment variable:
    - Variable name: `MAVEN_HOME`
    - Variable value: `C:\Program Files\Apache\maven`
4. Add to PATH: `%MAVEN_HOME%\bin`
5. Verify:
   ```bash
   mvn -version
   ```

**For macOS:**

```bash
# Install using Homebrew
brew install maven

# Verify
mvn -version
```

**For Linux:**

```bash
# For Ubuntu/Debian
sudo apt-get update
sudo apt-get install maven

# Verify
mvn -version
```

---

### D. Installing IntelliJ IDEA

1. Download Community Edition from: https://www.jetbrains.com/idea/download/
2. Run the installer
3. During installation:
    - Create Desktop Shortcut (recommended)
    - Add "bin" folder to PATH (recommended)
    - Add "Open Folder as Project" (recommended)
4. Launch IntelliJ IDEA
5. On first launch:
    - Choose theme (Light/Dark)
    - Skip featured plugins (or install as needed)
    - Click "Start using IntelliJ IDEA"

---

### E. Downloading JavaFX SDK 25

1. Go to: https://gluonhq.com/products/javafx/
2. Choose your OS:
    - Windows: `javafx-sdk-25.0.1-windows.zip`
    - macOS: `javafx-sdk-25.0.1-macos.zip`
    - Linux: `javafx-sdk-25.0.1-linux.zip`
3. Extract to a known location:
    - **Windows**: `C:\javafx-sdk-25.0.1`
    - **macOS/Linux**: `/Library/Java/javafx-sdk-25.0.1` or `~/javafx-sdk-25.0.1`
4. **Important**: Remember this path - you'll need it later!

---

### F. Creating GitHub Account

1. Go to: https://github.com/signup
2. Create your account
3. Verify your email address
4. Set up your profile:
    - Add profile picture (optional)
    - Add bio (optional)
5. Configure Git with your information:
   ```bash
   git config --global user.name "Your Name"
   git config --global user.email "your.email@example.com"
   ```

---

## 👥 Team Setup Instructions

Follow these steps to set up the project on your local machine:

### Step 1: Fork the Repository

1. Go to: https://github.com/Medfadili20Dev/Chicken_Farm_Management_System
2. Click the **"Fork"** button in the top-right corner
3. **IMPORTANT**: Uncheck "Copy the main branch only"
4. Click "Create fork"
5. You now have your own copy of the repository

---

### Step 2: Clone YOUR Fork in IntelliJ

1. Open IntelliJ IDEA
2. Click the **hamburger menu** (three lines) in the top-left
3. Go to **Git** → **Clone**
4. Paste YOUR fork URL (not the original repo):
   ```
   https://github.com/YOUR-USERNAME/Chicken_Farm_Management_System.git
   ```
5. Choose where to save the project on your computer
6. Click **Clone** and wait for it to finish

---

### Step 3: Switch to Dev Branch

1. Open the **Terminal** in IntelliJ (bottom toolbar)
2. Run these commands:
   ```bash
   git checkout dev
   git pull origin dev
   ```
3. You're now on the `dev` branch where all development happens

---

### Step 4: Configure JavaFX in IntelliJ

1. Go to **File** → **Project Structure** (or press `Ctrl+Alt+Shift+S`)
2. Click **Libraries** in the left panel
3. Click the **+** button → **Java**
4. Navigate to your JavaFX SDK `lib` folder:
    - Example: `C:\javafx-sdk-25.0.1\lib`
5. Select **all** `.jar` files in that folder
6. Click **OK**
7. Click **Apply** and **OK**

---

### Step 5: Set VM Options

1. Go to **Run** → **Edit Configurations**
2. If no configuration exists:
    - Click **+** → **Application**
    - Name: `Farm App`
    - Main class: `ma.farm.App`
3. Click **Modify options** → **Add VM options**
4. In the VM options field, paste:
   ```
   --module-path "C:\javafx-sdk-25.0.1\lib" --add-modules javafx.controls,javafx.fxml
   ```
   **⚠️ Replace the path with YOUR JavaFX location!**
5. Click **Apply** and **OK**

---

### Step 6: Reload Maven Project

1. Click the **Maven** button (M logo) in the right sidebar
2. Click the **Reload All Maven Projects** icon (circular arrows)
3. Wait for Maven to download all dependencies
4. You should see "BUILD SUCCESS" in the log

---

### Step 7: Build the Project

1. Open the **Terminal** in IntelliJ
2. Run:
   ```bash
   mvn clean install
   ```
3. Wait for the build to complete
4. You should see "BUILD SUCCESS"

---

### Step 8: Run the Application

**Method 1: Using Maven (Recommended)**

1. In the **Maven** sidebar (right side)
2. Expand: **Chicken Farm Management System** → **Plugins** → **javafx**
3. Double-click **javafx:run**
4. The application should launch! 🎉

**Method 2: Run App.java Directly**

1. Navigate to `src/main/java/ma/farm/App.java`
2. Right-click on `App.java`
3. Select **Run 'App.main()'**

---

### Step 9: Verify Setup

✅ Application window should open  
✅ You should see the login page  
✅ No errors in the console  
✅ You can enter the default credentials:
- Email: `admin@farm.ma`
- Password: `admin123`

---

### Step 10: Start Working on Your Tasks

1. Check the **"Chicken Farm MVP Tracking"** project board for your assigned tasks
2. Create a feature branch:
   ```bash
   git checkout -b feature/your-task-name
   ```
   Example: `git checkout -b feature/login-page`
3. Make your changes and commit:
   ```bash
   git add .
   git commit -m "feat: add login page functionality"
   ```
4. Push to YOUR fork:
   ```bash
   git push origin feature/your-task-name
   ```
5. Go to GitHub and create a **Pull Request** to the **original repo's `dev` branch**

---

## 🧪 Running Tests

To run the test suite:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserDAOTest

# Run with coverage report
mvn clean test jacoco:report
```

---

## 📝 Default Login Credentials

For MVP testing purposes:

- **Email:** `admin@farm.ma`
- **Password:** `admin123`

---

## 🗓️ Development Roadmap

### Phase 1: MVP (Current - 5 Days)
- ✅ Login page
- ✅ Dashboard with metrics
- ✅ Chicken Bay management
- ✅ Eggs Bay tracking
- ✅ Storage management
- ✅ Task assignment
- ✅ Personnel records

### Phase 2: Enhanced Features (Future)
- User role management
- Advanced reporting
- Data export (PDF/Excel)
- Email notifications
- Backup and restore

### Phase 3: Advanced Features (Future)
- Multi-user authentication
- Supplier management
- Customer management
- Financial tracking
- Legal compliance (CNSS/ONSSA)
- Mobile app companion

---

## 🐛 Troubleshooting

### Issue: "Module not found" error

**Solution:**
- Verify JavaFX VM options are set correctly in Run Configuration
- Check that the JavaFX path in VM options matches your installation
- Reload Maven project

---

### Issue: "Database locked" error

**Solution:**
- Close all database connections properly
- Make sure no other application is accessing `farm.db`
- Restart the application
- If persists, delete `farm.db` and let it regenerate

---

### Issue: Maven build fails

**Solution:**
```bash
# Clean and force update
mvn clean install -U

# If still fails, delete .m2 repository cache
rm -rf ~/.m2/repository
mvn clean install
```

---

### Issue: JavaFX not loading

**Solution:**
- Verify JavaFX SDK path in VM options is correct
- Check that all JavaFX `.jar` files are added as libraries
- Make sure you're using JavaFX 25 compatible with Java 17+
- Try re-downloading JavaFX SDK

---

### Issue: "Cannot find main class App"

**Solution:**
- Make sure you've built the project: `mvn clean install`
- Verify the main class is set to `ma.farm.App` in Run Configuration
- Reload Maven project
- Invalidate caches: **File** → **Invalidate Caches** → **Restart**

---

### Issue: Port already in use

**Solution:**
- No ports are used in this desktop application
- If you see this error, another application might be interfering
- Check for conflicting JavaFX processes

---

## 🤝 Contributing

### Branch Naming Conventions

- **Feature branches:** `feature/description`
    - Example: `feature/login-validation`
- **Bug fixes:** `bugfix/description`
    - Example: `bugfix/egg-count-calculation`
- **Hotfixes:** `hotfix/description`
    - Example: `hotfix/database-connection`

### Commit Message Format

```
type: brief description

Detailed explanation (optional)

- Additional details (optional)
- Related issue #123 (optional)
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code formatting
- `refactor`: Code restructuring
- `test`: Adding tests
- `chore`: Maintenance tasks

**Examples:**
```
feat: add egg collection dialog

Implemented dialog for recording daily egg collections
from House 2 and House 3 with cracked egg tracking.
```

### Pull Request Process

1. Create a feature branch from `dev`
2. Make your changes and commit
3. Push to YOUR fork
4. Create Pull Request to original repo's `dev` branch
5. Fill in the PR template:
    - Description of changes
    - Related issues
    - Testing performed
6. Request review from team members
7. Address review comments
8. Once approved, your PR will be merged

### Code Review Guidelines

**As a Reviewer:**
- Be constructive and respectful
- Focus on code quality and standards
- Check for potential bugs
- Verify tests are included

**As a Developer:**
- Respond to all comments
- Ask questions if unclear
- Update code based on feedback
- Keep PRs focused and manageable

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.

### MIT License Summary

Permission is granted to use, copy, modify, and distribute this software for any purpose, with or without fee, provided that the copyright notice and permission notice appear in all copies.

---

## 🙏 Acknowledgments

### Supervising Professors

We would like to thank our professors for their guidance and support throughout this project:

- Professor [ Youssef ES-SAADY ]
- Professor [ Abderrahmane SADIQ ]
- Professor [ AICHA DAKIR ]

### Team Recognition

Special thanks to all team members for their dedication and hard work:

- **Mohamed Yacine Elfadili** - For exceptional project leadership and coordination
- **Mohamed Hammou** - For robust database design and backend implementation
- **Hafid Anssem** - For creating an intuitive and responsive user interface
- **Mohamed Amine Haifi** - For thorough testing and comprehensive documentation
- **Ismail Ouchraa** - For solid software architecture and design patterns


---

---

## 🆔 Identity Card Generation (ID Card)

Added feature to generate identity card images (PNG) for personnel.

**How it works**

- Uses **OpenHTMLToPDF** (Apache-2.0) to render `src/main/resources/templates/identity-card.html` with placeholders `{{name}}`, `{{role}}`, and `{{barcodeDataImg}}`.
- Produces PNG by rasterizing the generated PDF using **Apache PDFBox**.
- QR codes are generated using **ZXing** and embedded as data URIs in the template. The template has been updated to a portrait layout (large QR at top, caption, then name and role) to match the requested style.
- **UI usage**: In the Personnel view, click the "➕ Generer carte" button, select a personnel, and save the PNG file.
- **Programmatic API**: `ma.farm.util.IdentityCardGenerator#generateAsPng(String name, String role, String barcodeData)` and `#saveAsPng(...)`.

**Licensing**

- This feature uses Apache-licensed libraries (OpenHTMLToPDF, PDFBox, ZXing) and is safe for commercial use.
- The project still contains an **iText (AGPL)** dependency; avoid using iText pdfHTML in production unless you accept AGPL obligations or obtain a commercial license.

---

**Built by the Farm Management Dev Team | University Java Project 2025**
