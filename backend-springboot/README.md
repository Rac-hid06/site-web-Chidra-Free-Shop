# Backend Spring Boot

## Deployment on Render

Create a PostgreSQL database in Render, then configure the Web Service:

- Root Directory: `backend-springboot`
- Runtime: Docker
- Dockerfile Path: `./Dockerfile`
- `SPRING_DATASOURCE_URL`: `jdbc:postgresql://<internal-host>:5432/<database>?sslmode=require`
- `SPRING_DATASOURCE_USERNAME`: database user from Render
- `SPRING_DATASOURCE_PASSWORD`: database password from Render
- `ADMIN_EMAIL`: email for the initial administrator account
- `ADMIN_PASSWORD`: a private password between 10 and 72 UTF-8 bytes
- `AUTH_COOKIE_SECURE`: `true`
- `AUTH_COOKIE_SAME_SITE`: `None`
- `APP_CORS_ALLOWED_ORIGINS`: `https://site-web-chidra-free-shop.vercel.app`

Use the PostgreSQL **internal** host and database details from Render. Store
credentials only in Render environment variables, never in Git. The application
creates the initial administrator account on first startup if that email is not
already present. Public registrations can only create customer accounts.

The Vercel site proxies `/api/*` to this Render service so browser sessions use
the Vercel site origin instead of relying on third-party cookies. The configured
CORS origin must still match the public Vercel site URL.

The former `ADMIN_API_TOKEN` is no longer used. Administrator-only API routes
require a valid signed-in administrator account. User passwords are BCrypt
hashed, and the database stores only hashes of 12-hour, revocable session tokens.
The browser receives an HttpOnly, Secure, SameSite=None session cookie; never
copy session cookies into client-side storage.

## Local development

Without `SPRING_DATASOURCE_URL`, the application uses a local H2 database.
Configure `ADMIN_EMAIL` and `ADMIN_PASSWORD` to create a local administrator.
For localhost development over HTTP, use `AUTH_COOKIE_SECURE=false` and
`AUTH_COOKIE_SAME_SITE=Lax`.
The PostgreSQL driver is included for Render deployment.
