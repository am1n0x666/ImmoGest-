package immogest;
import immogest.controleur.Controleur;
import immogest.service.*;
import immogest.util.HibernateUtil;
import immogest.exceptions.PersistanceException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.logging.*;
public class App extends Application {
    private static final Logger LOGGER=Logger.getLogger(App.class.getName());
    private Stage fenetre;
    private ServiceImmoGest service;
    @Override public void start(Stage fenetre) {
        this.fenetre=fenetre;
        try {
            service=new ServiceImmoGest(new ServicePersistance(HibernateUtil.getSessionFactory()));
            fenetre.setTitle("ImmoGest"); afficher("accueil"); fenetre.show();
        } catch (PersistanceException e) { LOGGER.log(Level.SEVERE,e.getMessage(),e); new Alert(Alert.AlertType.ERROR,e.getMessage()).showAndWait(); }
    }
    public void afficher(String ecran) {
        try {
            FXMLLoader chargeur=new FXMLLoader(App.class.getResource("/vues/"+ecran+".fxml"));
            Scene scene=new Scene(chargeur.load(),1060,720);
            Controleur controleur=chargeur.getController(); controleur.initialiser(this,service);
            fenetre.setScene(scene);
        } catch (IOException e) { LOGGER.log(Level.SEVERE,"Écran introuvable.",e); new Alert(Alert.AlertType.ERROR,"Impossible d'ouvrir cet écran.").showAndWait(); }
    }
    @Override public void stop() { HibernateUtil.fermer(); }
    public static void main(String[] args) { launch(args); }
}
