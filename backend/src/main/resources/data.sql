INSERT INTO student (id, firstName, lastName, username, email) VALUES
  (1, 'Ada', 'Lovelace', 'ada', 'ada@example.com'),
  (2, 'Alan', 'Turing', 'alan', 'alan@example.com'),
  (3, 'Grace', 'Hopper', 'grace', 'grace@example.com');

INSERT INTO module (code, name, mnc) VALUES
  ('CS101', 'Software Engineering', TRUE),
  ('COMP0020', 'Systems Engineering', FALSE),
  ('COMP0030', 'Human Computer Interaction', FALSE);

INSERT INTO registration (student_id, module_code) VALUES
  (1, 'CS101'),
  (1, 'COMP0020'),
  (2, 'CS101'),
  (2, 'COMP0030'),
  (3, 'COMP0020'),
  (3, 'COMP0030');

INSERT INTO grade (score, academic_year, semester, student_id, module_code) VALUES
  (92, '2024/25', 'Semester 1', 1, 'CS101'),
  (85, '2024/25', 'Semester 2', 1, 'COMP0020'),
  (76, '2024/25', 'Semester 1', 2, 'CS101'),
  (88, '2024/25', 'Semester 2', 2, 'COMP0030'),
  (79, '2024/25', 'Semester 1', 3, 'COMP0020');