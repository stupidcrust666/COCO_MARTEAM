# Smart Parking Management System (COCO_MARTEAM)

### Team Members
- Buro, Chloie M.
- Guday, Lorenzo Jose L.
- Hilado, Myrrh Felicity J.
- Lacia, Princess Jane A.
- Lumapak, Aldrin F.

---

A Java Swing desktop application designed for parking attendants and cashiers to manage multi-zone vehicle check-ins, real-time slot availability, automated fee calculation with PWD/Senior discounts, payment processing, and persistent transaction history.

---

## Features & System Requirements Coverage

### 1. Multi-Screen Navigable GUI (`javax.swing`)
- **Native System Look & Feel (`Main`)**: Applies the operating system's native UI theme (`UIManager`) across all windows, buttons, tables, and dialogs.
- **Login Screen (`LoginForm`)**: Authenticates parking attendants/operators with password visibility toggle and input validation before granting access to the dashboard.
- **Parking Dashboard (`SmartParking`)**: Displays live occupancy across 10 typed slots (`C-01` to `C-04` for Cars, `M-01` to `M-04` for Motorcycles, and `V-01` to `V-02` for Vans) with color-coded status indicators (`AVAILABLE` / `OCCUPIED`), validates plate numbers and vehicle-slot type matching, prevents duplicate check-ins, and tracks available capacity.
- **Checkout & Payment (`CheckoutPaymentForm`)**: Calculates parking duration (with a 1-hour minimum charge) using official rates from the `Vehicle_Type` enum (`PHP 20.00/hr` Motorcycle, `PHP 40.00/hr` Car, `PHP 60.00/hr` Van), dynamically applies a 20% PWD/Senior Citizen discount, processes payments (`CASH`, `GCASH`, `PAYMAYA`, `CREDIT_CARD`, `DEBIT_CARD`), and releases the slot only after payment is confirmed.
- **Parking & Payment History (`RecordsHistoryForm`)**: Displays all `PENDING` and `PAID` transactions with color-coded statuses, real-time keyword search, status filtering, automatic revenue totals, and a confirmed **Clear History** reset utility for demo testing.

### 2. Persistent File Reading & Writing (`java.io`)
- Automatically writes every new check-in (`PENDING`) and completed checkout (`PAID`) to **`parking_records.csv`** via `BufferedWriter` and `FileWriter`.
- Automatically reads saved records from **`parking_records.csv`** via `BufferedReader` and `FileReader` upon startup and refresh so transaction logs persist across sessions.

### 3. Interactive User Feedback (`JOptionPane`)
- Input validation alerts (empty fields, invalid login credentials, duplicate plate numbers, vehicle-slot type mismatches, and unselected rows).
- Action confirmations and digital payment receipts upon successful check-in, checkout, history clearing, and logout.

---

## Object-Oriented Architecture

- **Abstraction & Inheritance**: `Person` (abstract base class) extended by `Driver` and `Parking_Attendant`.
- **Interfaces**: `Discountable` (20% discount calculation) and `Prioritizable` (PWD/Senior priority slot eligibility) implemented by `Driver`.
- **Encapsulation & Domain Models**: `Parking_Lot`, `Parking_Slot` (with overloaded constructors for typed and priority zones), `Vehicle`, `Ticket`, and `Payment`, supported by rich enums `Vehicle_Type`, `Payment_Method`, and `Payment_Status`.
