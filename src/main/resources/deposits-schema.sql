CREATE TABLE IF NOT EXISTS landlord_bank_accounts (
 user_id BIGINT PRIMARY KEY,bank_name VARCHAR(80) NOT NULL,account_number VARCHAR(40) NOT NULL,account_holder VARCHAR(120) NOT NULL,
 webhook_key_hash VARCHAR(64) NULL,qr_image MEDIUMBLOB NULL,
 FOREIGN KEY(user_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS rental_deposits (
 request_id BIGINT PRIMARY KEY,landlord_id BIGINT NOT NULL,amount DECIMAL(18,2) NOT NULL,
 payment_code VARCHAR(30) NOT NULL UNIQUE,status VARCHAR(24) NOT NULL,
 bank_name VARCHAR(80) NULL,account_number VARCHAR(40) NULL,account_holder VARCHAR(120) NULL,paid_at DATETIME NULL,
 FOREIGN KEY(request_id) REFERENCES contract_requests(request_id),FOREIGN KEY(landlord_id) REFERENCES user(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS deposit_bank_events (
 landlord_id BIGINT NOT NULL,provider_id BIGINT NOT NULL,request_id BIGINT NULL,amount DECIMAL(18,2) NOT NULL,
 result VARCHAR(30) NOT NULL,received_at DATETIME NOT NULL,
 PRIMARY KEY(landlord_id,provider_id),FOREIGN KEY(landlord_id) REFERENCES user(id),FOREIGN KEY(request_id) REFERENCES rental_deposits(request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
