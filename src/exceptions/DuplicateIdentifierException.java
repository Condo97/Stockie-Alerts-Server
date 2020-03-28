package exceptions;

public class DuplicateIdentifierException extends Exception {
	private String name;
	
	public DuplicateIdentifierException(String name) {
		this.name = name;
	}
	
	@Override
	public String getLocalizedMessage() {
		return "Duplicate " + name + " identifier.";
	}
}
