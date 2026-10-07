package immogest.util;
import immogest.modele.*;
import immogest.exceptions.PersistanceException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.io.*;
import java.util.Properties;
import java.util.logging.*;
public final class HibernateUtil {
    private static final Logger LOGGER=Logger.getLogger(HibernateUtil.class.getName());
    private static SessionFactory factory;
    private HibernateUtil() { }
    public static SessionFactory getSessionFactory() {
        if (factory==null || factory.isClosed()) { factory=ouvrir("config/database.properties"); }
        return factory;
    }
    public static SessionFactory ouvrir(String fichier) {
        Validation.texte(fichier,"Configuration");
        try (FileInputStream entree=new FileInputStream(fichier)) {
            Properties p=new Properties(); p.load(entree);
            Configuration c=new Configuration().configure();
            c.setProperty("hibernate.connection.url",p.getProperty("url",""));
            c.setProperty("hibernate.connection.username",p.getProperty("user",""));
            c.setProperty("hibernate.connection.password",p.getProperty("password",""));
            return c.buildSessionFactory();
        } catch (IOException | RuntimeException e) {
            LOGGER.log(Level.SEVERE,"Connexion impossible.",e);
            throw new PersistanceException("Connexion MySQL impossible. Lance initialiser-base.ps1 et vérifie config/database.properties.",e);
        }
    }
    public static void fermer() { if (factory!=null) { factory.close(); factory=null; } }
}
