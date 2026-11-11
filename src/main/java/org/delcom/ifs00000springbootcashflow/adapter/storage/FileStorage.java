package org.delcom.ifs00000springbootcashflow.adapter.storage;

import org.delcom.ifs00000springbootcashflow.domain.exception.ValidationException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Menyimpan upload (foto profil, bukti transaksi) ke folder lokal dengan nama UUID + ekstensi. */
public class FileStorage {

    public static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new UncheckedIOException("Tidak dapat membuat folder upload: " + root, e);
        }
    }

    public Path root() {
        return root;
    }

    /** Menyimpan data dan mengembalikan path relatif (nama file), mis. "3f2a...c1.png". */
    public String store(byte[] data, String originalName) throws ValidationException {
        if (data == null || data.length == 0) {
            throw new ValidationException("File kosong");
        }
        if (data.length > MAX_BYTES) {
            throw new ValidationException("Ukuran file maksimal 5 MB");
        }
        String ext = extensionOf(originalName);
        if (!ALLOWED.contains(ext)) {
            throw new ValidationException("Format file harus salah satu dari: jpg, jpeg, png, gif, webp");
        }
        String name = UUID.randomUUID() + "." + ext;
        try {
            Files.write(root.resolve(name), data);
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal menyimpan file", e);
        }
        return name;
    }

    /** Menghapus file bila ada; path null/kosong atau di luar folder upload diabaikan. */
    public void delete(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        Path target = root.resolve(relativePath).normalize();
        if (!target.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal menghapus file", e);
        }
    }

    /** URL publik untuk path relatif, atau null bila tidak ada file. */
    public static String urlOf(String relativePath) {
        return relativePath == null || relativePath.isBlank() ? null : "/uploads/" + relativePath;
    }

    private static String extensionOf(String name) {
        if (name == null) {
            return "";
        }
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
