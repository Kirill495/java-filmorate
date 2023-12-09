package ru.yandex.practicum.filmorate.validators.filmSearchParameter;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Constraint(validatedBy = SearchParameterValidator.class)
@Documented
public @interface FilmSearchParam {
    String message() default "{FilmSearchParameter.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
