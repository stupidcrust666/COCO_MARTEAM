Parking Management System 
# COCO_MARTEAM
- Buro, Chloie M.
- Guday, Lorenzo Jose L.
- Hilado, Myrrh Felicity J.
- Lacia, Princess Jane A.
- Lumapak, Aldrin F.;



---

Smart Parking Management System (COCO_MARTEAM)

A Java Swing desktop application designed for parking attendants and cashiers to manage multi-zone vehicle check-ins, real-time slot availability, automated fee calculation with PWD/Senior discounts, payment processing, and persistent transaction history.

---

## Features & System Requirements Coverage

### 1. Multi-Screen Navigable GUI (`javax.swing`)
* **Login Screen (`LoginForm`)**: Authenticates parking attendants/operators with password visibility toggle and validation prompts before granting access to the dashboard.
* **Parking Dashboard (`SmartParking`)**: Displays live occupancy across 10 typed slots (`C-01` to `C-04` for Cars, `M-01` to `M-04` for Motorcycles, and `V-01` to `V-02` for Vans), validates plate numbers and vehicle types, prevents duplicate check-ins, and tracks available capacity.
* **Checkout & Payment (`CheckoutPaymentForm`)**: Calculates parking duration and hourly fees (`PHP 30/hr` Motorcycle, `PHP 50/hr` Car, `PHP 70/hr` Van), dynamically applies a 20% PWD/Senior Citizen discount, and processes payments across `CASH`, `GCASH`, or `CARD`.
* **Parking & Payment History (`RecordsHistoryForm`)**: Displays all `PENDING` and `PAID` transactions with real-time keyword search, status filtering, and automatic revenue totals.

### 2. Persistent File Reading & Writing (`java.io`)
* Automatically writes every new check-in (`PENDING`) and completed checkout (`PAID`) to **`parking_records.csv`** via `BufferedWriter`.
* Automatically reads saved records from **`parking_records.csv`** via `BufferedReader` upon startup and refresh so transaction logs persist across sessions.

### 3. Interactive User Feedback (`JOptionPane`)
* Input validation alerts (empty fields, invalid login credentials, duplicate plate numbers, vehicle-slot type mismatches, and unselected rows).
* Action confirmations and digital payment receipts upon successful check-in, checkout, and logout.

---

## Object-Oriented Architecture

* **Abstraction & Inheritance**: `Person` (abstract base class) extended by `Driver` and `Parking_Attendant`.
* **Interfaces**: `Discountable` (20% discount calculation) and `Prioritizable` (PWD/Senior priority slot eligibility) implemented by `Driver`.
* **Encapsulation & Domain Models**: `Parking_Lot`, `Parking_Slot`, `Vehicle`, `Ticket`, and `Payment`, supported by enums `Vehicle_Type`, `Payment_Method`, and `Payment_Status`.

---

## How to Run

1. Open the project folder in **IntelliJ IDEA** (JDK 17 or higher).
2. Ensure the `src/` directory is marked as **Sources Root**.
3. Run **`Main.java`** (`Main.main()`).
4. Log in with the attendant credentials configured in `LoginForm.java`.
  
