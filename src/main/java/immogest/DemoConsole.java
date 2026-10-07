package immogest;
import immogest.modele.*;
import immogest.service.*;
import immogest.exceptions.BienIndisponibleException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.*;
public class DemoConsole {
    private static final Logger LOGGER=Logger.getLogger(DemoConsole.class.getName());
    public static void main(String[] args) {
        ServiceQuittancement quittancement=new ServiceQuittancement();
        List<Bien> biens=List.of(new Appartement("Rue A","Tunis",60,1000),new Maison("Rue B","Tunis",60,1000),new LocalCommercial("Rue C","Tunis",60,1000));
        for (Bien bien:biens) { System.out.println(bien.getType()+" : "+quittancement.calculerLoyer(bien)); }
        ServiceImmoGest service=new ServiceImmoGest();
        Locataire locataire=new Locataire("Ali","123","a@b"); Gestionnaire gestionnaire=new Gestionnaire("Sara","G1");
        try {
            service.signerBail(biens.get(0),locataire,gestionnaire,LocalDate.now(),LocalDate.now().plusYears(1));
            service.signerBail(biens.get(0),locataire,gestionnaire,LocalDate.now(),LocalDate.now().plusYears(1));
        } catch (BienIndisponibleException e) { LOGGER.log(Level.WARNING,"Erreur métier attendue : "+e.getMessage(),e); }
    }
}
