/* ===========================
Database Schema
    ===========================*/

-- Enable foreign key constraints (must be set per connection in SQLite)
DROP TABLE IF EXISTS houses;
DROP TABLE IF EXISTS egg_production;
DROP TABLE IF EXISTS chickens;
DROP TABLE IF EXISTS mortality;


PRAGMA foreign_keys = ON;

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


-- ============================================================
-- Houses (Chicken Bays) Database Schema for SQLite
-- ============================================================
-- This script creates the houses table for tracking chicken
-- housing, capacity, and health status.
-- ============================================================

-- Drop existing table if exists (for clean reinstall)
DROP TABLE IF EXISTS houses;


-- ============================================================
-- Main Table: houses
-- ============================================================
-- Stores information about chicken houses/bays
-- Each house has a specific type, capacity, and health status
-- ============================================================

CREATE TABLE IF NOT EXISTS houses (
    -- Primary Key (SQLite uses INTEGER for autoincrement)
                                      id INTEGER PRIMARY KEY AUTOINCREMENT,

    -- House identifier (e.g., H1, H2, H3, H4)
                                      name VARCHAR(10) NOT NULL UNIQUE,

    -- Type of houses
                                      type VARCHAR(50) NOT NULL,

    -- Current number of chickens in the house
                                      chickenCount INTEGER DEFAULT 0
                                          CHECK (chickenCount >= 0),

    -- Maximum capacity for chickens
                                      capacity INTEGER NOT NULL
                                          CHECK (capacity > 0),

    -- Health status of the flock
                                      healthStatus VARCHAR(50) DEFAULT 'Good'
                                          CHECK (healthStatus IN ('Good', 'Fair', 'Poor')),

    -- Date of last cleaning
                                      lastCleaningDate DATE,

    -- Creation date of the house record
                                      creationDate DATE DEFAULT CURRENT_DATE,

    -- Timestamps for tracking modifications
                                      created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Indexes for Performance
-- ============================================================

-- Index on house type for filtering by type
CREATE INDEX idx_houses_type
    ON houses(type);

-- Index on health status for filtering by status
CREATE INDEX idx_houses_health
    ON houses(healthStatus);

-- ============================================================
-- Triggers for SQLite
-- ============================================================

-- Trigger to update the updated_at timestamp on UPDATE
CREATE TRIGGER trg_houses_updated_at
    AFTER UPDATE ON houses
    FOR EACH ROW
BEGIN
    UPDATE houses
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

-- ============================================================
-- Sample Data: Houses
-- ============================================================

INSERT INTO houses (name, type, chickenCount, capacity, healthStatus, lastCleaningDate, creationDate)
VALUES
    ('H1', 'Day-old', 1200, 1200, 'Good', '2025-12-05', '2025-01-01'),
    ('H2', 'Egg Layer', 850, 1000, 'Good', '2025-12-04', '2025-02-15'),
    ('H3', 'Meat Female', 780, 1000, 'Good', '2025-12-03', '2025-02-20'),
    ('H4', 'Meat Male', 520, 600, 'Fair', '2025-12-02', '2025-03-10');


-- ============================================================
-- Egg Production Database Schema for SQLite
-- ============================================================
-- This script creates the egg_production table and related
-- database objects for tracking daily egg collection data.
-- ============================================================

-- Drop existing table if exists (for clean reinstall)
DROP TABLE IF EXISTS egg_production;

-- ============================================================
-- Main Table: egg_production
-- ============================================================
-- Stores daily egg production records for each house
-- One record per house per day
-- ============================================================

CREATE TABLE egg_production (
    -- Primary Key (SQLite uses INTEGER for autoincrement)
                                id INTEGER PRIMARY KEY AUTOINCREMENT,

    -- Foreign Key to houses table
                                houseId INTEGER NOT NULL,

    -- Production date (stored as TEXT in ISO format: YYYY-MM-DD)
                                productionDate TEXT NOT NULL,

    -- Egg metrics
                                eggsCollected INTEGER NOT NULL DEFAULT 0
                                    CHECK (eggsCollected >= 0),
                                crackedEggs INTEGER NOT NULL DEFAULT 0
                                    CHECK (crackedEggs >= 0),
                                goodEggs INTEGER NOT NULL DEFAULT 0
                                    CHECK (goodEggs >= 0),

    -- Mortality tracking
                                deadChickens INTEGER NOT NULL DEFAULT 0
                                    CHECK (deadChickens >= 0),

    -- Metadata
                                collectedBy TEXT,
                                notes TEXT,

    -- Timestamps (SQLite uses TEXT for datetime)
                                created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                                updated_at TEXT DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key Constraint
                                FOREIGN KEY (houseId) REFERENCES houses(id)
                                    ON DELETE CASCADE
                                    ON UPDATE CASCADE,

    -- Unique Constraint: One record per house per day
                                UNIQUE (houseId, productionDate),

    -- Business Logic Constraint: Cracked eggs cannot exceed total collected
                                CHECK (crackedEggs <= eggsCollected),

    -- Business Logic Constraint: Good eggs should equal collected minus cracked
                                CHECK (goodEggs = eggsCollected - crackedEggs)
);

-- ============================================================
-- Indexes for Performance
-- ============================================================

-- Index on houseId for filtering by house
CREATE INDEX idx_egg_production_house
    ON egg_production(houseId);

-- Index on productionDate for date-based queries
CREATE INDEX idx_egg_production_date
    ON egg_production(productionDate);

-- Composite index for house and date range queries
CREATE INDEX idx_egg_production_house_date
    ON egg_production(houseId, productionDate);

-- Index on collectedBy for worker performance tracking
CREATE INDEX idx_egg_production_collector
    ON egg_production(collectedBy);

-- ============================================================
-- Triggers for SQLite
-- ============================================================

-- Trigger to update the updated_at timestamp on UPDATE
CREATE TRIGGER trg_egg_production_updated_at
    AFTER UPDATE ON egg_production
    FOR EACH ROW
BEGIN
    UPDATE egg_production
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

-- Trigger: Validate and auto-calculate before insert
CREATE TRIGGER trg_egg_production_before_insert
    BEFORE INSERT ON egg_production
    FOR EACH ROW
BEGIN
    -- Validate that cracked eggs don't exceed collected
    SELECT CASE
               WHEN NEW.crackedEggs > NEW.eggsCollected THEN
                   RAISE(ABORT, 'Cracked eggs cannot exceed total eggs collected')
               WHEN NEW.eggsCollected < 0 OR NEW.crackedEggs < 0 OR NEW.deadChickens < 0 THEN
                   RAISE(ABORT, 'Egg counts and deaths must be non-negative')
               END;
END;

-- Trigger: Validate before update
CREATE TRIGGER trg_egg_production_before_update
    BEFORE UPDATE ON egg_production
    FOR EACH ROW
BEGIN
    -- Validate that cracked eggs don't exceed collected
    SELECT CASE
               WHEN NEW.crackedEggs > NEW.eggsCollected THEN
                   RAISE(ABORT, 'Cracked eggs cannot exceed total eggs collected')
               WHEN NEW.eggsCollected < 0 OR NEW.crackedEggs < 0 OR NEW.deadChickens < 0 THEN
                   RAISE(ABORT, 'Egg counts and deaths must be non-negative')
               END;
END;

-- ============================================================
-- Sample Data (7 days for H2 and H3)
-- ============================================================

INSERT INTO egg_production
(houseId, productionDate, eggsCollected, crackedEggs, goodEggs,
 deadChickens, collectedBy, notes)
VALUES
    -- H2 (Layers) - 7 days of production
    (2, '2025-12-01', 4500, 120, 4380, 2, 'Ahmed', 'Normal production day'),
    (2, '2025-12-02', 4620, 95, 4525, 1, 'Ahmed', 'Slightly higher production'),
    (2, '2025-12-03', 4480, 150, 4330, 3, 'Fatima', 'More cracked eggs, check nesting boxes'),
    (2, '2025-12-04', 4550, 110, 4440, 0, 'Ahmed', 'Good day, no deaths'),
    (2, '2025-12-05', 4590, 105, 4485, 2, 'Fatima', 'Normal operations'),
    (2, '2025-12-06', 4520, 125, 4395, 1, 'Ahmed', 'Weekend collection'),
    (2, '2025-12-07', 4470, 130, 4340, 2, 'Fatima', 'Rain affected collection time'),

    -- H3 (Layers) - 7 days of production
    (3, '2025-12-01', 3200, 85, 3115, 1, 'Hassan', 'New batch starting production'),
    (3, '2025-12-02', 3350, 90, 3260, 0, 'Hassan', 'Production increasing'),
    (3, '2025-12-03', 3420, 78, 3342, 2, 'Fatima', 'Good quality eggs'),
    (3, '2025-12-04', 3380, 95, 3285, 1, 'Hassan', 'Normal day'),
    (3, '2025-12-05', 3450, 88, 3362, 0, 'Hassan', 'Excellent collection'),
    (3, '2025-12-06', 3390, 102, 3288, 3, 'Fatima', 'Higher mortality, monitor health'),
    (3, '2025-12-07', 3410, 92, 3318, 1, 'Hassan', 'Back to normal');



-- ============================================================
-- Create chickens table
-- ============================================================
CREATE TABLE IF NOT EXISTS chickens (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        houseId INTEGER NOT NULL,
                                        batchNumber TEXT NOT NULL,
                                        quantity INTEGER NOT NULL,
                                        arrivalDate TEXT NOT NULL,
                                        ageInDays INTEGER NOT NULL,
                                        gender TEXT NOT NULL,
                                        healthStatus TEXT NOT NULL,
                                        averageWeight REAL,
                                        nextTransferDate TEXT,
                                        created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                                        FOREIGN KEY (houseId) REFERENCES houses(id) ON DELETE CASCADE
);

-- ============================================================
-- Create mortality table
-- ============================================================
CREATE TABLE IF NOT EXISTS mortality (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         houseId INTEGER NOT NULL,
                                         deathDate TEXT NOT NULL,
                                         count INTEGER NOT NULL,
                                         cause TEXT NOT NULL,
                                         symptoms TEXT,
                                         isOutbreak INTEGER DEFAULT 0,
                                         recordedBy TEXT NOT NULL,
                                         notes TEXT,
                                         recorded_at TEXT DEFAULT CURRENT_TIMESTAMP,
                                         FOREIGN KEY (houseId) REFERENCES houses(id) ON DELETE CASCADE
);

-- ============================================================
-- Indexes for Performance
-- ============================================================
-- Houses indexes
CREATE INDEX IF NOT EXISTS idx_houses_type ON houses(type);
CREATE INDEX IF NOT EXISTS idx_houses_health ON houses(healthStatus);

-- Chickens indexes
CREATE INDEX IF NOT EXISTS idx_chickens_house ON chickens(houseId);
CREATE INDEX IF NOT EXISTS idx_chickens_arrival ON chickens(arrivalDate);
CREATE INDEX IF NOT EXISTS idx_chickens_transfer ON chickens(nextTransferDate);
CREATE INDEX IF NOT EXISTS idx_chickens_batch ON chickens(batchNumber);

-- Mortality indexes
CREATE INDEX IF NOT EXISTS idx_mortality_house_date ON mortality(houseId, deathDate);
CREATE INDEX IF NOT EXISTS idx_mortality_date ON mortality(deathDate);
CREATE INDEX IF NOT EXISTS idx_mortality_cause ON mortality(cause);
CREATE INDEX IF NOT EXISTS idx_mortality_outbreak ON mortality(isOutbreak);

-- ============================================================
-- Triggers for SQLite
-- ============================================================
-- Trigger for houses updated_at
CREATE TRIGGER IF NOT EXISTS trg_houses_updated_at
    AFTER UPDATE ON houses
    FOR EACH ROW
BEGIN
    UPDATE houses
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

-- Trigger to update house chickenCount when chickens are added/deleted
CREATE TRIGGER IF NOT EXISTS trg_chickens_after_insert
    AFTER INSERT ON chickens
    FOR EACH ROW
BEGIN
    UPDATE houses
    SET chickenCount = chickenCount + NEW.quantity
    WHERE id = NEW.houseId;
END;

CREATE TRIGGER IF NOT EXISTS trg_chickens_after_delete
    AFTER DELETE ON chickens
    FOR EACH ROW
BEGIN
    UPDATE houses
    SET chickenCount = chickenCount - OLD.quantity
    WHERE id = OLD.houseId;
END;

CREATE TRIGGER IF NOT EXISTS trg_chickens_after_update
    AFTER UPDATE ON chickens
    FOR EACH ROW
BEGIN
    UPDATE houses
    SET chickenCount = chickenCount + (NEW.quantity - OLD.quantity)
    WHERE id = NEW.houseId;
END;

-- Trigger to update house chickenCount when mortality is recorded
CREATE TRIGGER IF NOT EXISTS trg_mortality_after_insert
    AFTER INSERT ON mortality
    FOR EACH ROW
BEGIN
    UPDATE houses
    SET chickenCount = chickenCount - NEW.count
    WHERE id = NEW.houseId;
END;

-- ============================================================
-- Sample Data
-- ============================================================
-- Insert houses (using INSERT OR IGNORE to avoid duplicates)
INSERT OR IGNORE INTO houses (name, type, chickenCount, capacity, healthStatus, lastCleaningDate, creationDate) VALUES
                                                                                                                    ('H1', 'Day-old', 1200, 1200, 'Good', '2025-12-05', '2025-01-01'),
                                                                                                                    ('H2', 'Egg Layer', 850, 1000, 'Good', '2025-12-04', '2025-02-15'),
                                                                                                                    ('H3', 'Meat Female', 780, 1000, 'Good', '2025-12-03', '2025-02-20'),
                                                                                                                    ('H4', 'Meat Male', 520, 600, 'Fair', '2025-12-02', '2025-03-10');

-- Insert sample chicken batches
INSERT OR IGNORE INTO chickens (houseId, batchNumber, quantity, arrivalDate, ageInDays, gender, healthStatus, averageWeight, nextTransferDate) VALUES
-- House 1 (H1) - Day-old
(1, 'BATCH-2023-001', 500, '2023-11-24', 45, 'Mixed', 'Healthy', 1.8, '2024-01-15'),
(1, 'BATCH-2023-002', 300, '2023-12-09', 30, 'Female', 'Growing', 1.5, '2024-01-30'),
(1, 'BATCH-2023-003', 450, '2023-10-15', 75, 'Male', 'Mature', 2.3, '2024-01-10'),

-- House 2 (H2) - Egg Layer
(2, 'BATCH-2023-004', 600, '2023-11-19', 50, 'Mixed', 'Healthy', 1.9, '2024-02-01'),
(2, 'BATCH-2023-005', 350, '2023-12-14', 25, 'Female', 'Growing', 1.4, '2024-02-15'),
(2, 'BATCH-2023-006', 400, '2023-10-04', 70, 'Male', 'Mature', 2.4, '2024-01-25'),

-- House 3 (H3) - Meat Female
(3, 'BATCH-2023-007', 550, '2023-11-14', 55, 'Mixed', 'Healthy', 2.0, '2024-02-10'),
(3, 'BATCH-2023-008', 320, '2023-12-19', 20, 'Female', 'Growing', 1.3, '2024-02-28'),
(3, 'BATCH-2023-009', 480, '2023-10-29', 65, 'Male', 'Mature', 2.2, '2024-01-20'),

-- House 4 (H4) - Meat Male
(4, 'BATCH-2023-010', 520, '2023-11-09', 60, 'Mixed', 'Healthy', 2.1, '2024-02-05'),
(4, 'BATCH-2023-011', 280, '2023-12-24', 15, 'Female', 'Growing', 1.2, '2024-03-05'),
(4, 'BATCH-2023-012', 420, '2023-11-29', 40, 'Male', 'Mature', 2.1, '2024-01-30');

-- Insert sample mortality records
INSERT OR IGNORE INTO mortality (houseId, deathDate, count, cause, symptoms, isOutbreak, recordedBy, notes) VALUES
-- House 1
(1, date('now'), 2, 'Natural Causes', 'No symptoms', 0, 'John Smith', 'Found in morning check'),
(1, date('now', '-1 day'), 3, 'Disease', 'Lethargy, loss of appetite', 1, 'John Smith', 'Quarantine area affected'),
(1, date('now', '-3 days'), 1, 'Natural Causes', 'No symptoms', 0, 'John Smith', NULL),
(1, date('now', '-5 days'), 2, 'Accident', 'Entangled in wires', 0, 'John Smith', 'Clean up wiring'),
(1, date('now', '-7 days'), 4, 'Disease', 'Neurological symptoms', 1, 'John Smith', 'Major outbreak, immediate action taken'),

-- House 2
(2, date('now'), 1, 'Injury', 'Broken wing', 0, 'Jane Doe', 'Predator attack'),
(2, date('now', '-2 days'), 2, 'Accident', 'Crushed by equipment', 0, 'Jane Doe', 'Equipment maintenance needed'),
(2, date('now', '-4 days'), 3, 'Disease', 'Diarrhea, dehydration', 1, 'Jane Doe', 'Water quality check needed'),
(2, date('now', '-5 days'), 2, 'Disease', 'Swollen joints', 1, 'Jane Doe', 'Veterinary check scheduled'),
(2, date('now', '-7 days'), 1, 'Natural Causes', 'No symptoms', 0, 'Jane Doe', NULL),

-- House 3
(3, date('now', '-1 day'), 1, 'Natural Causes', 'Old age', 0, 'Mike Brown', NULL),
(3, date('now', '-3 days'), 2, 'Injury', 'Fighting injuries', 0, 'Mike Brown', 'Separate aggressive birds'),
(3, date('now', '-6 days'), 1, 'Natural Causes', 'No symptoms', 0, 'Mike Brown', NULL),

-- House 4
(4, date('now', '-2 days'), 4, 'Disease', 'Respiratory issues', 1, 'Sarah Johnson', 'Outbreak suspected'),
(4, date('now', '-4 days'), 1, 'Natural Causes', 'No symptoms', 0, 'Sarah Johnson', NULL),
(4, date('now', '-6 days'), 3, 'Injury', 'Foot injuries', 0, 'Sarah Johnson', 'Check flooring conditions');

-- ============================================================
-- Feed Table
-- ============================================================
-- Stores feed inventory for different chicken types
-- ============================================================

DROP TABLE IF EXISTS feed;

CREATE TABLE IF NOT EXISTS feed (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    quantityKg REAL NOT NULL DEFAULT 0,
    pricePerKg REAL NOT NULL DEFAULT 0,
    supplier VARCHAR(100),
    lastRestockDate DATE,
    expiryDate DATE,
    minStockLevel REAL NOT NULL DEFAULT 100,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- Index for feed queries
CREATE INDEX IF NOT EXISTS idx_feed_type ON feed(type);
CREATE INDEX IF NOT EXISTS idx_feed_expiry ON feed(expiryDate);

-- Trigger to update the updated_at timestamp on UPDATE
CREATE TRIGGER IF NOT EXISTS trg_feed_updated_at
    AFTER UPDATE ON feed
    FOR EACH ROW
BEGIN
    UPDATE feed
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

-- Sample Feed Data
INSERT INTO feed (name, type, quantityKg, pricePerKg, supplier, lastRestockDate, expiryDate, minStockLevel) VALUES
    ('Starter Crumbs', 'Day-old', 500.0, 12.50, 'AgriSupply Morocco', '2025-01-10', '2025-07-10', 200.0),
    ('Chick Starter Mash', 'Day-old', 350.0, 11.00, 'FeedCorp International', '2025-01-08', '2025-06-08', 150.0),
    ('Layer Pellets Premium', 'Layer', 800.0, 15.00, 'AgriSupply Morocco', '2025-01-12', '2025-08-12', 300.0),
    ('Layer Mash Standard', 'Layer', 120.0, 13.50, 'Local Farm Supplies', '2024-12-20', '2025-05-20', 200.0),
    ('Broiler Grower Feed', 'Meat Growth', 650.0, 14.00, 'FeedCorp International', '2025-01-05', '2025-06-05', 250.0),
    ('Meat Finisher Pellets', 'Meat Growth', 450.0, 16.00, 'AgriSupply Morocco', '2025-01-11', '2025-07-11', 200.0),
    ('Broiler Starter Crumbs', 'Meat Growth', 75.0, 13.00, 'Local Farm Supplies', '2024-12-15', '2025-03-15', 100.0),
    ('Organic Layer Feed', 'Layer', 200.0, 22.00, 'Green Organic Feeds', '2025-01-09', '2025-04-09', 100.0);

-- ============================================================
-- Medications Table
-- ============================================================
-- Stores medication inventory for poultry health
-- ============================================================

DROP TABLE IF EXISTS medications;

CREATE TABLE IF NOT EXISTS medications (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    unit VARCHAR(20) NOT NULL,
    pricePerUnit REAL NOT NULL DEFAULT 0,
    supplier VARCHAR(100),
    purchaseDate DATE,
    expiryDate DATE,
    minStockLevel INTEGER NOT NULL DEFAULT 10,
    usage TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
);

-- Index for medication queries
CREATE INDEX IF NOT EXISTS idx_medications_type ON medications(type);
CREATE INDEX IF NOT EXISTS idx_medications_expiry ON medications(expiryDate);

-- Trigger to update the updated_at timestamp on UPDATE
CREATE TRIGGER IF NOT EXISTS trg_medications_updated_at
    AFTER UPDATE ON medications
    FOR EACH ROW
BEGIN
    UPDATE medications
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

-- Sample Medications Data
INSERT INTO medications (name, type, quantity, unit, pricePerUnit, supplier, purchaseDate, expiryDate, minStockLevel, usage) VALUES
    ('Newcastle Disease Vaccine', 'Vaccine', 500, 'doses', 2.50, 'VetPharma Morocco', '2025-01-05', '2026-01-05', 200, 'Administer to chicks at day 7 and day 21'),
    ('Infectious Bronchitis Vaccine', 'Vaccine', 300, 'doses', 3.00, 'VetPharma Morocco', '2025-01-03', '2025-12-03', 150, 'Spray or drinking water at day 1'),
    ('Gumboro Vaccine', 'Vaccine', 50, 'doses', 2.75, 'AgriVet Supplies', '2024-12-20', '2025-06-20', 100, 'Drinking water at day 14 and 21'),
    ('Amoxicillin 10%', 'Antibiotic', 25, 'bottles', 45.00, 'VetPharma Morocco', '2025-01-08', '2026-06-08', 10, '1g per liter of drinking water for 5 days'),
    ('Enrofloxacin', 'Antibiotic', 8, 'bottles', 55.00, 'MedVet International', '2024-11-15', '2025-11-15', 10, '0.5ml per liter for respiratory infections'),
    ('Vitamin AD3E', 'Supplement', 40, 'bottles', 35.00, 'AgriVet Supplies', '2025-01-10', '2026-07-10', 15, '1ml per liter during stress periods'),
    ('Electrolyte Powder', 'Supplement', 60, 'sachets', 8.00, 'Local Farm Supplies', '2025-01-12', '2026-01-12', 20, '1 sachet per 20 liters during heat stress'),
    ('Ivermectin', 'Antiparasitic', 15, 'bottles', 65.00, 'VetPharma Morocco', '2024-12-28', '2025-12-28', 10, 'External parasite treatment, use as directed'),
    ('Cocci-Stop', 'Antiparasitic', 5, 'bottles', 48.00, 'MedVet International', '2024-10-15', '2025-04-15', 10, 'Coccidiosis prevention, 1ml per 2 liters'),
    ('Calcium Supplement', 'Supplement', 80, 'kg', 12.00, 'Local Farm Supplies', '2025-01-06', '2026-06-06', 25, 'Mix with feed at 2% for laying hens');

-- ============================================================
-- Equipment Table
-- ============================================================
-- Stores farm equipment inventory and maintenance records
-- ============================================================

DROP TABLE IF EXISTS equipment;

CREATE TABLE IF NOT EXISTS equipment (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'Good',
    purchaseDate DATE,
    purchasePrice REAL NOT NULL DEFAULT 0,
    lastMaintenanceDate DATE,
    nextMaintenanceDate DATE,
    location VARCHAR(100),
    notes TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    CHECK (status IN ('Good', 'Fair', 'Broken'))
);

-- Index for equipment queries
CREATE INDEX IF NOT EXISTS idx_equipment_status ON equipment(status);
CREATE INDEX IF NOT EXISTS idx_equipment_category ON equipment(category);
CREATE INDEX IF NOT EXISTS idx_equipment_maintenance ON equipment(nextMaintenanceDate);

-- Trigger to update the updated_at timestamp on UPDATE
CREATE TRIGGER IF NOT EXISTS trg_equipment_updated_at
    AFTER UPDATE ON equipment
    FOR EACH ROW
BEGIN
    UPDATE equipment
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

-- Sample Equipment Data
INSERT INTO equipment (name, category, quantity, status, purchaseDate, purchasePrice, lastMaintenanceDate, nextMaintenanceDate, location, notes) VALUES
    ('Automatic Feeder System', 'Feeding', 4, 'Good', '2024-03-15', 2500.00, '2025-01-05', '2025-04-05', 'House H1-H4', 'Programmable feeding schedule'),
    ('Water Nipple Drinkers', 'Feeding', 200, 'Good', '2024-06-20', 1200.00, '2025-01-10', '2025-07-10', 'All Houses', 'Stainless steel, leak-proof'),
    ('Feed Storage Bins', 'Feeding', 6, 'Fair', '2023-08-10', 800.00, '2024-12-15', '2025-03-15', 'Feed Storage Area', 'Some rust spots, schedule repainting'),
    ('Egg Collection Belts', 'Collection', 2, 'Good', '2024-05-22', 3500.00, '2025-01-08', '2025-05-08', 'House H2, H3', 'Automatic egg conveyor system'),
    ('Egg Grading Machine', 'Collection', 1, 'Good', '2024-07-18', 4200.00, '2025-01-02', '2025-04-02', 'Processing Room', 'Grades by weight and checks quality'),
    ('Pressure Washer', 'Cleaning', 2, 'Fair', '2023-02-28', 650.00, '2024-11-20', '2025-02-20', 'Equipment Shed', 'One unit needs new hose'),
    ('Ventilation Fans', 'Climate', 12, 'Good', '2024-01-10', 1800.00, '2025-01-12', '2025-07-12', 'All Houses', '48-inch industrial fans'),
    ('Heating Lamps', 'Climate', 20, 'Good', '2024-09-05', 400.00, '2024-12-20', '2025-06-20', 'House H1', 'Infrared brooders for chicks'),
    ('Generator', 'Other', 1, 'Broken', '2022-11-15', 5500.00, '2024-08-10', '2024-11-10', 'Power Room', 'Motor needs replacement, ordered parts'),
    ('Digital Scale', 'Other', 3, 'Good', '2024-04-12', 250.00, '2025-01-01', '2025-07-01', 'Processing Room', 'Calibrated monthly'),
    ('Vaccination Sprayer', 'Medical', 2, 'Good', '2024-08-25', 320.00, '2025-01-06', '2025-04-06', 'Medical Supply Room', 'Fine mist for vaccine delivery'),
    ('Egg Washer', 'Cleaning', 1, 'Broken', '2023-05-14', 1800.00, '2024-10-05', '2025-01-05', 'Processing Room', 'Pump malfunction, repair scheduled'),
    ('Manure Spreader', 'Cleaning', 1, 'Fair', '2022-07-20', 2200.00, '2024-09-15', '2025-03-15', 'Outside Storage', 'Tires need replacement soon');

-- ============================================================
-- Tasks Table
-- ============================================================
-- Stores daily tasks for farm workers
-- ============================================================

DROP TABLE IF EXISTS tasks;

CREATE TABLE IF NOT EXISTS tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    description TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Pending',
    dueDate DATE,
    completedAt TEXT,
    assignedTo VARCHAR(100),
    houseId INTEGER,
    category VARCHAR(50),
    crackedEggs INTEGER DEFAULT 0,
    notes TEXT,
    priority VARCHAR(20) DEFAULT 'Medium',
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    CHECK (status IN ('Done', 'Pending', 'Missed')),
    CHECK (priority IN ('High', 'Medium', 'Low')),
    FOREIGN KEY (houseId) REFERENCES houses(id) ON DELETE SET NULL
);

-- Index for task queries
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_dueDate ON tasks(dueDate);
CREATE INDEX IF NOT EXISTS idx_tasks_assignedTo ON tasks(assignedTo);
CREATE INDEX IF NOT EXISTS idx_tasks_category ON tasks(category);
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority);

-- Trigger to update the updated_at timestamp on UPDATE
CREATE TRIGGER IF NOT EXISTS trg_tasks_updated_at
    AFTER UPDATE ON tasks
    FOR EACH ROW
BEGIN
    UPDATE tasks
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

-- Sample Tasks Data
INSERT INTO tasks (description, status, dueDate, completedAt, assignedTo, houseId, category, crackedEggs, notes, priority) VALUES
    ('Morning egg collection - House H2', 'Done', date('now'), datetime('now', '-2 hours'), 'Ahmed', 2, 'Collection', 15, 'Normal collection, some soft shells noted', 'High'),
    ('Morning egg collection - House H3', 'Done', date('now'), datetime('now', '-1 hours'), 'Fatima', 3, 'Collection', 8, 'Good quality eggs today', 'High'),
    ('Clean water drinkers - All houses', 'Pending', date('now'), NULL, 'Hassan', NULL, 'Cleaning', 0, 'Weekly deep cleaning scheduled', 'Medium'),
    ('Feed restocking - House H1', 'Pending', date('now'), NULL, 'John Doe', 1, 'Feeding', 0, 'Day-old starter feed running low', 'High'),
    ('Vaccination - Newcastle booster H1', 'Pending', date('now', '+1 day'), NULL, 'Jane Smith', 1, 'Medical', 0, 'Day 21 booster for current batch', 'High'),
    ('Equipment maintenance check', 'Pending', date('now', '+2 days'), NULL, 'Maria Garcia', NULL, 'Other', 0, 'Monthly maintenance inspection', 'Medium'),
    ('Afternoon egg collection - House H2', 'Missed', date('now', '-1 day'), NULL, 'Ahmed', 2, 'Collection', 0, 'Worker was absent, needs rescheduling', 'High'),
    ('Clean brooder area - House H1', 'Done', date('now', '-1 day'), datetime('now', '-1 day', '+8 hours'), 'Hassan', 1, 'Cleaning', 0, 'Full sanitization completed', 'Medium'),
    ('Check ventilation fans', 'Done', date('now', '-2 days'), datetime('now', '-2 days', '+10 hours'), 'John Doe', NULL, 'Other', 0, 'All fans operational', 'Low'),
    ('Feed inventory count', 'Done', date('now', '-3 days'), datetime('now', '-3 days', '+6 hours'), 'Jane Smith', NULL, 'Feeding', 0, 'Inventory updated in system', 'Medium'),
    ('Repair broken egg washer', 'Pending', date('now', '+3 days'), NULL, 'External Technician', NULL, 'Other', 0, 'Parts ordered, technician scheduled', 'High'),
    ('Morning health inspection - House H4', 'Missed', date('now', '-2 days'), NULL, 'Fatima', 4, 'Medical', 0, 'Rescheduled due to weather', 'High'),
    ('Order new feed supplies', 'Done', date('now', '-4 days'), datetime('now', '-4 days', '+9 hours'), 'Maria Garcia', NULL, 'Feeding', 0, 'Order placed with AgriSupply', 'Medium'),
    ('Update mortality records', 'Pending', date('now'), NULL, 'Hassan Ibrahim', NULL, 'Other', 0, 'Weekly mortality report due', 'Low'),
    ('Evening feeding - All houses', 'Pending', date('now'), NULL, 'Sophia Laurent', NULL, 'Feeding', 0, 'Standard evening feed schedule', 'Medium');

-- ============================================================
-- End of Schema
-- ============================================================
