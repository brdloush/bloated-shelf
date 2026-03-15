-- Create sequences with INCREMENT BY matching Hibernate's allocationSize (50)
-- Use pooled optimizer: sequence value is the HIGH end of the ID range
CREATE SEQUENCE seq_author     INCREMENT BY 50 START WITH 50;
CREATE SEQUENCE seq_book       INCREMENT BY 50 START WITH 50;
CREATE SEQUENCE seq_library_member INCREMENT BY 50 START WITH 50;
CREATE SEQUENCE seq_review     INCREMENT BY 50 START WITH 50;
CREATE SEQUENCE seq_loan_record INCREMENT BY 50 START WITH 50;

-- Genre already has rows from V2, so start well above the existing max
CREATE SEQUENCE seq_genre INCREMENT BY 50 START WITH 1000;

-- Drop GENERATED ALWAYS AS IDENTITY and replace with sequence-backed defaults
-- (GENERATED ALWAYS prohibits explicit inserts, which Hibernate SEQUENCE strategy needs)
ALTER TABLE author         ALTER COLUMN id DROP IDENTITY, ALTER COLUMN id SET DEFAULT nextval('seq_author');
ALTER TABLE book           ALTER COLUMN id DROP IDENTITY, ALTER COLUMN id SET DEFAULT nextval('seq_book');
ALTER TABLE library_member ALTER COLUMN id DROP IDENTITY, ALTER COLUMN id SET DEFAULT nextval('seq_library_member');
ALTER TABLE review         ALTER COLUMN id DROP IDENTITY, ALTER COLUMN id SET DEFAULT nextval('seq_review');
ALTER TABLE loan_record    ALTER COLUMN id DROP IDENTITY, ALTER COLUMN id SET DEFAULT nextval('seq_loan_record');
ALTER TABLE genre          ALTER COLUMN id DROP IDENTITY, ALTER COLUMN id SET DEFAULT nextval('seq_genre');