-- Roles (using consistent naming without ROLE_ prefix)
INSERT INTO roles (name) 
SELECT 'READER' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'READER');

INSERT INTO roles (name) 
SELECT 'AUTHOR' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'AUTHOR');

INSERT INTO roles (name) 
SELECT 'ADMIN' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');

-- Admin user (password: admin123)
INSERT INTO users (email, password, first_name, last_name, created_at) 
VALUES ('admin@example.com', '$2a$10$3Qnh0QYtvBBGT.5XjAlJPONEQHBAXPKyJ9OKqP/CwOv9QiQQP8Z1K', 'Admin', 'User', NOW())
ON DUPLICATE KEY UPDATE email=email;

-- Add admin role to admin user
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@example.com' AND r.name = 'ADMIN';

-- Categories
INSERT INTO categories (name, slug, description) 
VALUES ('Tehnologija', 'tehnologija', 'Članci o najnovijim tehnološkim dostignućima')
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO categories (name, slug, description) 
VALUES ('Programiranje', 'programiranje', 'Teme vezane za programiranje i razvoj softvera')
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO categories (name, slug, description) 
VALUES ('Web Dizajn', 'web-dizajn', 'Saveti i trikovi za web dizajn')
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO categories (name, slug, description) 
VALUES ('Mobilne Aplikacije', 'mobilne-aplikacije', 'Sve o razvoju mobilnih aplikacija')
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO categories (name, slug, description) 
VALUES ('Baze Podataka', 'baze-podataka', 'Članci o bazama podataka i upravljanju podacima')
ON DUPLICATE KEY UPDATE name=name;

-- Tags
INSERT INTO tags (name, slug) VALUES ('Java', 'java') ON DUPLICATE KEY UPDATE name=name;
INSERT INTO tags (name, slug) VALUES ('Spring Boot', 'spring-boot') ON DUPLICATE KEY UPDATE name=name;
INSERT INTO tags (name, slug) VALUES ('React', 'react') ON DUPLICATE KEY UPDATE name=name;
INSERT INTO tags (name, slug) VALUES ('Next.js', 'nextjs') ON DUPLICATE KEY UPDATE name=name;
INSERT INTO tags (name, slug) VALUES ('MySQL', 'mysql') ON DUPLICATE KEY UPDATE name=name; 