# SpendWise

A smart expense tracking application that uses AI to automatically categorize your spending and provide personalized financial insights. Built with Spring Boot and React.

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)

## What is this project?

This is a full-stack web application that helps you manage your personal finances. Instead of manually categorizing every expense, the AI does it for you. You can chat with an AI assistant to get insights about your spending patterns, see visual charts of where your money goes, and get predictions for future expenses.

Think of it as having a personal finance assistant that learns from your spending habits and helps you make better financial decisions.

## Key Features

- **Smart Categorization**: AI automatically sorts your transactions into categories (Food, Travel, Shopping, etc.)
- **AI Chatbot**: Ask questions like "How much did I spend on food last month?" and get instant answers
- **Visual Analytics**: See your spending through interactive pie charts and trend lines
- **Secure Authentication**: Your data is protected with JWT-based login system
- **Monthly Insights**: Get AI-generated predictions and recommendations for your finances
- **Easy Transaction Management**: Add, view, and delete transactions with a clean interface

## 🛠️ Tech Stack

### Backend
- **Spring Boot 3.2** - Main framework for building the REST API
- **PostgreSQL** - Database to store users and transactions
- **Spring Security + JWT** - Handles user authentication and authorization
- **Google Gemini AI** - FREE AI service for categorization and insights (no credit card needed!)
- **Maven** - Dependency management

### Frontend
- **React 18** - UI framework
- **Vite** - Fast development server and build tool
- **Tailwind CSS** - For styling
- **Recharts** - Interactive charts
- **Axios** - API calls to backend

## Prerequisites

Before running this project, make sure you have:

- Java 17 or higher
- Node.js 18 or higher
- PostgreSQL 14 or higher
- Maven 3.9+
- A Google Gemini API key (free from https://aistudio.google.com/)

## Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/tusquake/SpendWise.git
cd ai-expense-tracker
```

### 2. Setup Database

Create a PostgreSQL database:

```bash
psql -U postgres
CREATE DATABASE expense_tracker;
\q
```

### 3. Configure Backend

Create a `.env` file in the `backend` folder:

```env
DATABASE_URL=jdbc:postgresql://localhost:5432/expense_tracker
DB_USERNAME=postgres
DB_PASSWORD=your_password
GEMINI_API_KEY=your_gemini_api_key
JWT_SECRET=your_secret_key
```

### 4. Run Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend will start on `http://localhost:8080`

### 5. Setup Frontend

```bash
cd frontend
npm install
```

Create `.env` file in the `frontend` folder:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### 6. Run Frontend

```bash
npm run dev
```

Frontend will start on `http://localhost:5173`

## How to Use

1. **Register/Login**: Create an account or login with your credentials
2. **Add Transactions**: Click "Add Transaction" and enter your expense details
3. **View Dashboard**: See your spending visualized in charts and graphs
4. **Get AI Insights**: The AI banner at the top shows personalized financial advice
5. **Chat with AI**: Click the chat icon to ask questions about your expenses
6. **Track Trends**: Monitor your monthly spending patterns

## API Endpoints

### Authentication
- `POST /api/auth/register` - Create new user account
- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/refresh` - Refresh expired token

### Transactions
- `GET /api/transactions/all` - Get all your transactions
- `POST /api/transactions/add` - Add a new transaction
- `DELETE /api/transactions/{id}` - Delete a transaction

### AI Features
- `GET /api/ai/insights` - Get AI-generated financial insights
- `POST /api/ai/analyze` - Analyze and categorize transactions
- `POST /api/ai/chatbot` - Chat with AI assistant

Full API documentation available at: `http://localhost:8080/swagger-ui.html`

## Project Structure

```
ai-expense-tracker/
├── backend/
│   ├── src/main/java/com/finance/aiexpense/
│   │   ├── controller/     # REST API endpoints
│   │   ├── service/        # Business logic
│   │   ├── repository/     # Database operations
│   │   ├── entity/         # Database models
│   │   ├── dto/            # Data transfer objects
│   │   ├── security/       # JWT and authentication
│   │   └── config/         # Configuration files
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/     # Reusable UI components
│   │   ├── pages/          # Main pages (Login, Dashboard)
│   │   ├── context/        # React context (Auth)
│   │   ├── services/       # API calls
│   │   └── utils/          # Helper functions
│   └── package.json
└── README.md
```

## 🧪 Testing

```bash
# Backend tests
cd backend
mvn test

# Frontend tests
cd frontend
npm test
```

## Security Features

- Passwords are encrypted using BCrypt
- JWT tokens for secure API access
- CORS configured to prevent unauthorized access
- SQL injection prevention through JPA
- Input validation on all endpoints

## Why I Built This

As someone managing personal finances, I found it tedious to manually categorize every expense. I wanted to learn how to integrate AI into a real-world application, so I built this project to solve both problems. It demonstrates full-stack development skills, AI integration, security implementation, and clean architecture principles.

## What I Learned

- Integrating AI APIs (Google Gemini) into a Spring Boot application
- Implementing JWT-based authentication from scratch
- Building responsive React dashboards with charts
- Working with PostgreSQL and JPA relationships
- Managing state in React with Context API
- Deploying full-stack applications