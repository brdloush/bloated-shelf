CREATE TABLE author (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    birth_year SMALLINT,
    nationality VARCHAR(100)
);

CREATE TABLE genre (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE book (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author_id BIGINT NOT NULL REFERENCES author(id),
    isbn VARCHAR(20) UNIQUE,
    published_year SMALLINT,
    available_copies SMALLINT NOT NULL DEFAULT 1
);

CREATE TABLE book_genre (
    book_id BIGINT NOT NULL REFERENCES book(id),
    genre_id BIGINT NOT NULL REFERENCES genre(id),
    PRIMARY KEY (book_id, genre_id)
);

CREATE TABLE library_member (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    full_name VARCHAR(200) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    member_since DATE NOT NULL
);

CREATE TABLE review (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id BIGINT NOT NULL REFERENCES book(id),
    member_id BIGINT NOT NULL REFERENCES library_member(id),
    rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    reviewed_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE loan_record (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    book_id BIGINT NOT NULL REFERENCES book(id),
    member_id BIGINT NOT NULL REFERENCES library_member(id),
    loaned_at DATE NOT NULL,
    due_date DATE NOT NULL,
    returned_at DATE
);

-- Basic indexes for common lookups
CREATE INDEX idx_book_author ON book(author_id);
CREATE INDEX idx_review_book ON review(book_id);
CREATE INDEX idx_loan_member ON loan_record(member_id);
CREATE INDEX idx_loan_book ON loan_record(book_id);
