package io.luliin.twoshopbackend;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.RabbitMQContainer;

/**
 * @author Julia Wigenstedt
 * Date: 2022-01-21
 */
@ContextConfiguration(initializers = AbstractContainerBaseTest.TwoShopApplicationTestsContextInitializer.class)
@ActiveProfiles(value = "test")
public abstract class AbstractContainerBaseTest implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    public static final RabbitMQContainer RABBIT_MQ_CONTAINER;

    static {
        RABBIT_MQ_CONTAINER = new RabbitMQContainer("rabbitmq:3.9.5");
        RABBIT_MQ_CONTAINER.start();
    }

    public static class TwoShopApplicationTestsContextInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(@NonNull ConfigurableApplicationContext configurableApplicationContext) {

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    configurableApplicationContext,
                    "spring.rabbitmq.host=" + RABBIT_MQ_CONTAINER.getContainerIpAddress(), "spring.rabbitmq.port=" + RABBIT_MQ_CONTAINER.getMappedPort(5672));

        }
    }
}

