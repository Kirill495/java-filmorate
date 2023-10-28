package ru.yandex.practicum.filmorate.validators.releaseFilmDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReleaseFilmDateValidator implements ConstraintValidator<ReleaseFilmDate, LocalDate> {

  private LocalDate minDate;

  @Override
  public void initialize(ReleaseFilmDate constraintAnnotation) {
    ConstraintValidator.super.initialize(constraintAnnotation);
    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    if (!constraintAnnotation.minDate().isBlank()) {
      this.minDate = LocalDate.parse(constraintAnnotation.minDate(), formatter);
    }
  }

  @Override
  public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
    return value != null && value.isAfter(minDate);
  }

}
