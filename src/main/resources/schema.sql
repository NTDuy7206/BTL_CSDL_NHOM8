CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT NOT NULL,
    display_name VARCHAR(120) NULL,
    email VARCHAR(190) NULL,
    phone VARCHAR(30) NULL,
    avatar_object_key VARCHAR(255) NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT fk_user_profiles_user
        FOREIGN KEY (user_id) REFERENCES `user` (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS contract_requests (
    request_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    room_id VARCHAR(20) NOT NULL,
    requested_start_date DATE NOT NULL,
    message VARCHAR(500) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    PRIMARY KEY (request_id),
    KEY ix_contract_requests_status_created (status, created_at),
    KEY ix_contract_requests_user (user_id),
    CONSTRAINT fk_contract_requests_user FOREIGN KEY (user_id) REFERENCES `user` (id) ON DELETE CASCADE,
    CONSTRAINT fk_contract_requests_room FOREIGN KEY (room_id) REFERENCES rooms (room_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS payment_reports (
    payment_report_id BIGINT NOT NULL AUTO_INCREMENT,
    payer_user_id BIGINT NOT NULL,
    invoice_id VARCHAR(20) NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    payment_method VARCHAR(40) NOT NULL,
    transaction_reference VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at DATETIME NOT NULL,
    PRIMARY KEY (payment_report_id),
    KEY ix_payment_reports_payer (payer_user_id, created_at),
    KEY ix_payment_reports_invoice (invoice_id, status),
    CONSTRAINT fk_payment_reports_user FOREIGN KEY (payer_user_id) REFERENCES `user` (id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_reports_invoice FOREIGN KEY (invoice_id) REFERENCES invoices (invoice_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Preserve USER=0 and ADMIN=1 while consolidating legacy roles.
CREATE TABLE IF NOT EXISTS demo_seed_history (
    seed_key VARCHAR(100) NOT NULL PRIMARY KEY,
    completed BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

UPDATE user SET role=1 WHERE role=4;
UPDATE user SET role=0 WHERE role IN (2,3);
CREATE TABLE IF NOT EXISTS tenant_accounts (
    user_id BIGINT NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(20) NOT NULL UNIQUE,
    CONSTRAINT fk_ta_user FOREIGN KEY(user_id) REFERENCES user(id),
    CONSTRAINT fk_ta_tenant FOREIGN KEY(tenant_id) REFERENCES tenants(tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS rental_request_details (
    request_id BIGINT NOT NULL PRIMARY KEY,
    tenant_id VARCHAR(20) NOT NULL,
    requested_end_date DATE NOT NULL,
    contract_id VARCHAR(20) NULL,
    CONSTRAINT fk_rr_request FOREIGN KEY(request_id) REFERENCES contract_requests(request_id) ON DELETE CASCADE,
    CONSTRAINT fk_rr_tenant FOREIGN KEY(tenant_id) REFERENCES tenants(tenant_id),
    CONSTRAINT fk_rr_contract FOREIGN KEY(contract_id) REFERENCES contracts(contract_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS landlord_properties (
 user_id BIGINT NOT NULL PRIMARY KEY, room_limit INT NOT NULL,
 CONSTRAINT fk_lp_user FOREIGN KEY(user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS room_landlords (
 room_id VARCHAR(20) NOT NULL PRIMARY KEY, user_id BIGINT NOT NULL,
 CONSTRAINT fk_rl_room FOREIGN KEY(room_id) REFERENCES rooms(room_id),
 CONSTRAINT fk_rl_user FOREIGN KEY(user_id) REFERENCES landlord_properties(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS contract_stays (
 contract_id VARCHAR(20) PRIMARY KEY,check_in DATE NULL,check_out DATE NULL,
 check_in_electric INT NULL,check_in_water INT NULL,check_out_electric INT NULL,check_out_water INT NULL,monthly_rent DECIMAL(18,2) NULL,
 FOREIGN KEY(contract_id) REFERENCES contracts(contract_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS billing_start (id INT PRIMARY KEY,start_month DATE NOT NULL) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS automatic_bills (
 contract_id VARCHAR(20) NOT NULL,year INT NOT NULL,month INT NOT NULL,invoice_id VARCHAR(20) NOT NULL,
 PRIMARY KEY(contract_id,year,month),FOREIGN KEY(contract_id) REFERENCES contracts(contract_id),FOREIGN KEY(invoice_id) REFERENCES invoices(invoice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS billing_alerts (
 contract_id VARCHAR(20) NOT NULL,year INT NOT NULL,month INT NOT NULL,message VARCHAR(500) NOT NULL,
 PRIMARY KEY(contract_id,year,month),FOREIGN KEY(contract_id) REFERENCES contracts(contract_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS invoice_notifications (
 invoice_id VARCHAR(20) PRIMARY KEY,user_id BIGINT NOT NULL,message VARCHAR(300) NOT NULL,seen BOOLEAN NOT NULL DEFAULT FALSE,
 FOREIGN KEY(invoice_id) REFERENCES invoices(invoice_id) ON DELETE CASCADE,FOREIGN KEY(user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS tenant_landlord_selection (
 user_id BIGINT NOT NULL PRIMARY KEY,
 landlord_id BIGINT NOT NULL,
 FOREIGN KEY(user_id) REFERENCES user(id) ON DELETE CASCADE,
 FOREIGN KEY(landlord_id) REFERENCES landlord_properties(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS tenant_landlords (
 tenant_id VARCHAR(20) NOT NULL,
 user_id BIGINT NOT NULL,
 PRIMARY KEY(tenant_id,user_id),
 FOREIGN KEY(tenant_id) REFERENCES tenants(tenant_id) ON DELETE CASCADE,
 FOREIGN KEY(user_id) REFERENCES landlord_properties(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS finance_receipts (
 payment_report_id BIGINT PRIMARY KEY,
 invoice_id VARCHAR(20) NOT NULL UNIQUE,
 landlord_id BIGINT NOT NULL,
 received_on DATE NOT NULL,
 amount DECIMAL(18,2) NOT NULL,
 FOREIGN KEY(payment_report_id) REFERENCES payment_reports(payment_report_id),
 FOREIGN KEY(invoice_id) REFERENCES invoices(invoice_id),
 FOREIGN KEY(landlord_id) REFERENCES user(id),
 INDEX ix_receipt_owner_day(landlord_id,received_on)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS utility_disbursements (
 id VARCHAR(36) PRIMARY KEY,landlord_id BIGINT NOT NULL,bill_year INT NOT NULL,bill_month INT NOT NULL,
 utility_type VARCHAR(20) NOT NULL,amount DECIMAL(18,2) NOT NULL,paid_on DATE NOT NULL,
 provider VARCHAR(120) NOT NULL,reference VARCHAR(200) NOT NULL,
 UNIQUE KEY uq_utility_period(landlord_id,bill_year,bill_month,utility_type),
 FOREIGN KEY(landlord_id) REFERENCES user(id),INDEX ix_utility_owner_day(landlord_id,paid_on)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS landlord_expenses (
 id VARCHAR(36) PRIMARY KEY,landlord_id BIGINT NOT NULL,amount DECIMAL(18,2) NOT NULL,paid_on DATE NOT NULL,
 description VARCHAR(300) NOT NULL,reference VARCHAR(200) NOT NULL,
 UNIQUE KEY uq_expense_receipt(landlord_id,reference),
 FOREIGN KEY(landlord_id) REFERENCES user(id),INDEX ix_expense_owner_day(landlord_id,paid_on)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS room_listings (
 room_id VARCHAR(20) PRIMARY KEY,title VARCHAR(120) NOT NULL,description TEXT NOT NULL,address VARCHAR(250) NOT NULL,
 FOREIGN KEY(room_id) REFERENCES rooms(room_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS room_photos (
 id VARCHAR(36) PRIMARY KEY,room_id VARCHAR(20) NOT NULL,mime_type VARCHAR(30) NOT NULL,
 image_data LONGBLOB NOT NULL,thumbnail_data MEDIUMBLOB NOT NULL,
 FOREIGN KEY(room_id) REFERENCES rooms(room_id) ON DELETE CASCADE,INDEX ix_room_photos(room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE IF NOT EXISTS payment_report_routes (
 payment_report_id BIGINT PRIMARY KEY,landlord_id BIGINT NOT NULL,
 FOREIGN KEY(payment_report_id) REFERENCES payment_reports(payment_report_id),
 FOREIGN KEY(landlord_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
