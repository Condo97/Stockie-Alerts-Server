package exceptions;

public class ExpiredIdentityException extends Exception {
    public ExpiredIdentityException() {}

    @Override
    public String getLocalizedMessage() {
        return "Expired Identity";
    }
}
