package io.luliin.twoshopbackend.mail;

import io.luliin.twoshopbackend.AbstractContainerBaseTest;
import io.luliin.twoshopbackend.dto.AppUser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.support.TestPropertySourceUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-24
 */
@SpringBootTest
@ActiveProfiles("test")
class MailSenderTest extends AbstractContainerBaseTest {


    @Autowired
    private MailSender mailSender;

    private static AppUser appUser;


    @BeforeAll
    static void setUp() {
        System.out.println(System.getenv("WELCOME_MAIL_URL"));
        appUser = new AppUser(1L,
                "luliin",
                "twoshopinfo@gmail.com",
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

    @Override
    public void initialize(@NonNull ConfigurableApplicationContext applicationContext) {
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                applicationContext,
                "spring.rabbitmq.host=" + RABBIT_MQ_CONTAINER.getContainerIpAddress(), "spring.rabbitmq.port=" + RABBIT_MQ_CONTAINER.getMappedPort(5672));

    }
}