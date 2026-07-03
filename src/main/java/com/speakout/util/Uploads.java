package com.speakout.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.servlet.http.Part;

/** Evidence files live outside the WAR so redeploys don't wipe them. */
public final class Uploads {

    public static final long MAX_BYTES = 50L * 1024 * 1024;

    private Uploads() {
    }

    public static Path baseDir() {
        return Path.of(System.getProperty("user.home"), "speakout-uploads");
    }

    /**
     * Stores an uploaded part under {reportId}/{safe-filename} and returns the
     * relative path recorded in EVIDENCE.file_url.
     */
    public static String save(Part part, String reportId) throws IOException {
        String original = part.getSubmittedFileName() == null ? "file" : part.getSubmittedFileName();
        String safe = original.replaceAll("[^A-Za-z0-9._-]", "_");
        Path dir = baseDir().resolve(reportId);
        Files.createDirectories(dir);
        Path target = dir.resolve(safe);
        int n = 1;
        while (Files.exists(target)) {
            target = dir.resolve(n++ + "-" + safe);
        }
        try (var in = part.getInputStream()) {
            Files.copy(in, target);
        }
        return reportId + "/" + target.getFileName();
    }

    /** Maps a MIME type to the EVIDENCE.file_type check values. */
    public static String detectType(String contentType) {
        if (contentType == null) return "document";
        if (contentType.startsWith("image/")) return "image";
        if (contentType.startsWith("video/")) return "video";
        return "document";
    }
}
