PRAGMA foreign_keys = ON;

-- ============================================================
-- JOB TITLES TABLE (New Structure)
-- ============================================================
-- Defines all possible job titles for personnel
-- Two categories: Administration and Farm
-- ============================================================

CREATE TABLE IF NOT EXISTS jobTitles (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    department VARCHAR(20) NOT NULL CHECK (department IN ('administration', 'farm')),
    description VARCHAR(200)
);

-- ============================================================
-- PERSONNEL TABLE (New Structure)
-- ============================================================
-- Stores all personnel: Admin (Owner, Cashier, Staff) and Farm (Supervisors, Subordinates)
-- ============================================================

CREATE TABLE IF NOT EXISTS personnel (
    id INTEGER PRIMARY KEY AUTOINCREMENT,

    -- Basic Information
    fullName VARCHAR(100) NOT NULL,
    age INTEGER NOT NULL CHECK (age >= 18 AND age <= 100),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,

    -- Job Information
    jobTitle INTEGER NOT NULL,
    department VARCHAR(20) NOT NULL CHECK (department IN ('administration', 'farm')),

    -- Admin Staff Positions (comma-separated: 'accounting,hr,legal,sales')
    -- Only used when jobTitle = 'admin_staff'
    positions VARCHAR(100) DEFAULT NULL,

    -- Employment Details
    hireDate DATE,
    salary DECIMAL(10, 2) DEFAULT 0.00,
    isActive BOOLEAN DEFAULT 1,

    -- Contact Information
    address VARCHAR(255),
    emergencyContact VARCHAR(100),

    -- Supervisor Relationship (for subordinates)
    -- Links subordinate workers to their supervisor
    supervisorId INTEGER DEFAULT NULL,

    -- Timestamps
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Keys
    FOREIGN KEY (jobTitle) REFERENCES jobTitles(id),
    FOREIGN KEY (supervisorId) REFERENCES personnel(id) ON DELETE RESTRICT
);

-- ============================================================
-- INDEXES FOR PERSONNEL
-- ============================================================

CREATE INDEX IF NOT EXISTS idx_personnel_supervisor ON personnel(supervisorId);
CREATE INDEX IF NOT EXISTS idx_personnel_email ON personnel(email);
CREATE INDEX IF NOT EXISTS idx_personnel_jobTitle ON personnel(jobTitle);
CREATE INDEX IF NOT EXISTS idx_personnel_department ON personnel(department);

-- ============================================================
-- TRIGGER: Update updated_at timestamp
-- ============================================================

CREATE TRIGGER IF NOT EXISTS trg_personnel_updated_at
    AFTER UPDATE ON personnel
    FOR EACH ROW
BEGIN
    UPDATE personnel
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

-- ============================================================
-- DEFAULT DATA: Job Titles
-- ============================================================
-- Administration Department
INSERT OR IGNORE INTO jobTitles (name, department, description) VALUES
    ('farm_owner', 'administration', 'Farm Owner / General Manager - Overall farm management');
INSERT OR IGNORE INTO jobTitles (name, department, description) VALUES
    ('cashier', 'administration', 'Cashier - Bridge between admin and farm operations');
INSERT OR IGNORE INTO jobTitles (name, department, description) VALUES
    ('admin_staff', 'administration', 'Admin Staff - Accounting, HR, Legal, Sales positions');

-- Farm Department - Supervisors
INSERT OR IGNORE INTO jobTitles (name, department, description) VALUES
    ('veterinary_supervisor', 'farm', 'Veterinary Supervisor - Manages animal health team');
INSERT OR IGNORE INTO jobTitles (name, department, description) VALUES
    ('inventory_supervisor', 'farm', 'Inventory & Supply Supervisor - Manages inventory team');
INSERT OR IGNORE INTO jobTitles (name, department, description) VALUES
    ('farmhand_supervisor', 'farm', 'Farmhand Supervisor - Manages farmhand workers');

-- Farm Department - Subordinates
INSERT OR IGNORE INTO jobTitles (name, department, description) VALUES
    ('veterinary_subordinate', 'farm', 'Veterinary Subordinate - Works under veterinary supervisor');
INSERT OR IGNORE INTO jobTitles (name, department, description) VALUES
    ('inventory_subordinate', 'farm', 'Inventory Subordinate - Works under inventory supervisor');
INSERT OR IGNORE INTO jobTitles (name, department, description) VALUES
    ('farmhand_subordinate', 'farm', 'Farmhand Subordinate - Works under farmhand supervisor');

-- ============================================================
-- USERS TABLE
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    creationDate DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Insert default admin user if not exists
INSERT OR IGNORE INTO users (name, email, password)
VALUES ('Administrator', 'admin@farm.ma', 'admin123');

-- ============================================================
-- Houses (Chicken Bays) Database Schema for SQLite
-- ============================================================

CREATE TABLE IF NOT EXISTS houses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(10) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    chickenCount INTEGER DEFAULT 0 CHECK (chickenCount >= 0),
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    healthStatus VARCHAR(50) DEFAULT 'Good' CHECK (healthStatus IN ('Good', 'Fair', 'Poor')),
    lastCleaningDate DATE,
    creationDate DATE DEFAULT CURRENT_DATE,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_houses_type ON houses(type);
CREATE INDEX IF NOT EXISTS idx_houses_health ON houses(healthStatus);

CREATE TRIGGER IF NOT EXISTS trg_houses_updated_at
    AFTER UPDATE ON houses
    FOR EACH ROW
BEGIN
    UPDATE houses
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

-- ============================================================
-- Egg Production Table
-- ============================================================

CREATE TABLE IF NOT EXISTS egg_production (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    houseId INTEGER NOT NULL,
    productionDate TEXT NOT NULL,
    eggsCollected INTEGER NOT NULL DEFAULT 0 CHECK (eggsCollected >= 0),
    crackedEggs INTEGER NOT NULL DEFAULT 0 CHECK (crackedEggs >= 0),
    goodEggs INTEGER NOT NULL DEFAULT 0 CHECK (goodEggs >= 0),
    deadChickens INTEGER NOT NULL DEFAULT 0 CHECK (deadChickens >= 0),
    collectedBy TEXT,
    notes TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (houseId) REFERENCES houses(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (houseId, productionDate),
    CHECK (crackedEggs <= eggsCollected),
    CHECK (goodEggs = eggsCollected - crackedEggs)
);

CREATE INDEX IF NOT EXISTS idx_egg_production_house ON egg_production(houseId);
CREATE INDEX IF NOT EXISTS idx_egg_production_date ON egg_production(productionDate);
CREATE INDEX IF NOT EXISTS idx_egg_production_house_date ON egg_production(houseId, productionDate);
CREATE INDEX IF NOT EXISTS idx_egg_production_collector ON egg_production(collectedBy);

CREATE TRIGGER IF NOT EXISTS trg_egg_production_updated_at
    AFTER UPDATE ON egg_production
    FOR EACH ROW
BEGIN
    UPDATE egg_production
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

CREATE TRIGGER IF NOT EXISTS trg_egg_production_before_insert
    BEFORE INSERT ON egg_production
    FOR EACH ROW
BEGIN
    SELECT CASE
        WHEN NEW.crackedEggs > NEW.eggsCollected THEN
            RAISE(ABORT, 'Cracked eggs cannot exceed total eggs collected')
        WHEN NEW.eggsCollected < 0 OR NEW.crackedEggs < 0 OR NEW.deadChickens < 0 THEN
            RAISE(ABORT, 'Egg counts and deaths must be non-negative')
    END;
END;

CREATE TRIGGER IF NOT EXISTS trg_egg_production_before_update
    BEFORE UPDATE ON egg_production
    FOR EACH ROW
BEGIN
    SELECT CASE
        WHEN NEW.crackedEggs > NEW.eggsCollected THEN
            RAISE(ABORT, 'Cracked eggs cannot exceed total eggs collected')
        WHEN NEW.eggsCollected < 0 OR NEW.crackedEggs < 0 OR NEW.deadChickens < 0 THEN
            RAISE(ABORT, 'Egg counts and deaths must be non-negative')
    END;
END;

-- ============================================================
-- Chickens Table
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

CREATE INDEX IF NOT EXISTS idx_chickens_house ON chickens(houseId);
CREATE INDEX IF NOT EXISTS idx_chickens_arrival ON chickens(arrivalDate);
CREATE INDEX IF NOT EXISTS idx_chickens_transfer ON chickens(nextTransferDate);
CREATE INDEX IF NOT EXISTS idx_chickens_batch ON chickens(batchNumber);

-- ============================================================
-- Mortality Table
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

CREATE INDEX IF NOT EXISTS idx_mortality_house_date ON mortality(houseId, deathDate);
CREATE INDEX IF NOT EXISTS idx_mortality_date ON mortality(deathDate);
CREATE INDEX IF NOT EXISTS idx_mortality_cause ON mortality(cause);
CREATE INDEX IF NOT EXISTS idx_mortality_outbreak ON mortality(isOutbreak);

-- Triggers for chicken count updates
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

CREATE TRIGGER IF NOT EXISTS trg_mortality_after_insert
    AFTER INSERT ON mortality
    FOR EACH ROW
BEGIN
    UPDATE houses
    SET chickenCount = chickenCount - NEW.count
    WHERE id = NEW.houseId;
END;

-- ============================================================
-- Feed Table
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

CREATE INDEX IF NOT EXISTS idx_feed_type ON feed(type);
CREATE INDEX IF NOT EXISTS idx_feed_expiry ON feed(expiryDate);

CREATE TRIGGER IF NOT EXISTS trg_feed_updated_at
    AFTER UPDATE ON feed
    FOR EACH ROW
BEGIN
    UPDATE feed
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

-- ============================================================
-- Medications Table
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

CREATE INDEX IF NOT EXISTS idx_medications_type ON medications(type);
CREATE INDEX IF NOT EXISTS idx_medications_expiry ON medications(expiryDate);

CREATE TRIGGER IF NOT EXISTS trg_medications_updated_at
    AFTER UPDATE ON medications
    FOR EACH ROW
BEGIN
    UPDATE medications
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

-- ============================================================
-- Equipment Categories Table
-- ============================================================

CREATE TABLE IF NOT EXISTS equipment_categories (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL CHECK (category IN ('Feeding', 'Cleaning', 'Medical', 'Other')),
    location VARCHAR(100),
    notes TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (name)
);

-- ============================================================
-- Equipment Items Table
-- ============================================================

CREATE TABLE IF NOT EXISTS equipment_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    categoryId INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Good' CHECK (status IN ('Good', 'Fair', 'Broken')),
    purchaseDate DATE,
    purchasePrice REAL NOT NULL DEFAULT 0.00 CHECK (purchasePrice >= 0),
    lastMaintenanceDate DATE,
    nextMaintenanceDate DATE,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    updated_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (categoryId) REFERENCES equipment_categories(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_equipment_categories_category ON equipment_categories(category);
CREATE INDEX IF NOT EXISTS idx_equipment_categories_location ON equipment_categories(location);
CREATE INDEX IF NOT EXISTS idx_equipment_items_category ON equipment_items(categoryId);
CREATE INDEX IF NOT EXISTS idx_equipment_items_status ON equipment_items(status);
CREATE INDEX IF NOT EXISTS idx_equipment_items_maintenance ON equipment_items(nextMaintenanceDate);
CREATE INDEX IF NOT EXISTS idx_equipment_items_category_status ON equipment_items(categoryId, status);

CREATE TRIGGER IF NOT EXISTS trg_equipment_categories_updated_at
    AFTER UPDATE ON equipment_categories
    FOR EACH ROW
BEGIN
    UPDATE equipment_categories
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

CREATE TRIGGER IF NOT EXISTS trg_equipment_items_updated_at
    AFTER UPDATE ON equipment_items
    FOR EACH ROW
BEGIN
    UPDATE equipment_items
    SET updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.id;
END;

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
-- Equipment Views
-- ============================================================

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
-- Tasks Table
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

CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_dueDate ON tasks(dueDate);
CREATE INDEX IF NOT EXISTS idx_tasks_assignedTo ON tasks(assignedTo);
CREATE INDEX IF NOT EXISTS idx_tasks_category ON tasks(category);
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority);

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
