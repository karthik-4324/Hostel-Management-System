# Hostel Management System - IntelliJ IDEA Setup Guide

## Prerequisites

Before setting up the project in IntelliJ IDEA, ensure you have:

1. **Java 17 or higher** installed
2. **IntelliJ IDEA** (Community or Ultimate Edition)
3. **MongoDB** installed and running locally
4. **Maven** (usually comes with IntelliJ)

## Step-by-Step Setup in IntelliJ IDEA

### 1. Import the Project

#### Option A: Import Existing Project
1. Open IntelliJ IDEA
2. Click **"Open"** or **"Import Project"**
3. Navigate to your project folder and select the root directory (where `pom.xml` is located)
4. Click **"Open"**
5. IntelliJ will automatically detect it as a Maven project
6. Click **"Import Maven Project"** if prompted

#### Option B: Create New Project from Existing Sources
1. Open IntelliJ IDEA
2. Click **"File" → "New" → "Project from Existing Sources"**
3. Select your project directory
4. Choose **"Import project from external model"**
5. Select **"Maven"**
6. Click **"Next"** and follow the wizard

### 2. Configure Project Settings

#### Set Project SDK
1. Go to **"File" → "Project Structure"** (Ctrl+Alt+Shift+S)
2. Under **"Project Settings" → "Project"**
3. Set **"Project SDK"** to Java 17 or higher
4. Set **"Project language level"** to 17 or higher
5. Click **"Apply"** and **"OK"**

#### Configure Maven
1. Go to **"File" → "Settings"** (Ctrl+Alt+S)
2. Navigate to **"Build, Execution, Deployment" → "Build Tools" → "Maven"**
3. Ensure **"Maven home path"** is set correctly
4. Check **"Import Maven projects automatically"**
5. Click **"Apply"** and **"OK"**

### 3. Install Required Plugins

1. Go to **"File" → "Settings" → "Plugins"**
2. Install these plugins if not already installed:
   - **Spring Boot** (for Spring Boot support)
   - **MongoDB Plugin** (for MongoDB integration)
   - **Lombok** (if using Lombok annotations)

### 4. Configure Database Connection

#### Update application.properties
1. Open `src/main/resources/application.properties`
2. Update the MongoDB connection string:

```properties
# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/hostel_management

# Or if you have authentication:
# spring.data.mongodb.uri=mongodb://username:password@localhost:27017/hostel_management
```

#### Set up MongoDB
1. Make sure MongoDB is running on your system
2. You can start MongoDB using:
   ```bash
   # On Windows
   net start MongoDB
   
   # On macOS/Linux
   sudo systemctl start mongod
   # or
   brew services start mongodb-community
   ```

### 5. Configure Environment Variables

#### Option A: Using IntelliJ Run Configuration
1. Go to **"Run" → "Edit Configurations"**
2. Select your Spring Boot application
3. In **"Environment variables"**, add:
   ```
   JWT_SECRET=mySecretKey123456789012345678901234567890
   JWT_EXPIRATION=86400000
   EMAIL_HOST=smtp.gmail.com
   EMAIL_PORT=587
   EMAIL_USER=your-email@gmail.com
   EMAIL_PASS=your-app-password
   ```

#### Option B: Using application-dev.properties
1. Create `src/main/resources/application-dev.properties`
2. Add your development-specific configurations
3. Set active profile in IntelliJ run configuration: `spring.profiles.active=dev`

### 6. Build and Run the Project

#### Build the Project
1. Open the **Maven** tool window (View → Tool Windows → Maven)
2. Expand your project
3. Run **"Lifecycle" → "clean"** then **"compile"**
4. Or use terminal: `mvn clean compile`

#### Run the Application
1. Locate `HostelManagementApplication.java` in `src/main/java/com/hostel/`
2. Right-click and select **"Run 'HostelManagementApplication'"**
3. Or click the green play button next to the main method
4. The application will start on `http://localhost:8080`

### 7. Verify Setup

#### Test the Health Endpoint
1. Open your browser or Postman
2. Navigate to: `http://localhost:8080/api/health`
3. You should see:
   ```json
   {
     "status": "OK",
     "message": "Hostel Management System API is running",
     "timestamp": "2024-01-01T12:00:00"
   }
   ```

#### Test Authentication
1. Use Postman to test registration:
   ```
   POST http://localhost:8080/api/auth/register
   Content-Type: application/json
   
   {
     "firstName": "John",
     "lastName": "Doe",
     "email": "john.doe@example.com",
     "password": "password123",
     "role": "STUDENT",
     "phone": "1234567890",
     "dateOfBirth": "2000-01-01",
     "gender": "MALE",
     "emergencyContact": {
       "name": "Jane Doe",
       "phone": "0987654321",
       "relation": "Mother"
     }
   }
   ```

### 8. IntelliJ-Specific Tips

#### Enable Auto-Import
1. Go to **"File" → "Settings" → "Editor" → "General" → "Auto Import"**
2. Check **"Add unambiguous imports on the fly"**
3. Check **"Optimize imports on the fly"**

#### Configure Code Style
1. Go to **"File" → "Settings" → "Editor" → "Code Style" → "Java"**
2. Set up your preferred formatting rules
3. Enable **"Reformat code"** and **"Optimize imports"** in commit dialog

#### Use Spring Boot Dashboard
1. Go to **"View" → "Tool Windows" → "Services"**
2. You'll see your Spring Boot application listed
3. Use this to start/stop/monitor your application

#### Database Tool Window
1. Go to **"View" → "Tool Windows" → "Database"**
2. Add a new MongoDB data source
3. Configure connection to your local MongoDB instance

### 9. Debugging

#### Set Breakpoints
1. Click in the left gutter next to line numbers to set breakpoints
2. Run the application in debug mode (Debug button or Shift+F9)
3. Use the debugger to step through code

#### View Logs
1. Check the **"Run"** tool window for application logs
2. Configure logging levels in `application.properties`:
   ```properties
   logging.level.com.hostel=DEBUG
   logging.level.org.springframework.security=DEBUG
   ```

### 10. Common Issues and Solutions

#### Issue: "Cannot resolve symbol" errors
**Solution:** 
- Refresh Maven project: Maven tool window → Reload button
- Invalidate caches: File → Invalidate Caches and Restart

#### Issue: MongoDB connection errors
**Solution:**
- Ensure MongoDB is running
- Check connection string in application.properties
- Verify MongoDB port (default: 27017)

#### Issue: Port already in use
**Solution:**
- Change port in application.properties: `server.port=8081`
- Or kill the process using the port

#### Issue: JWT secret too short
**Solution:**
- Ensure JWT_SECRET is at least 32 characters long
- Update in environment variables or application.properties

### 11. Development Workflow

1. **Make Changes** to your code
2. **Build** the project (Ctrl+F9)
3. **Run Tests** if available
4. **Start/Restart** the application
5. **Test** endpoints using Postman or browser
6. **Debug** if needed using IntelliJ debugger

### 12. Additional IntelliJ Features

#### HTTP Client
1. Create `.http` files for testing endpoints
2. Use IntelliJ's built-in HTTP client instead of Postman

#### Spring Boot Run Dashboard
1. Provides easy management of Spring Boot applications
2. Shows application status, logs, and metrics

#### Database Integration
1. Connect to MongoDB directly from IntelliJ
2. Browse collections and documents
3. Run queries directly from IDE

## Project Structure in IntelliJ

```
hostel-management/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/hostel/
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       ├── security/
│   │   │       ├── service/
│   │   │       └── HostelManagementApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/
├── target/
├── pom.xml
└── README.md
```

## Next Steps

1. Set up your MongoDB connection
2. Configure environment variables
3. Run the application
4. Test the endpoints
5. Start developing additional features

Your Spring Boot Hostel Management System is now ready for development in IntelliJ IDEA!