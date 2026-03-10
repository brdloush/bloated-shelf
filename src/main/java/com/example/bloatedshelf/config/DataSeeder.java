package com.example.bloatedshelf.config;

import com.example.bloatedshelf.domain.*;
import com.example.bloatedshelf.repository.*;
import net.datafaker.Faker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Profile("seed")
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;
    private final GenreRepository genreRepository;
    private final LibraryMemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final LoanRecordRepository loanRepository;

    @Value("${app.seed.authors:30}")
    private int numAuthors;

    @Value("${app.seed.books:200}")
    private int numBooks;

    @Value("${app.seed.members:50}")
    private int numMembers;

    @Value("${app.seed.reviews-per-book:5}")
    private int avgReviewsPerBook;

    @Value("${app.seed.loans-per-member:8}")
    private int avgLoansPerMember;

    public DataSeeder(AuthorRepository authorRepository, BookRepository bookRepository,
                      GenreRepository genreRepository, LibraryMemberRepository memberRepository,
                      ReviewRepository reviewRepository, LoanRecordRepository loanRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
        this.genreRepository = genreRepository;
        this.memberRepository = memberRepository;
        this.reviewRepository = reviewRepository;
        this.loanRepository = loanRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (authorRepository.count() > 0) {
            log.info("Database already seeded. Skipping DataSeeder.");
            return;
        }

        log.info("Starting Data Seeding...");
        Faker faker = new Faker(new Random(42)); // Fixed seed for determinism

        List<Genre> allGenres = genreRepository.findAll();
        if (allGenres.isEmpty()) {
            throw new IllegalStateException("Genres must be seeded by Flyway first!");
        }

        // 1. Authors
        List<Author> authors = new ArrayList<>();
        for (int i = 0; i < numAuthors; i++) {
            Author author = new Author();
            author.setFirstName(faker.name().firstName());
            author.setLastName(faker.name().lastName());
            author.setBirthYear((short) faker.number().numberBetween(1800, 2000));
            author.setNationality(faker.nation().nationality());
            authors.add(author);
        }
        authorRepository.saveAll(authors);
        log.info("Seeded {} authors", authors.size());

        // 2. Books
        List<Book> books = new ArrayList<>();
        for (int i = 0; i < numBooks; i++) {
            Book book = new Book();
            book.setTitle(faker.book().title());
            book.setAuthor(authors.get(faker.number().numberBetween(0, authors.size())));
            book.setIsbn(faker.code().isbn13(true));
            book.setPublishedYear((short) faker.number().numberBetween(1850, 2024));
            book.setAvailableCopies((short) faker.number().numberBetween(1, 10));

            // Assign 1-3 random genres
            int numBookGenres = faker.number().numberBetween(1, 4);
            Set<Genre> bookGenres = new HashSet<>();
            for (int j = 0; j < numBookGenres; j++) {
                bookGenres.add(allGenres.get(faker.number().numberBetween(0, allGenres.size())));
            }
            book.setGenres(bookGenres);
            books.add(book);
        }
        bookRepository.saveAll(books);
        log.info("Seeded {} books", books.size());

        // 3. Library Members
        List<LibraryMember> members = new ArrayList<>();
        for (int i = 0; i < numMembers; i++) {
            LibraryMember member = new LibraryMember();
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            member.setUsername(faker.internet().username() + i); // ensure uniqueness
            member.setFullName(firstName + " " + lastName);
            member.setEmail(faker.internet().emailAddress(firstName.toLowerCase() + "." + lastName.toLowerCase() + i));
            Date date = faker.date().past(365 * 5, TimeUnit.DAYS); // Up to 5 years ago
            member.setMemberSince(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            members.add(member);
        }
        memberRepository.saveAll(members);
        log.info("Seeded {} library members", members.size());

        // 4. Reviews
        List<Review> reviews = new ArrayList<>();
        for (Book book : books) {
            int numReviews = faker.number().numberBetween(1, avgReviewsPerBook * 2);
            for (int i = 0; i < numReviews; i++) {
                Review review = new Review();
                review.setBook(book);
                review.setMember(members.get(faker.number().numberBetween(0, members.size())));
                review.setRating((short) faker.number().numberBetween(1, 6)); // 1 to 5
                review.setComment(faker.lorem().paragraph());
                
                Date reviewDate = faker.date().past(365, TimeUnit.DAYS);
                review.setReviewedAt(reviewDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
                reviews.add(review);
            }
        }
        reviewRepository.saveAll(reviews);
        log.info("Seeded {} reviews", reviews.size());

        // 5. Loan Records
        List<LoanRecord> loans = new ArrayList<>();
        for (LibraryMember member : members) {
            int numLoans = faker.number().numberBetween(1, avgLoansPerMember * 2);
            for (int i = 0; i < numLoans; i++) {
                LoanRecord loan = new LoanRecord();
                loan.setMember(member);
                loan.setBook(books.get(faker.number().numberBetween(0, books.size())));
                
                // Past 2 years
                Date loanDate = faker.date().past(365 * 2, TimeUnit.DAYS);
                LocalDate loanedAt = loanDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                loan.setLoanedAt(loanedAt);
                loan.setDueDate(loanedAt.plusDays(30)); // 30 day loan period
                
                // ~70% returned
                if (faker.number().numberBetween(0, 100) < 70) {
                    loan.setReturnedAt(loanedAt.plusDays(faker.number().numberBetween(1, 40)));
                }
                
                loans.add(loan);
            }
        }
        loanRepository.saveAll(loans);
        log.info("Seeded {} loan records", loans.size());

        log.info("Data Seeding Complete!");
    }
}
