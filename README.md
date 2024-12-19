# reitit-app

A Clojure web application using reitit for routing, featuring task management with feature flags.

## Features

- Task management (create, list, delete)
- Feature flag system for toggling functionality
- Interactive UI with Alpine.js
- API documentation with Swagger UI
- Optional task fields controlled by feature flags:
  - Task priorities
  - Task categories
  - Due dates

## Prerequisites

Make sure you have the following installed:

- [Clojure](https://clojure.org/guides/getting_started)
- [Leiningen](https://leiningen.org/)

## Installation

Clone the repository:

```bash
git clone [https://github.com/suemkwon/reitit-app.git]
cd reitit-project
```

Install dependencies:

```bash
lein deps
```

## Running the Application

To run the application, use:

```bash
lein run
```

Access the application:

UI: http://localhost:3000
API Documentation: http://localhost:3000/swagger
API Endpoints: http://localhost:3000/api/

## Project Structure

```bash
reitit-project/
├── src/
│   └── reitit-app/
│       ├── core.clj        
├── resources/
│   └── public/
│       ├── index.html
└── project.clj             
```

## API Endpoints

- GET /api/ - List all tasks
- POST /api/tasks - Create a new task
- DELETE /api/tasks - Delete a task
- GET /api/features - List feature flags
- POST /api/features/:feature-key - Toggle feature flag

## Technologies Used

Backend:

- Reitit (routing)
- Ring (web server)
- Swagger (API documentation)


Frontend:

- Alpine.js (interactivity)
- Tailwind CSS (styling)
- Axios (API communication)

