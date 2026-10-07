package immogest.exceptions;
public class SaisieException extends IllegalArgumentException {
    private final String champ;
    public SaisieException(String champ,String message) { super(message); this.champ=champ; }
    public String getChamp() { return champ; }
}
