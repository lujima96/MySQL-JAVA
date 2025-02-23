

# Projects App

A menu-driven CRUD (Create, Read, Update, Delete) application built in Java that connects to a MySQL database to manage project records. This application allows users to create projects along with their associated materials, steps, and categories, and to perform various operations using a simple command-line interface.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technologies](#technologies)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Database Setup](#database-setup)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Error Handling](#error-handling)
- [Extending the Application](#extending-the-application)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

The **Projects App** is a Java-based console application that demonstrates how to perform CRUD operations on project data stored in a MySQL database. Users interact with the application via a menu system that lets them add new projects, list all projects, update existing projects, delete projects, and view project details including associated materials, steps, and categories.

---

## Features

- **Add a Project:**  
  Input project details such as name, estimated hours, actual hours, difficulty (scale of 1-5), notes, and additional entities like materials, steps, and categories.

- **List All Projects:**  
  Retrieve and display a list of all projects currently stored in the database.

- **Update a Project:**  
  Modify existing project details by selecting a project and updating its fields interactively.

- **Delete a Project:**  
  Remove a project from the database after confirmation.

- **Select a Project:**  
  Display detailed information for a selected project, including its associated materials.

---

## Technologies

- **Java** – Core programming language (JDK 8 or higher recommended)
- **MySQL** – Database management system
- **JDBC** – Java Database Connectivity for interacting with the MySQL database

---

## Prerequisites

- **Java Development Kit (JDK) 8 or higher**  
- **MySQL Database** installed and running  
- Basic knowledge of SQL for setting up the database schema

---

## Installation

1. **Clone or Download the Repository:**

   ```bash
   git clone https://github.com/yourusername/ProjectsApp.git
   ```

2. **Open the Project:**  
   Use your favorite IDE (such as IntelliJ IDEA, Eclipse, or VS Code) to open the project.

3. **Compile the Code:**  
   If you prefer the command line, navigate to the project's root directory and compile the source files:

   ```bash
   javac -d bin src/projects/*.java src/projects/entity/*.java src/projects/exception/*.java src/projects/service/*.java
   ```

---

## Database Setup

1. **Create a MySQL Database:**  
   Create a new database (for example, `projectsdb`).

2. **Configure Database Connection:**  
   Update the database connection settings in your project (typically in a configuration file or directly in the `ProjectService` class) to reflect your MySQL username, password, and database URL.

3. **Create Tables:**  
   Run the provided SQL scripts or manually create the necessary tables (`project`, `material`, `step`, `category`, etc.) based on your application’s schema.

---

## Usage

1. **Run the Application:**

   - **Via IDE:**  
     Run the `main()` method in the `projects.ProjectsApp` class.

   - **Via Command Line:**

     ```bash
     java -cp bin projects.ProjectsApp
     ```

2. **Interact with the Menu:**  
   Follow the on-screen prompts to add, list, update, delete, or select a project. The menu will display options such as:
   - 1) Add a project
   - 2) List all projects
   - 3) Update a project
   - 4) Delete a project
   - 5) Select a project
   - 0) Exit

3. **Input Data:**  
   The application will prompt you for various details. For example, when adding a project, you’ll be asked to input the project name, estimated hours, actual hours, difficulty, notes, and associated materials, steps, and categories.

---

## Project Structure

```
ProjectsApp/
├─ README.md                   // This README file
├─ src/
│   └─ projects/
│       ├─ ProjectsApp.java    // Main menu-driven application
│       ├─ entity/
│       │   ├─ Category.java
│       │   ├─ Material.java
│       │   ├─ Project.java
│       │   └─ Step.java
│       ├─ exception/
│       │   └─ DbException.java
│       └─ service/
│           └─ ProjectService.java  // Service layer for CRUD operations
└─ bin/                        // Compiled class files (if using javac)
```

---

## Error Handling

- The application validates user input (e.g., ensuring numeric values for hours and difficulty) and provides feedback if the input is invalid.
- Custom exceptions (e.g., `DbException`) are used to handle errors related to database operations.
- Informative messages are displayed to help the user correct any mistakes.

---

## Extending the Application

- **Additional Features:**  
  Consider adding features such as advanced project search, more detailed reporting, or even a graphical user interface (GUI).

- **Modular Code:**  
  The code is structured using a service layer, making it easier to add new functionalities or integrate with other systems.

- **Database Enhancements:**  
  You can further normalize the database schema or add new tables to support more complex relationships.

---

## Contributing

Contributions are welcome! If you have ideas for improvements or new features, please open an issue or submit a pull request.

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

Feel free to update this README with any additional information relevant to your project. Enjoy managing your projects with this handy CRUD application!

Here is a link to the project youtube video! -- https://www.youtube.com/watch?v=rkRdQve3Aj0
