//package ru.yandex.practicum.filmorate.model;
//
//import static javax.validation.Validation.byProvider;
//import static org.junit.jupiter.api.Assertions.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import ru.yandex.practicum.filmorate.validators.ReleaseDateConstraint;
//import ru.yandex.practicum.filmorate.validators.ReleaseDateValidator;
//
//import javax.validation.ConstraintViolation;
//import javax.validation.Validation;
//import javax.validation.Validator;
//import javax.validation.ValidatorFactory;
//import java.time.LocalDate;
//import java.util.Set;
//
//class FilmTest {
//
//  private Film film;
//  private Validator validator;
//  private ReleaseDateValidator validator1;
//
//  @BeforeEach
//  void setUp() {
//    film = new Film();
//    film.setName("ААА");
//    film.setDuration(100);
//    film.setDescription("");
//    film.setReleaseDate(LocalDate.now());
//    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
//    validator = factory.getValidator();
//    validator1 = new ReleaseDateValidator();
//    configuration Validation.byProvider(Film.class).providerResolver(new MyResolverStrategy()).configure();
//    ValidatorFactory factory1 = configuration.buildValidatorFactory();
//  }
//
//  @Test
//  void validationOfFilmWithBlankNameShouldFail() {
//    film.setName("");
//    Set<ConstraintViolation<Film>> violations = validator.validate(film);
//    assertEquals(1, violations.size());
//    ConstraintViolation<Film> violation = violations.stream().findAny().orElse(null);
//    assertEquals("name", violation.getPropertyPath().toString());
//    assertEquals("{javax.validation.constraints.NotBlank.message}", violation.getMessageTemplate());
//  }
//
//  @Test
//  void validationOfFilmWithTooLongDescriptionShouldFail() {
//    String description = "____________________";
//    StringBuilder filmDescription = new StringBuilder();
//    for (int i = 0; i < 15; i++) {
//      filmDescription.append(description);
//    }
//    film.setDescription(filmDescription.toString());
//    Set<ConstraintViolation<Film>> violations = validator.validate(film);
//    assertEquals(1, violations.size());
//    ConstraintViolation<Film> violation = violations.stream().findFirst().get();
//
//    assertEquals("description", violation.getPropertyPath().toString());
//    assertEquals("{javax.validation.constraints.Size.message}", violation.getMessageTemplate());
//  }
//}