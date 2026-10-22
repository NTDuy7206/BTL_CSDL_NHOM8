CREATE TABLE IF NOT EXISTS `cleaning_supports` (
  `support_id` varchar(20) NOT NULL,
  `support_date` date DEFAULT NULL,
  `task_content` text DEFAULT NULL,
  `is_completed` tinyint(1) DEFAULT NULL,
  `room_id` varchar(20) DEFAULT NULL,
  `employee_id` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`support_id`),
  KEY `fk_clean_room` (`room_id`),
  KEY `fk_clean_employee` (`employee_id`),
  CONSTRAINT `fk_clean_employee` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`employee_id`),
  CONSTRAINT `fk_clean_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `contracts` (
  `contract_id` varchar(20) NOT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `deposit` decimal(18,2) DEFAULT NULL,
  `room_id` varchar(20) DEFAULT NULL,
  `tenant_id` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`contract_id`),
  KEY `fk_contract_room` (`room_id`),
  KEY `fk_contract_tenant` (`tenant_id`),
  CONSTRAINT `fk_contract_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`),
  CONSTRAINT `fk_contract_tenant` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `employees` (
  `employee_id` varchar(20) NOT NULL,
  `cccd` varchar(20) DEFAULT NULL,
  `full_name` varchar(50) DEFAULT NULL,
  `position` varchar(50) DEFAULT NULL,
  `base_salary` decimal(18,2) DEFAULT NULL,
  `seniority` int(11) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `phone_number` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`employee_id`),
  UNIQUE KEY `cccd` (`cccd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `incident_resolutions` (
  `resolution_id` varchar(20) NOT NULL,
  `resolution_date` date DEFAULT NULL,
  `result` text DEFAULT NULL,
  `is_completed` tinyint(1) DEFAULT NULL,
  `incident_id` varchar(20) DEFAULT NULL,
  `employee_id` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`resolution_id`),
  KEY `fk_res_incident` (`incident_id`),
  KEY `fk_res_employee` (`employee_id`),
  CONSTRAINT `fk_res_employee` FOREIGN KEY (`employee_id`) REFERENCES `employees` (`employee_id`),
  CONSTRAINT `fk_res_incident` FOREIGN KEY (`incident_id`) REFERENCES `incidents` (`incident_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `incidents` (
  `incident_id` varchar(20) NOT NULL,
  `incident_content` text DEFAULT NULL,
  `report_date` date DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `room_id` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`incident_id`),
  KEY `fk_incident_room` (`room_id`),
  CONSTRAINT `fk_incident_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `invoices` (
  `invoice_id` varchar(20) NOT NULL,
  `month` int(11) DEFAULT NULL,
  `year` int(11) DEFAULT NULL,
  `rent_amount` decimal(18,2) DEFAULT NULL,
  `electricity_amount` decimal(18,2) DEFAULT NULL,
  `water_amount` decimal(18,2) DEFAULT NULL,
  `fine_amount` decimal(18,2) DEFAULT NULL,
  `total_amount` decimal(18,2) DEFAULT NULL,
  `contract_id` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`invoice_id`),
  KEY `fk_invoice_contract` (`contract_id`),
  CONSTRAINT `fk_invoice_contract` FOREIGN KEY (`contract_id`) REFERENCES `contracts` (`contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `rooms` (
  `room_id` varchar(20) NOT NULL,
  `floor` int(11) DEFAULT NULL,
  `area` float DEFAULT NULL,
  `room_type` varchar(50) DEFAULT NULL,
  `status` varchar(50) DEFAULT NULL,
  `base_price` decimal(18,2) DEFAULT NULL,
  PRIMARY KEY (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `rules` (
  `rule_id` varchar(20) NOT NULL,
  `violation_name` varchar(100) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `prescribed_fine` decimal(18,2) DEFAULT NULL,
  PRIMARY KEY (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `tenants` (
  `tenant_id` varchar(20) NOT NULL,
  `cccd` varchar(20) DEFAULT NULL,
  `full_name` varchar(50) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `permanent_address` varchar(200) DEFAULT NULL,
  `phone_number` varchar(10) DEFAULT NULL,
  PRIMARY KEY (`tenant_id`),
  UNIQUE KEY `cccd` (`cccd`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `password` varchar(255) NOT NULL,
  `role` tinyint(4) NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `user_profiles` (
  `user_id` bigint(20) NOT NULL,
  `display_name` varchar(120) DEFAULT NULL,
  `email` varchar(190) DEFAULT NULL,
  `phone` varchar(30) DEFAULT NULL,
  `avatar_object_key` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_user_profiles_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
CREATE TABLE IF NOT EXISTS `utility_readings` (
  `reading_id` varchar(20) NOT NULL,
  `month` int(11) DEFAULT NULL,
  `year` int(11) DEFAULT NULL,
  `electricity_price` decimal(18,2) DEFAULT NULL,
  `water_price` decimal(18,2) DEFAULT NULL,
  `electricity_start_index` int(11) DEFAULT NULL,
  `electricity_end_index` int(11) DEFAULT NULL,
  `water_start_index` int(11) DEFAULT NULL,
  `water_end_index` int(11) DEFAULT NULL,
  `room_id` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`reading_id`),
  KEY `fk_reading_room` (`room_id`),
  CONSTRAINT `fk_reading_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
CREATE TABLE IF NOT EXISTS `violations` (
  `violation_id` varchar(20) NOT NULL,
  `violation_name` varchar(100) DEFAULT NULL,
  `violation_date` date DEFAULT NULL,
  `room_id` varchar(20) DEFAULT NULL,
  `rule_id` varchar(20) DEFAULT NULL,
  `detailed_description` text DEFAULT NULL,
  `actual_fine` decimal(18,2) DEFAULT NULL,
  PRIMARY KEY (`violation_id`),
  KEY `fk_violation_room` (`room_id`),
  KEY `fk_violation_rule` (`rule_id`),
  CONSTRAINT `fk_violation_room` FOREIGN KEY (`room_id`) REFERENCES `rooms` (`room_id`),
  CONSTRAINT `fk_violation_rule` FOREIGN KEY (`rule_id`) REFERENCES `rules` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
