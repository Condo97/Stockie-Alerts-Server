package exceptions;

public class InvalidIdentifierException extends Exception {
	private String credentialName;
	
	public InvalidIdentifierException(String credentialName) {
		this.credentialName = credentialName;
	}
	
	@Override
	public String getLocalizedMessage() {
		return "Invalid " + credentialName + ".";
	}
}
