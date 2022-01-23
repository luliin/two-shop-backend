package io.luliin.twoshopbackend.mail;

import io.luliin.twoshopbackend.dto.AppUser;
import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-24
 */

class MailSenderTest {


    private static MailSender mailSender;

    private static AppUser appUser;


    @BeforeAll
    static void setUp() {
        mailSender = new MailSender();
        appUser = new AppUser(1L,
                "luliin",
                "juliaemmamatilda@gmail.com",
                "Julia",
                "Wigenstedt",
                null,
                null);
    }

    @Test
    void sendWelcomeMessage() {
        final String s = mailSender.sendWelcomeMessage(appUser);
        assertNotNull(s);
    }
}