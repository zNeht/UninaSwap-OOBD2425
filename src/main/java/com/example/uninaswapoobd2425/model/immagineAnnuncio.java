package com.example.uninaswapoobd2425.model;

import java.time.LocalDateTime;

public class immagineAnnuncio {
    private int idImmagine;
    private int idAnnuncio;
    private String path;
    private int ordine;
    private boolean principale;
    private LocalDateTime uploadedAt;

    public int getIdImmagine() { return idImmagine; }
    public int getIdAnnuncio() { return idAnnuncio; }
    public String getPath() { return path; }
    public int getOrdine() { return ordine; }
    public boolean isPrincipale() { return principale; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }

    public void setIdImmagine(int idImmagine) { this.idImmagine = idImmagine; }
    public void setIdAnnuncio(int idAnnuncio) { this.idAnnuncio = idAnnuncio; }
    public void setPath(String path) { this.path = path; }
    public void setOrdine(int ordine) { this.ordine = ordine; }
    public void setPrincipale(boolean principale) { this.principale = principale; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
