package immogest.dao;
import immogest.modele.Bien;
import org.hibernate.SessionFactory;
public class BienDao extends Dao<Bien> {
    public BienDao(SessionFactory factory) { super(factory,Bien.class); }
}
