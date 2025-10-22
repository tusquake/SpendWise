> ⚠️ **Note:** If the login endpoint takes time to respond, it’s because the backend is hosted on **Render’s free tier**, which goes idle after 15 minutes of inactivity. It may take **50 seconds to 1 minute** to wake up again.  
>  We plan to **migrate to AWS** in the future for faster and more reliable performance.


# SpendWise

A smart expense tracking application that uses AI to automatically categorize your spending and provide personalized financial insights. Built with Spring Boot and React.

![Version](https://img.shields.io/badge/version-2.0.0-blue.svg)

<b>Application Overview : </b>

1. Login/Register
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/0fd1c520-7343-4760-aad9-1443b5471dd9" />

2. Dashboard
<img width="1920" height="840" alt="image" src="https://github.com/user-attachments/assets/bddf6267-a45b-41c0-8d6a-fbe7048d9e20" />

3. Add Transaction
<img width="1920" height="835" alt="image" src="https://github.com/user-attachments/assets/09be23f7-af35-487c-8913-920cd0d15fb1" />

4. Chatbot
<img width="1920" height="842" alt="image" src="https://github.com/user-attachments/assets/e71405c5-19f8-4715-afcc-a39ea3a13060" />

5. Caching System (Notice the Time Difference)
<img width="1920" height="1018" alt="image" src="https://github.com/user-attachments/assets/2cf73aab-fefd-4081-a504-6c951fd98f4e" />
<img width="1920" height="1016" alt="image" src="https://github.com/user-attachments/assets/e15df4e5-782b-4bd9-be34-e034946429f3" />

6. Rate Limiter for Chatbot
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/4b3545ed-0038-4202-a371-c9ecd86c5146" />

6. Circuit Breaker(If Gemini api take lot of time or is failing fast with threshold of more than 50%)
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/bd12fe31-118c-4428-8768-2aa7dacf2158" />

8. Subscription Plans
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/f3142308-f0fc-4f9a-ae17-f2becbf77a5f" />

9. Payment Integration
<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/8eb4870e-a6b8-4064-a5bd-56897daccf0d" />

## What is this project?

SpendWise is a full-stack expense tracking application I built to solve the problem of manual transaction categorization. The core idea is simple - you add transactions, and the AI handles the categorization automatically. I integrated Google's Gemini API for the AI capabilities and built the entire authentication and payment flow from scratch.

The application has grown beyond basic CRUD operations. It now includes proper caching with Redis, rate limiting to handle API abuse, OAuth2.0 for social logins, and working on adding payment processing. I wanted to build something that could actually include production features, not just a portfolio project.

## Core Features

**AI-Powered Categorization**
The Gemini API analyzes transaction descriptions and assigns categories automatically. I'm using a prompt engineering approach where I send transaction context and get back structured category data. Works pretty well for most common expense types.

**Chatbot Interface**
Built a chat interface where you can ask questions about your spending. The backend maintains conversation context and queries the database based on natural language inputs. Currently handles queries like spending summaries, category breakdowns, and trend analysis.

**Authentication System**
Implemented both traditional JWT-based auth and OAuth2.0. Users can login with email/password or use Google/GitHub OAuth. The JWT tokens have a 24-hour expiry with refresh token rotation. OAuth integration uses Spring Security OAuth2 Client.

**Redis Caching Layer**
Added Redis to cache frequently accessed data like transaction lists and category summaries. This reduced database load significantly. I'm using Spring Cache abstraction with TTL-based invalidation. Cache hit rate is around 60-70% in typical usage.

**Rate Limiting**
Implemented a token bucket algorithm using Redis. Different rate limits apply based on subscription tier. Free users get 2 requests/day, Premium gets 15, and Enterprise gets 30. This prevents API abuse and keeps server costs manageable.

**Subscription & Payments**
To be included....

**Analytics Dashboard**
Visual representation of spending using Recharts. Pie charts for category distribution, line graphs for monthly trends, and comparison views for period-over-period analysis. All data is aggregated on the backend to avoid sending raw transaction data.

## Tech Stack

### Backend
- Spring Boot 3.2 with Java 17
- PostgreSQL for persistent storage
- Redis for caching and rate limiting
- Spring Security with JWT and OAuth2.0
- Google Gemini AI API
- Payment Gateway SDK (Stripe/Razorpay) (To be included....)
- Maven for dependency management

### Frontend
- React 18 with functional components and hooks
- Vite for build tooling and dev server
- Tailwind CSS for styling
- Recharts for data visualization
- Axios for HTTP requests
- Context API for state management

## Prerequisites

You'll need these installed:

- Java 17 or higher
- Node.js 18+
- PostgreSQL 14+
- Redis 7.0+
- Maven 3.9+
- Google Gemini API key (free tier available)
- OAuth2.0 credentials (Google Cloud Console)
- Payment gateway credentials

## Setup Instructions

### 1. Clone and Setup Database

```bash
git clone https://github.com/tusquake/SpendWise.git
cd ai-expense-tracker

# Create PostgreSQL database
psql -U postgres
CREATE DATABASE expense_tracker;
\q
```


### 2. Backend Configuration

Create `.env` file in the backend folder:

```env
# Database
DATABASE_URL=jdbc:postgresql://localhost:5432/expense_tracker
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=your_jwt_secret_min_256_bits
JWT_EXPIRATION=86400000

# Google Gemini AI
GEMINI_API_KEY=your_gemini_api_key

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
CACHE_TTL=3600

# OAuth2.0
OAUTH2_GOOGLE_CLIENT_ID=your_google_client_id
OAUTH2_GOOGLE_CLIENT_SECRET=your_google_client_secret
OAUTH2_GITHUB_CLIENT_ID=your_github_client_id
OAUTH2_GITHUB_CLIENT_SECRET=your_github_client_secret

# Rate Limiting
RATE_LIMIT_FREE=100
RATE_LIMIT_PRO=1000
RATE_LIMIT_ENTERPRISE=10000

# Payment Gateway
PAYMENT_API_KEY=your_payment_key
PAYMENT_SECRET=your_payment_secret
PAYMENT_WEBHOOK_SECRET=your_webhook_secret
```

### 4. Run Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs on `spendwise-pgcx.onrender.com/`

### 5. Frontend Configuration

Create `.env` in the frontend folder:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_PAYMENT_PUBLIC_KEY=your_payment_public_key
VITE_OAUTH_REDIRECT_URI=http://localhost:5173/auth/callback
```

### 6. Run Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend runs on `spendwise-1-bcdd.onrender.com`

Full Swagger docs: `spendwise-pgcx.onrender.com/swagger-ui/index.html#/`

## Subscription Tiers

| Feature | Free | Premium | Enterprise |
|---------|------|-----|------------|
| Transactions/Month | 100 | Unlimited | Unlimited |
| AI Chat Messages | 2/day | 15/month | 30/month |
| Data Export | No | Yes | Yes |
| Historical Data | 3 months | 2 years | Unlimited |
| Custom Categories | 10 | 50 | Unlimited |
| OAuth Login | Yes | Yes | Yes |
| Price | Free | ₹9/month |₹19/month |

## Architecture Notes

**Caching Strategy**
I'm using a write-through cache pattern. On transaction create/update/delete, I invalidate related cache keys. For reads, check cache first, then DB if miss. Cache keys are namespaced by user ID to prevent data leaks.

**Rate Limiting Implementation**
Token bucket algorithm. Each request decrements the user's token count. Tokens refill based on tier limits. I'm using sliding window counters to prevent burst abuse at window boundaries.

**OAuth2.0 Flow**
Standard authorization code flow. User clicks "Login with Google", gets redirected to Google consent screen, redirected back with auth code, backend exchanges code for tokens, creates/updates user, issues JWT. GitHub OAuth works the same way.


**Database Schema**
User -> Transactions (one-to-many)
User -> Subscription (one-to-one)
Subscription -> Plan (many-to-one)
Transactions have indexed category and date columns for fast queries.

## Project Structure

```
ai-expense-tracker/
├── backend/
│   ├── src/main/java/com/finance/aiexpense/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── payment/
│   │   │   ├── stategy/
│   │   │   ├── factory/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── security/
│   │   ├── exception/
│   │   ├── enums/
│   │   └── config/
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── context/
│   │   ├── services/
│   │   └── utils/
│   └── package.json
└── README.md
```

## Security Implementation

- BCrypt password hashing with salt rounds of 12(“Salt rounds of 12” means the password is being hashed using bcrypt 12 iterations deep, balancing good security and reasonable performance.)
- JWT tokens signed with HS256
- CORS configured for frontend origin only
- Input validation using Bean Validation
- Rate limiting per IP and per user
- HTTPS enforced in production

## Performance Metrics

Based on local testing with 50 transactions:

- Average API response time: 200-300ms when cache miss(but 12-20ms when cache hit)
- Cache hit rate: 65-70%
- Database query time: 40-60ms average
- AI categorization: 800-1200ms per transaction

## Known Issues

- Gemini API sometimes misclassifies ambiguous transactions
- Cache invalidation can be inconsistent under high write load
- OAuth callback occasionally fails with state mismatch (investigating)
- Large transaction exports can timeout (need to add async processing)

## Future Roadmap

**Bank Statement Upload**
Planning to add PDF and CSV parsing. Will use Apache PDFBox for PDF extraction and parse bank-specific formats. AI will handle transaction matching and deduplication.

**Enhanced AI Capabilities**
Want to add spending predictions using time series analysis, anomaly detection for unusual transactions, and personalized budget recommendations based on historical patterns.

**Multi-Provider Payment Support**
Currently using mock payment gateway. Planning to add Stripe, PayPal, and Razorpay as alternatives. Will abstract payment logic behind a common interface(Using Stategy and Factory Design Pattern).

**Advanced Analytics**
Year-over-year comparisons, category trend analysis, spending forecasts using linear regression, and custom report generation with PDF export.

**Shared Accounts**
Family or team expense tracking with role-based access control. Need to redesign the data model to support account hierarchies.

**Mobile Applications**
React Native app for iOS and Android. Will reuse the same backend APIs. Considering adding push notifications for budget alerts.

**Additional Features Under Consideration**
- Multi-currency support with real-time exchange rates
- Receipt scanning with OCR (probably using Google Cloud Vision)
- Tax report generation for business expenses
- Investment portfolio tracking
- Cryptocurrency wallet integration
- Plaid or similar API integration for automatic transaction sync (This is complex due to banking regulations and API costs)

## Why I Built This

I started this as a simple CRUD app to learn Spring Boot, but it evolved into something more complex. I wanted to understand how production systems handle caching, rate limiting, and payment processing. The OAuth2.0 implementation taught me a lot about security flows. Integrating the AI was interesting because it's not just calling an API - you need to handle rate limits, retries, and fallbacks.

The project helped me understand the full lifecycle of a feature: design, implementation, testing, deployment, and monitoring. I made mistakes along the way (like not implementing rate limiting early enough), but that's how you learn.

## Contact

For questions or issues:
- Email: sethtushar111@gmail.com

---

If this project helped you learn something, consider starring it on GitHub.
