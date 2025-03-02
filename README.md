# Chat Application
A simple chat application built with Java and Spring Boot

This is a real-time chat application where users can send messages to each other.
Application provides a function to set up own account with email verification.

# Requirements
- Java 17+
- Spring boot 2.5 +
- Maven

## Installation and Set Up

1. Clone the repository
git clone https://github.com/OskarCh29/group-chat.git

2. Navigate to the project directory for application.yaml configuration

3. In configurate file (application.yaml) provide datasource informations
     * url, username, password (MySQL information default provided)
     * Set up the run configuration with enviromental variables for mail verification (For new accounts only)
     * username,password for mail service could be provided in file or as variables
   

4. Navigate to the project directory (pom.xml file location) via terminal and start the app
   ```bash
   cd group-chat
   mvn spring-boot:run
   
       
## How to use?
After starting application open the app in your browser (http://localhost:8080).
Log in with your credentials (username and password -> Provided by database or create new with email)
Start chatting with other users in the chat window in real time. For visual improvments, source files .html should be changed in the static directory

## Testing
To run tests use : mvn test
* Be aware that testing requires Docker Container for integration tests with MySQL
* Basic configuration settled in testClasses (for specific preferences need to be updated with version,userdata)

# Functions
- User authentication with generated tokens
- Email verification for new users
- Forgot password function
- Message history stored in MySQL database
- Real-time messaging with WebSocket

# Techonologies used for project
- Java
- Spring boot
- MySQL
- WebSocket
- Maven
   
