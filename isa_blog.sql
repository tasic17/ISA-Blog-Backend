-- Kreiranje baze podataka
DROP DATABASE IF EXISTS isa_blog;
CREATE DATABASE isa_blog;
USE isa_blog;

-- Tabela za role
CREATE TABLE roles (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(50) NOT NULL UNIQUE
);

-- Tabela za korisnike
CREATE TABLE users (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50) NOT NULL,
                       email VARCHAR(100) NOT NULL UNIQUE,
                       contact_number VARCHAR(20),
                       password VARCHAR(255) NOT NULL,
                       bio TEXT,
                       profile_picture_url VARCHAR(500),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabela za povezivanje korisnika i rola (Many-to-Many)
CREATE TABLE user_roles (
                            user_id INT,
                            role_id INT,
                            PRIMARY KEY (user_id, role_id),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Tabela za kategorije postova
CREATE TABLE categories (
                            id INT PRIMARY KEY AUTO_INCREMENT,
                            name VARCHAR(100) NOT NULL UNIQUE,
                            description TEXT,
                            slug VARCHAR(100) UNIQUE
);

-- Tabela za postove
CREATE TABLE posts (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       title VARCHAR(255) NOT NULL,
                       slug VARCHAR(255) UNIQUE,
                       content TEXT NOT NULL,
                       excerpt TEXT,
                       featured_image_url VARCHAR(500),
                       author_id INT NOT NULL,
                       category_id INT,
                       status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') DEFAULT 'DRAFT',
                       views INT DEFAULT 0,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       published_at TIMESTAMP NULL,
                       FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
                       FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
                       INDEX idx_status (status),
                       INDEX idx_author (author_id),
                       INDEX idx_published (published_at)
);

-- Tabela za tagove
CREATE TABLE tags (
                      id INT PRIMARY KEY AUTO_INCREMENT,
                      name VARCHAR(50) NOT NULL UNIQUE,
                      slug VARCHAR(50) UNIQUE
);

-- Tabela za povezivanje postova i tagova (Many-to-Many)
CREATE TABLE post_tags (
                           post_id INT,
                           tag_id INT,
                           PRIMARY KEY (post_id, tag_id),
                           FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
                           FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

-- Tabela za komentare
CREATE TABLE comments (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          post_id INT NOT NULL,
                          user_id INT NOT NULL,
                          parent_comment_id INT NULL,
                          content TEXT NOT NULL,
                          is_approved BOOLEAN DEFAULT TRUE,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                          FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
                          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                          FOREIGN KEY (parent_comment_id) REFERENCES comments(id) ON DELETE CASCADE,
                          INDEX idx_post (post_id),
                          INDEX idx_user (user_id)
);

-- Tabela za lajkove na postove
CREATE TABLE post_likes (
                            user_id INT,
                            post_id INT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            PRIMARY KEY (user_id, post_id),
                            FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                            FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
);

-- Insertovanje početnih podataka za role
INSERT INTO roles (name) VALUES
                             ('READER'),    -- Obični čitalac
                             ('AUTHOR'),    -- Autor koji može pisati postove
                             ('ADMIN');     -- Administrator

-- Insertovanje početnih kategorija
INSERT INTO categories (name, description, slug) VALUES
                                                     ('Tehnologija', 'Postovi o najnovijim tehnologijama i trendovima', 'tehnologija'),
                                                     ('Sport', 'Sportske vesti i analize', 'sport'),
                                                     ('Kultura', 'Kulturni događaji i recenzije', 'kultura'),
                                                     ('Nauka', 'Naučna otkrića i edukacija', 'nauka'),
                                                     ('Putovanja', 'Putopisi i saveti za putovanja', 'putovanja'),
                                                     ('Hrana', 'Recepti i restorani', 'hrana'),
                                                     ('Lifestyle', 'Saveti za svakodnevni život', 'lifestyle');

-- Insertovanje početnih tagova
INSERT INTO tags (name, slug) VALUES
                                  ('Java', 'java'),
                                  ('Spring Boot', 'spring-boot'),
                                  ('React', 'react'),
                                  ('JavaScript', 'javascript'),
                                  ('Tutorial', 'tutorial'),
                                  ('Vesti', 'vesti'),
                                  ('Saveti', 'saveti'),
                                  ('Recenzija', 'recenzija');

-- Kreiranje test korisnika (password: password123)
INSERT INTO users (first_name, last_name, email, contact_number, password, bio) VALUES
                                                                                    ('Admin', 'User', 'admin@blog.com', '123456789', '$2a$10$DowJ9Ik0aPfHgK2W2Z8iKOACMdBHMFJ3lPB2xPBOx8JKywkfCL9xm', 'Administrator sistema'),
                                                                                    ('Marko', 'Marković', 'marko@blog.com', '987654321', '$2a$10$DowJ9Ik0aPfHgK2W2Z8iKOACMdBHMFJ3lPB2xPBOx8JKywkfCL9xm', 'Autor i blogger'),
                                                                                    ('Ana', 'Anić', 'ana@blog.com', '555666777', '$2a$10$DowJ9Ik0aPfHgK2W2Z8iKOACMdBHMFJ3lPB2xPBOx8JKywkfCL9xm', 'Čitalac bloga');

-- Dodela rola korisnicima
INSERT INTO user_roles (user_id, role_id) VALUES
                                              (1, 3), -- Admin user -> ADMIN
                                              (2, 2), -- Marko -> AUTHOR
                                              (3, 1); -- Ana -> READER