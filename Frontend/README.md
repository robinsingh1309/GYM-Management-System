# FitManager Frontend

React frontend for the FitManager gym administration application. It provides authenticated screens for managing members, memberships, pricing plans, payments, exercise recommendations, and the personal assistant.

## Technology stack

- React 19
- Vite 8
- React Router 6
- Ant Design 6
- Axios
- Plain CSS with shared FitManager theme variables

## Prerequisites

- A Node.js version supported by Vite 8
- npm
- The FitManager Spring Boot backend running on `http://localhost:8080`

## Install and run

Run these commands from the `Frontend` directory:

```bash
npm ci
npm run dev
```

The development server is available at `http://localhost:5173`.

The API base URL is currently defined in `src/api/axiosClient.js` as `http://localhost:8080`. Update that constant when the backend runs at a different address.

## Available commands

```bash
npm run dev      # Start the Vite development server
npm run build    # Create a production build in dist/
npm run lint     # Run ESLint across the frontend
npm run preview  # Preview the production build locally
```

There is currently no automated frontend test runner configured.

## Authentication

The frontend supports:

- Email and password registration and login
- Google OAuth 2.0 login through the backend
- JWT-based access to protected pages

After login, the JWT is stored in browser `localStorage` under `fitmanager_token`. The shared Axios client adds it to API requests as a Bearer token.

Google login begins at the backend endpoint:

```text
http://localhost:8080/oauth2/authorization/google
```

The backend redirects successful authentication to `/oauth2/callback`, where the frontend exchanges the temporary handoff code for a JWT.

## Routes

| Route | Purpose |
|---|---|
| `/login` | Email/password and Google login |
| `/signup` | Account registration |
| `/oauth2/callback` | Google authentication callback |
| `/dashboard` | Gym activity overview |
| `/members` | Member management |
| `/memberships` | Membership management |
| `/memberships-type` | Membership pricing plans |
| `/payments` | Payment management |
| `/personal-assistant` | Knowledge-base assistant |
| `/exercise-recommendations` | Exercise recommendations |

All application routes except authentication routes are rendered through `ProtectedRoute` and `AppLayout`.

## Project structure

```text
src/
├── api/                 Shared authenticated Axios client
├── components/common/   Shared application components such as the sidebar
├── context/             Authentication state and JWT handling
├── features/            Feature modules grouped by business capability
│   ├── auth/
│   ├── dashboard/
│   ├── exerciseRecommendations/
│   ├── members/
│   ├── memberships/
│   ├── membershipTypes/
│   ├── payments/
│   └── personalAssistant/
├── layouts/             Authenticated application shell
├── routes/              React Router configuration
└── utils/               Shared formatting and domain helpers
```

Feature-specific API functions live inside each feature's `api` directory and use the shared client in `src/api/axiosClient.js`. Components should not create separate Axios clients or duplicate authentication handling.

## Roles

The backend supplies either the `ADMIN` or `STAFF` role in the JWT. The frontend reads that role through `AuthContext` to control actions that require administrator permission. The backend remains the authoritative authorization boundary.

## Styling

Authenticated pages use the shared dark-theme variables defined in `src/layouts/AppLayout.css`. Feature styles should reuse those variables and existing Ant Design components instead of introducing another styling system.

## Production notes

- Build the frontend with `npm run build`.
- Serve the generated `dist/` directory through a static host.
- Configure the API base URL for the deployed backend.
- Configure the backend CORS origin and `frontend.base-url` for the deployed frontend URL.
- Use HTTPS in production, especially for OAuth redirects and authentication cookies.
