# Global World Currency Converter & Calculator (Android)

Dark-mode-only Android app concept with:
- Global currency converter
- Dedicated calculator with live currency conversion
- Free and premium feature tiers
- Stripe-based `$5/month` premium subscription path for sideloaded APK distribution

## Current implementation status

### Functional now (in-app)
- Dark mode as the only theme
- Bottom navigation (Convert, Calculator, Rates, Premium)
- Searchable currency picker
- Converter screen with swap, refresh, and favorites button actions
- Calculator screen with arithmetic keypad and conversion
- Global rates screen with search and favorite toggles
- Free tier currency access + premium-gated currency set
- Anonymous free usage (no account required)

### Partially functional (requires external configuration)
- Real payments for sideloaded APK (`$5/month`) using Stripe:
  - Add `EXCHANGE_RATE_API_KEY`, `STRIPE_PUBLISHABLE_KEY`, and `BACKEND_BASE_URL` in `local.properties` (or environment variables)
  - Implement backend endpoint for PaymentIntent/SetupIntent creation
  - Implement webhook to activate/renew premium
- Premium login + purchase restore:
  - Email input and premium state handling are included
  - Full restore flow requires backend identity/subscription verification

## Why some premium features are not fully live yet
- Sideloaded APK payments cannot rely on Play Billing; they need external processor/backend.
- Subscription lifecycle (renewals, restores, cancellations) must be server-verified for security.
- Without backend verification, the app can only provide demo activation in local storage.

## Build notes
- This repository includes the app source and configuration placeholders.
- In this environment, Android dependencies from `dl.google.com` are not reachable, so Gradle build/test execution is blocked until network access is available.
