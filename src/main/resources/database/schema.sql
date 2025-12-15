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


-- ============================================================
-- UPDATE: Add supervisor relationship to personnel table
-- ============================================================

-- Add supervisorId column to existing personnel table (if not exists)
-- Note: This is safe to run multiple times
ALTER TABLE personnel ADD COLUMN supervisorId INTEGER REFERENCES personnel(id) ON DELETE SET NULL;

-- Create index for supervisor lookups
CREATE INDEX IF NOT EXISTS idx_personnel_supervisor ON personnel(supervisorId);

-- ============================================================
-- UPDATE: Add new job titles for operations personnel
-- ============================================================

-- Insert new job titles (using INSERT OR IGNORE to prevent duplicates)
INSERT OR IGNORE INTO jobTitles (name) VALUES ('veterinary');
INSERT OR IGNORE INTO jobTitles (name) VALUES ('inventory_tracker');
INSERT OR IGNORE INTO jobTitles (name) VALUES ('supervisor');
INSERT OR IGNORE INTO jobTitles (name) VALUES ('farmhand');
INSERT OR IGNORE INTO jobTitles (name) VALUES ('administration');
INSERT OR IGNORE INTO jobTitles (name) VALUES ('cashier');
INSERT OR IGNORE INTO jobTitles (name) VALUES ('tracker');
INSERT OR IGNORE INTO jobTitles (name) VALUES ('worker');





/* ===========================
    Default Data
    ===========================*/


-- Insert default admin user if not exists
INSERT OR IGNORE INTO users (name, email, password)
VALUES ('Administrator', 'admin@farm.ma', 'admin123')
ON CONFLICT(email) DO NOTHING;



-- Insert default shifts if not exists
INSERT OR IGNORE INTO shifts (name, startTime, endTime)
VALUES ('morning', '06:00:00', '15:00:00')
ON CONFLICT(name) DO NOTHING;

INSERT OR IGNORE INTO shifts (name, startTime, endTime)
VALUES ('evening', '15:00:01', '00:00:00')
ON CONFLICT(name) DO NOTHING;

-- -- Insert default personnel if not exists
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('John Doe', 35, '0612345678', 'john.doe@farm.ma', 1, '2025-12-05', 5000.00, 1, 1, '123 Farm Road', 'Jane Doe 0698765432');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Jane Smith', 28, '0623456789', 'jane.smith@farm.ma', 2, '2025-12-05', 4000.00, 2, 1, '456 Farm Lane', 'John Smith 0687654321');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Maria Garcia', 31, '0645678901', 'maria.garcia@farm.ma', 2, '2025-12-05', 4200.00, 1, 1, '321 Valley Road', 'Carlos Garcia 0665432109');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Hassan Ibrahim', 38, '0656789012', 'hassan.ibrahim@farm.ma', 1, '2025-12-05', 5500.00, 2, 1, '654 Mountain View', 'Layla Ibrahim 0654321098');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Sophia Laurent', 26, '0667890123', 'sophia.laurent@farm.ma', 2, '2025-12-05', 3800.00, 1, 1, '987 Garden Path', 'Pierre Laurent 0643210987');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Mohammed Al-Rashid', 45, '0678901234', 'mohammed.rashid@farm.ma', 1, '2025-12-05', 6200.00, 1, 1, '147 Oak Street', 'Amira Al-Rashid 0632109876');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Elena Rodriguez', 33, '0689012345', 'elena.rodriguez@farm.ma', 2, '2025-12-05', 4100.00, 2, 1, '258 Pine Avenue', 'Diego Rodriguez 0621098765');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Khalid El-Mansouri', 50, '0690123456', 'khalid.mansouri@farm.ma', 1, '2025-12-05', 6500.00, 1, 1, '369 Elm Court', 'Zainab El-Mansouri 0610987654');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Lucia Moretti', 29, '0601234567', 'lucia.moretti@farm.ma', 2, '2025-12-05', 3900.00, 1, 1, '741 Birch Lane', 'Marco Moretti 0699876543');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Omar Saeed', 36, '0612345670', 'omar.saeed@farm.ma', 1, '2025-12-05', 5300.00, 2, 1, '852 Cedar Road', 'Noor Saeed 0688765432');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Francesca Rossi', 27, '0623456781', 'francesca.rossi@farm.ma', 2, '2025-12-05', 3700.00, 2, 1, '963 Maple Drive', 'Antonio Rossi 0677654321');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Youssef Benali', 44, '0634567892', 'youssef.benali@farm.ma', 1, '2025-12-05', 5800.00, 1, 1, '159 Spruce Way', 'Aya Benali 0666543210');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Isabella Torres', 32, '0645678903', 'isabella.torres@farm.ma', 2, '2025-12-05', 4300.00, 2, 1, '357 Willow Street', 'Roberto Torres 0655432109');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Karim Bouvier', 40, '0656789014', 'karim.bouvier@farm.ma', 1, '2025-12-05', 5600.00, 1, 0, '456 Ashwood Place', 'Maryam Bouvier 0644321098');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Giulia Marchetti', 25, '0667890125', 'giulia.marchetti@farm.ma', 2, '2025-12-05', 3600.00, 1, 1, '789 Cypress Circle', 'Alessandro Marchetti 0633210987');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Nabil Al-Hakim', 48, '0678901236', 'nabil.hakim@farm.ma', 1, '2025-12-05', 6400.00, 2, 1, '987 Juniper Road', 'Leila Al-Hakim 0622109876');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Claudia Barbosa', 30, '0689012347', 'claudia.barbosa@farm.ma', 2, '2025-12-05', 4000.00, 2, 1, '654 Hickory Lane', 'Rafael Barbosa 0611098765');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Samir El-Khawaja', 41, '0690123458', 'samir.khawaja@farm.ma', 1, '2025-12-05', 5700.00, 1, 1, '123 Ash Street', 'Rania El-Khawaja 0600987654');
-- INSERT OR IGNORE INTO personnel (fullName, age, phone, email, jobTitle, hireDate, salary, shift, isActive, address, emergencyContact)
-- VALUES ('Valentina Esposito', 28, '0601234569', 'valentina.esposito@farm.ma', 2, '2025-12-05', 3850.00, 2, 1, '741 Walnut Avenue', 'Matteo Esposito 0699876541');


-- ============================================================
-- Houses (Chicken Bays) Database Schema for SQLite
-- ============================================================
-- This script creates the houses table for tracking chicken
-- housing, capacity, and health status.
-- ============================================================



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


-- ============================================================
-- Egg Production Database Schema for SQLite
-- ============================================================
-- This script creates the egg_production table and related
-- database objects for tracking daily egg collection data.
-- ============================================================



-- ============================================================
-- Main Table: egg_production
-- ============================================================
-- Stores daily egg production records for each house
-- One record per house per day
-- ============================================================

CREATE TABLE IF NOT EXISTS egg_production (
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

-- -- Sample Feed Data
-- INSERT INTO feed (name, type, quantityKg, pricePerKg, supplier, lastRestockDate, expiryDate, minStockLevel) VALUES
--     ('Starter Crumbs', 'Day-old', 500.0, 12.50, 'AgriSupply Morocco', '2025-01-10', '2025-07-10', 200.0),
--     ('Chick Starter Mash', 'Day-old', 350.0, 11.00, 'FeedCorp International', '2025-01-08', '2025-06-08', 150.0),
--     ('Layer Pellets Premium', 'Layer', 800.0, 15.00, 'AgriSupply Morocco', '2025-01-12', '2025-08-12', 300.0),
--     ('Layer Mash Standard', 'Layer', 120.0, 13.50, 'Local Farm Supplies', '2024-12-20', '2025-05-20', 200.0),
--     ('Broiler Grower Feed', 'Meat Growth', 650.0, 14.00, 'FeedCorp International', '2025-01-05', '2025-06-05', 250.0),
--     ('Meat Finisher Pellets', 'Meat Growth', 450.0, 16.00, 'AgriSupply Morocco', '2025-01-11', '2025-07-11', 200.0),
--     ('Broiler Starter Crumbs', 'Meat Growth', 75.0, 13.00, 'Local Farm Supplies', '2024-12-15', '2025-03-15', 100.0),
--     ('Organic Layer Feed', 'Layer', 200.0, 22.00, 'Green Organic Feeds', '2025-01-09', '2025-04-09', 100.0);


-- ============================================================
-- Medications Table
-- ============================================================
-- Stores medication inventory for poultry health
-- ============================================================


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

-- -- Sample Medications Data
-- INSERT INTO medications (name, type, quantity, unit, pricePerUnit, supplier, purchaseDate, expiryDate, minStockLevel, usage) VALUES
--     ('Newcastle Disease Vaccine', 'Vaccine', 500, 'doses', 2.50, 'VetPharma Morocco', '2025-01-05', '2026-01-05', 200, 'Administer to chicks at day 7 and day 21'),
--     ('Infectious Bronchitis Vaccine', 'Vaccine', 300, 'doses', 3.00, 'VetPharma Morocco', '2025-01-03', '2025-12-03', 150, 'Spray or drinking water at day 1'),
--     ('Gumboro Vaccine', 'Vaccine', 50, 'doses', 2.75, 'AgriVet Supplies', '2024-12-20', '2025-06-20', 100, 'Drinking water at day 14 and 21'),
--     ('Amoxicillin 10%', 'Antibiotic', 25, 'bottles', 45.00, 'VetPharma Morocco', '2025-01-08', '2026-06-08', 10, '1g per liter of drinking water for 5 days'),
--     ('Enrofloxacin', 'Antibiotic', 8, 'bottles', 55.00, 'MedVet International', '2024-11-15', '2025-11-15', 10, '0.5ml per liter for respiratory infections'),
--     ('Vitamin AD3E', 'Supplement', 40, 'bottles', 35.00, 'AgriVet Supplies', '2025-01-10', '2026-07-10', 15, '1ml per liter during stress periods'),
--     ('Electrolyte Powder', 'Supplement', 60, 'sachets', 8.00, 'Local Farm Supplies', '2025-01-12', '2026-01-12', 20, '1 sachet per 20 liters during heat stress'),
--     ('Ivermectin', 'Antiparasitic', 15, 'bottles', 65.00, 'VetPharma Morocco', '2024-12-28', '2025-12-28', 10, 'External parasite treatment, use as directed'),
--     ('Cocci-Stop', 'Antiparasitic', 5, 'bottles', 48.00, 'MedVet International', '2024-10-15', '2025-04-15', 10, 'Coccidiosis prevention, 1ml per 2 liters'),
--     ('Calcium Supplement', 'Supplement', 80, 'kg', 12.00, 'Local Farm Supplies', '2025-01-06', '2026-06-06', 25, 'Mix with feed at 2% for laying hens');


-- ============================================================
-- Equipment Categories and Items Database Schema for SQLite
-- ============================================================
-- This replaces the old single equipment table with two tables:
-- 1. equipment_categories: Types of equipment (e.g., "Shovels")
-- 2. equipment_items: Individual items within each category
-- ============================================================

-- ============================================================
-- DROP OLD TABLE
-- ============================================================
DROP TABLE IF EXISTS equipment;

-- ============================================================
-- Main Table: equipment_categories
-- ============================================================
-- Stores equipment category information
-- Each category represents a type of equipment (e.g., Shovels, Feeders)
-- ============================================================

CREATE TABLE IF NOT EXISTS equipment_categories (
    -- Primary Key (SQLite uses INTEGER for autoincrement)
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    
    -- Category name (e.g., "Shovels", "Water Pumps")
    name VARCHAR(100) NOT NULL,
    
    -- Category type (Feeding, Cleaning, Medical, Other)
    category VARCHAR(50) NOT NULL
        CHECK (category IN ('Feeding', 'Cleaning', 'Medical', 'Other')),
    
    -- Where this equipment type is typically stored/used
    location VARCHAR(100),
    
    -- Additional notes about this equipment type
    notes TEXT,
    
    -- Timestamps for tracking modifications
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    
    -- Unique constraint: No duplicate category names
    UNIQUE (name)
);

-- ============================================================
-- Main Table: equipment_items
-- ============================================================
-- Stores individual equipment items
-- Each item belongs to a category and has its own status/maintenance info
-- ============================================================

CREATE TABLE IF NOT EXISTS equipment_items (
    -- Primary Key (SQLite uses INTEGER for autoincrement)
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    
    -- Foreign Key to equipment_categories
    categoryId INTEGER NOT NULL,
    
    -- Status of this specific item
    status VARCHAR(20) NOT NULL DEFAULT 'Good'
        CHECK (status IN ('Good', 'Fair', 'Broken')),
    
    -- Purchase information
    purchaseDate DATE,
    purchasePrice REAL NOT NULL DEFAULT 0.00
        CHECK (purchasePrice >= 0),
    
    -- Maintenance tracking
    lastMaintenanceDate DATE,
    nextMaintenanceDate DATE,
    
    -- Timestamps for tracking modifications
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign Key Constraint
    FOREIGN KEY (categoryId) REFERENCES equipment_categories(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ============================================================
-- Indexes for Performance
-- ============================================================

-- Index on category type for filtering
CREATE INDEX IF NOT EXISTS idx_equipment_categories_category 
    ON equipment_categories(category);

-- Index on location for filtering
CREATE INDEX IF NOT EXISTS idx_equipment_categories_location 
    ON equipment_categories(location);

-- Index on categoryId for joining items to categories
CREATE INDEX IF NOT EXISTS idx_equipment_items_category 
    ON equipment_items(categoryId);

-- Index on status for filtering broken items
CREATE INDEX IF NOT EXISTS idx_equipment_items_status 
    ON equipment_items(status);

-- Index on nextMaintenanceDate for maintenance scheduling
CREATE INDEX IF NOT EXISTS idx_equipment_items_maintenance 
    ON equipment_items(nextMaintenanceDate);

-- Composite index for category and status queries
CREATE INDEX IF NOT EXISTS idx_equipment_items_category_status 
    ON equipment_items(categoryId, status);

-- ============================================================
-- Triggers for SQLite
-- ============================================================

-- Trigger to update updated_at timestamp on equipment_categories UPDATE
CREATE TRIGGER IF NOT EXISTS trg_equipment_categories_updated_at
    AFTER UPDATE ON equipment_categories
    FOR EACH ROW
BEGIN
    UPDATE equipment_categories
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

-- Trigger to update updated_at timestamp on equipment_items UPDATE
CREATE TRIGGER IF NOT EXISTS trg_equipment_items_updated_at
    AFTER UPDATE ON equipment_items
    FOR EACH ROW
BEGIN
    UPDATE equipment_items
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

-- Trigger to validate maintenance dates
CREATE TRIGGER IF NOT EXISTS trg_equipment_items_validate_dates
    BEFORE INSERT ON equipment_items
    FOR EACH ROW
BEGIN
    SELECT CASE
        WHEN NEW.nextMaintenanceDate IS NOT NULL 
             AND NEW.lastMaintenanceDate IS NOT NULL 
             AND NEW.nextMaintenanceDate < NEW.lastMaintenanceDate THEN
            RAISE(ABORT, 'Next maintenance date cannot be before last maintenance date')
        WHEN NEW.purchasePrice < 0 THEN
            RAISE(ABORT, 'Purchase price cannot be negative')
    END;
END;

-- ============================================================
-- Sample Data (Optional - for testing)
-- ============================================================

-- Sample Equipment Categories
-- INSERT INTO equipment_categories (name, category, location, notes) VALUES
--     ('Shovels', 'Cleaning', 'Equipment Shed', 'Standard garden shovels for cleaning'),
--     ('Automatic Feeders', 'Feeding', 'All Houses', 'Programmable feeding systems'),
--     ('Water Pumps', 'Feeding', 'Water Room', 'Electric water pumps for drinking systems'),
--     ('Vaccination Sprayers', 'Medical', 'Medical Supply Room', 'Fine mist sprayers'),
--     ('Pressure Washers', 'Cleaning', 'Equipment Shed', 'High-pressure cleaning equipment');

-- Sample Equipment Items (for testing)
-- Shovels (categoryId = 1)
-- INSERT INTO equipment_items (categoryId, status, purchaseDate, purchasePrice, lastMaintenanceDate, nextMaintenanceDate) VALUES
--     (1, 'Good', '2024-01-15', 25.00, '2025-01-10', '2025-07-10'),
--     (1, 'Good', '2024-01-15', 25.00, '2025-01-10', '2025-07-10'),
--     (1, 'Fair', '2023-06-20', 22.00, '2024-12-05', '2025-06-05'),
--     (1, 'Broken', '2023-06-20', 22.00, '2024-11-15', NULL);

-- Automatic Feeders (categoryId = 2)
-- INSERT INTO equipment_items (categoryId, status, purchaseDate, purchasePrice, lastMaintenanceDate, nextMaintenanceDate) VALUES
--     (2, 'Good', '2024-03-15', 2500.00, '2025-01-05', '2025-04-05'),
--     (2, 'Good', '2024-03-15', 2500.00, '2025-01-05', '2025-04-05'),
--     (2, 'Good', '2024-03-15', 2500.00, '2025-01-05', '2025-04-05'),
--     (2, 'Good', '2024-03-15', 2500.00, '2025-01-05', '2025-04-05');

-- ============================================================
-- Views for Easy Querying
-- ============================================================

-- View: Equipment summary with counts
CREATE VIEW IF NOT EXISTS v_equipment_summary AS
SELECT 
    ec.id,
    ec.name,
    ec.category,
    ec.location,
    COUNT(ei.id) as totalItems,
    SUM(CASE WHEN ei.status = 'Good' THEN 1 ELSE 0 END) as goodItems,
    SUM(CASE WHEN ei.status = 'Fair' THEN 1 ELSE 0 END) as fairItems,
    SUM(CASE WHEN ei.status = 'Broken' THEN 1 ELSE 0 END) as brokenItems,
    SUM(ei.purchasePrice) as totalValue
FROM equipment_categories ec
LEFT JOIN equipment_items ei ON ec.id = ei.categoryId
GROUP BY ec.id, ec.name, ec.category, ec.location;

-- View: Items needing maintenance soon (within 7 days)
CREATE VIEW IF NOT EXISTS v_maintenance_due AS
SELECT 
    ec.name as categoryName,
    ei.id as itemId,
    ei.status,
    ei.nextMaintenanceDate,
    CAST((julianday(ei.nextMaintenanceDate) - julianday('now')) AS INTEGER) as daysUntilMaintenance
FROM equipment_items ei
JOIN equipment_categories ec ON ei.categoryId = ec.id
WHERE ei.nextMaintenanceDate IS NOT NULL
    AND ei.nextMaintenanceDate <= date('now', '+7 days')
    AND ei.nextMaintenanceDate >= date('now')
ORDER BY ei.nextMaintenanceDate;

-- ============================================================
-- End of Equipment Schema
-- ============================================================

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

-- -- Sample Tasks Data
-- INSERT INTO tasks (description, status, dueDate, completedAt, assignedTo, houseId, category, crackedEggs, notes, priority) VALUES
--     ('Morning egg collection - House H2', 'Done', date('now'), datetime('now', '-2 hours'), 'Ahmed', NULL, 'Collection', 15, 'Normal collection, some soft shells noted', 'High'),
--     ('Morning egg collection - House H3', 'Done', date('now'), datetime('now', '-1 hours'), 'Fatima', NULL, 'Collection', 8, 'Good quality eggs today', 'High'),
--     ('Clean water drinkers - All houses', 'Pending', date('now'), NULL, 'Hassan', NULL, 'Cleaning', 0, 'Weekly deep cleaning scheduled', 'Medium'),
--     ('Feed restocking - House H1', 'Pending', date('now'), NULL, 'John Doe', NULL, 'Feeding', 0, 'Day-old starter feed running low', 'High'),
--     ('Vaccination - Newcastle booster H1', 'Pending', date('now', '+1 day'), NULL, 'Jane Smith', NULL, 'Medical', 0, 'Day 21 booster for current batch', 'High'),
--     ('Equipment maintenance check', 'Pending', date('now', '+2 days'), NULL, 'Maria Garcia', NULL, 'Other', 0, 'Monthly maintenance inspection', 'Medium'),
--     ('Afternoon egg collection - House H2', 'Missed', date('now', '-1 day'), NULL, 'Ahmed', NULL, 'Collection', 0, 'Worker was absent, needs rescheduling', 'High');

-- ============================================================
-- End of Schema
-- ============================================================