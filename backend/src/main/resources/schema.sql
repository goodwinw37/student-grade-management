DROP TABLE IF EXISTS grade CASCADE;
DROP TABLE IF EXISTS registration CASCADE;
DROP TABLE IF EXISTS student CASCADE;
DROP TABLE IF EXISTS module CASCADE;

CREATE TABLE student(
  id INT PRIMARY KEY,
  firstName VARCHAR(30) NOT NULL,
  lastName VARCHAR(30) NOT NULL,
  username VARCHAR(30) NOT NULL UNIQUE,
  email VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE module(
  code VARCHAR(10) PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  mnc BOOLEAN NOT NULL
);

CREATE TABLE grade(
  id SERIAL PRIMARY KEY,
  score INT NOT NULL,
  academic_year VARCHAR(9) NOT NULL,
  semester VARCHAR(15) NOT NULL,
  student_id INT NOT NULL,
  module_code VARCHAR(10) NOT NULL,
  FOREIGN KEY (student_id)
    REFERENCES student (id),
  FOREIGN KEY (module_code)
    REFERENCES module (code),
  CONSTRAINT uk_grade_student_module_year_semester
    UNIQUE (student_id, module_code, academic_year, semester)
);

CREATE TABLE registration(
  id SERIAL PRIMARY KEY,
  student_id INT NOT NULL,
  module_code VARCHAR(10) NOT NULL,
  FOREIGN KEY (student_id)
    REFERENCES student (id),
  FOREIGN KEY (module_code)
    REFERENCES module (code)
);
