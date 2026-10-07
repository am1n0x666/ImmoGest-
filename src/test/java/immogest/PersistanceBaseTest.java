package immogest;
import immogest.modele.*;
import immogest.service.*;
import immogest.exceptions.*;
import immogest.util.HibernateUtil;
import org.hibernate.*;
import org.junit.jupiter.api.*;
import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class PersistanceBaseTest {
    private static SessionFactory factory;
    private static Properties config;
    private ServicePersistance persistance;
    private Bien bien;
    private Locataire locataire;
    private Gestionnaire gestionnaire;
    private Bail bail;
    @BeforeAll static void ouvrir() throws Exception {
        config=new Properties();
        try (FileInputStream f=new FileInputStream("config/test.properties")) { config.load(f); }
        try (Connection c=connexion()) { assertEquals("immogest_test",c.getCatalog(),"Les tests doivent utiliser leur base dédiée."); }
        factory=HibernateUtil.ouvrir("config/test.properties");
    }
    private static Connection connexion() throws SQLException { return DriverManager.getConnection(config.getProperty("url"),config.getProperty("user"),config.getProperty("password")); }
    private static void sql(String texte) throws SQLException {
        try (Connection c=connexion(); Statement s=c.createStatement()) { s.execute(texte); }
    }
    @BeforeEach void preparer() throws Exception {
        for(String table:new String[]{"ligne_quittance","quittance","defaut","etat_des_lieux","bail","appartement_equipement","maison_equipement","bien","locataire","gestionnaire"}) { sql("DELETE FROM "+table); }
        persistance=new ServicePersistance(factory);
        bien=new Appartement("Rue 1","Tunis",60,1000);
        ((Appartement)bien).ajouterEquipement("Lit");
        locataire=new Locataire("Ali","123","a@b"); gestionnaire=new Gestionnaire("Sara","G1");
        persistance.getBienDao().create(bien); persistance.getLocataireDao().create(locataire); persistance.getGestionnaireDao().create(gestionnaire);
        bail=new Bail(bien,locataire,gestionnaire,LocalDate.now().minusDays(1),LocalDate.now().plusYears(1));
    }
    @AfterAll static void fermer() { if (factory!=null) { factory.close(); } }
    @Test void transactionEnregistreLesDeuxObjets() throws Exception {
        EtatDesLieux etat=new EtatDesLieux(bail,LocalDate.now(),List.of("Rayure"));
        persistance.enregistrerBailEtEtat(bail,etat);
        assertEquals(1,persistance.getBailDao().findAll().size()); assertEquals(1,persistance.getEtatDesLieuxDao().findAll().size());
        assertEquals(bail.getId(),persistance.getEtatDesLieuxDao().findById(etat.getId()).orElseThrow().getBail().getId());
    }
    @Test void echecEtatAnnuleAussiLeBailDejaInsere() throws Exception {
        sql("ALTER TABLE etat_des_lieux ADD CONSTRAINT refus_test CHECK (dateRealisation < '1900-01-01')");
        try {
            PersistanceException erreur=assertThrows(PersistanceException.class,() -> persistance.enregistrerBailEtEtat(bail,new EtatDesLieux(bail,LocalDate.now(),List.of())));
            assertTrue(erreur.getMessage().contains("n'ont pas été enregistrés")); assertNotNull(erreur.getCause());
            assertTrue(persistance.getBailDao().findAll().isEmpty()); assertTrue(persistance.getEtatDesLieuxDao().findAll().isEmpty());
        } finally { sql("ALTER TABLE etat_des_lieux DROP CHECK refus_test"); }
    }
    @Test void rechargementRetrouveLesDonnees() throws Exception {
        persistance.enregistrerBailEtEtat(bail,new EtatDesLieux(bail,LocalDate.now(),List.of()));
        try (SessionFactory nouvelle=HibernateUtil.ouvrir("config/test.properties")) {
            ServiceImmoGest service=new ServiceImmoGest(new ServicePersistance(nouvelle));
            assertEquals(1,service.getBaux().size()); assertEquals(1,service.getBiens().size());
            assertEquals(locataire.getNom(),service.rechercherLocataireParId(locataire.getId()).getNom());
        }
    }
    @Test void crudBien() {
        Maison maison=new Maison("Rue 2","Sousse",80,800); persistance.getBienDao().create(maison);
        Maison trouve=(Maison)persistance.getBienDao().findById(maison.getId()).orElseThrow();
        trouve.ajouterEquipement("Table"); persistance.getBienDao().update(trouve);
        assertTrue(((Maison)persistance.getBienDao().findById(maison.getId()).orElseThrow()).estMeuble());
        persistance.getBienDao().delete(maison.getId()); assertTrue(persistance.getBienDao().findById(maison.getId()).isEmpty());
    }
    @Test void crudBail() {
        persistance.getBailDao().create(bail); Bail lu=persistance.getBailDao().findById(bail.getId()).orElseThrow();
        lu.setLoyer(1200); persistance.getBailDao().update(lu);
        assertEquals(1200,persistance.getBailDao().findById(bail.getId()).orElseThrow().getLoyer());
        persistance.getBailDao().delete(bail.getId()); assertTrue(persistance.getBailDao().findById(bail.getId()).isEmpty());
    }
    @Test void crudQuittanceEtComposition() {
        persistance.getBailDao().create(bail); Quittance q=new ServiceQuittancement().genererQuittance(bail,LocalDate.now()); persistance.getQuittanceDao().create(q);
        Quittance lu=persistance.getQuittanceDao().findById(q.getId()).orElseThrow(); assertEquals(1100,lu.getMontantTotal(),0.001);
        persistance.getQuittanceDao().update(lu); persistance.getQuittanceDao().delete(q.getId());
        assertTrue(persistance.getQuittanceDao().findById(q.getId()).isEmpty());
        try (Session s=factory.openSession()) { assertEquals(0L,s.createQuery("select count(l) from LigneQuittance l",Long.class).getSingleResult()); }
    }
    @Test void erreurCleEtrangereEstEnveloppee() {
        persistance.getBailDao().create(bail);
        PersistanceException e=assertThrows(PersistanceException.class,() -> persistance.getBienDao().delete(bien.getId()));
        assertNotNull(e.getCause()); assertTrue(persistance.getBienDao().findById(bien.getId()).isPresent());
    }
    @Test void serviceConserveCoherenceEnCasDeRollback() throws Exception {
        ServiceImmoGest service=new ServiceImmoGest(persistance);
        sql("ALTER TABLE etat_des_lieux ADD CONSTRAINT refus_test CHECK (dateRealisation < '1900-01-01')");
        try {
            assertThrows(PersistanceException.class,() -> service.signerBailAvecEtat(bien,locataire,gestionnaire,bail.getDateDebut(),bail.getDateFin(),LocalDate.now(),List.of()));
            assertTrue(service.getBaux().isEmpty()); assertTrue(persistance.getBailDao().findAll().isEmpty());
        } finally { sql("ALTER TABLE etat_des_lieux DROP CHECK refus_test"); }
    }
    @Test void servicePersistantComplet() throws Exception {
        ServiceImmoGest service=new ServiceImmoGest(persistance);
        Bail signe=service.signerBailAvecEtat(bien,locataire,gestionnaire,bail.getDateDebut(),bail.getDateFin(),LocalDate.now(),List.of());
        service.appliquerIndexationLoyer(signe,103);
        assertEquals(1030,persistance.getBailDao().findById(signe.getId()).orElseThrow().getLoyer(),0.001);
        Quittance q=service.calculerQuittance(signe,LocalDate.now()); assertEquals(1130,q.getMontantTotal(),0.001);
        assertTrue(persistance.getQuittanceDao().findById(q.getId()).isPresent());
        service.cloturerBail(signe); assertTrue(persistance.getBailDao().findById(signe.getId()).orElseThrow().isCloture());
    }
    @Test void chevauchementVerifieAussiEnBase() throws Exception {
        persistance.enregistrerBailEtEtat(bail,new EtatDesLieux(bail,LocalDate.now(),List.of()));
        Bail autre=new Bail(bien,locataire,gestionnaire,bail.getDateDebut(),bail.getDateFin());
        assertThrows(BienIndisponibleException.class,() -> persistance.enregistrerBailEtEtat(autre,new EtatDesLieux(autre,LocalDate.now(),List.of())));
        assertEquals(1,persistance.getBailDao().findAll().size());
    }
}
