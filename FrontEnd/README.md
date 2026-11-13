# Learnflow — Learning Progress Tracker

A responsive React + TypeScript frontend for the capstone guide. The application implements the frontend portion of the React track, with a working browser-local demo and an HTTP adapter for a separate backend. It does not include a backend or database server.

## Run locally

Prerequisite: Node.js 22.12+ and npm.

Run all commands below from the `frontend/` directory (`cd frontend` from the project root).

```sh
npm install
npm run dev
```

Open the local address printed by Vite (normally http://127.0.0.1:5173).

```sh
npm test
npm run build
npm run preview
```

`npm run build` checks TypeScript and produces `dist/`. `npm run preview` serves that production build.

## Explore the demo

The sign-in screen offers **Coordinator demo** and **Candidate demo**, using fictional data. No hardcoded passwords or real personal data are required. The initial seed includes three learners, four activities, and eight assignments (five completed).

- **Overview:** derived summary cards, assignment status breakdown, activity list, learner progress, and cohort filter.
- **Activities:** search, create, edit, planned-hours validation, and assignment actions.
- **Learners:** search, account creation, case-insensitive email uniqueness, individual progress, assignment creation, and credential resets.
- **Progress:** cohort-wide assignment statuses and hours.
- **My Training:** only the signed-in candidate's assignments; status selection with an explicit save and confirmation.
- **Onboarding:** temporary credentials displayed once, 24-hour expiry, mandatory first-login password change, and logout.

Changes persist in localStorage; the current demo identity is stored in sessionStorage. An eight-hour demo session expires automatically. Demo passwords are salted PBKDF2 hashes, not plaintext. Resets invalidate older demo sessions. **Browser-local role checks and hashes are demonstration behavior, not a production authentication boundary.** People who control the browser can edit its local data. Never use real credentials or personal data in demo mode.

To test onboarding end to end:

1. Open Coordinator demo and create a candidate. Save the displayed temporary credentials privately; they are not redisplayed.
2. Open that learner's detail and assign an activity.
3. Sign out, then sign in using the new candidate credentials in the same browser and origin.
4. Set a new password, open My Training, change the activity status, and save.
5. Sign out and reopen Coordinator demo. The cohort summary reflects the saved progress after refresh.

For a clean demo, clear this origin's browser storage. Other origins or browser profiles have separate datasets. Candidate demo is a quick preview for seeded Jordan Lee; to test first-login behavior, create a new account through the coordinator instead.

## Connect a backend

Copy `.env.example` to `.env.local`, set `VITE_API_BASE_URL` to your backend origin, and restart Vite. An empty value uses the local demo. API mode hides the demo entry buttons and uses `fetch` with `credentials: 'include'`.

`src/service.ts` is the integration boundary. The adapter uses the guide's `/api/v1` paths:

| Method | Route | Purpose |
| --- | --- | --- |
| POST | /auth/login | Sign in |
| POST | /auth/logout | Invalidate session |
| POST | /auth/change-password | Complete first-login setup |
| GET / POST | /activities | List / create activities |
| PUT | /activities/{id} | Edit activity |
| GET / POST | /learners | List / create candidates |
| GET | /learners/{id}/assignments | Learner detail and coordinator summary data |
| POST | /learners/{id}/reset-password | Generate new temporary credentials |
| POST | /assignments | Assign activity |
| PATCH | /assignments/{id}/status | Save own progress |
| GET | /me/assignments | Candidate's own training |

Expected response shapes are the types in `src/types.ts`. IDs are opaque strings. List responses are JSON arrays. Login must return the display metadata `{ id, name, email, role, mustChangePassword, version, expiresAt }` in addition to setting a secure session cookie (`expiresAt` is epoch milliseconds; `version` may be a placeholder in API mode). This metadata is for UI rendering; the server must derive authorization from its cookie, never trust the metadata. Add this response metadata to the minimal login response described in the brief, or adapt `service.login` to your existing response.

Candidate `/me/assignments` responses include an `activity: { id, title, topic, plannedHours }` object for each assignment. Create-candidate/reset responses return `{ email, temporaryPassword, expiresAt }`. The frontend derives coordinator totals from the loaded assignment arrays; it does not require the optional `/cohort-summary` response. Requests use the field names shown in the guide: `name`, `email`, `cohort`; `title`, `topic`, `plannedHours`; `learnerId`, `activityId`; `status`; `currentPassword`, `newPassword`. Status values are exactly `Not Started`, `In Progress`, and `Completed`.

Return `{ "message": "Helpful error message" }` for failures, using the guide's 400/401/403/404/409 statuses. The UI handles loading, empty results, validation, successful saves, network failures, expired sessions, and storage failures.

The backend owns persistent storage, account provisioning, authorization/ownership, input validation, hashing, session rotation, expiry, rate limits, CSRF protection, and `Cache-Control: no-store` for credential responses. Configure CORS for the frontend origin with credential support. If your CSRF middleware requires a token header, add token retrieval and the header in `request()` before enabling API mode. Use HTTPS and secure HttpOnly session cookies outside local development. No credentials belong in Vite environment variables; these variables are public build configuration.

## Source layout

```text
src/App.tsx           Screens, dialogs, forms, and navigation
src/styles.css       Responsive design and reduced-motion support
src/service.ts       HTTP adapter and browser-local demo service
src/types.ts         Frontend data contracts
src/seed.ts          Fictional demo dataset
src/service.test.ts  Core data rules and onboarding/progress workflow
```

The interface includes labeled controls, keyboard navigation, focus-trapped dialogs with Escape dismissal, visible focus states, a skip link, screen-reader status/error announcements, mobile navigation, and horizontally scrollable data tables. Fonts use Google Fonts when available, with local sans-serif fallbacks. The logo, chart, and illustrations are SVG/CSS; there are no remote image dependencies.

## Scope

All eight functional requirements have corresponding frontend flows. Server-side acceptance criteria (SQL persistence, secure authentication, session cookies, CSRF, rate limiting, and server ownership enforcement) require a connected backend and are not satisfied by a standalone frontend. No hosting or cloud deployment is needed to run this project.
