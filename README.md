# Barber Appointment System ✂️

## 📌 About The Project
This is a console-based Barber Appointment System built with Java. While it works as a fully functional scheduling application, my main goal with this project was to practice and apply software engineering principles—specifically **Clean Architecture** and **SOLID** design. 

The project is built entirely in core Java without using any external frameworks, which allowed me to understand how layered architectures and object-oriented designs work under the hood.

## 🏗️ Technical Highlights
* **Clean Architecture:** The codebase is divided into clear layers (Core Domain, Use Cases, Presentation, and Infrastructure). Business logic is completely isolated from the UI and database operations.
* **SOLID Principles:** Focused heavily on keeping the system decoupled. For example, I used the **Dependency Inversion Principle (DIP)** to connect the core application logic to the CSV data logic using interfaces.
* **Design Patterns:** Implemented several design patterns to solve common architectural problems:
  * *Facade:* To make the system easier to interact with from the outside.
  * *Factory & Builder:* To handle the creation of complex objects safely.
  * *Strategy:* To manage different behaviors dynamically.
  * *Singleton:* To manage the active user session.
* **Data Persistence:** Custom file handling to read, write, and update `.csv` files as a database, without relying on ORMs.
* **Console UI:** A dynamic terminal interface that includes a matrix-style schedule view and input validation.

## 📄 Documentation
For a deep dive into the system's design, please check the **[Software_Architecture_Document.pdf](./Software_Architecture_Document.pdf)** included in the repository. 
This document includes:
* UML Diagrams (Class, Use Case, Sequence, Activity, Statechart)
* Data Flow Diagrams (DFD Level 0 & 1)
* Detailed explanations of the SOLID principles and design patterns used
* User Manual with console screenshots

## 🚀 How to Run
1. Make sure you have Java installed on your system.
2. Clone this repository to your local machine.
3. Ensure that `customers.csv`, `barbers.csv`, and `appointments.csv` are in the project's root folder.
4. Run the main application class to start the console interface.
