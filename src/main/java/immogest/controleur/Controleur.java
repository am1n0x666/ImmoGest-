package immogest.controleur;
import immogest.App;
import immogest.modele.*;
import immogest.service.*;
import immogest.exceptions.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import java.time.LocalDate;
import java.util.logging.*;
public class Controleur {
    private static final Logger LOGGER=Logger.getLogger(Controleur.class.getName());
    private App app;
    private ServiceImmoGest service;
    private ServiceSaisie saisie;
    @FXML private BorderPane racine;
    @FXML private Label message;
    @FXML private ComboBox<String> type;
    @FXML private ComboBox<Bien> bien;
    @FXML private ComboBox<Locataire> locataire;
    @FXML private ComboBox<Gestionnaire> gestionnaire;
    @FXML private ComboBox<Bail> bail;
    @FXML private TextField ville,maximum,debut,fin,dateEtat,defauts,emission,adresse,surface,loyer,nom,telephone,email,nomGestionnaire,matricule;
    @FXML private TableView<Bien> tableau;
    @FXML private TableColumn<Bien,String> colonneType,colonneAdresse,colonneVille;
    @FXML private TableColumn<Bien,Double> colonneLoyer;
    @FXML private TextArea quittance;
    public void initialiser(App app,ServiceImmoGest service) {
        this.app=app; this.service=service; this.saisie=new ServiceSaisie(service);
        if (type!=null) { type.getItems().addAll("Appartement","Maison","LocalCommercial"); type.setValue("Appartement"); }
        if (debut!=null) { debut.setText(LocalDate.now().toString()); fin.setText(LocalDate.now().plusYears(1).toString()); }
        if (dateEtat!=null) { dateEtat.setText(LocalDate.now().toString()); }
        if (emission!=null) { emission.setText(LocalDate.now().toString()); }
        if (bien!=null) { bien.getItems().setAll(service.getBiens()); locataire.getItems().setAll(service.getLocataires()); gestionnaire.getItems().setAll(service.getGestionnaires()); }
        if (bail!=null) { bail.getItems().setAll(service.getBauxActifs()); }
        if (tableau!=null) {
            type.getItems().add(0,"Tous"); type.setValue("Tous");
            colonneType.setCellValueFactory(new PropertyValueFactory<>("type")); colonneAdresse.setCellValueFactory(new PropertyValueFactory<>("adresse"));
            colonneVille.setCellValueFactory(new PropertyValueFactory<>("ville")); colonneLoyer.setCellValueFactory(new PropertyValueFactory<>("loyerBase"));
            rechercher();
        }
    }
    @FXML private void accueil() { app.afficher("accueil"); }
    @FXML private void biens() { app.afficher("biens"); }
    @FXML private void signature() { app.afficher("signature"); }
    @FXML private void quittances() { app.afficher("quittance"); }
    @FXML private void donnees() { app.afficher("donnees"); }
    private void nettoyer() {
        message.setText(""); message.setStyle("-fx-text-fill: #245e3d; -fx-padding: 15 0 0 0;");
        for (Node node:racine.lookupAll(".erreur")) { node.getStyleClass().remove("erreur"); node.setStyle(""); }
    }
    private void erreur(RuntimeException e) {
        LOGGER.log(Level.WARNING,e.getMessage(),e);
        message.setStyle("-fx-text-fill: #9d2424; -fx-padding: 15 0 0 0;");
        if (e instanceof SaisieException) {
            SaisieException saisieErreur=(SaisieException)e;
            Node champ=racine.lookup("#"+saisieErreur.getChamp());
            if (champ!=null) { champ.getStyleClass().add("erreur"); champ.setStyle("-fx-border-color: #c62828; -fx-border-width: 2;"); champ.requestFocus(); }
            message.setText(e.getMessage());
        } else if (e instanceof PersistanceException) { new Alert(Alert.AlertType.ERROR,e.getMessage()).showAndWait(); }
        else { message.setText(e.getMessage()); }
    }
    @FXML private void rechercher() {
        nettoyer();
        try { tableau.getItems().setAll(saisie.rechercher(type.getValue(),ville.getText(),maximum.getText(),debut.getText(),fin.getText())); message.setText(tableau.getItems().size()+" bien(s) disponible(s)."); }
        catch (IllegalArgumentException | PersistanceException e) { erreur(e); }
    }
    @FXML private void signer() {
        nettoyer();
        try { saisie.signer(bien.getValue(),locataire.getValue(),gestionnaire.getValue(),debut.getText(),fin.getText(),dateEtat.getText(),defauts.getText()); message.setText("Bail et état des lieux enregistrés."); }
        catch (BienIndisponibleException e) { LOGGER.log(Level.WARNING,e.getMessage(),e); new Alert(Alert.AlertType.WARNING,e.getMessage()).showAndWait(); }
        catch (IllegalArgumentException | PersistanceException e) { erreur(e); }
    }
    @FXML private void emettre() {
        nettoyer();
        try { quittance.setText(saisie.afficherQuittance(saisie.emettre(bail.getValue(),emission.getText()))); message.setText("Quittance enregistrée."); }
        catch (IllegalArgumentException | PersistanceException e) { erreur(e); }
    }
    @FXML private void cloturer() {
        nettoyer();
        try { service.cloturerBail(bail.getValue()); bail.getItems().setAll(service.getBauxActifs()); quittance.clear(); message.setText("Bail clôturé."); }
        catch (IllegalArgumentException | IllegalStateException | PersistanceException e) { erreur(e); }
    }
    @FXML private void ajouterBien() {
        nettoyer();
        try { saisie.ajouterBien(type.getValue(),adresse.getText(),ville.getText(),surface.getText(),loyer.getText()); message.setText("Bien enregistré."); }
        catch (IllegalArgumentException | PersistanceException e) { erreur(e); }
    }
    @FXML private void ajouterLocataire() {
        nettoyer();
        try { saisie.ajouterLocataire(nom.getText(),telephone.getText(),email.getText()); message.setText("Locataire enregistré."); }
        catch (IllegalArgumentException | PersistanceException e) { erreur(e); }
    }
    @FXML private void ajouterGestionnaire() {
        nettoyer();
        try { saisie.ajouterGestionnaire(nomGestionnaire.getText(),matricule.getText()); message.setText("Gestionnaire enregistré."); }
        catch (IllegalArgumentException | PersistanceException e) { erreur(e); }
    }
}
