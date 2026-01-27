package com.example.uninaswapoobd2425.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

public class ScambioImageHandler {

    private final Path baseDir;

    public ScambioImageHandler() {
        this.baseDir = Paths.get(System.getProperty("user.dir"), "imgScambi");
    }

    public String saveImage(int idOfferta, String sourcePath, int ordine) throws IOException {
        Path offertaDir = baseDir.resolve(String.valueOf(idOfferta));
        Files.createDirectories(offertaDir);

        File file = new File(sourcePath);
        String ext = getExt(file.getName());
        String filename = "img_" + ordine + "_" + UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);

        Path dest = offertaDir.resolve(filename);
        Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

        return "imgScambi/" + idOfferta + "/" + filename;
    }

    private String getExt(String name) {
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return "";
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
