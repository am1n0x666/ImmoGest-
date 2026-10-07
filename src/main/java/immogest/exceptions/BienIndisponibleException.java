package immogest.exceptions;
public class BienIndisponibleException extends Exception {
    public BienIndisponibleException(String message) { super(message); }
    public BienIndisponibleException(String message, Throwable cause) { super(message,cause); }
}
