

BEGIN;

-- Finalizza annuncio e chiude le altre offerte in attesa.
CREATE FUNCTION public.fn_finalizzazione_transazione() RETURNS trigger
    LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE annuncio
       SET stato = 'concluso'
     WHERE id_annuncio = NEW.id_annuncio;

    UPDATE offerta
       SET stato = 'rifiutata'
     WHERE id_annuncio = NEW.id_annuncio
       AND id_offerta <> NEW.id_offerta
       AND stato = 'in_attesa';

    RETURN NEW;
END;
$$;

-- Verifica limite massimo di immagini per annuncio.
CREATE FUNCTION public.fn_immagine_annuncio_limit() RETURNS trigger
    LANGUAGE plpgsql
AS $$
DECLARE
    v_count INT;
BEGIN
    SELECT COUNT(*) INTO v_count
      FROM immagine_annuncio
     WHERE id_annuncio = NEW.id_annuncio;

    IF v_count >= 5 THEN
        RAISE EXCEPTION 'Massimo 5 immagini per annuncio';
    END IF;

    RETURN NEW;
END;
$$;

-- Valida vincoli logici sulle offerte.
-- Controlla: offerente diverso dal venditore, importo solo per vendita,
-- messaggio obbligatorio per regalo e unicita' di offerta attiva/accettata.
CREATE FUNCTION public.fn_validazione_offerta() RETURNS trigger
    LANGUAGE plpgsql
AS $$
DECLARE
    v_tipo tipo_annuncio_enum;
    v_venditore VARCHAR;
BEGIN
    SELECT a.tipo, a.matricola_venditore
      INTO v_tipo, v_venditore
      FROM annuncio a
     WHERE a.id_annuncio = NEW.id_annuncio;

    IF v_tipo IS NULL THEN
        RAISE EXCEPTION 'Annuncio non trovato per offerta %', NEW.id_offerta;
    END IF;

    IF NEW.matricola_offerente = v_venditore THEN
        RAISE EXCEPTION 'Offerta non consentita: offerente uguale al venditore';
    END IF;

    IF v_tipo = 'vendita' THEN
        IF NEW.importo_proposto IS NULL OR NEW.importo_proposto <= 0 THEN
            RAISE EXCEPTION 'Offerta vendita: importo_proposto obbligatorio e > 0';
        END IF;
    ELSE
        IF NEW.importo_proposto IS NOT NULL THEN
            RAISE EXCEPTION 'Offerta non vendita: importo_proposto deve essere NULL';
        END IF;
    END IF;

    IF v_tipo = 'regalo' THEN
        IF NEW.messaggio IS NULL OR char_length(trim(NEW.messaggio)) = 0 THEN
            RAISE EXCEPTION 'Offerta regalo: messaggio obbligatorio';
        END IF;
        IF char_length(NEW.messaggio) > 300 THEN
            RAISE EXCEPTION 'Offerta regalo: messaggio oltre 300 caratteri';
        END IF;
    END IF;

    IF NEW.stato NOT IN ('rifiutata', 'ritirata') THEN
        IF EXISTS (
            SELECT 1
              FROM offerta o
             WHERE o.id_annuncio = NEW.id_annuncio
               AND o.matricola_offerente = NEW.matricola_offerente
               AND o.stato NOT IN ('rifiutata', 'ritirata')
               AND (TG_OP = 'INSERT' OR o.id_offerta <> NEW.id_offerta)
        ) THEN
            RAISE EXCEPTION 'Esiste gia una offerta attiva per questo annuncio';
        END IF;
    END IF;

    IF NEW.stato = 'accettata' THEN
        IF EXISTS (
            SELECT 1
              FROM offerta o
             WHERE o.id_annuncio = NEW.id_annuncio
               AND o.stato = 'accettata'
               AND (TG_OP = 'INSERT' OR o.id_offerta <> NEW.id_offerta)
        ) THEN
            RAISE EXCEPTION 'Annuncio con offerta gia accettata';
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

-- Consente oggetti solo per offerte di tipo scambio.
CREATE FUNCTION public.fn_validazione_oggetto_scambio() RETURNS trigger
    LANGUAGE plpgsql
AS $$
DECLARE
    v_tipo tipo_annuncio_enum;
BEGIN
    SELECT a.tipo
      INTO v_tipo
      FROM offerta o
      JOIN annuncio a ON a.id_annuncio = o.id_annuncio
     WHERE o.id_offerta = NEW.id_offerta;

    IF v_tipo IS NULL THEN
        RAISE EXCEPTION 'Offerta non trovata per oggetto_scambio';
    END IF;

    IF v_tipo <> 'scambio' THEN
        RAISE EXCEPTION 'Oggetto scambio consentito solo per annunci di tipo scambio';
    END IF;

    RETURN NEW;
END;
$$;

-- Valida coerenza della transazione con l'offerta accettata.
CREATE FUNCTION public.fn_validazione_transazione() RETURNS trigger
    LANGUAGE plpgsql
AS $$
DECLARE
    v_id_annuncio INT;
    v_stato stato_offerta_enum;
BEGIN
    SELECT o.id_annuncio, o.stato
      INTO v_id_annuncio, v_stato
      FROM offerta o
     WHERE o.id_offerta = NEW.id_offerta;

    IF v_id_annuncio IS NULL THEN
        RAISE EXCEPTION 'Offerta non trovata per transazione';
    END IF;

    IF v_stato <> 'accettata' THEN
        RAISE EXCEPTION 'Transazione consentita solo per offerta accettata';
    END IF;

    IF NEW.id_annuncio <> v_id_annuncio THEN
        RAISE EXCEPTION 'Transazione id_annuncio non coerente con offerta';
    END IF;

    RETURN NEW;
END;
$$;

-- Impedisce di aggiungere ai preferiti il proprio annuncio.
CREATE FUNCTION public.fn_validazione_wishlist() RETURNS trigger
    LANGUAGE plpgsql
AS $$
DECLARE
    v_venditore VARCHAR;
BEGIN
    SELECT a.matricola_venditore
      INTO v_venditore
      FROM annuncio a
     WHERE a.id_annuncio = NEW.id_annuncio;

    IF v_venditore IS NULL THEN
        RAISE EXCEPTION 'Annuncio non trovato per wishlist';
    END IF;

    IF NEW.id_utente = v_venditore THEN
        RAISE EXCEPTION 'Non puoi aggiungere ai preferiti un tuo annuncio';
    END IF;

    RETURN NEW;
END;
$$;

-- Verifica che la recensione sia tra i partecipanti della transazione.
CREATE FUNCTION public.fn_validazioni_recensioni() RETURNS trigger
    LANGUAGE plpgsql
AS $$
DECLARE
    v_offerente VARCHAR;
    v_venditore VARCHAR;
BEGIN
    SELECT o.matricola_offerente, a.matricola_venditore
      INTO v_offerente, v_venditore
      FROM transazione t
      JOIN offerta o ON o.id_offerta = t.id_offerta
      JOIN annuncio a ON a.id_annuncio = t.id_annuncio
     WHERE t.id_transazione = NEW.id_transazione;

    IF v_offerente IS NULL THEN
        RAISE EXCEPTION 'Transazione non trovata per recensione';
    END IF;

    IF NOT (
        (NEW.id_utente_recensore = v_offerente AND NEW.id_utente_recensito = v_venditore)
        OR
        (NEW.id_utente_recensore = v_venditore AND NEW.id_utente_recensito = v_offerente)
    ) THEN
        RAISE EXCEPTION 'Recensione non coerente con i partecipanti della transazione';
    END IF;

    RETURN NEW;
END;
$$;

-- Blocca recensioni a se stessi.
CREATE FUNCTION public.trg_no_self_review() RETURNS trigger
    LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.id_utente_recensito = NEW.id_utente_recensore THEN
        RAISE EXCEPTION 'Un utente non puo recensire se stesso';
    END IF;
    RETURN NEW;
END
$$;


-- Recensioni: blocca auto-recensioni.
CREATE TRIGGER no_self_review
BEFORE INSERT OR UPDATE ON public.recensioni
FOR EACH ROW EXECUTE FUNCTION public.trg_no_self_review();

-- Transazioni: aggiorna annuncio e offerte correlate.
CREATE TRIGGER trg_finalizzazione_transazione
AFTER INSERT ON public.transazione
FOR EACH ROW EXECUTE FUNCTION public.fn_finalizzazione_transazione();

-- Immagini: limita il numero per annuncio.
CREATE TRIGGER trg_immagine_annuncio_limit
BEFORE INSERT ON public.immagine_annuncio
FOR EACH ROW EXECUTE FUNCTION public.fn_immagine_annuncio_limit();

-- Offerte: valida regole di business.
CREATE TRIGGER trg_validazione_offerta
BEFORE INSERT OR UPDATE ON public.offerta
FOR EACH ROW EXECUTE FUNCTION public.fn_validazione_offerta();

-- Oggetti scambio: ammessi solo se annuncio e' scambio.
CREATE TRIGGER trg_validazione_oggetto_scambio
BEFORE INSERT OR UPDATE ON public.oggetto_scambio
FOR EACH ROW EXECUTE FUNCTION public.fn_validazione_oggetto_scambio();

-- Transazioni: coerenza con offerta accettata.
CREATE TRIGGER trg_validazione_transazione
BEFORE INSERT OR UPDATE ON public.transazione
FOR EACH ROW EXECUTE FUNCTION public.fn_validazione_transazione();

-- Wishlist: evita preferiti sul proprio annuncio.
CREATE TRIGGER trg_validazione_wishlist
BEFORE INSERT ON public.wishlist
FOR EACH ROW EXECUTE FUNCTION public.fn_validazione_wishlist();

-- Recensioni: controlla coerenza con transazione.
CREATE TRIGGER trg_validazioni_recensioni
BEFORE INSERT OR UPDATE ON public.recensioni
FOR EACH ROW EXECUTE FUNCTION public.fn_validazioni_recensioni();

COMMIT;
