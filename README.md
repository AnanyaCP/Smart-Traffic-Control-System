# Smart Traffic Control System

A Java-based traffic simulation application that demonstrates multithreading, emergency vehicle prioritization, and event logging using MySQL and file handling.

## Overview

The Smart Traffic Control System simulates traffic signal operations and vehicle movement through a graphical user interface. It prioritizes emergency vehicles using a serve-and-resume mechanism, allowing normal traffic operations to resume after emergency handling.

The application combines Java multithreading, Swing-based animation, and database connectivity to demonstrate concurrent processing and event management.

## Features

* **Multithreaded Simulation:** Manages traffic operations and vehicle animation using multiple threads.
* **Emergency Vehicle Prioritization:** Gives priority to emergency vehicles during traffic simulation.
* **Serve-and-Resume Mechanism:** Handles emergency traffic before resuming normal traffic operations.
* **Graphical User Interface:** Displays traffic signals and vehicle movement using Java Swing.
* **Database Logging:** Stores system events in MySQL using JDBC.
* **File Handling:** Maintains event logs in files for recordkeeping.

## Technologies Used

| Technology          | Purpose                                           |
| ------------------- | ------------------------------------------------- |
| Java                | Application logic and object-oriented programming |
| Java Multithreading | Concurrent execution of simulation tasks          |
| Java Swing          | Graphical user interface and animation            |
| MySQL               | Persistent event storage                          |
| JDBC                | Database connectivity                             |
| File Handling       | Event logging                                     |

## Project Structure

```text
src/
└── Project/
    ├── TrafficApp.java
    ├── TrafficEngine.java
    ├── TrafficPersistence.java
    └── Vehicle.java
```

### Component Description

* **TrafficApp.java:** Entry point of the application and graphical interface initialization.
* **TrafficEngine.java:** Manages traffic signal logic and simulation operations.
* **TrafficPersistence.java:** Handles event storage using MySQL and file handling.
* **Vehicle.java:** Represents vehicles participating in the simulation.

## System Workflow

1. The application starts through the graphical interface.
2. The traffic engine manages signal operations and traffic flow.
3. Separate threads handle simulation tasks and vehicle animation.
4. Emergency vehicles receive priority through the serve-and-resume mechanism.
5. System events are recorded in the database and log files.

## Prerequisites

* Java Development Kit (JDK)
* MySQL Server
* MySQL Connector/J
* A Java development environment, such as IntelliJ IDEA or Eclipse

## Installation and Setup

### 1. Clone the Repository

```bash
git clone https://github.com/AnanyaCP/Smart-Traffic-Control-System.git
```

Replace `https://github.com/AnanyaCP/Smart-Traffic-Control-System.git` with the URL of this repository.

### 2. Create the Database

Execute the following SQL statement in MySQL:

```sql
CREATE DATABASE traffic_db;
```

Create any required tables according to the database queries used in the application.

### 3. Configure Database Connectivity

Update the JDBC connection settings in the application to match your MySQL configuration.

Ensure that the MySQL Connector/J driver is available on the project's classpath.

### 4. Run the Application

Open the project in your Java IDE, configure the required dependencies, and run `TrafficApp.java`.

## Learning Outcomes

This project demonstrates practical implementation of:

* Object-oriented programming in Java
* Multithreading and concurrent execution
* GUI development using Java Swing
* Database connectivity using JDBC
* MySQL data persistence
* File handling and event logging

## Future Enhancements

* Adaptive traffic signal timing based on traffic conditions
* Traffic density monitoring
* Enhanced emergency vehicle prioritization
* Traffic statistics and reporting
* Additional traffic simulation scenarios

## Author

**Ananya C P**

B.Tech, Information Science and Technology
