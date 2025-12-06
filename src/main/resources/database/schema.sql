/* ===========================
Database Schema
    ===========================*/


CREATE TABLE IF NOT EXISTS jobTitles (
     id INTEGER PRIMARY KEY AUTOINCREMENT,
     name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS shifts (
     id INTEGER PRIMARY KEY AUTOINCREMENT,
     name VARCHAR(50) NOT NULL UNIQUE,
     startTime TIME NOT NULL,
     endTime TIME NOT NULL
);




CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    creationDate DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS personnel (
     id INTEGER PRIMARY KEY AUTOINCREMENT,
     fullName VARCHAR(100) NOT NULL,
     age INTEGER NOT NULL,
     phone VARCHAR(20) NOT NULL,
     email VARCHAR(100) NOT NULL UNIQUE,
     jobTitle INTEGER NOT NULL,
     hireDate DATE,
     salary DECIMAL(10, 2) DEFAULT 0.00,
     shift INTEGER NOT NULL,
     isActive BOOLEAN DEFAULT 1,
     address VARCHAR(255),
     emergencyContact VARCHAR(100),
     FOREIGN KEY (jobTitle) REFERENCES jobTitles(id),
     FOREIGN KEY (shift) REFERENCES shifts(id)
);









/* ===========================
    Default Data
    ===========================*/


-- Insert default admin user if not exists
INSERT OR IGNORE INTO users (name, email, password)
VALUES ('Administrator', 'admin@farm.ma', 'admin123')
ON CONFLICT(email) DO NOTHING;

-- Insert default job titles if not exists
INSERT OR IGNORE INTO jobTitles (name) VALUES ('tracker')
ON CONFLICT(name) DO NOTHING;
INSERT OR IGNORE INTO jobTitles (name) VALUES ('worker')
ON CONFLICT(name) DO NOTHING;

-- Insert default shifts if not exists
INSERT OR IGNORE INTO shifts (name, startTime, endTime)
VALUES ('morning', '06:00:00', '15:00:00')
ON CONFLICT(name) DO NOTHING;

INSERT OR IGNORE INTO shifts (name, startTime, endTime)
VALUES ('evening', '15:00:01', '00:00:00')
ON CONFLICT(name) DO NOTHING;

-- Insert default personnel if not exists
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('John Doe', 35, '0612345678', 'john.doe@farm.ma', 1, '2025-12-05', 5000.00, 1, 1, '123 Farm Road', 'Jane Doe 0698765432');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Jane Smith', 28, '0623456789', 'jane.smith@farm.ma', 2, '2025-12-05', 4000.00, 2, 1, '456 Farm Lane', 'John Smith 0687654321');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Maria Garcia', 31, '0645678901', 'maria.garcia@farm.ma', 2, '2025-12-05', 4200.00, 1, 1, '321 Valley Road', 'Carlos Garcia 0665432109');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Hassan Ibrahim', 38, '0656789012', 'hassan.ibrahim@farm.ma', 1, '2025-12-05', 5500.00, 2, 1, '654 Mountain View', 'Layla Ibrahim 0654321098');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Sophia Laurent', 26, '0667890123', 'sophia.laurent@farm.ma', 2, '2025-12-05', 3800.00, 1, 1, '987 Garden Path', 'Pierre Laurent 0643210987');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Mohammed Al-Rashid', 45, '0678901234', 'mohammed.rashid@farm.ma', 1, '2025-12-05', 6200.00, 1, 1, '147 Oak Street', 'Amira Al-Rashid 0632109876');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Elena Rodriguez', 33, '0689012345', 'elena.rodriguez@farm.ma', 2, '2025-12-05', 4100.00, 2, 1, '258 Pine Avenue', 'Diego Rodriguez 0621098765');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Khalid El-Mansouri', 50, '0690123456', 'khalid.mansouri@farm.ma', 1, '2025-12-05', 6500.00, 1, 1, '369 Elm Court', 'Zainab El-Mansouri 0610987654');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Lucia Moretti', 29, '0601234567', 'lucia.moretti@farm.ma', 2, '2025-12-05', 3900.00, 1, 1, '741 Birch Lane', 'Marco Moretti 0699876543');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Omar Saeed', 36, '0612345670', 'omar.saeed@farm.ma', 1, '2025-12-05', 5300.00, 2, 1, '852 Cedar Road', 'Noor Saeed 0688765432');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Francesca Rossi', 27, '0623456781', 'francesca.rossi@farm.ma', 2, '2025-12-05', 3700.00, 2, 1, '963 Maple Drive', 'Antonio Rossi 0677654321');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Youssef Benali', 44, '0634567892', 'youssef.benali@farm.ma', 1, '2025-12-05', 5800.00, 1, 1, '159 Spruce Way', 'Aya Benali 0666543210');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Isabella Torres', 32, '0645678903', 'isabella.torres@farm.ma', 2, '2025-12-05', 4300.00, 2, 1, '357 Willow Street', 'Roberto Torres 0655432109');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Karim Bouvier', 40, '0656789014', 'karim.bouvier@farm.ma', 1, '2025-12-05', 5600.00, 1, 0, '456 Ashwood Place', 'Maryam Bouvier 0644321098');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Giulia Marchetti', 25, '0667890125', 'giulia.marchetti@farm.ma', 2, '2025-12-05', 3600.00, 1, 1, '789 Cypress Circle', 'Alessandro Marchetti 0633210987');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Nabil Al-Hakim', 48, '0678901236', 'nabil.hakim@farm.ma', 1, '2025-12-05', 6400.00, 2, 1, '987 Juniper Road', 'Leila Al-Hakim 0622109876');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Claudia Barbosa', 30, '0689012347', 'claudia.barbosa@farm.ma', 2, '2025-12-05', 4000.00, 2, 1, '654 Hickory Lane', 'Rafael Barbosa 0611098765');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Samir El-Khawaja', 41, '0690123458', 'samir.khawaja@farm.ma', 1, '2025-12-05', 5700.00, 1, 1, '123 Ash Street', 'Rania El-Khawaja 0600987654');
INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
VALUES ('Valentina Esposito', 28, '0601234569', 'valentina.esposito@farm.ma', 2, '2025-12-05', 3850.00, 2, 1, '741 Walnut Avenue', 'Matteo Esposito 0699876541');
