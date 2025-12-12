
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

-- Sample Feed Data

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


-- ============================================================
-- Equipment Table
-- ============================================================
-- Stores farm equipment inventory and maintenance records
-- ============================================================


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


-- ============================================================
-- Tasks Table
-- ============================================================
-- Stores daily tasks for farm workers
-- ============================================================


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


-- ============================================================
-- End of Schema
-- ============================================================
