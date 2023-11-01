package ru.yandex.practicum.filmorate.validators.releaseFilmDate;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

@Retention(RUNTIME)
@Constraint(validatedBy = ReleaseFilmDateValidator.class)
@Documented
public @interface ReleaseFilmDate {

    String message() default "{ReleaseFilmDate.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    String minDate() default "1895-12-28";

}
