package ru.yandex.practicum.filmorate.validators.filmSearchParameter;

import ru.yandex.practicum.filmorate.model.FilmSearchParameter;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class SearchParameterValidator implements ConstraintValidator<FilmSearchParam, String> {

    @Override
    public void initialize(FilmSearchParam constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        try {
            for (String valueItem: value.split(",")) {
                FilmSearchParameter.valueOf(valueItem.toUpperCase());
            }
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
