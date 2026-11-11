package org.delcom.ifs00000springbootcashflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DotEnvTest {

    @AfterEach
    void cleanup() {
        for (String k : new String[]{"DOTENV_A", "DOTENV_B", "DOTENV_C", "DOTENV_D"}) {
            System.clearProperty(k);
        }
    }

    @Test
    void loadsKeysCommentsAndQuotes(@TempDir Path dir) throws Exception {
        Path f = dir.resolve(".env");
        Files.writeString(f, "# komentar\n\nDOTENV_A=satu\nDOTENV_B=\"dua spasi\"\nDOTENV_C='tiga'\nbaris-tanpa-sama\n=kosong\nDOTENV_D=\"x\n");
        DotEnv.load(f);
        assertEquals("satu", System.getProperty("DOTENV_A"));
        assertEquals("dua spasi", System.getProperty("DOTENV_B"));
        assertEquals("tiga", System.getProperty("DOTENV_C"));
        assertEquals("\"x", System.getProperty("DOTENV_D"));
    }

    @Test
    void doesNotOverrideExistingProperty(@TempDir Path dir) throws Exception {
        System.setProperty("DOTENV_A", "dari-sistem");
        Path f = dir.resolve(".env");
        Files.writeString(f, "DOTENV_A=dari-file\n");
        DotEnv.load(f);
        assertEquals("dari-sistem", System.getProperty("DOTENV_A"));
    }

    @Test
    void missingFileIsIgnored(@TempDir Path dir) {
        DotEnv.load(dir.resolve("tidak-ada.env"));
        assertNull(System.getProperty("DOTENV_A"));
    }

    @Test
    void defaultLoadReadsDotEnvInWorkingDirectoryIfPresent() {
        DotEnv.load();
    }
}
