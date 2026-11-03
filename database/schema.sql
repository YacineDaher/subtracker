-- =============================================
-- SubTracker - SQL Server schema
-- Run once (SSMS or Azure Data Studio) before starting the app.
-- Safe to re-run: every object is created only if missing.
-- =============================================

-- 1. Database
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'subtracker_db')
BEGIN
CREATE DATABASE subtracker_db;
END
GO

USE subtracker_db;
GO

-- 2. Users (passwords are stored as BCrypt hashes, ~60 characters)
IF OBJECT_ID('dbo.users', 'U') IS NULL
BEGIN
CREATE TABLE users (
                       id INT PRIMARY KEY IDENTITY(1,1),
                       username NVARCHAR(50) NOT NULL UNIQUE,
                       password NVARCHAR(255) NOT NULL,
                       email NVARCHAR(255),
                       account_type VARCHAR(20) DEFAULT 'individual', -- 'individual' or 'family'
                       profile_picture VARCHAR(255) NULL
);
END
GO

-- 3. Family members (must exist before abonnements, which references it)
IF OBJECT_ID('dbo.family_members', 'U') IS NULL
BEGIN
CREATE TABLE family_members (
                                id INT PRIMARY KEY IDENTITY(1,1),
                                user_id INT NOT NULL,
                                name NVARCHAR(100) NOT NULL,
                                username NVARCHAR(50) NOT NULL UNIQUE,
                                password NVARCHAR(255) NOT NULL,
                                created_at DATETIME DEFAULT GETDATE(),
                                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
END
GO

-- 4. Subscriptions
IF OBJECT_ID('dbo.abonnements', 'U') IS NULL
BEGIN
CREATE TABLE abonnements (
                             id INT PRIMARY KEY IDENTITY(1,1),
                             nom NVARCHAR(100) NOT NULL,
                             prix DECIMAL(10, 2) NOT NULL,
                             date_debut DATE NOT NULL,
                             frequence NVARCHAR(20) NOT NULL,   -- Mensuel, Annuel
                             categorie NVARCHAR(50) NOT NULL,
                             user_id INT,
                             assigned_to_member_id INT NULL,
                             logo_url VARCHAR(500) NULL,
                             color_hex VARCHAR(20) NULL,
                             FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                             FOREIGN KEY (assigned_to_member_id) REFERENCES family_members(id)
);
END
GO

-- 5. Upgrade path for databases created from an older version of this script
IF COL_LENGTH('dbo.abonnements', 'assigned_to_member_id') IS NULL
ALTER TABLE abonnements ADD assigned_to_member_id INT NULL;
GO
IF COL_LENGTH('dbo.abonnements', 'logo_url') IS NULL
ALTER TABLE abonnements ADD logo_url VARCHAR(500) NULL;
GO
IF COL_LENGTH('dbo.abonnements', 'color_hex') IS NULL
ALTER TABLE abonnements ADD color_hex VARCHAR(20) NULL;
GO

PRINT 'SubTracker database is ready.';