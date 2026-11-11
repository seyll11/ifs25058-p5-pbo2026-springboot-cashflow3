package org.delcom.ifs00000springbootcashflow.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.delcom.ifs00000springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs00000springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs00000springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs00000springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs00000springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PresenterAndStorageTest {

    private final FinancePresenter presenter = new FinancePresenter();
    private final Transaction income =
            new Transaction(3, 1, "Gaji", 1_000_000, TransactionType.INCOME, null, LocalDateTime.of(2026, 3, 4, 9, 5));
    private final Transaction expense =
            new Transaction(4, 1, "Makan", 25_000, TransactionType.EXPENSE, null, null);

    @Test
    void formatsMoneyWithThousandsAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.money(1_000_000));
        assertEquals("Rp 0.00", presenter.money(0));
        assertEquals("-Rp 5.00", presenter.money(-5));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.balance(4_975_000));
    }

    @Test
    void formatsLinesLabelsDatesAndMessages() {
        assertEquals("[3] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.line(income));
        assertEquals("[4] Makan | Rp 25,000.00 | [Pengeluaran]", presenter.line(expense));
        assertEquals("04-03-2026 09:05", presenter.date(income.createdAt()));
        assertEquals("-", presenter.date(null));
        assertEquals("Pemasukan Rp 1,000,000.00 berhasil dicatat", presenter.addedMessage(income));
        assertEquals("Transaksi [3] berhasil diperbarui", presenter.updatedMessage(income));
        assertEquals("Transaksi [4] Makan dihapus", presenter.deletedMessage(expense));
        assertEquals("Gagal: oops", presenter.errorMessage(new ValidationException("oops")));
    }

    @Test
    void storesDeletesAndValidatesFiles(@TempDir Path dir) throws Exception {
        FileStorage storage = new FileStorage(dir.resolve("up").toString());
        assertTrue(Files.isDirectory(storage.root()));

        String name = storage.store(new byte[]{1, 2, 3}, "Foto.PNG");
        assertTrue(name.endsWith(".png"));
        assertTrue(Files.exists(storage.root().resolve(name)));
        assertEquals("/uploads/" + name, FileStorage.urlOf(name));
        assertNull(FileStorage.urlOf(null));
        assertNull(FileStorage.urlOf(" "));

        storage.delete(name);
        assertFalse(Files.exists(storage.root().resolve(name)));
        storage.delete(name);
        storage.delete(null);
        storage.delete("  ");

        Path outside = dir.resolve("luar.txt");
        Files.writeString(outside, "jangan dihapus");
        storage.delete("../luar.txt");
        assertTrue(Files.exists(outside));

        assertThrows(ValidationException.class, () -> storage.store(null, "a.png"));
        assertThrows(ValidationException.class, () -> storage.store(new byte[0], "a.png"));
        assertThrows(ValidationException.class, () -> storage.store(new byte[(int) FileStorage.MAX_BYTES + 1], "a.png"));
        assertThrows(ValidationException.class, () -> storage.store(new byte[]{1}, "skrip.exe"));
        assertThrows(ValidationException.class, () -> storage.store(new byte[]{1}, "tanpa-ekstensi"));
        assertThrows(ValidationException.class, () -> storage.store(new byte[]{1}, null));
    }

    @Test
    void failsFastWhenUploadDirCannotBeCreated(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("berkas");
        Files.writeString(file, "x");
        assertThrows(java.io.UncheckedIOException.class, () -> new FileStorage(file.resolve("sub").toString()));
    }
}
