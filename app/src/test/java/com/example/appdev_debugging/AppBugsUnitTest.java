package com.example.appdev_debugging;

import org.junit.Before;
import org.junit.Test;

import com.example.appdev_debugging.auth.AuthManager;
import com.example.appdev_debugging.auth.AuthResult;
import com.example.appdev_debugging.data.InMemoryUserRepository;
import com.example.appdev_debugging.util.Validator;

import static org.junit.Assert.*;

public class AppBugsUnitTest {

    private AuthManager authManager;

    @Before
    public void setUp() {
        authManager = new AuthManager(new InMemoryUserRepository());
    }

    /** Bug 1: VALID .ph emails are rejected */
    @Test
    public void testPhEmailsAreValid() {
        assertNull("juan@school.ph should be valid", Validator.validateEmail("juan@school.ph"));
        assertNull("juan@up.edu.ph should be valid", Validator.validateEmail("juan@up.edu.ph"));
        assertNull("juan@gov.ph should be valid", Validator.validateEmail("juan@gov.ph"));
        assertNull("juan@school.com should be valid", Validator.validateEmail("juan@school.com"));
    }

    /** Bug 2: weak password are accepted */
    @Test
    public void testWeakPasswordsRejected() {
        assertNotNull("abcdefgh (letters only) should be rejected", Validator.validateNewPassword("abcdefgh"));
        assertNotNull("12345678 (digits only) should be rejected", Validator.validateNewPassword("12345678"));
        assertNotNull("abc12 (too short) should be rejected", Validator.validateNewPassword("abc12"));
        assertNull("Password123 (letters and numbers >= 8) should be accepted", Validator.validateNewPassword("Password123"));
        assertNull("abcd1234 (letters and numbers >= 8) should be accepted", Validator.validateNewPassword("abcd1234"));
    }

    /** Bug 4: demo account doesnt work */
    @Test
    public void testDemoAccountLogin() {
        AuthManager demoAuthManager = ServiceLocator.getAuthManager();
        AuthResult result = demoAuthManager.login(ServiceLocator.DEMO_EMAIL, ServiceLocator.DEMO_PASSWORD);
        assertTrue("Demo account login must succeed", result.isSuccess());
        assertNotNull("User object should not be null", result.getUser());
        assertEquals("Demo User", result.getUser().getFullName());
    }

    /** Bug 5: capitalize emails cant log in */
    @Test
    public void testCapitalizedEmailLoginAndDuplicatePrevention() {
        // 1. Register with capitalized email
        AuthResult registerResult = authManager.register("Juan Dela Cruz", "Juan.DelaCruz@Mail.com", "Password123", "Password123");
        assertTrue("Registration with capitalized email should succeed", registerResult.isSuccess());

        // 2. Log in using exact capitalized email
        AuthResult loginResult1 = authManager.login("Juan.DelaCruz@Mail.com", "Password123");
        assertTrue("Login with exact capitalized email should succeed", loginResult1.isSuccess());

        // 3. Log in using lowercase email
        AuthResult loginResult2 = authManager.login("juan.delacruz@mail.com", "Password123");
        assertTrue("Login with lowercase email should succeed", loginResult2.isSuccess());

        // 4. Attempt to re-register same email with different casing should fail
        AuthResult duplicateRegisterResult = authManager.register("Juan Dela Cruz", "JUAN.DELACRUZ@MAIL.COM", "Password123", "Password123");
        assertFalse("Re-registering same email with different casing should fail", duplicateRegisterResult.isSuccess());
        assertEquals(AuthResult.Field.EMAIL, duplicateRegisterResult.getField());
    }
}
