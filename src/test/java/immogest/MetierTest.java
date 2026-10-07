package immogest;
import immogest.modele.*;
import immogest.service.*;
import immogest.exceptions.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
class MetierTest {
    private ServiceImmoGest service;
    private Bien bien;
    private Locataire locataire;
    private Gestionnaire gestionnaire;
    private LocalDate debut;
    private Bail bail;
    @BeforeEach void preparer() {
        service=new ServiceImmoGest(); bien=new Appartement("Rue 1","Tunis",60,1000);
        locataire=new Locataire("Ali","123","a@b"); gestionnaire=new Gestionnaire("Sara","G1");
        debut=LocalDate.now().minusDays(1); bail=new Bail(bien,locataire,gestionnaire,debut,debut.plusYears(1));
    }
    @Test void locataireValide() throws Exception {
        assertEquals("Ali",locataire.getNom()); assertEquals("123",locataire.getTelephone()); assertEquals("a@b",locataire.getEmail());
    }
    @Test void emailSansArobaseRefuse() throws Exception {
        assertThrows(IllegalArgumentException.class,() -> new Locataire("Ali","123","abc"));
    }
    @Test void nomVideRefuse() throws Exception {
        assertThrows(IllegalArgumentException.class,() -> new Locataire(" ","123","a@b"));
    }
    @Test void matriculeVideRefuse() throws Exception {
        assertThrows(IllegalArgumentException.class,() -> new Gestionnaire("Sara",""));
    }
    @Test void surfaceNulleRefusee() throws Exception {
        assertThrows(IllegalArgumentException.class,() -> new Appartement("Rue","Tunis",0,500));
    }
    @Test void loyerNonFiniRefuse() throws Exception {
        assertThrows(IllegalArgumentException.class,() -> new Maison("Rue","Tunis",50,Double.NaN)); assertThrows(IllegalArgumentException.class,() -> new Maison("Rue","Tunis",50,Double.POSITIVE_INFINITY));
    }
    @Test void constructeursSurcharges() throws Exception {
        assertEquals(500,new Appartement("Rue","Tunis",40).getLoyerBase()); assertEquals(500,new Maison("Rue","Tunis",40).getLoyerBase()); assertEquals(500,new LocalCommercial("Rue","Tunis",40).getLoyerBase());
    }
    @Test void loyerAppartementPolymorphe() throws Exception {
        assertEquals(1100,new ServiceQuittancement().calculerLoyer(bien),0.001);
    }
    @Test void loyerMaisonPolymorphe() throws Exception {
        Bien maison=new Maison("Rue","Tunis",60,1000); assertEquals(1050,new ServiceQuittancement().calculerLoyer(maison),0.001);
    }
    @Test void loyerLocalPolymorphe() throws Exception {
        Bien local=new LocalCommercial("Rue","Tunis",60,1000); assertEquals(1150,new ServiceQuittancement().calculerLoyer(local),0.001);
    }
    @Test void equipementsCopies() throws Exception {
        Appartement a=(Appartement)bien; assertFalse(a.estMeuble()); a.ajouterEquipement("Lit"); a.getEquipements().clear(); assertTrue(a.estMeuble()); assertThrows(IllegalArgumentException.class,() -> a.ajouterEquipement(""));
    }
    @Test void equipementsMaison() throws Exception {
        Maison m=new Maison("Rue","Tunis",80); assertFalse(m.estMeuble()); m.ajouterEquipement("Table"); assertEquals(List.of("Table"),m.getEquipements()); assertTrue(m.estMeuble());
    }
    @Test void datesEgalesRefusees() throws Exception {
        assertThrows(IllegalArgumentException.class,() -> new Bail(bien,locataire,gestionnaire,debut,debut));
    }
    @Test void signatureNominale() throws Exception {
        Bail resultat=service.signerBail(bien,locataire,gestionnaire,debut,debut.plusYears(1)); assertSame(bien,resultat.getBien()); assertEquals(1,service.getBaux().size());
    }
    @Test void chevauchementRefuse() throws Exception {
        service.signerBail(bien,locataire,gestionnaire,debut,debut.plusYears(1)); BienIndisponibleException e=assertThrows(BienIndisponibleException.class,() -> service.signerBail(bien,locataire,gestionnaire,debut.plusDays(1),debut.plusMonths(2))); assertTrue(e.getMessage().contains("déjà loué")); assertEquals(1,service.getBaux().size());
    }
    @Test void periodesAdjacentesAcceptees() throws Exception {
        service.signerBail(bien,locataire,gestionnaire,debut,debut.plusDays(10)); service.signerBail(bien,locataire,gestionnaire,debut.plusDays(10),debut.plusDays(20)); assertEquals(2,service.getBaux().size());
    }
    @Test void doubleClotureRefusee() throws Exception {
        service.cloturerBail(bail); assertTrue(bail.isCloture()); assertThrows(IllegalStateException.class,() -> service.cloturerBail(bail));
    }
    @Test void clotureLibereBien() throws Exception {
        Bail b=service.signerBail(bien,locataire,gestionnaire,debut,debut.plusYears(1)); service.cloturerBail(b); assertDoesNotThrow(() -> service.signerBail(bien,locataire,gestionnaire,debut,debut.plusYears(1)));
    }
    @Test void quittanceSansLignesRefusee() throws Exception {
        assertThrows(IllegalArgumentException.class,() -> new Quittance(bail,debut,List.of(),List.of()));
    }
    @Test void quittanceAvecCharges() throws Exception {
        Quittance q=service.calculerQuittance(bail,debut); assertEquals(1100,q.getMontantTotal(),0.001); assertEquals(2,q.getLignes().size()); q.getLignes().clear(); assertEquals(2,q.getLignes().size());
    }
    @Test void quittanceBailClotureRefusee() throws Exception {
        bail.cloturer(); assertThrows(IllegalArgumentException.class,() -> service.calculerQuittance(bail,debut));
    }
    @Test void etatAvantBailRefuse() throws Exception {
        assertThrows(IllegalArgumentException.class,() -> service.creerEtatDesLieux(bail,debut.minusDays(1),List.of()));
    }
    @Test void etatFuturRefuse() throws Exception {
        assertThrows(IllegalArgumentException.class,() -> service.creerEtatDesLieux(bail,LocalDate.now().plusDays(1),List.of()));
    }
    @Test void etatCopieDefensive() throws Exception {
        List<String> defauts=new ArrayList<>(); defauts.add("Rayure"); EtatDesLieux e=service.creerEtatDesLieux(bail,debut,defauts); defauts.clear(); e.getListeDefauts().clear(); assertEquals(List.of("Rayure"),e.getListeDefauts()); assertSame(bien,e.getBien()); assertSame(bail,e.getBail()); assertEquals(debut,e.getDateRealisation());
    }
    @Test void absenceBienOptional() throws Exception {
        assertTrue(service.rechercherBienParAdresse("inconnu").isEmpty()); service.enregistrerBien(bien); assertTrue(service.rechercherBienParAdresse("RUE").isPresent());
    }
    @Test void absenceLocataireException() throws Exception {
        LocataireIntrouvableException e=assertThrows(LocataireIntrouvableException.class,() -> service.rechercherLocataireParId(99L)); assertTrue(e.getMessage().contains("99"));
    }
    @Test void listesProtegees() throws Exception {
        service.enregistrerBien(bien); service.enregistrerLocataire(locataire); service.enregistrerGestionnaire(gestionnaire); service.getBiens().clear(); service.getLocataires().clear(); service.getGestionnaires().clear(); assertEquals(1,service.getBiens().size()); assertEquals(1,service.getLocataires().size()); assertEquals(1,service.getGestionnaires().size());
    }
    @Test void rechercheFiltreVilleTypePrixDisponibilite() throws Exception {
        service.enregistrerBien(bien); service.enregistrerBien(new Maison("Rue 2","Sousse",80,500)); assertEquals(1,service.rechercherBiens("Appartement","TUN",1200,debut,debut.plusDays(10)).size()); assertTrue(service.rechercherBiens("Appartement","Tunis",1000,debut,debut.plusDays(10)).isEmpty()); service.signerBail(bien,locataire,gestionnaire,debut,debut.plusDays(10)); assertTrue(service.rechercherBiens("Appartement","",1200,debut,debut.plusDays(10)).isEmpty());
    }
    @Test void importContinueApresLigneInvalide() throws Exception {
        Path f=Files.createTempFile("biens", ".txt");
        try { Files.writeString(f,"Appartement;Rue;Tunis;60;700\nincorrect\nMaison;Rue;Sousse;80;800\nLocalCommercial;Rue;Sfax;90;900\nInconnu;Rue;Tunis;50;500\n\n"); assertEquals(3,service.importerBiensDepuisFichier(f.toString()).size()); }
        finally { Files.deleteIfExists(f); }
    }
    @Test void bailEtatSansMutationSiInvalide() throws Exception {
        assertThrows(IllegalArgumentException.class,() -> service.signerBailAvecEtat(bien,locataire,gestionnaire,debut,debut.plusYears(1),debut.minusDays(1),List.of())); assertTrue(service.getBaux().isEmpty());
    }
    @Test void bailEtatNominal() throws Exception {
        service.signerBailAvecEtat(bien,locataire,gestionnaire,debut,debut.plusYears(1),debut,List.of()); assertEquals(1,service.getBauxActifs().size());
    }
    @Test void parametresNullRefuses() throws Exception {
        assertThrows(IllegalArgumentException.class,() -> service.enregistrerBien(null)); assertThrows(IllegalArgumentException.class,() -> service.cloturerBail(null)); assertThrows(IllegalArgumentException.class,() -> service.rechercherBienParAdresse(null)); assertThrows(IllegalArgumentException.class,() -> service.rechercherLocataireParId(null)); assertThrows(IllegalArgumentException.class,() -> service.calculerQuittance(bail,null));
    }
    @Test void getterBienEtGestionnaire() throws Exception {
        assertEquals("Tunis",bien.getVille()); assertEquals(60,bien.getSurfaceM2()); assertEquals("Appartement",bien.getType()); assertEquals("G1",gestionnaire.getMatricule()); assertSame(gestionnaire,bail.getGestionnaire()); assertEquals(debut.plusYears(1),bail.getDateFin());
    }
}
