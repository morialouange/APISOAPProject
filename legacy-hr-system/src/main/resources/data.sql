-- ═══════════════════════════════════════════════════════════════════════
-- Jeu de donnees de demonstration du systeme RH legacy
-- ═══════════════════════════════════════════════════════════════════════
-- Tables creees par Hibernate (spring.jpa.hibernate.ddl-auto=create) :
--   employee(id, employee_code, first_name, last_name, department, status)
--   employee_skill(id, employee_id, skill_name, proficiency)
--
-- Ce script est execute apres la creation du schema thanks a
-- spring.jpa.defer-datasource-initialization=true.
-- ═══════════════════════════════════════════════════════════════════════

-- ─── Departement IT ───
INSERT INTO employee (id, employee_code, first_name, last_name, department, status) VALUES
  (1, 'EMP-001', 'Alice',   'Bernard',  'IT',        'AVAILABLE'),
  (2, 'EMP-002', 'Bruno',   'Clement',  'IT',        'BUSY'),
  (3, 'EMP-003', 'Claire',  'Dubois',   'IT',        'ON_LEAVE'),
  (4, 'EMP-004', 'David',   'Essa',     'IT',        'AVAILABLE');

-- ─── Departement MARKETING ───
INSERT INTO employee (id, employee_code, first_name, last_name, department, status) VALUES
  (5, 'EMP-005', 'Emma',    'Fontaine', 'MARKETING', 'BUSY'),
  (6, 'EMP-006', 'Felix',   'Gauthier', 'MARKETING', 'AVAILABLE'),
  (7, 'EMP-007', 'Grace',   'Haddad',   'MARKETING', 'AVAILABLE');

-- ─── Departement FINANCE ───
INSERT INTO employee (id, employee_code, first_name, last_name, department, status) VALUES
  (8, 'EMP-008', 'Hugo',    'Irvine',   'FINANCE',   'AVAILABLE'),
  (9, 'EMP-009', 'Ines',    'Jourdain', 'FINANCE',   'ON_LEAVE');

-- ─── Competences (table employee_skill) ───
INSERT INTO employee_skill (id, employee_id, skill_name, proficiency) VALUES
  -- Alice Bernard (IT, disponible)
  (1,  1, 'Java',          'EXPERT'),
  (2,  1, 'Spring Boot',   'EXPERT'),
  (3,  1, 'SOAP',          'INTERMEDIATE'),
  (4,  1, 'PostgreSQL',    'INTERMEDIATE'),
  -- Bruno Clement (IT, occupe)
  (5,  2, 'Python',        'EXPERT'),
  (6,  2, 'Docker',        'INTERMEDIATE'),
  (7,  2, 'Machine Learning', 'INTERMEDIATE'),
  -- Claire Dubois (IT, en conge)
  (8,  3, 'JavaScript',    'EXPERT'),
  (9,  3, 'React',         'INTERMEDIATE'),
  -- David Essa (IT, disponible)
  (10, 4, 'SQL',           'EXPERT'),
  (11, 4, 'MongoDB',       'EXPERT'),
  (12, 4, 'REST API',      'INTERMEDIATE'),
  -- Emma Fontaine (MARKETING, occupee)
  (13, 5, 'SEO',           'EXPERT'),
  (14, 5, 'Google Ads',    'INTERMEDIATE'),
  -- Felix Gauthier (MARKETING, disponible)
  (15, 6, 'Content Writing', 'EXPERT'),
  (16, 6, 'Communication', 'INTERMEDIATE'),
  -- Grace Haddad (MARKETING, disponible)
  (17, 7, 'Analyse de donnees', 'INTERMEDIATE'),
  (18, 7, 'Excel',         'EXPERT'),
  -- Hugo Irvine (FINANCE, disponible)
  (19, 8, 'Comptabilite',  'EXPERT'),
  (20, 8, 'Excel',         'EXPERT'),
  -- Ines Jourdain (FINANCE, en conge)
  (21, 9, 'Audit',         'EXPERT'),
  (22, 9, 'Fiscalite',     'INTERMEDIATE');

-- Remise a zero des sequences d'identifiants auto-incrementes, afin que
-- les prochaines insertions generees par l'application ne rentrent pas en
-- collision avec les identifiants fixes ci-dessus.
ALTER TABLE employee ALTER COLUMN id RESTART WITH 100;
ALTER TABLE employee_skill ALTER COLUMN id RESTART WITH 100;
