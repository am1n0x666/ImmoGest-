package immogest.service;
import immogest.modele.*;
import immogest.exceptions.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.*;
public class ServiceSaisie {
    private static final Logger LOGGER=Logger.getLogger(ServiceSaisie.class.getName());
    private final ServiceImmoGest service;
    public ServiceSaisie(ServiceImmoGest service) { Validation.objet(service,"Service"); this.service=service; }
    public LocalDate date(String texte,String champ) {
        if (texte==null || texte.isBlank()) { throw new SaisieException(champ,"Date obligatoire (AAAA-MM-JJ)."); }
        try { return LocalDate.parse(texte.trim()); }
        catch (DateTimeParseException e) { LOGGER.log(Level.WARNING,"Date invalide.",e); throw new SaisieException(champ,"Date invalide : utilise AAAA-MM-JJ."); }
    }
    public double nombre(String texte,String champ) {
        if (texte==null || texte.isBlank()) { throw new SaisieException(champ,"Nombre obligatoire."); }
        try {
            double valeur=Double.parseDouble(texte.trim().replace(',','.'));
            if (!Double.isFinite(valeur) || valeur<=0) { throw new SaisieException(champ,"Le nombre doit être strictement positif."); }
            return valeur;
        } catch (NumberFormatException e) { LOGGER.log(Level.WARNING,"Nombre invalide.",e); throw new SaisieException(champ,"Nombre invalide."); }
    }
    public void texte(String valeur,String champ) {
        if (valeur==null || valeur.isBlank() || valeur.length()>255) { throw new SaisieException(champ,"Texte obligatoire, 255 caractères maximum."); }
    }
    public List<Bien> rechercher(String type,String ville,String maximum,String debut,String fin) {
        Validation.objet(type,"Type"); Validation.objet(ville,"Ville");
        double max=maximum==null || maximum.isBlank() ? Double.MAX_VALUE : nombre(maximum,"maximum");
        LocalDate d=date(debut,"debut"); LocalDate f=date(fin,"fin");
        if (!f.isAfter(d)) { throw new SaisieException("fin","La fin doit être après le début."); }
        return service.rechercherBiens(type,ville,max,d,f);
    }
    public Bail signer(Bien bien,Locataire locataire,Gestionnaire gestionnaire,String debut,String fin,String dateEtat,String defauts) throws BienIndisponibleException {
        if (bien==null) { throw new SaisieException("bien","Choisis un bien."); }
        if (locataire==null) { throw new SaisieException("locataire","Choisis un locataire."); }
        if (gestionnaire==null) { throw new SaisieException("gestionnaire","Choisis un gestionnaire."); }
        LocalDate d=date(debut,"debut"); LocalDate f=date(fin,"fin"); LocalDate e=date(dateEtat,"dateEtat");
        if (!f.isAfter(d)) { throw new SaisieException("fin","La fin doit être après le début."); }
        if (e.isBefore(d) || e.isAfter(LocalDate.now())) { throw new SaisieException("dateEtat","Date entre le début du bail et aujourd'hui attendue."); }
        Validation.objet(defauts,"Défauts"); List<String> liste=new ArrayList<>();
        if (!defauts.isBlank()) { for (String s:defauts.split(";")) { texte(s,"defauts"); liste.add(s.trim()); } }
        return service.signerBailAvecEtat(bien,locataire,gestionnaire,d,f,e,liste);
    }
    public void ajouterBien(String type,String adresse,String ville,String surface,String loyer) {
        texte(type,"type"); texte(adresse,"adresse"); texte(ville,"ville");
        service.enregistrerBien(service.creerBien(type,adresse.trim(),ville.trim(),nombre(surface,"surface"),nombre(loyer,"loyer")));
    }
    public void ajouterLocataire(String nom,String telephone,String email) {
        texte(nom,"nom"); texte(telephone,"telephone"); texte(email,"email");
        if (!email.contains("@")) { throw new SaisieException("email","L'email doit contenir @."); }
        service.enregistrerLocataire(new Locataire(nom.trim(),telephone.trim(),email.trim()));
    }
    public void ajouterGestionnaire(String nom,String matricule) {
        texte(nom,"nomGestionnaire"); texte(matricule,"matricule");
        service.enregistrerGestionnaire(new Gestionnaire(nom.trim(),matricule.trim()));
    }
    public Quittance emettre(Bail bail,String date) {
        if (bail==null) { throw new SaisieException("bail","Choisis un bail."); }
        return service.calculerQuittance(bail,date(date,"emission"));
    }
    public String afficherQuittance(Quittance quittance) {
        Validation.objet(quittance,"Quittance");
        String texte="QUITTANCE DE LOYER\n\nLocataire : "+quittance.getBail().getLocataire().getNom()+"\nBien : "+quittance.getBail().getBien()+"\nÉmission : "+quittance.getDateEmission()+"\n\n";
        for (Quittance.LigneQuittance ligne:quittance.getLignes()) { texte+=ligne.getDesignation()+" : "+String.format(Locale.FRANCE,"%.2f",ligne.getMontant())+"\n"; }
        return texte+"\nTOTAL : "+String.format(Locale.FRANCE,"%.2f",quittance.getMontantTotal());
    }
}
