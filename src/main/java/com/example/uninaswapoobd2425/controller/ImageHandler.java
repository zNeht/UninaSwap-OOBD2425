package com.example.uninaswapoobd2425.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

public class ImageHandler {

    public static final int MAX_IMG = 5;

    private final Path baseDir;

    public record SavedImage(String pathRelativoDb, int ordine, boolean isPrincipale, LocalDateTime uploadedAt) {}

    public ImageHandler() {
        // cartella esterna al jar/resources
        this.baseDir = Paths.get(System.getProperty("user.dir"), "imgAnnunci");
    }

    public List<SavedImage> saveImages(int idAnnuncio, List<File> selectedFiles) throws IOException {
        if (selectedFiles == null) return List.of();
        if (selectedFiles.size() > MAX_IMG) throw new IllegalArgumentException("Massimo 5 immagini");

        Path annuncioDir = baseDir.resolve(String.valueOf(idAnnuncio));
        Files.createDirectories(annuncioDir);

        List<SavedImage> result = new ArrayList<>();
        int ordine = 1;

        for (File f : selectedFiles) {
            String ext = getExt(f.getName());
            String filename = "img_" + ordine + "_" + UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

            Path dest = annuncioDir.resolve(filename);
            Files.copy(f.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            // path RELATIVO (come vuoi tu nel DB)
            String dbPath = "imgAnnunci/" + idAnnuncio + "/" + filename;

            result.add(new SavedImage(dbPath, ordine, ordine == 1, LocalDateTime.now()));
            ordine++;
        }

        return result;
    }

    /** Risolve il path relativo DB in un file reale per caricarlo in JavaFX */
    public Path resolveToAbsolute(String dbPath) {
        // dbPath: imgAnnunci/123/...
        return Paths.get(System.getProperty("user.dir")).resolve(dbPath);
    }

    private String getExt(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return "";
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}