package immogest.service;
import immogest.modele.*;
import immogest.exceptions.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.*;
public class ServiceImmoGest {
    private static final Logger LOGGER=Logger.getLogger(ServiceImmoGest.class.getName());
    private final List<Bien> biens=new ArrayList<>();
    private final List<Bail> baux=new ArrayList<>();
    private final List<Locataire> locataires=new ArrayList<>();
    private final List<Gestionnaire> gestionnaires=new ArrayList<>();
    private final ServiceQuittancement quittancement=new ServiceQuittancement();
    private ServicePersistance persistance;
    public ServiceImmoGest() { }
    public ServiceImmoGest(ServicePersistance persistance) {
        Validation.objet(persistance,"Persistance"); this.persistance=persistance;
        biens.addAll(persistance.getBienDao().findAll()); baux.addAll(persistance.getBailDao().findAll());
        locataires.addAll(persistance.getLocataireDao().findAll()); gestionnaires.addAll(persistance.getGestionnaireDao().findAll());
    }
    public void enregistrerBien(Bien bien) {
        Validation.objet(bien,"Bien"); bien.valider();
        if (persistance!=null) { persistance.getBienDao().create(bien); }
        biens.add(bien);
    }
    public void enregistrerLocataire(Locataire locataire) {
        Validation.objet(locataire,"Locataire"); locataire.valider();
        if (persistance!=null) { persistance.getLocataireDao().create(locataire); }
        locataires.add(locataire);
    }
    public void enregistrerGestionnaire(Gestionnaire gestionnaire) {
        Validation.objet(gestionnaire,"Gestionnaire"); gestionnaire.valider();
        if (persistance!=null) { persistance.getGestionnaireDao().create(gestionnaire); }
        gestionnaires.add(gestionnaire);
    }
    public List<Bien> getBiens() { return new ArrayList<>(biens); }
    public List<Bail> getBaux() { return new ArrayList<>(baux); }
    public List<Locataire> getLocataires() { return new ArrayList<>(locataires); }
    public List<Gestionnaire> getGestionnaires() { return new ArrayList<>(gestionnaires); }
    public List<Bail> getBauxActifs() {
        List<Bail> resultat=new ArrayList<>(); for (Bail bail:baux) { if (!bail.isCloture()) { resultat.add(bail); } } return resultat;
    }
    public Bail signerBail(Bien bien,Locataire locataire,Gestionnaire gestionnaire,LocalDate debut,LocalDate fin) throws BienIndisponibleException {
        Bail bail=new Bail(bien,locataire,gestionnaire,debut,fin);
        try {
            verifierDisponibilite(bail);
            if (persistance!=null) { persistance.getBailDao().create(bail); }
            baux.add(bail); return bail;
        } catch (BienIndisponibleException e) {
            LOGGER.log(Level.WARNING,e.getMessage(),e); throw e;
        } finally { LOGGER.info("Demande de signature terminée."); }
    }
    public Bail signerBailAvecEtat(Bien bien,Locataire locataire,Gestionnaire gestionnaire,LocalDate debut,LocalDate fin,LocalDate dateEtat,List<String> defauts) throws BienIndisponibleException {
        Bail bail=new Bail(bien,locataire,gestionnaire,debut,fin);
        EtatDesLieux etat=new EtatDesLieux(bail,dateEtat,defauts);
        try {
            verifierDisponibilite(bail);
            if (persistance!=null) { persistance.enregistrerBailEtEtat(bail,etat); }
            baux.add(bail); return bail;
        } catch (BienIndisponibleException e) {
            LOGGER.log(Level.WARNING,e.getMessage(),e); throw e;
        } finally { LOGGER.info("Demande de bail et état des lieux terminée."); }
    }
    private void verifierDisponibilite(Bail bail) throws BienIndisponibleException {
        for (Bail existant:baux) {
            Bien a=existant.getBien(); Bien b=bail.getBien();
            boolean memeBien=a==b || (a.getId()!=null && a.getId().equals(b.getId()));
            if (memeBien && existant.chevauche(bail.getDateDebut(),bail.getDateFin())) {
                throw new BienIndisponibleException("Ce bien est déjà loué pour cette période.");
            }
        }
    }
    public void cloturerBail(Bail bail) {
        Validation.objet(bail,"Bail");
        if (bail.isCloture()) { throw new IllegalStateException("Ce bail est déjà clôturé."); }
        if (persistance!=null) { persistance.cloturerBail(bail.getId()); }
        bail.cloturer(); assert bail.isCloture();
    }
    public Quittance calculerQuittance(Bail bail,LocalDate date) {
        Validation.objet(bail,"Bail"); Validation.objet(date,"Date d'émission");
        Quittance q=quittancement.genererQuittance(bail,date);
        if (persistance!=null) { persistance.getQuittanceDao().create(q); }
        return q;
    }
    public EtatDesLieux creerEtatDesLieux(Bail bail,LocalDate date,List<String> defauts) {
        Validation.objet(bail,"Bail"); Validation.objet(date,"Date d'état des lieux"); Validation.objet(defauts,"Défauts");
        return new EtatDesLieux(bail,date,defauts);
    }
    public Optional<Bien> rechercherBienParAdresse(String adresse) {
        Validation.objet(adresse,"Adresse");
        for (Bien bien:biens) { if (bien.getAdresse().toLowerCase(Locale.ROOT).contains(adresse.toLowerCase(Locale.ROOT))) { return Optional.of(bien); } }
        return Optional.empty();
    }
    public Locataire rechercherLocataireParId(Long id) throws LocataireIntrouvableException {
        if (id==null || id<=0) { throw new IllegalArgumentException("Identifiant positif obligatoire."); }
        for (Locataire locataire:locataires) { if (id.equals(locataire.getId())) { return locataire; } }
        throw new LocataireIntrouvableException("Aucun locataire pour l'identifiant "+id+".");
    }
    public List<Bien> rechercherBiens(String type,String ville,double maximum,LocalDate debut,LocalDate fin) {
        Validation.objet(type,"Type"); Validation.objet(ville,"Ville");
        Validation.positif(maximum,"Loyer maximum"); Validation.objet(debut,"Début"); Validation.objet(fin,"Fin");
        if (!fin.isAfter(debut)) { throw new IllegalArgumentException("La fin doit être après le début."); }
        List<Bien> resultat=new ArrayList<>();
        for (Bien bien:biens) {
            boolean libre=true;
            for (Bail bail:baux) {
                if ((bail.getBien()==bien || (bien.getId()!=null && bien.getId().equals(bail.getBien().getId()))) && bail.chevauche(debut,fin)) { libre=false; }
            }
            if (libre && (type.equals("Tous") || type.equals(bien.getType())) && bien.getVille().toLowerCase(Locale.ROOT).contains(ville.toLowerCase(Locale.ROOT)) && bien.calculerLoyerCharges()<=maximum) { resultat.add(bien); }
        }
        return resultat;
    }
    public List<Bien> importerBiensDepuisFichier(String chemin) {
        Validation.texte(chemin,"Fichier"); List<Bien> importes=new ArrayList<>(); int numero=0;
        try (BufferedReader lecteur=new BufferedReader(new FileReader(chemin,java.nio.charset.StandardCharsets.UTF_8))) {
            String ligne;
            while ((ligne=lecteur.readLine())!=null) {
                numero++; if (ligne.isBlank()) { continue; }
                try {
                    String[] c=ligne.split(";",-1);
                    if (c.length!=5) { throw new IllegalArgumentException("Format : Type;adresse;ville;surface;loyer"); }
                    Bien bien=creerBien(c[0].trim(),c[1].trim(),c[2].trim(),Double.parseDouble(c[3].trim()),Double.parseDouble(c[4].trim()));
                    enregistrerBien(bien); importes.add(bien);
                } catch (IllegalArgumentException e) { LOGGER.log(Level.WARNING,"Ligne "+numero+" ignorée : "+e.getMessage(),e); }
            }
        } catch (IOException e) { LOGGER.log(Level.SEVERE,"Import impossible.",e); throw new IllegalArgumentException("Le fichier ne peut pas être lu.",e); }
        return importes;
    }
    public Bien creerBien(String type,String adresse,String ville,double surface,double loyer) {
        Validation.texte(type,"Type");
        switch (type) {
            case "Appartement": return new Appartement(adresse,ville,surface,loyer);
            case "Maison": return new Maison(adresse,ville,surface,loyer);
            case "LocalCommercial": return new LocalCommercial(adresse,ville,surface,loyer);
            default: throw new IllegalArgumentException("Type de bien inconnu.");
        }
    }
    public void appliquerIndexationLoyer(Bail bail,double indiceReference) {
        Validation.objet(bail,"Bail"); Validation.positif(indiceReference,"Indice de référence");
        if (bail.isCloture()) { throw new IllegalArgumentException("Le bail est clôturé."); }
        double nouveauLoyer=bail.getLoyer()*indiceReference/100;
        Validation.positif(nouveauLoyer,"Nouveau loyer");
        if (persistance!=null) { persistance.modifierLoyer(bail.getId(),nouveauLoyer); }
        bail.setLoyer(nouveauLoyer);
    }
}
