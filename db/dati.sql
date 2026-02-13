
BEGIN;

-- Utenti
INSERT INTO public.utente (matricola, nome, cognome, mail, password) VALUES
  ('N86001234', 'Mario',  'Rossi',   'n86001234@studenti.unina.it', 'pass1'),
  ('N86005678', 'Luigi',  'Bianchi', 'n86005678@studenti.unina.it', 'pass2'),
  ('N86009012', 'Anna',   'Verdi',   'n86009012@studenti.unina.it', 'pass3'),
  ('N86003456', 'Sofia',  'Russo',   'n86003456@studenti.unina.it', 'pass4'),
  ('N86007890', 'Paolo',  'Gallo',   'n86007890@studenti.unina.it', 'pass5'),
  ('N86001111', 'Giulia', 'Fontana', 'n86001111@studenti.unina.it', 'pass6'),
  ('N86002222', 'Marco',  'Esposito','n86002222@studenti.unina.it', 'pass7'),
  ('N86003333', 'Elena',  'Romano',  'n86003333@studenti.unina.it', 'pass8'),
  ('N86004444', 'Luca',   'Greco',   'n86004444@studenti.unina.it', 'pass9'),
  ('N86005555', 'Sara',   'Costa',   'n86005555@studenti.unina.it', 'pass10');

-- Annunci
INSERT INTO public.annuncio (id_annuncio, titolo, descrizione, prezzo, matricola_venditore, categoria, tipo, stato) VALUES
  (1,  'Libro Analisi 1',       'Testo in ottime condizioni.',         35.00, 'N86001234', 'libri',              'vendita', 'attivo'),
  (2,  'Mouse Gaming',          'Mouse RGB usato poco.',                20.00, 'N86005678', 'informatica',        'vendita', 'attivo'),
  (3,  'Chitarra acustica',     'Scambio con accessori musicali.',      NULL,  'N86009012', 'strumenti_musicali', 'scambio', 'attivo'),
  (4,  'Dispense di Fisica',    'Regalo per studenti del corso.',       NULL,  'N86003456', 'libri',              'regalo',  'attivo'),
  (5,  'Tastiera meccanica',    'Scambio con periferiche equivalenti.', NULL,  'N86007890', 'informatica',        'scambio', 'attivo'),
  (6,  'Giacca invernale',      'Taglia M, molto calda.',               60.00, 'N86001111', 'abbigliamento',      'vendita', 'attivo'),
  (7,  'Zaino universitario',   'Regalo, condizioni buone.',            NULL,  'N86002222', 'altro',              'regalo',  'attivo'),
  (8,  'Pedaliera effetti',     'Scambio con strumenti audio.',         NULL,  'N86003333', 'strumenti_musicali', 'scambio', 'attivo'),
  (9,  'Tablet 10 pollici',     'Completo di custodia.',               120.00, 'N86004444', 'informatica',        'vendita', 'attivo'),
  (10, 'Lampada da scrivania',  'Luce LED regolabile.',                 15.00, 'N86005555', 'altro',              'vendita', 'attivo');

-- Immagini annunci (una per annuncio)
INSERT INTO public.immagine_annuncio (id_immagine, id_annuncio, path, ordine, is_principale) VALUES
  (1,  1,  'imgAnnunci/1/img_1.jpg',  1, true),
  (2,  2,  'imgAnnunci/2/img_1.jpg',  1, true),
  (3,  3,  'imgAnnunci/3/img_1.jpg',  1, true),
  (4,  4,  'imgAnnunci/4/img_1.jpg',  1, true),
  (5,  5,  'imgAnnunci/5/img_1.jpg',  1, true),
  (6,  6,  'imgAnnunci/6/img_1.jpg',  1, true),
  (7,  7,  'imgAnnunci/7/img_1.jpg',  1, true),
  (8,  8,  'imgAnnunci/8/img_1.jpg',  1, true),
  (9,  9,  'imgAnnunci/9/img_1.jpg',  1, true),
  (10, 10, 'imgAnnunci/10/img_1.jpg', 1, true);

-- Offerte
INSERT INTO public.offerta (id_offerta, id_annuncio, matricola_offerente, stato, importo_proposto, messaggio) VALUES
  (1,  1,  'N86005678', 'accettata', 40.00, NULL),
  (2,  2,  'N86009012', 'in_attesa', 18.00, NULL),
  (3,  3,  'N86003456', 'accettata', NULL,  'Scambio con romanzo e cuffie.'),
  (4,  4,  'N86007890', 'accettata', NULL,  'Mi servirebbe per studio.'),
  (5,  5,  'N86001111', 'in_attesa', NULL,  'Propongo tastiera MIDI.'),
  (6,  6,  'N86002222', 'accettata', 55.00, NULL),
  (7,  7,  'N86003333', 'in_attesa', NULL,  'Posso passare domani.'),
  (8,  8,  'N86004444', 'accettata', NULL,  'Scambio con tablet 8\".'),
  (9,  9,  'N86005555', 'in_attesa', 90.00, NULL),
  (10, 10, 'N86001234', 'in_attesa', 12.00, NULL);

-- Oggetti di scambio (solo per offerte su annunci di tipo scambio)
INSERT INTO public.oggetto_scambio (id_oggetto, nome_oggetto, id_offerta, path) VALUES
  (1,  'Romanzo',        3, 'imgScambi/3/item1.jpg'), 
  (2,  'Cuffie',         3, 'imgScambi/3/item2.jpg'),
  (3,  'Plettro',        3, 'imgScambi/3/item3.jpg'),
  (4,  'Custodia',       3, 'imgScambi/3/item4.jpg'),
  (5,  'Tastiera MIDI',  5, 'imgScambi/5/item1.jpg'),
  (6,  'Supporto',       5, 'imgScambi/5/item2.jpg'),
  (7,  'Cavo USB',       5, 'imgScambi/5/item3.jpg'),
  (8,  'Tablet 8\"',      8, 'imgScambi/8/item1.jpg'),
  (9,  'Cover',          8, 'imgScambi/8/item2.jpg'),
  (10, 'Caricatore',     8, 'imgScambi/8/item3.jpg');

-- Transazioni (solo per offerte accettate)
INSERT INTO public.transazione (id_transazione, id_offerta, id_annuncio) VALUES
  (1, 1, 1),
  (2, 3, 3),
  (3, 4, 4),
  (4, 6, 6),
  (5, 8, 8);

-- Recensioni (2 per transazione)
INSERT INTO public.recensioni (id_recensione, id_transazione, id_utente_recensore, id_utente_recensito, voto, commento) VALUES
  (1, 1, 'N86005678', 'N86001234', 5, 'Venditore preciso e disponibile.'),
  (2, 1, 'N86001234', 'N86005678', 5, 'Acquirente puntuale.'),
  (3, 2, 'N86003456', 'N86009012', 4, 'Scambio corretto.'),
  (4, 2, 'N86009012', 'N86003456', 5, 'Ottima comunicazione.'),
  (5, 3, 'N86007890', 'N86003456', 4, 'Regalo molto utile.'),
  (6, 3, 'N86003456', 'N86007890', 5, 'Utente gentile.'),
  (7, 4, 'N86002222', 'N86001111', 5, 'Prodotto come descritto.'),
  (8, 4, 'N86001111', 'N86002222', 4, 'Transazione semplice.'),
  (9, 5, 'N86004444', 'N86003333', 5, 'Scambio perfetto.'),
  (10,5, 'N86003333', 'N86004444', 5, 'Affidabile e veloce.');

-- Wishlist
INSERT INTO public.wishlist (id_wishlist, id_annuncio, id_utente) VALUES
  (1,  1,  'N86009012'),
  (2,  2,  'N86003456'),
  (3,  3,  'N86007890'),
  (4,  4,  'N86001111'),
  (5,  5,  'N86002222'),
  (6,  6,  'N86003333'),
  (7,  7,  'N86004444'),
  (8,  8,  'N86005555'),
  (9,  9,  'N86001234'),
  (10, 10, 'N86005678');

COMMIT;
