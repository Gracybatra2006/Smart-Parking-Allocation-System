# Smart Parking Allocation System 🚗

A Java-based parking management application that intelligently allocates parking spaces according to vehicle availability time and walking distance from the entrance.

The system dynamically generates a parking layout based on user-defined plot dimensions and manages vehicle allocation, slot availability, and parking records using Java Swing and MySQL.

## 📌 Project Overview

The Smart Parking Allocation System aims to make parking management more organized and efficient. Instead of assigning parking spaces randomly or simply selecting the nearest available slot, the system considers how much time a driver has available and the walking distance from the parking slot to the entrance.

Vehicles with more available time can be assigned farther parking spaces, while vehicles with less available time are prioritized for closer spaces.

The application also generates a parking layout according to the actual dimensions of the available plot, considering parking slots, driving aisles, entrance lanes, turning areas, and pedestrian pathways.

## ✨ Features

- **Intelligent Parking Allocation:** Assigns parking slots based on vehicle availability time and walking distance.
- **Dynamic Parking Layout:** Generates parking layouts according to user-defined plot length and width.
- **Walking Time Calculation:** Considers the walking distance between the allocated slot and the entrance.
- **Vehicle Management:** Accepts and validates vehicle registration numbers.
- **Duplicate Vehicle Prevention:** Prevents the same vehicle from receiving multiple active parking slots.
- **Slot Availability Tracking:** Displays occupied and available parking spaces.
- **Visual Parking Map:** Provides a graphical representation of the parking layout and slot occupancy.
- **Slot Release:** Allows users to release an occupied parking space.
- **Database Integration:** Stores parking slots, vehicle records, and allocation details in MySQL.

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Core application development |
| Java Swing | Graphical User Interface (GUI) |
| MySQL | Database management |
| JDBC | Java–MySQL connectivity |
| XAMPP | Local MySQL server |
| VS Code | Development environment |

## 📂 Project Structure

```text
Smart-Parking-Allocation-System/
│
├── Main.java
├── DBConnection.java
├── ParkingDatabase.java
├── ParkingLayout.java
├── ParkingMapPanel.java
├── TestConnection.java
│
├── database/
│   └── smart_parking.sql
│
├── lib/
│   └── mysql-connector-j-26.10.0.jar
│
├── screenshots/
│   ├── application-gui.png
│   └── parking-layout.png
│
├── .gitignore
└── README.md
```

## ⚙️ Requirements

Before running the application, make sure you have:

- Java Development Kit (JDK) installed.
- Visual Studio Code or another Java IDE.
- XAMPP installed.
- MySQL Connector/J included in the project.
- Git (optional, for cloning the repository).

## 🚀 Installation and Execution

### Step 1: Clone the Repository

Open a terminal and execute:

```bash
git clone YOUR_GITHUB_REPOSITORY_URL
```

Navigate to the project directory:

```bash
cd Smart-Parking-Allocation-System
```

### Step 2: Start MySQL Using XAMPP

1. Open the XAMPP Control Panel.
2. Start the **MySQL** module.
3. Open your browser and visit:

```text
http://localhost/phpmyadmin
```

### Step 3: Import the Database

1. Open phpMyAdmin.
2. Create a database named `smart_parking`, if it does not already exist.
3. Select the `smart_parking` database.
4. Click the **Import** tab.
5. Choose the `database/smart_parking.sql` file.
6. Click **Import** or **Go** to execute the SQL file.

### Step 4: Configure Database Connection

Open `DBConnection.java` and verify the database connection details.

The default local XAMPP configuration is:

```java
private static final String URL =
        "jdbc:mysql://localhost:3306/smart_parking";

private static final String USER = "root";
private static final String PASSWORD = "";
```

If your MySQL username, password, or port is different, update these values accordingly.

### Step 5: Compile the Java Files

Open the project folder in VS Code and run:

```bash
javac -cp ".;lib/*" DBConnection.java ParkingLayout.java ParkingDatabase.java ParkingMapPanel.java Main.java
```

### Step 6: Run the Application

Execute:

```bash
java -cp ".;lib/*" Main
```

The Smart Parking Allocation System GUI should now open.

## 🖥️ Application Screenshots

### Application GUI

<!-- Add your application GUI screenshot here -->

![Smart Parking Application GUI](screenshots/application-gui.png)

### Dynamically Generated Parking Layout

<!-- Add your parking layout screenshot here -->

![Smart Parking Layout](screenshots/parking-layout.png)

## 🎯 Project Objective

The main objective of this project is to develop a practical parking management solution that improves parking-space utilization through time-based allocation, walking-distance consideration, and dynamic layout generation.

It demonstrates how Java programming, database management, and graphical interfaces can be combined to solve a real-world parking management problem.

## 👩‍💻 Contributors

Developed as an academic project.

---

**If you find this project useful, consider giving the repository a ⭐ on GitHub!**