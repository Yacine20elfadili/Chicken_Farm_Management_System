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


-- ============================================================
-- Houses (Chicken Bays) Database Schema for SQLite
-- ============================================================
-- This script creates the houses table for tracking chicken
-- housing, capacity, and health status.
-- ============================================================

-- Drop existing table if exists (for clean reinstall)
DROP TABLE IF EXISTS houses;

-- Enable foreign key constraints (must be set per connection in SQLite)
PRAGMA foreign_keys = ON;

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
-- Useful Views
-- ============================================================

-- View: Daily production summary with calculated metrics
CREATE VIEW v_daily_production_summary AS
SELECT 
    ep.id,
    ep.houseId,
    h.name AS house_name,
    ep.productionDate,
    ep.eggsCollected,
    ep.crackedEggs,
    ep.goodEggs,
    ep.deadChickens,
    ep.collectedBy,
    -- Calculate efficiency rate
    ROUND((CAST(ep.goodEggs AS REAL) * 100.0) / NULLIF(ep.eggsCollected, 0), 2) AS efficiency_rate,
    -- Calculate cracked rate
    ROUND((CAST(ep.crackedEggs AS REAL) * 100.0) / NULLIF(ep.eggsCollected, 0), 2) AS cracked_rate,
    ep.notes
FROM 
    egg_production ep
    INNER JOIN houses h ON ep.houseId = h.id
ORDER BY 
    ep.productionDate DESC, h.name;

-- View: Weekly production summary per house
CREATE VIEW v_weekly_production_summary AS
SELECT 
    h.id AS house_id,
    h.name AS house_name,
    strftime('%Y', ep.productionDate) AS year,
    strftime('%W', ep.productionDate) AS week,
    COUNT(*) AS days_recorded,
    SUM(ep.eggsCollected) AS total_collected,
    SUM(ep.crackedEggs) AS total_cracked,
    SUM(ep.goodEggs) AS total_good,
    SUM(ep.deadChickens) AS total_deaths,
    ROUND(AVG((CAST(ep.goodEggs AS REAL) * 100.0) / NULLIF(ep.eggsCollected, 0)), 2) AS avg_efficiency,
    ROUND((CAST(SUM(ep.goodEggs) AS REAL) * 100.0) / NULLIF(SUM(ep.eggsCollected), 0), 2) AS week_efficiency
FROM 
    egg_production ep
    INNER JOIN houses h ON ep.houseId = h.id
GROUP BY 
    h.id, h.name, strftime('%Y', ep.productionDate), strftime('%W', ep.productionDate)
ORDER BY 
    year DESC, week DESC, h.name;

-- View: Monthly production summary per house
CREATE VIEW v_monthly_production_summary AS
SELECT 
    h.id AS house_id,
    h.name AS house_name,
    strftime('%Y', ep.productionDate) AS year,
    strftime('%m', ep.productionDate) AS month,
    COUNT(*) AS days_recorded,
    SUM(ep.eggsCollected) AS total_collected,
    SUM(ep.crackedEggs) AS total_cracked,
    SUM(ep.goodEggs) AS total_good,
    SUM(ep.deadChickens) AS total_deaths,
    ROUND(AVG(ep.eggsCollected), 0) AS avg_daily_collection,
    ROUND(AVG((CAST(ep.goodEggs AS REAL) * 100.0) / NULLIF(ep.eggsCollected, 0)), 2) AS avg_efficiency,
    ROUND((CAST(SUM(ep.goodEggs) AS REAL) * 100.0) / NULLIF(SUM(ep.eggsCollected), 0), 2) AS month_efficiency
FROM 
    egg_production ep
    INNER JOIN houses h ON ep.houseId = h.id
GROUP BY 
    h.id, h.name, strftime('%Y', ep.productionDate), strftime('%m', ep.productionDate)
ORDER BY 
    year DESC, month DESC, h.name;

-- View: Worker performance summary
CREATE VIEW v_worker_performance AS
SELECT 
    ep.collectedBy AS worker_name,
    COUNT(*) AS total_collections,
    COUNT(DISTINCT ep.houseId) AS houses_worked,
    SUM(ep.eggsCollected) AS total_eggs_collected,
    SUM(ep.goodEggs) AS total_good_eggs,
    ROUND(AVG((CAST(ep.goodEggs AS REAL) * 100.0) / NULLIF(ep.eggsCollected, 0)), 2) AS avg_efficiency,
    MIN(ep.productionDate) AS first_collection,
    MAX(ep.productionDate) AS last_collection
FROM 
    egg_production ep
WHERE 
    ep.collectedBy IS NOT NULL
GROUP BY 
    ep.collectedBy
ORDER BY 
    total_collections DESC;

-- ============================================================
-- Common Queries (as comments for reference)
-- ============================================================

/*
-- Get today's production across all houses
SELECT * FROM v_daily_production_summary 
WHERE productionDate = date('now');

-- Get this week's summary for a specific house
SELECT * FROM v_weekly_production_summary 
WHERE house_id = 2 
  AND year = strftime('%Y', 'now')
  AND week = strftime('%W', 'now');

-- Get production trend for last 30 days
SELECT 
    productionDate,
    SUM(eggsCollected) AS total_eggs,
    ROUND(AVG((CAST(goodEggs AS REAL) * 100.0) / eggsCollected), 2) AS avg_efficiency
FROM egg_production
WHERE productionDate >= date('now', '-30 days')
GROUP BY productionDate
ORDER BY productionDate;

-- Find days with efficiency below 90%
SELECT * FROM v_daily_production_summary
WHERE efficiency_rate < 90
ORDER BY productionDate DESC;

-- Get house statistics for date range
SELECT 
    houseId,
    COUNT(*) AS days_recorded,
    SUM(eggsCollected) AS total_collected,
    SUM(crackedEggs) AS total_cracked,
    SUM(goodEggs) AS total_good,
    SUM(deadChickens) AS total_deaths,
    ROUND(AVG(eggsCollected), 0) AS avg_daily_collection,
    ROUND(AVG((CAST(goodEggs AS REAL) * 100.0) / NULLIF(eggsCollected, 0)), 2) AS avg_efficiency
FROM egg_production
WHERE houseId = 2 
  AND productionDate BETWEEN '2025-12-01' AND '2025-12-31'
GROUP BY houseId;

-- Get top 10 production days for a house
SELECT 
    productionDate,
    eggsCollected,
    goodEggs,
    crackedEggs,
    ROUND((CAST(goodEggs AS REAL) * 100.0) / NULLIF(eggsCollected, 0), 2) AS efficiency_rate,
    collectedBy
FROM egg_production
WHERE houseId = 2
ORDER BY eggsCollected DESC
LIMIT 10;
*/

-- ============================================================
-- End of Schema
-- ============================================================