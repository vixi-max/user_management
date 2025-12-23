package com.hendisantika.usermanagement.controller;



import com.hendisantika.usermanagement.exception.CustomFieldValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomFieldValidationExceptionTest {

    @Test
    void testExceptionMessageAndFieldName() {
        String message = "Username not available";
        String fieldName = "username";

        CustomFieldValidationException exception =
                new CustomFieldValidationException(message, fieldName);

        // Vérifie que le message est correct
        assertEquals(message, exception.getMessage());

        // Vérifie que le fieldName est correct
        assertEquals(fieldName, exception.getFieldName());
    }

    @Test
    void testExceptionInheritance() {
        CustomFieldValidationException exception =
                new CustomFieldValidationException("Error", "field");

        // Vérifie que c'est bien une Exception
        assertTrue(exception instanceof Exception);
    }
}

