package immogest.service;
import immogest.modele.*;
import immogest.dao.*;
import immogest.exceptions.*;
import org.hibernate.*;
import java.util.logging.*;
public class ServicePersistance {
    private static final Logger LOGGER=Logger.getLogger(ServicePersistance.class.getName());
    private final SessionFactory factory;
    public ServicePersistance(SessionFactory factory) { Validation.objet(factory,"SessionFactory"); this.factory=factory; }
    public BienDao getBienDao() { return new BienDao(factory); }
    public BailDao getBailDao() { return new BailDao(factory); }
    public QuittanceDao getQuittanceDao() { return new QuittanceDao(factory); }
    public LocataireDao getLocataireDao() { return new LocataireDao(factory); }
    public GestionnaireDao getGestionnaireDao() { return new GestionnaireDao(factory); }
    public EtatDesLieuxDao getEtatDesLieuxDao() { return new EtatDesLieuxDao(factory); }
    public void enregistrerBailEtEtat(Bail bail,EtatDesLieux etat) throws BienIndisponibleException {
        Validation.objet(bail,"Bail"); Validation.objet(etat,"État des lieux"); bail.valider(); etat.valider();
        if (etat.getBail()!=bail || bail.getId()!=null || etat.getId()!=null) { throw new IllegalArgumentException("Un nouveau bail et son état des lieux sont attendus."); }
        try (Session session=factory.openSession()) {
            Transaction tx=session.beginTransaction();
            try {
                Long nombre=session.createQuery("select count(b) from Bail b where b.bien.id=:bien and b.cloture=false and b.dateDebut<:fin and b.dateFin>:debut",Long.class)
                    .setParameter("bien",bail.getBien().getId()).setParameter("fin",bail.getDateFin()).setParameter("debut",bail.getDateDebut()).getSingleResult();
                if (nombre>0) { throw new BienIndisponibleException("Ce bien est déjà loué pour cette période."); }
                session.persist(bail); session.flush();
                session.persist(etat); session.flush();
                tx.commit();
            } catch (BienIndisponibleException e) { LOGGER.log(Level.WARNING,e.getMessage(),e); tx.rollback(); throw e; }
            catch (RuntimeException e) { LOGGER.log(Level.SEVERE,"Bail et état des lieux annulés.",e); if (tx.isActive()) { tx.rollback(); } throw e; }
        } catch (RuntimeException e) { LOGGER.log(Level.SEVERE,"Erreur de transaction.",e); throw new PersistanceException("Le bail et l'état des lieux n'ont pas été enregistrés.",e); }
    }
    public void cloturerBail(Long id) {
        if (id==null || id<=0) { throw new IllegalArgumentException("Identifiant positif obligatoire."); }
        Bail bail=getBailDao().findById(id).orElseThrow(() -> new IllegalArgumentException("Bail introuvable."));
        bail.cloturer(); getBailDao().update(bail);
    }
    public void modifierLoyer(Long id,double montant) {
        if (id==null || id<=0) { throw new IllegalArgumentException("Identifiant positif obligatoire."); }
        Validation.positif(montant,"Loyer");
        Bail bail=getBailDao().findById(id).orElseThrow(() -> new IllegalArgumentException("Bail introuvable."));
        if (bail.isCloture()) { throw new IllegalArgumentException("Le bail est clôturé."); }
        bail.setLoyer(montant); getBailDao().update(bail);
    }
}
