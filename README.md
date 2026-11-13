# Hostel Management System

A full-stack web application for running a college hostel: room allocation, fee tracking, complaints, leave requests, announcements, and role-based access for **admins, wardens, and students**.

Built with **React**, **Spring Boot**, and **MongoDB**.

<!-- Add a screenshot here, for example:
![Student dashboard](docs/screenshots/student-dashboard.png)
-->

## Features

### Roles at a glance

| Feature | Admin | Warden | Student |
|---|---|---|---|
| Students (create, edit, delete) | Full control | View only | No access |
| Rooms and room allocation | Full control | View only | No access |
| Fees | View all, record payments, delete | View all | View own |
| Complaints | Change status, delete any | Change status | Submit, edit and delete own (while pending) |
| Leaves | Approve, reject, delete | Approve, reject | Apply, cancel own (while pending) |
| Announcements | Create, edit, delete | Read | Read |
| Staff accounts (wardens, admins) | Create, remove | No access | No access |

### Highlights

- **Authentication and security**: JWT login, BCrypt-hashed passwords, role checks enforced on the **server** (not just hidden in the UI).
- **Forgot password**: emailed reset link with a single-use token that expires in 30 minutes. Only a hash of the token is stored.
- **Change password** from the profile page.
- **Room management**: capacity, bed numbers, automatic occupancy and status. A room that still has students cannot be deleted.
- **Room allocation**: the admin assigns a student to a room and bed. Double allocation, full rooms, and taken beds are rejected.
- **Automatic fees**: allocating a room creates the student's room-rent fee from the room's monthly rent.
- **Fee payments**: partial and full payments with history, and overpayment is blocked.
- **Leaving a room**: a student (or the admin) can vacate a room only after all pending fees are paid.
- **Complaints**: category, priority, status workflow (pending, in progress, resolved, closed, rejected).
- **Leaves**: date validation, approve or reject (a reason is required to reject).
- **Announcements**: target audience (all, students, wardens, admins), pinning, expiry dates, likes.
- **Staff management**: the admin creates and removes wardens and admins, and cannot delete themselves or the last admin.
- **Notification bell**: shows new announcements, complaint and leave updates, and pending fees, depending on the role.
- **Role-based dashboards** and a responsive layout with a slide-in menu on small screens.
- **Data cleanup**: deleting a student also frees their bed and removes their fees.

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React (Vite), React Router, Tailwind CSS, Axios, lucide-react, react-hot-toast, Headless UI |
| Backend | Java 17, Spring Boot 3.2, Spring Security, Spring Data MongoDB, Bean Validation, Spring Mail |
| Auth | JWT (jjwt), BCrypt |
| Database | MongoDB |

## Project structure

```
Hostel-Management-System/
├── frontend/                  # React app
│   └── src/
│       ├── components/        # Sidebar, Header, NotificationBell, Logo, ...
│       ├── contexts/          # AuthContext
│       ├── pages/             # Dashboard, Students, Rooms, Fees, Complaints,
│       │                      # Leaves, Announcements, Staff, Profile, Login, ...
│       └── services/          # api.js (Axios setup and API calls)
├── src/main/java/com/hostel/
│   ├── config/                # SecurityConfig, AdminSeeder
│   ├── controller/            # REST controllers (role checks live here)
│   ├── model/                 # MongoDB documents
│   ├── repository/            # Spring Data repositories
│   ├── security/              # JWT filter and utilities
│   └── service/               # Business logic
├── src/main/resources/        # application.properties
└── pom.xml
```

## Getting started

### Prerequisites

- Java 17
- Maven
- Node.js 18 or newer
- MongoDB running locally (default `mongodb://localhost:27017`)

### 1. Clone

```bash
git clone https://github.com/YOUR-USERNAME/Hostel-Management-System.git
cd Hostel-Management-System
```

### 2. Add your local settings

Secrets are **not** committed. Create `src/main/resources/application-local.properties`:

```properties
# Use a long random value, for example 48 random bytes encoded as Base64
jwt.secret=PASTE_A_LONG_RANDOM_SECRET_HERE

# Email for password reset (Gmail with an app password)
spring.mail.username=your-gmail-address@gmail.com
spring.mail.password=your-gmail-app-password

# Optional: creates the first admin when no admin exists
app.admin.email=admin@example.com
app.admin.password=choose-a-strong-password
```

If email settings are left out, the password reset link is printed in the backend console instead, which is handy for development.

### 3. Run the backend

```bash
mvn spring-boot:run
```

The API runs on `http://localhost:5000` with the context path `/api`.

### 4. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:3000`.

### 5. First login

- If you set `app.admin.email` and `app.admin.password`, sign in with those. The admin can then create wardens and other admins from the **Staff** page.
- Students register themselves on the sign-up page. Public sign-up always creates a **student** account.

## API overview

The server context path is `/api`, so full URLs start with `http://localhost:5000/api/api/...`.

| Area | Base path |
|---|---|
| Authentication | `/api/auth` (login, register, profile, forgot-password, reset-password, change-password) |
| Students and staff (admin) | `/api/admin` |
| Warden views | `/api/warden` |
| Rooms | `/api/rooms` |
| Fees | `/api/fees` |
| Complaints | `/api/complaints` |
| Leaves | `/api/leaves` |
| Announcements | `/api/announcements` |
| Dashboards | `/api/dashboard` |

## Business rules

- A student has at most one room and one bed in it. Beds cannot be double-booked.
- Allocating a room creates a fee for that student. The amount is the room's monthly rent, due in 30 days.
- A student can leave a room only when all pending fees are paid.
- A room that has students cannot be deleted.
- Deleting a student removes them from their room and deletes their fees.
- Students can edit or delete a complaint, or cancel a leave, only while it is still pending.
- Public registration cannot create admins or wardens.

## Security notes

- Passwords are hashed with BCrypt. Password reset tokens are stored only as SHA-256 hashes.
- The forgot-password response is the same whether or not an email exists.
- Secrets live in `application-local.properties`, which is listed in `.gitignore`.

## Known limitations and roadmap

- [ ] Monthly fee generation (currently the first fee is created on room allocation)
- [ ] Image upload for complaints
- [ ] Tighten `SecurityConfig` so only login, register, and password reset are public (role checks currently live in the controllers)
- [ ] Revoke existing tokens when a password changes
- [ ] Automated tests
- [ ] Deployment (Vercel for the frontend, a cloud host for the API)

## My contributions

<!-- Edit this section so it matches exactly what you built. -->
This was a group project. I worked on:

- Role-based authorization across the backend and the matching UI restrictions
- Room allocation and vacating, including the checks that stop double allocation
- Automatic fee creation, payment recording, and the "pay before leaving the room" rule
- Complaints, leaves, and announcements workflows
- Staff management, forgot-password and change-password flows
- The UI redesign: theme, sidebar, notification bell, and responsive layout

## Team

- Your name: [GitHub profile link]
- Teammate names: add them here

## Screenshots

<!-- Put images in docs/screenshots/ and link them here:
![Login](docs/screenshots/login.png)
![Admin dashboard](docs/screenshots/admin-dashboard.png)
![Rooms](docs/screenshots/rooms.png)
![Fees](docs/screenshots/fees.png)
![Complaints](docs/screenshots/complaints.png)
-->
