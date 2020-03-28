package exceptions;

public class DuplicateObjectException extends Exception {
    private String name;

    public DuplicateObjectException(String name) {
        this.name = name;
    }

    @Override
    public String getLocalizedMessage() {
        return "Duplicate " + name + " object.";
    }
}
