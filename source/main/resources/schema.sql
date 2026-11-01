CREATE TABLE IF NOT EXISTS business (
    business_id TEXT PRIMARY KEY,
    business_name TEXT NOT NULL,
    phone TEXT,
    email TEXT,
    minimum_cash_threshold REAL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS dataset_version (
    version_id TEXT PRIMARY KEY,
    version_number INTEGER,
    file_name TEXT,
    upload_date TIMESTAMP,
    record_count INTEGER,
    previous_version_id TEXT
);

CREATE TABLE IF NOT EXISTS transaction_record (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transaction_id TEXT,
    version_id TEXT,
    transaction_date DATE,
    transaction_type TEXT,
    category TEXT,
    description TEXT,
    amount REAL,
    customer TEXT,
    customer_phone TEXT,
    customer_email TEXT,
    supplier TEXT,
    due_date DATE,
    payment_date DATE,
    status TEXT
);
CREATE INDEX IF NOT EXISTS idx_transaction_id ON transaction_record(transaction_id);
CREATE INDEX IF NOT EXISTS idx_transaction_date ON transaction_record(transaction_date);
CREATE INDEX IF NOT EXISTS idx_transaction_version ON transaction_record(version_id);

CREATE TABLE IF NOT EXISTS data_change (
    change_id TEXT PRIMARY KEY,
    version_id TEXT,
    transaction_id TEXT,
    change_type TEXT,
    field_name TEXT,
    old_value TEXT,
    new_value TEXT,
    changed_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customer (
    customer_id TEXT PRIMARY KEY,
    customer_name TEXT,
    phone TEXT,
    email TEXT,
    average_payment_delay REAL,
    total_outstanding REAL
);

CREATE TABLE IF NOT EXISTS recurring_payment (
    recurring_id TEXT PRIMARY KEY,
    description TEXT,
    category TEXT,
    amount REAL,
    frequency TEXT,
    next_due_date DATE,
    active BOOLEAN
);

CREATE TABLE IF NOT EXISTS forecast (
    forecast_id TEXT PRIMARY KEY,
    version_id TEXT,
    forecast_date DATE,
    opening_cash REAL,
    expected_inflow REAL,
    expected_outflow REAL,
    closing_cash REAL
);

CREATE TABLE IF NOT EXISTS warning (
    warning_id TEXT PRIMARY KEY,
    version_id TEXT,
    warning_type TEXT,
    severity TEXT,
    warning_date DATE,
    projected_date DATE,
    message TEXT,
    status TEXT
);

CREATE TABLE IF NOT EXISTS anomaly (
    anomaly_id TEXT PRIMARY KEY,
    version_id TEXT,
    transaction_id TEXT,
    anomaly_type TEXT,
    severity TEXT,
    expected_value REAL,
    actual_value REAL,
    explanation TEXT
);

CREATE TABLE IF NOT EXISTS application_settings (
    setting_id TEXT PRIMARY KEY,
    theme TEXT,
    forecast_days INTEGER,
    minimum_cash_threshold REAL,
    currency TEXT,
    shopkeeper_phone TEXT,
    shopkeeper_email TEXT
);

CREATE TABLE IF NOT EXISTS communication_log (
    log_id TEXT PRIMARY KEY,
    transaction_id TEXT,
    customer_name TEXT,
    channel TEXT,
    recipient_type TEXT,
    recipient_contact TEXT,
    subject TEXT,
    message TEXT,
    status TEXT,
    timestamp TIMESTAMP
);

CREATE TABLE IF NOT EXISTS scenario (
    scenario_id TEXT PRIMARY KEY,
    name TEXT,
    description TEXT,
    created_at TIMESTAMP,
    base_version_id TEXT
);

CREATE TABLE IF NOT EXISTS scenario_adjustment (
    adjustment_id TEXT PRIMARY KEY,
    scenario_id TEXT,
    adjustment_type TEXT,
    description TEXT,
    amount REAL,
    target_date DATE
);

INSERT OR IGNORE INTO business (business_id, business_name, phone, email, minimum_cash_threshold, created_at, updated_at) 
VALUES ('B1', 'My Small Business', '+91 98765 00000', 'shopkeeper@cashflow.com', 50000.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT OR IGNORE INTO application_settings (setting_id, theme, forecast_days, minimum_cash_threshold, currency, shopkeeper_phone, shopkeeper_email)
VALUES ('DEFAULT', 'arctic-ledger', 90, 50000.0, 'INR', '+91 98765 00000', 'shopkeeper@cashflow.com');
