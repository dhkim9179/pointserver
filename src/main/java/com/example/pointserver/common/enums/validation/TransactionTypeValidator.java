package com.example.pointserver.common.enums.validation;

import com.example.pointserver.common.enums.TransactionType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TransactionTypeValidator implements ConstraintValidator<ValidTransactionType, String> {
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s.toLowerCase().equals(TransactionType.ORDER.getCode()) ||
            s.toLowerCase().equals(TransactionType.EVENT.getCode()) ||
            s.toLowerCase().equals(TransactionType.PROMOTION.getCode()) ||
            s.toLowerCase().equals(TransactionType.ADMIN.getCode())
        ) {
            return true;
        }
        return false;
    }
}
