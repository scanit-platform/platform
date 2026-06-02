# ScanIt Web Frontend

ScanIt Web is a Next.js frontend for the ScanIt platform. The app uses the Next.js App Router, React Server Components, TypeScript, Tailwind CSS 4, and a Feature-Sliced Design inspired folder structure.

This README is written for teammates who already know React but have not worked much with Next.js.

## Quick Start

```bash
npm install
printf 'VITE_API_URL=http://localhost:8080\n' > .env
npm run dev
```

Open `http://localhost:3000`.

Useful commands:

```bash
npm run dev      # local development server
npm run build    # production build and type validation
npm run start    # run the production build
npm run lint     # ESLint
npx tsc --noEmit # TypeScript check
```

## Environment

The frontend reads the backend base URL from:

```env
VITE_API_URL=http://localhost:8080
```

Local backend:

```text
http://localhost:8080
```

Production backend:

```text
https://platform-erpd.onrender.com
```

Do not hardcode API URLs in components or feature code. Use `src/shared/api/client.ts`.

## Current Routes

Routes live in the repository-level `app/` folder because this project uses the Next.js App Router.

```text
app/
  page.tsx            redirects / to /dashboard
  dashboard/page.tsx  dashboard route
  login/page.tsx      login/register route
  layout.tsx          root HTML layout, fonts, metadata, global CSS
```

For React developers: route files are not normal page components. They are thin route adapters. Most UI composition should happen under `src/views`.

## Project Structure

```text
app/
  dashboard/page.tsx
  login/page.tsx
  layout.tsx
  page.tsx

src/
  app/
    fonts.ts
    metadata.ts
    styles/globals.css

  views/
    dashboard/DashboardView.ts
    login/LoginView.ts

  widgets/
    auth-layout/ui/auth-layout.tsx
    dashboard-overview/ui/dashboard-overview.tsx

  features/
    auth/
      api/auth-api.ts
      model/
      types/auth.ts
      ui/auth-form.tsx

  entities/
    user/
      api/get-current-user.ts
      types/user.ts

  shared/
    api/client.ts
    ui/
```

## Layer Responsibilities

We follow a strict direction of responsibility:

```text
shared -> entities -> features -> widgets -> views -> app
```

Higher layers may import lower layers. Lower layers must not import higher layers.

Allowed examples:

```ts
// OK: a widget composes a feature
import { AuthForm } from "@/src/features/auth/ui/auth-form";

// OK: a feature uses shared infrastructure
import { apiFetch } from "@/src/shared/api/client";
```

Forbidden examples:

```ts
// Wrong: shared cannot know about auth
import { login } from "@/src/features/auth/api/auth-api";

// Wrong: a feature cannot import a widget or view
import { DashboardOverview } from "@/src/widgets/dashboard-overview/ui/dashboard-overview";
```

## What Each Layer Means

`app/`

Next.js routing only. Keep route files small. They should parse route params/search params and render a view.

`src/app/`

Application setup: global CSS, metadata, fonts, providers if added later. No business logic.

`src/views/`

Route-level composition. A view decides which widgets/features appear on a route. Keep API details and form logic out of views.

`src/widgets/`

Large composed UI blocks: layouts, sidebars, dashboards, headers, page sections. Widgets can combine features, entities, and shared UI.

`src/features/`

User actions and workflows: login, register, upload receipt, filter dashboard, scan receipt. A feature owns its UI, validation, API calls, action state, and local workflow logic.

`src/entities/`

Business domain models: user, receipt, transaction, budget. Keep domain types, entity API mapping, and entity-specific selectors here. No page layout code.

`src/shared/`

Reusable primitives and infrastructure only: Button, Input, generic API client, generic hooks, constants, small helpers. No ScanIt business workflows here.

## How To Add A New Feature

Example: adding receipt upload.

Create:

```text
src/features/receipt-upload/
  api/upload-receipt.ts
  model/upload-state.ts
  types/upload-receipt.ts
  ui/receipt-upload-form.tsx
```

Then compose it from a widget or view:

```tsx
import { ReceiptUploadForm } from "@/src/features/receipt-upload/ui/receipt-upload-form";

export function ReceiptPanel() {
  return <ReceiptUploadForm />;
}
```

Do not put feature logic directly inside `app/dashboard/page.tsx`.

## How To Add A New Entity

Example: receipts.

Create:

```text
src/entities/receipt/
  api/get-receipts.ts
  types/receipt.ts
```

Entity types should match backend DTOs or clearly map from backend DTOs.

```ts
export type Receipt = {
  id: number;
  merchant: string;
  total: number;
  createdAt: string;
};
```

If the backend response differs from frontend naming, map it inside the entity or feature API file, not inside UI components.

## Next.js Notes For React Developers

Server components are the default in the App Router. Files are server components unless they start with:

```ts
"use client";
```

Use client components when you need browser-only behavior:

- `useState`
- `useEffect`
- event handlers like `onClick`
- form interaction state
- direct browser APIs

Server components are good for:

- reading cookies
- redirecting
- loading server-side data
- composing static or server-rendered UI

Server actions use:

```ts
"use server";
```

Current auth form submission is handled with a server action in:

```text
src/features/auth/model/actions/submit-auth-form.ts
```

## Authentication Flow

Current behavior:

- `/dashboard` is available to guests with sample data.
- Guests can use the app preview without registering.
- Guest CTAs route to `/login?mode=signup` or `/login?mode=signin`.
- Login/register call the backend through `src/features/auth/api/auth-api.ts`.
- Auth token is stored in an HTTP-only cookie by `src/features/auth/model/session.ts`.
- Authenticated dashboard requests load the current user from `src/entities/user/api/get-current-user.ts`.

Auth request DTOs:

```ts
type AuthRequest = {
  email: string;
  password: string;
};

type RegisterRequest = {
  name: string;
  email: string;
  password: string;
};
```

Do not add `username`, `firstName`, `lastName`, `roles`, `permissions`, or `confirmPassword` to API requests unless the backend DTO changes. `confirmPassword` may exist only as frontend validation if needed.

## API Policy

Use the shared API client:

```ts
import { apiFetch } from "@/src/shared/api/client";
```

Feature API files should be small and typed:

```ts
export function login(payload: AuthRequest) {
  return apiFetch<AuthResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}
```

UI components should not call `fetch` directly. Put endpoint logic in `features/*/api` or `entities/*/api`.

## Styling Policy

Global tokens live in:

```text
src/app/styles/globals.css
```

Use existing CSS variables:

```css
--page-bg
--scanit-primary
--scanit-surface
--scanit-text
--scanit-text-secondary
--scanit-border
--scanit-soft
```

Shared reusable controls live in:

```text
src/shared/ui/
```

Before creating a new button/input style, check whether `Button`, `Input`, or existing `scanit-*` classes already cover the need.

## Naming Policy

Use clear names that describe ownership:

- `DashboardView` for route composition
- `DashboardOverview` for a widget
- `AuthForm` for feature UI
- `getCurrentUser` for entity API
- `submitAuthForm` for an auth server action

Avoid vague folders like `utils`, `components`, or `helpers` unless the code is genuinely generic and belongs in `shared`.

## Checklist Before Opening A PR

- The route file in `app/` is thin.
- Business logic is not inside route files or widgets.
- API calls are typed and placed in a feature/entity API file.
- Shared code does not import entities/features/widgets/views.
- New styles use existing ScanIt tokens.
- `npm run lint` passes.
- `npx tsc --noEmit` passes.
- `npm run build` passes for route/type issues.

