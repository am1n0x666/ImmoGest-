package immogest.dao;
import immogest.modele.*;
import immogest.exceptions.PersistanceException;
import org.hibernate.*;
import java.util.*;
import java.util.logging.*;
public class Dao<T extends Entite> {
    private static final Logger LOGGER=Logger.getLogger(Dao.class.getName());
    private final SessionFactory factory;
    private final Class<T> type;
    public Dao(SessionFactory factory,Class<T> type) {
        Validation.objet(factory,"SessionFactory"); Validation.objet(type,"Type"); this.factory=factory; this.type=type;
    }
    public void create(T objet) {
        Validation.objet(objet,"Objet"); objet.valider();
        if (objet.getId()!=null) { throw new IllegalArgumentException("Objet déjà enregistré."); }
        try (Session session=factory.openSession()) {
            Transaction tx=session.beginTransaction();
            try { session.persist(objet); tx.commit(); }
            catch (RuntimeException e) { annuler(tx,e); throw e; }
        } catch (RuntimeException e) { throw erreur(e); }
    }
    public Optional<T> findById(Long id) {
        verifierId(id);
        try (Session session=factory.openSession()) { return Optional.ofNullable(session.find(type,id)); }
        catch (RuntimeException e) { throw erreur(e); }
    }
    public List<T> findAll() {
        try (Session session=factory.openSession()) { return session.createQuery("from "+type.getSimpleName()+" order by id",type).getResultList(); }
        catch (RuntimeException e) { throw erreur(e); }
    }
    public T update(T objet) {
        Validation.objet(objet,"Objet"); objet.valider(); verifierId(objet.getId());
        try (Session session=factory.openSession()) {
            Transaction tx=session.beginTransaction();
            try {
                if (session.find(type,objet.getId())==null) { throw new IllegalArgumentException("Objet introuvable."); }
                T resultat=session.merge(objet); tx.commit(); return resultat;
            } catch (RuntimeException e) { annuler(tx,e); throw e; }
        } catch (RuntimeException e) { throw erreur(e); }
    }
    public void delete(Long id) {
        verifierId(id);
        try (Session session=factory.openSession()) {
            Transaction tx=session.beginTransaction();
            try {
                T objet=session.find(type,id);
                if (objet!=null) { session.remove(objet); }
                tx.commit();
            } catch (RuntimeException e) { annuler(tx,e); throw e; }
        } catch (RuntimeException e) { throw erreur(e); }
    }
    private void verifierId(Long id) { if (id==null || id<=0) { throw new IllegalArgumentException("Identifiant positif obligatoire."); } }
    private void annuler(Transaction tx,RuntimeException cause) {
        LOGGER.log(Level.SEVERE,"Transaction annulée.",cause);
        try { if (tx.isActive()) { tx.rollback(); } }
        catch (RuntimeException e) { LOGGER.log(Level.SEVERE,"Échec du rollback.",e); cause.addSuppressed(e); }
    }
    private PersistanceException erreur(RuntimeException cause) {
        LOGGER.log(Level.SEVERE,"Erreur de persistance.",cause);
        return new PersistanceException("Opération impossible dans la base de données.",cause);
    }
}
