package immogest.modele;
public final class Validation {
    private Validation() { }
    public static void texte(String valeur, String champ) {
        if (valeur == null || valeur.isBlank() || valeur.length() > 255) {
            throw new IllegalArgumentException(champ + " : texte obligatoire, 255 caractères maximum.");
        }
    }
    public static void positif(double valeur, String champ) {
        if (!Double.isFinite(valeur) || valeur <= 0) {
            throw new IllegalArgumentException(champ + " doit être un nombre strictement positif.");
        }
    }
    public static void objet(Object valeur, String champ) {
        if (valeur == null) { throw new IllegalArgumentException(champ + " obligatoire."); }
    }
}
