# PayCore Frontend

React + Vite + Tailwind dashboard for PayCore payment infrastructure.

## Theme: Midnight Pulse

Black base (`#080B12`) with electric blue accents (`#00B4FF`).

## Run

Backend must be running (`docker-compose up`).

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**

## Pages

- Dashboard — stats + recent payments
- Payments — full transaction list
- New Payment — initiate transfer
- Accounts — create VPAs, view ledger balances
- Fraud Queue — review flagged payments
