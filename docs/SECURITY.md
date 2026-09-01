# Security Architecture

## Overview

The Enterprise Billing & Payment Platform uses Spring Security with JWT-based authentication and role-based authorization.

The security layer is designed around two application roles:

- `ADMIN`
- `CUSTOMER`

Authentication is stateless. After successful login, the client receives a JWT and sends it with protected API requests.

---

## Authentication Flow

### Login

Endpoint:

```http
POST /api/auth/login
```

Request:

```json
{
  "email": "customer@example.com",
  "password": "Password123"
}
```

Flow:

```text
Login request
    ↓
AuthenticationManager
    ↓
CustomUserDetailsService
    ↓
AppUser loaded from database
    ↓
BCrypt password validation
    ↓
JWT generated
    ↓
JWT returned to client
```

The JWT contains the authenticated user's identity and expiration information.

JWT configuration is externalized using environment variables:

```properties
jwt.secret=${JWT_SECRET:change-this-local-development-secret-key-with-at-least-32-bytes}
jwt.expiration-ms=${JWT_EXPIRATION_MS:3600000}
```

A strong `JWT_SECRET` must be configured in production.

---

## JWT Request Authentication

Protected requests send:

```http
Authorization: Bearer <jwt>
```

`JwtAuthenticationFilter` performs the following steps:

1. Reads the Bearer token.
2. Extracts the user email.
3. Loads the current user from the database.
4. Validates the JWT.
5. Creates the Spring Security authentication context.
6. Continues the request.

The application does not blindly trust authorization information from the JWT. The current user is loaded from the database before authentication is established.

### Authentication Error Handling

The API distinguishes authentication and authorization failures:

```text
Missing authentication
→ 401 Unauthorized

Malformed JWT
→ 401 Unauthorized

Expired JWT
→ 401 Unauthorized

JWT belonging to an unknown/deleted user
→ 401 Unauthorized

Authenticated user without required permission
→ 403 Forbidden
```

`RestAuthenticationEntryPoint` handles unauthenticated access to protected resources.

---

## Password Security

Passwords are never stored in plain text.

Spring Security's `BCryptPasswordEncoder` is used before storing passwords:

```text
Raw password
    ↓
BCrypt
    ↓
Password hash stored in app_users
```

---

## Application Users

Authentication information is stored separately from the customer business entity.

The `app_users` table contains:

- user ID
- email
- encoded password
- role
- enabled flag
- creation timestamp
- linked customer ID

A CUSTOMER application user is linked to one customer.

ADMIN users are not required to have a customer association.

---

## Roles

### ADMIN

Administrative users can perform privileged business operations such as:

- creating customers
- creating accounts
- issuing and cancelling bills
- performing administrative payment lifecycle actions
- managing reconciliation operations
- activating and deactivating customers
- generating customer registration invitations

### CUSTOMER

Customers can access resources belonging to their own customer/account hierarchy.

A customer cannot access another customer's data.

---

## Ownership Authorization

Role checks alone are not sufficient for customer-facing APIs.

The platform includes:

- `CustomerAuthorizationService`
- `AccountAuthorizationService`

These services are used by Spring method security expressions.

Example:

```java
@PreAuthorize(
    "@customerAuthorizationService.canAccessCustomer(authentication, #customerId)"
)
```

and:

```java
@PreAuthorize(
    "@accountAuthorizationService.canAccessAccount(authentication, #accountId)"
)
```

This provides object-level authorization.

```text
Customer A
    ↓
Account A
    ↓
Bills / Payments / Allocations

Customer A
→ allowed

Customer B
→ forbidden

ADMIN
→ allowed where administrative access is supported
```

---

## Method-Level Security

Method security is enabled using:

```java
@EnableMethodSecurity
```

Protected controller methods use `@PreAuthorize`.

Examples include:

```java
@PreAuthorize("hasRole('ADMIN')")
```

and ownership checks through authorization services.

This protects business operations at the controller method level instead of relying only on URL matching rules.

---

## Customer Registration

Public registration does not accept a customer ID directly.

An earlier design allowed:

```text
customerId + password
```

This was replaced because possession of a customer ID does not prove ownership of the customer account.

The current flow uses one-time registration invitations.

### Invitation Creation

Only an ADMIN can generate an invitation:

```http
POST /api/v1/customers/{customerId}/registration-token
```

Flow:

```text
ADMIN
    ↓
Customer validated
    ↓
Existing AppUser checked
    ↓
Existing active invitation checked
    ↓
Secure random token generated
    ↓
SHA-256 hash stored
    ↓
Raw token returned once
```

The raw registration token is not stored in the database.

---

## Registration Token Security

Registration tokens use:

- `SecureRandom`
- 32 random bytes
- URL-safe Base64 encoding
- SHA-256 hashing
- 24-hour expiration
- one-time usage
- database-backed consumption state

Only the token hash is persisted.

```text
Raw invitation token
        ↓
      SHA-256
        ↓
registration_tokens.token_hash
```

A registration token is rejected when it is:

- missing
- invalid
- expired
- already used

A new token is also rejected when:

- the customer already has an application user
- the customer already has another active registration token

---

## Concurrent Registration Protection

Registration uses a pessimistic database lock when validating a token for account creation.

The repository uses:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
```

This prevents two concurrent registration requests from successfully consuming the same invitation.

Example:

```text
Request A                     Request B
    ↓                             ↓
locks token row              waits for lock
    ↓
creates AppUser
marks token used
commits
                                  ↓
                            obtains row lock
                                  ↓
                            token already used
                                  ↓
                               rejected
```

---

## Transactional Registration

User creation and registration-token consumption occur within one transaction.

```text
Validate and lock token
        ↓
Resolve customer
        ↓
Validate customer/user state
        ↓
Encode password
        ↓
Create AppUser
        ↓
Mark registration token used
        ↓
Commit transaction
```

If registration fails, the transaction rolls back.

---

## Customer Lifecycle Security

Customer lifecycle state is synchronized with login access.

When an ADMIN deactivates a customer:

```text
Customer.status
→ INACTIVE

AppUser.enabled
→ false
```

When the customer is activated:

```text
Customer.status
→ ACTIVE

AppUser.enabled
→ true
```

Both changes occur transactionally.

A customer therefore cannot continue authenticating with an enabled application login after being deactivated.

---

## Login Email Consistency

The customer email used during registration becomes the application's login identity.

Normal customer profile updates therefore do not allow the email address to be changed.

Customers may update:

- first name
- last name
- phone number

Email changes would require a dedicated verified-email-change process and are intentionally outside the normal profile update operation.

This prevents:

```text
Customer.email != AppUser.email
```

from occurring through normal profile updates.

---

## Razorpay Webhook Security

The Razorpay webhook remains publicly reachable because external Razorpay servers cannot provide an application JWT.

Endpoint:

```http
POST /api/v1/payments/webhooks/razorpay
```

Instead of JWT authentication, webhook authenticity is validated using the Razorpay webhook signature.

Invalid signatures are rejected.

---

## Swagger / OpenAPI

Swagger is configured with JWT Bearer authentication support.

Developers can:

1. Login through `/api/auth/login`.
2. Copy the JWT.
3. Use Swagger's **Authorize** option.
4. Test authenticated APIs.

Swagger endpoints are currently accessible without JWT for development convenience.

Production exposure should be reviewed separately.

---

## Admin Bootstrap

An initial ADMIN user can be created from environment configuration:

```properties
app.admin.email=${APP_ADMIN_EMAIL:}
app.admin.password=${APP_ADMIN_PASSWORD:}
```

No administrator is created when these values are empty.

Production administrator credentials must be supplied through environment variables and must not be committed to source control.

---

## Database Migrations

Security-related Flyway migrations:

```text
V12__create_app_users.sql
V13__link_app_user_to_customer.sql
V14__create_registration_tokens.sql
```

Applied Flyway migrations must not be edited after deployment.

Future schema changes must use new migration versions.

---

## Security Testing

Security behavior is covered by automated tests for:

- bill authorization
- customer authorization
- payment authorization
- payment allocation authorization
- reconciliation authorization
- Razorpay webhook handling
- JWT filter behavior
- registration-token validation
- ADMIN-only invitation generation
- application-user registration

Current project test result:

```text
Tests run: 61
Failures: 0
Errors: 0
Skipped: 0
```

---

## Production Security Requirements

Before production deployment:

- Configure a strong `JWT_SECRET`.
- Configure ADMIN credentials using environment variables.
- Never commit passwords or API secrets.
- Review Swagger/OpenAPI exposure.
- Keep HTTPS enabled at the deployment layer.
- Keep payment gateway secrets outside source control.
- Keep email-provider credentials outside source control.
- Rotate credentials that may have been exposed during development.

---

## Security Package Structure

```text
security/
├── auth/
│   ├── AuthController.java
│   ├── CustomUserDetailsService.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── RegisterRequest.java
│   └── RegisterResponse.java
│
├── authorization/
│   ├── AccountAuthorizationService.java
│   └── CustomerAuthorizationService.java
│
├── config/
│   ├── AdminBootstrap.java
│   ├── OpenApiConfig.java
│   ├── RestAuthenticationEntryPoint.java
│   └── SecurityConfig.java
│
├── jwt/
│   ├── JwtAuthenticationFilter.java
│   └── JwtService.java
│
├── registration/
│   ├── RegistrationToken.java
│   ├── RegistrationTokenController.java
│   ├── RegistrationTokenRepository.java
│   ├── RegistrationTokenResponse.java
│   └── RegistrationTokenService.java
│
├── role/
│   └── Role.java
│
└── user/
    ├── AppUser.java
    ├── AppUserRepository.java
    └── AppUserService.java
```

---

## Summary

The platform currently provides:

```text
JWT Authentication
        +
BCrypt Password Hashing
        +
ADMIN / CUSTOMER Roles
        +
Method-Level Authorization
        +
Customer Ownership Checks
        +
Account Ownership Checks
        +
One-Time Registration Invitations
        +
Hashed Registration Tokens
        +
Token Expiration
        +
Concurrent Registration Protection
        +
Customer/Login Lifecycle Synchronization
        +
401 / 403 Error Handling
        +
Automated Security Tests
```

This security layer provides the foundation for protecting the billing, payment, allocation, reconciliation, and customer APIs of the platform.