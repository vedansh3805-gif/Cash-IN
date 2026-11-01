# Independent Frontend Architecture & Integration Guide

---

## 1. Directory Structure

```text
frontend-only/
├── pom.xml                                  <-- Pure JavaFX dependencies (Zero DB, Zero Spring)
├── documentation/
│   ├── Front_ARCHITECTURE_AND_EXTRACTION_GUIDE.md
│   └── Front_USER_GUIDE.md
└── source/main/
    ├── java/com/cashflow/
    │   ├── model/                           <-- DTOs & Domain POJOs
    │   └── ui/
    │       ├── StandaloneFrontendApp.java   <-- Desktop application entry point
    │       ├── service/
    │       │   ├── CashFlowUiService.java   <-- Service boundary interface
    │       │   └── MockUiService.java       <-- Static mock data provider
    │       ├── theme/ThemeManager.java      <-- Dynamic CSS Theme Switcher
    │       └── view/                        <-- Full JavaFX desktop screens
    └── resources/styles/                    <-- 5 JavaFX CSS Theme Stylesheets
```

---

## 2. Decoupling & Isolation

1. **Service Boundary**:
   Every view is wired to the interface `CashFlowUiService`. Neither the views nor the application ever import database, SQLite, or Spring Boot classes.
2. **Mock Implementation**:
   `MockUiService.java` supplies static mock fixtures with realistic data curves for testing presentation views. It includes mock implementations for multi-channel communication (`initiateCall`, `sendSms`, `sendEmail`, `notifyBothParties`, `bulkNotifyDefaulters`, and `getCommunicationLogs`) with in-memory audit logs.
3. **Plugging In a Real Backend**:
   Simply create a class implementing `CashFlowUiService` that connects to your desired backend (REST, gRPC, or database) and pass it to `new MainLayout(myService)`. No UI code or stylesheet requires alteration.
4. **Dedicated Outreach & Contact Views**:
   - `CommunicationView.java`: Dedicated page featuring customer mobile number and email address directories, call/sms/email modals, and a live communication audit trail.
   - `ReceivablesView.java`: Overdue aging breakdown with inline buyer contacts and outreach actions.
5. **Communication & Contact DTOs**:
   - `Transaction.java`: features `customerPhone` and `customerEmail`.
   - `Customer.java`: features `phone`, `email`, and delay metrics.
   - `CommunicationLog.java`: features `logId`, `transactionId`, `channel`, `recipientType`, `recipientContact`, `messageBody`, and `status`.
   - `ApplicationSettings.java`: features `shopkeeperPhone` and `shopkeeperEmail`.
