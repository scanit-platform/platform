package com.scanit.category.validation;

import com.scanit.category.config.CategoryProperties;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryNameValidator implements ConstraintValidator<ValidCategoryName, String> {
    private final CategoryProperties categoryProperties;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        String trimmed = value.trim();
        return !trimmed.isBlank() && trimmed.length() <= categoryProperties.getMaxNameLength();
    }
}
