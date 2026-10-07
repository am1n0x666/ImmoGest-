package immogest;
import immogest.modele.*;
import immogest.service.*;
import immogest.exceptions.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class SaisieTest {
    private ServiceImmoGest service;
    private ServiceSaisie saisie;
    @BeforeEach void preparer() { service=new ServiceImmoGest(); saisie=new ServiceSaisie(service); }
    @Test void dateValide() throws Exception { assertEquals(LocalDate.of(2026,1,20),saisie.date("2026-01-20","debut")); }
    @Test void dateImpossible() throws Exception { SaisieException e=assertThrows(SaisieException.class,() -> saisie.date("2026-02-30","debut")); assertEquals("debut",e.getChamp()); assertTrue(e.getMessage().contains("invalide")); }
    @Test void dateVide() throws Exception { assertThrows(SaisieException.class,() -> saisie.date("","debut")); }
    @Test void nombreAvecVirgule() throws Exception { assertEquals(12.5,saisie.nombre("12,5","loyer")); }
    @Test void nombresInvalides() throws Exception { for(String v:new String[]{"", "abc", "NaN", "Infinity", "-5", "0"}) { assertThrows(SaisieException.class,() -> saisie.nombre(v,"loyer")); } }
    @Test void ajoutBien() throws Exception { saisie.ajouterBien("Appartement","Rue","Tunis","60","800"); assertEquals(1,service.getBiens().size()); }
    @Test void ajoutLocataire() throws Exception { saisie.ajouterLocataire("Ali","123","a@b"); assertEquals(1,service.getLocataires().size()); }
    @Test void ajoutGestionnaire() throws Exception { saisie.ajouterGestionnaire("Sara","G1"); assertEquals(1,service.getGestionnaires().size()); }
    @Test void emailInvalide() throws Exception { assertThrows(SaisieException.class,() -> saisie.ajouterLocataire("Ali","123","abc")); assertTrue(service.getLocataires().isEmpty()); }
    @Test void selectionBienObligatoire() throws Exception { SaisieException e=assertThrows(SaisieException.class,() -> saisie.signer(null,null,null,"","","","")); assertEquals("bien",e.getChamp()); }
    @Test void rechercheSansMaximum() throws Exception { saisie.ajouterBien("Maison","Rue","Tunis","80","800"); assertEquals(1,saisie.rechercher("Tous","","","2026-01-01","2026-02-01").size()); }
    @Test void rechercheDatesInversees() throws Exception { assertThrows(SaisieException.class,() -> saisie.rechercher("Tous","","","2026-02-01","2026-01-01")); }
    @Test void quittanceSelectionObligatoire() throws Exception { assertThrows(SaisieException.class,() -> saisie.emettre(null,"2026-01-01")); }
    @Test void formulaireComplet() throws Exception { Bien b=new Maison("Rue","Tunis",80,1000); Bail bail=saisie.signer(b,new Locataire("Ali","123","a@b"),new Gestionnaire("Sara","G1"),LocalDate.now().toString(),LocalDate.now().plusYears(1).toString(),LocalDate.now().toString(),"Rayure; Peinture"); assertTrue(saisie.afficherQuittance(saisie.emettre(bail,LocalDate.now().toString())).contains("TOTAL")); }
}
