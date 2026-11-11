package org.delcom.ifs00000springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.delcom.ifs00000springbootcashflow.usecase.AuthUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Smoke test: seluruh konteks Spring (AppConfig, JDBC H2, seed admin) harus dapat start. */
@SpringBootTest
@ActiveProfiles("test")
class AppTest {

    @Autowired
    private AuthUseCase authUseCase;

    @Test
    void contextLoadsAndRootAdminIsSeeded() throws Exception {
        assertNotNull(authUseCase);
        assertNotNull(authUseCase.login("admin", "admin123"));
    }
}
