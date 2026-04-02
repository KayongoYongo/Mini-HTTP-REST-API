# 🔗 Mini HTTP REST API — Java (No Frameworks)

A fully functional RESTful API built using **pure Java** with no external frameworks. This project demonstrates a deep understanding of how HTTP works under the hood by manually handling routing, request parsing, JSON serialization, and response formatting — everything Spring Boot does automatically.

---

## 📌 Why This Project?

Most Java developers reach for Spring Boot without understanding what happens beneath it. This project was built to demonstrate:

- How HTTP servers work at the socket level
- Manual request routing and path variable extraction
- Thread-safe data handling under concurrent requests
- Clean layered architecture without framework scaffolding

---

## 🛠️ Tech Stack

| Tool | Purpose |
|---|---|
| Java 11+ | Core language |
| `com.sun.net.httpserver` | Built-in JDK HTTP server |
| Gson | JSON parsing and serialization |
| Maven | Build management |
| JUnit 5 | Unit testing |

---

## 📁 Project Structure

```
mini-http-REST-api/
├── src/
│   ├── main/java/
│   │   ├── Server.java               # Entry point — starts the server
│   │   ├── handler/
│   │   │   └── TaskHandler.java      # Routes requests, sends responses
│   │   ├── service/
│   │   │   └── TaskService.java      # Business logic and validation
│   │   ├── repository/
│   │   │   └── TaskRepository.java   # In-memory data storage
│   │   └── model/
│   │       └── Task.java             # Task model and Status enum
│   └── test/java/
│       └── TaskServiceTest.java      # Unit tests for service layer
├── pom.xml
└── README.md
```

### Layer Responsibilities

```
HTTP Request
     │
     ▼
TaskHandler      → Parses request, routes to correct method, sends JSON response
     │
     ▼
TaskService      → Validates input, applies business rules, throws errors
     │
     ▼
TaskRepository   → Reads/writes to ConcurrentHashMap (in-memory store)
     │
     ▼
Task             → Plain Java object (POJO) representing a task
```

---

## 🚀 Getting Started

### Prerequisites
- Java 11 or higher
- Maven 3.6+

### Installation

```bash
# Clone the repository
git clone https://github.com/your-username/mini-http-api.git
cd mini-http-api

# Build the project
mvn clean install

# Run the server
mvn exec:java -Dexec.mainClass="Server"
```

The server will start on:
```
http://localhost:8080
```

---

## 🔌 API Endpoints

### Base URL: `http://localhost:8080`

---

### ✅ Get All Tasks
```
GET /tasks
```

**Response `200 OK`:**
```json
[
  {
    "id": 1,
    "title": "Buy milk",
    "description": "From the supermarket",
    "status": "PENDING",
    "createdAt": "2024-01-15T10:30:00"
  }
]
```

---

### ✅ Get a Single Task
```
GET /tasks/{id}
```

**Response `200 OK`:**
```json
{
  "id": 1,
  "title": "Buy milk",
  "description": "From the supermarket",
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00"
}
```

**Response `404 Not Found`:**
```json
{
  "error": "Task with id 99 not found"
}
```

---

### ✅ Create a Task
```
POST /tasks
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "Buy milk",
  "description": "From the supermarket"
}
```

**Response `201 Created`:**
```json
{
  "id": 1,
  "title": "Buy milk",
  "description": "From the supermarket",
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00"
}
```

**Response `400 Bad Request`** (missing title):
```json
{
  "error": "Title is required"
}
```

---

### ✅ Update a Task
```
PUT /tasks/{id}
Content-Type: application/json
```

**Request Body** (all fields optional — only send what you want to change):
```json
{
  "title": "Buy oat milk",
  "status": "IN_PROGRESS"
}
```

Valid status values: `PENDING`, `IN_PROGRESS`, `DONE`

**Response `200 OK`:**
```json
{
  "id": 1,
  "title": "Buy oat milk",
  "description": "From the supermarket",
  "status": "IN_PROGRESS",
  "createdAt": "2024-01-15T10:30:00"
}
```

---

### ✅ Delete a Task
```
DELETE /tasks/{id}
```

**Response `204 No Content`** — task deleted successfully

**Response `404 Not Found`:**
```json
{
  "error": "Task with id 1 not found"
}
```

---

## 📊 HTTP Status Code Reference

| Code | Meaning | When it's returned |
|---|---|---|
| `200` | OK | Successful GET or PUT |
| `201` | Created | Successful POST |
| `204` | No Content | Successful DELETE |
| `400` | Bad Request | Missing or invalid fields |
| `404` | Not Found | Task ID does not exist |
| `405` | Method Not Allowed | Unsupported HTTP method on a route |
| `500` | Internal Server Error | Unexpected server-side error |

---

## ⚙️ How It Works Internally

### Routing
There is no framework handling routing. `TaskHandler.java` manually matches the request path using string comparison and regex:

```java
if (path.equals("/tasks")) { ... }
else if (path.matches("/tasks/\\d+")) { ... }
```

### Thread Safety
The `HttpServer` processes requests on multiple threads simultaneously. To prevent race conditions:
- **`ConcurrentHashMap`** is used instead of a regular `HashMap` for the data store
- **`AtomicInteger`** is used for ID generation instead of a plain `int` counter

### JSON Handling
Gson serializes Java objects to JSON responses and deserializes incoming request bodies — the only external dependency in the project.

---

## 🧪 Running Tests

```bash
mvn test
```

Tests cover:
- Creating a task successfully
- Creating a task with a missing title → expects `IllegalArgumentException`
- Getting a task that does not exist → expects `IllegalArgumentException`
- Deleting a task that does not exist → expects `IllegalArgumentException`
- Updating a task successfully

---

## 💡 Key Design Decisions

**Why no Spring Boot?**
Spring Boot is excellent for production, but it abstracts away HTTP fundamentals. This project was intentionally built without it to demonstrate understanding of what happens at the lower level.

**Why in-memory storage?**
The focus of this project is the HTTP and architecture layer. A `ConcurrentHashMap` keeps the data layer simple so attention stays on routing, request handling, and thread safety. A database (PostgreSQL + JDBC) could be swapped in by only changing `TaskRepository.java`.

**Why constructor injection?**
Each class receives its dependencies through the constructor rather than creating them internally. This makes unit testing straightforward — you can pass a mock repository into `TaskService` without any framework.

---

## 🔮 Potential Extensions

- [ ] Persist data to a PostgreSQL database via JDBC
- [ ] Add query filtering: `GET /tasks?status=PENDING`
- [ ] Add pagination: `GET /tasks?page=1&size=5`
- [ ] Add request logging (method, path, response time)
- [ ] Add API key authentication via request header

---

## 👤 Author

**Your Name**
[GitHub](https://github.com/your-username) • [LinkedIn](https://linkedin.com/in/your-profile)
