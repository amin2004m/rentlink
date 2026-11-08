# RentLink — Backend API

RentLink is a peer‑to‑peer rental marketplace: owners list items for rent, lessees book
them for a date range, pay through an escrow‑style Stripe flow, and both sides review each
other afterwards. Admins moderate listings, users and disputes.

This repository is the **Spring Boot REST API**. The web client lives in a separate
[`rentlink-web`](../rentlink-web) project (Next.js).

## Tech stack

- **Java 21**, **Spring Boot 4**
- **MongoDB** (Spring Data MongoDB)
- **Spring Security** with stateless **JWT** access + refresh tokens
- **Stripe** for payments (escrow‑style hold/release/refund)
- **Cloudinary** for image hosting
- **Brevo SMTP** for e‑mail + **Firebase Cloud Messaging** for push notifications
- **springdoc‑openapi** (Swagger UI) for API docs

## Features

| Domain | What it does |
|---|---|
| **Auth** | Register / login / refresh; BCrypt passwords; 15‑min access + 7‑day refresh tokens |
| **Listings** | CRUD, image upload, public search with filters & pagination, admin approval workflow |
| **Bookings** | Full lifecycle: request → confirm/reject → pay → complete / cancel, with date‑overlap checks |
| **Payments** | Stripe PaymentIntent per booking, webhook‑driven status, escrow hold/release/refund, 10% commission |
| **Reviews** | One review per participant per **completed** booking; recalculates the reviewee's rating |
| **Disputes** | Either party opens a dispute; admin resolves with a financial outcome (refund lessee / release owner / none) |
| **Notifications** | E‑mail + FCM push on booking status changes; device‑token registration |
| **Admin** | Block/unblock users, moderate listings, resolve disputes, platform metrics report |

## Running locally

Prerequisites: **JDK 21**, **Docker** (for a throwaway MongoDB).

The quickest path is the bundled script, which starts a local MongoDB container and boots
the app with development credentials:

```bash
./run-local.sh
```

> `run-local.sh` and `local-truststore.jks` are git‑ignored because they hold local
> secrets. Copy the template below into your own `run-local.sh` and fill in real keys for
> the integrations you want to exercise (Stripe/Cloudinary/Brevo/Firebase). With the dummy
> values, those integrations no‑op or fail gracefully and the rest of the API works.

The app starts on <http://localhost:8080>. Interactive API docs:
<http://localhost:8080/swagger-ui.html>.

### Environment variables

| Variable | Purpose |
|---|---|
| `MONGODB_URI` | MongoDB connection string |
| `JWT_SECRET` | HMAC signing key (≥ 32 bytes) |
| `STRIPE_SECRET_KEY`, `STRIPE_WEBHOOK_SECRET` | Stripe API + webhook signature |
| `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` | Image hosting |
| `BREVO_SMTP_HOST`, `BREVO_SMTP_PORT`, `BREVO_SMTP_USERNAME`, `BREVO_SMTP_PASSWORD`, `MAIL_FROM_ADDRESS` | Outbound e‑mail |
| `FIREBASE_CREDENTIALS_JSON` | FCM service‑account JSON (`{}` disables push) |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Seeds an admin account on startup (optional) |
| `PORT` | HTTP port (default 8080) |

### Build & test

```bash
./gradlew build        # compile + package
./gradlew bootRun      # run (expects the env vars above)
```

## API overview

All paths are prefixed with `/api`. Auth is a Bearer JWT in the `Authorization` header.

- **Public:** `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`,
  `GET /listings`, `GET /listings/{id}`, `GET /listings/owner/{id}`, `GET /listings/search`,
  `GET /users/{id}/profile`, `GET /reviews/user/{id}`, `POST /payments/webhook`
- **Authenticated:** `GET /listings/my`, listing create/update/delete/image,
  all `/bookings/*`, `/payments/intent`, `/reviews`, `/disputes/*`, `/users/me*`,
  `/notifications/device-tokens`
- **Admin (`ROLE_ADMIN`):** everything under `/admin/*`

See Swagger UI for the full, always‑current contract.

## Notes on payments

Payment capture uses Stripe's automatic capture; escrow **hold / release / refund** is
modelled as `Payment.escrowStatus` state in the database rather than via Stripe Connect
transfers. This keeps the flow demonstrable without onboarding connected accounts. Wiring
real payouts (Connect transfers + deposit refunds) is the natural next step for production.
