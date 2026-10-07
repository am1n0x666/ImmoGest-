package immogest;
import immogest.modele.*;
import immogest.service.ServiceImmoGest;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class IndexationTest {
    private Bail bail;
    private ServiceImmoGest service;
    @BeforeEach void preparer() {
        service=new ServiceImmoGest();
        bail=new Bail(new Appartement("Rue 1","Tunis",60,1000),new Locataire("Ali","123","a@b"),new Gestionnaire("Sara","G1"),LocalDate.now(),LocalDate.now().plusYears(1));
    }
    @Test void indice103AugmenteLeLoyerDeTroisPourCent() {
        service.appliquerIndexationLoyer(bail,103);
        assertEquals(1030,bail.getLoyer(),0.001);
    }
    @Test void indiceInvalideRefuseSansModifierLeBail() {
        assertThrows(IllegalArgumentException.class,() -> service.appliquerIndexationLoyer(bail,0));
        assertEquals(1000,bail.getLoyer(),0.001);
    }
    @Test void bailClotureRefuse() {
        bail.cloturer();
        assertThrows(IllegalArgumentException.class,() -> service.appliquerIndexationLoyer(bail,103));
    }
}
