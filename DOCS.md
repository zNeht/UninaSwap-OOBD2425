 UninaSwap – Guida rapida alle classi

Panoramica delle principali classi con ruolo e metodi chiave. Percorsi relativi al progetto.

## Avvio e struttura base
- **Launcher.java**: entry point che lancia `MainApp`.
- **MainApp.java**: crea lo `Stage`, carica `login.fxml`, abilita trascinamento finestra e scena trasparente.
- **module-info.java**: dichiara moduli richiesti (JavaFX, java.sql, java.prefs ecc.).

## Sessione e autenticazione
- **Session.java** (model): conserva la matricola dell’utente in memoria e in `Preferences` (persistente).
  - `getMatricola()`: valore salvato o `null`.
  - `setMatricola(String)`: salva/rimuove matricola in memoria e prefs.
  - `clear()`: equivalente a `setMatricola(null)`.
- **loginController.java**:
  - `initialize()`: gestisce drag finestra e, se c’è matricola salvata, apre direttamente la homepage.
  - `handleLogin()`: valida input, verifica credenziali su DB, salva matricola in `Session`, apre homepage.
  - Navigazione: `onRegisterLinkClick()` per passare a registrazione; `handleMinimize/handleClose`.
- **registerController.java**:
  - Setup etichette flottanti sui campi.
  - `handleRegister()`: valida, controlla duplicati matricola/mail, inserisce `utente`.
  - Navigazione: `onLoginLinkClick()` per tornare al login; `handleMinimize/handleClose`.

## DB e DAO
- **DB.java**: `getConnection()` apre connessione PostgreSQL (URL/USER/PASS hardcoded).
- **annuncioDAO.java**:
  - `getAnnunciAttiviConImgPrincipale...`: elenca annunci attivi (filtri tipo/categoria/preferiti) con path immagine principale.
  - `insertAnnuncioReturningId(...)`: inserisce annuncio e restituisce id.
- **immagineAnnuncioDAO.java**:
  - `insertImages(...)`: inserisce batch immagini con ordine/is_principale.
  - `getImagePathsByAnnuncio(int)`: path ordinati (principale prima).
- **offertaDAO.java**:
  - `getOfferteRicevute(matricolaVenditore)` / `getOfferteInviate(matricolaOfferente)`: viste offerte con dati annuncio e immagine principale.
  - Metodi di update (stato, messaggio, importo) e `rifiutaAltreOfferteInAttesa(...)`.
  - `creaOffertaScambio(...)`: inserisce offerta di scambio in attesa.
- **wishlistDAO**, **recensioniDAO/recensioneDAO**, **oggettoScambioDAO**: CRUD specifici.
- **statisticheDAO.java**:
  - `getStatistiche(matricola)`: aggrega tutto.
  - Conteggi offerte inviate/accettate per tipologia (offerente).
  - Min/Max/Media/Count importi offerte di vendita accettate dall’offerente.
  - Min/Max/Media/Count importi offerte di vendita accettate dal venditore sui propri annunci.

## Modelli
- **annuncio.java**: POJO con id, titolo, descrizione, categoria, tipo, prezzo, stato, venditore, email venditore, immaginePath. Helper `isVendita/Scambio/Regalo`.
- **Enumerazioni**: `categoriaAnnuncio`, `tipoAnnuncio`, `statoAnnuncio`, `statoOfferta`.
- Altri POJO: `offerta`, `recensione`, `oggettoScambio`, `transazione`, `utente`, `Session` (già descritto).

## Controller UI principali
- **homepageController.java**:
  - Gestione menu filtri (vendita/scambio/regalo/preferiti/offerte/recensioni/statistiche).
  - `caricaAnnunci(...)` / `caricaPreferiti(...)`: leggono da DAO, creano card FXML, aprono dettaglio al click.
  - `openDettaglio(annuncio)`: apre popup con `dettaglioAnnuncio.fxml`.
  - Gestione overlay modale, profilo popup, logout (svuota Session).
  - Caricamenti viste secondarie: offerte, recensioni, statistiche.
- **annuncioCardController.java**:
  - `setData(annuncio, wishlisted, count, removeOnUnfavorite)`: popola card, carica immagine da filesystem, aggiorna badge/prezzo, stato wishlist.
  - Gestione wishlist: add/remove via DAO, aggiorna contatore, opzionale rimozione dalla griglia.
- **dettaglioAnnuncioController.java**:
  - `setAnnuncio(annuncio)`: testi (titolo, descrizione, badge tipo, prezzo), venditore email.
  - Carousel immagini: recupera path da `immagineAnnuncioDAO`, principale prima; frecce prev/next; click apre anteprima full-screen; fallback se mancano immagini.
  - `loadOfferPane(...)`: carica pannello offerta specifico (vendita/scambio/regalo).
- **nuovoAnnuncioController.java**:
  - Form creazione annuncio: toggle tipo (abilita/disabilita prezzo), combo categoria, caricamento immagini (max 5) con anteprime removibili.
  - `handlePubblicaAnnuncio()`: valida input, inserisce annuncio, salva immagini su disco (`ImageHandler`), inserisce record immagini, commit; chiude overlay.
  - `handleAnnulla()`: chiude overlay modale.
- **offerteController / offertaCardController / offerPaneController / offerVenditaController / offerScambioController / offerRegaloController**:
  - Elenco e card offerte, form invio/offerta per il dettaglio, logica accettazione/rifiuto (con `offertaDAO` e `rifiutaAltreOfferteInAttesa`).
- **recensioniController / recensioneCardController**:
  - Lista recensioni e card singole (autore, testo, rating), dati da DAO recensioni.
- **statisticheController.java**:
  - `loadData()`: se loggato, legge `statisticheDAO`.
  - `renderPie(...)`: popola PieChart offerte inviate/accettate per tipologia (gestione “Nessun dato”).
  - Label testuali: conteggi inviate/accettate per tipologia.
  - `lblStatsVendita`: totale/min/max/media offerte di vendita accettate (come offerente).
  - `lblAccettateVenditore`: totale/min/max/media offerte di vendita accettate dal venditore sui propri annunci.
- **ImageHandler.java**:
  - Base dir `imgAnnunci/` (relativa a `user.dir`).
  - `saveImages(idAnnuncio, files)`: copia immagini in `imgAnnunci/{id}/img_{ordine}_{uuid}.ext`, restituisce path relativo per DB, ordine, flag principale, timestamp.
  - `resolveToAbsolute(dbPath)`: risolve path DB in assoluto.

## Risorse UI
- FXML in `src/main/resources/com/example/uninaswapoobd2425/` (login, homepage, dettaglioAnnuncio, nuovoAnnuncio, offerte, recensioni, statistiche, card).
- **style.css**: stili globali (colori, card, badge, scrollbar, frecce carousel, menu, bottoni, ecc.).

## Flusso tipico
1. Avvio app → `loginController` controlla sessione salvata → eventualmente auto-login.
2. Login: verifica credenziali su DB → salva matricola in `Session` (prefs) → homepage.
3. Homepage: carica annunci, apre dettagli, gestisce overlay (nuovo annuncio/dettaglio).
4. Creazione annuncio: inserisce annuncio + immagini su disco e in tabella `immagine_annuncio`.
5. Offerte/recensioni/statistiche: viste dedicate caricano dati via rispettivi DAO.
6. Logout: `Session.clear()` → ritorno a login senza auto-login.

