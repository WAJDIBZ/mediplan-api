package com.sirm.mediplanapi;

import com.example.mediplan.MediplanApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = MediplanApiApplication.class)
@Testcontainers
class MediplanApiApplicationTests {
    @Test
    void contextLoads() {}
}
