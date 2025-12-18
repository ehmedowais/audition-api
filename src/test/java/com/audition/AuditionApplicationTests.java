package com.audition;

import static org.assertj.core.api.Assertions.assertThat;

import com.audition.web.AuditionController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class AuditionApplicationTests {

    @Autowired
    private transient ApplicationContext applicationContext;

    @Autowired
    private transient AuditionController auditionController;

    // TODO implement unit test. Note that an applicant should create additional unit tests as required.

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void shouldLoadAuditionController() {
        assertThat(auditionController).isNotNull();
    }

    @Test
    void shouldHaveRequiredBeansInContext() {
        assertThat(applicationContext.containsBean("auditionController")).isTrue();
        assertThat(applicationContext.containsBean("auditionService")).isTrue();
    }


    @Test
    void shouldLoadAllExpectedBeans() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        assertThat(beanNames).isNotEmpty();
        assertThat(beanNames.length).isGreaterThan(0);
    }

    @Test
    void shouldHaveActiveSpringProfiles() {
        String[] activeProfiles = applicationContext.getEnvironment().getActiveProfiles();
        assertThat(activeProfiles).isNotNull();
    }

}
