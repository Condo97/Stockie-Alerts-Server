package exceptions;

public class InvalidValueException extends Exception {
	private String valueName;
	
	public InvalidValueException(String valueName) {
		this.valueName = valueName;
	}
	
	@Override
	public String getLocalizedMessage() {
		return "Invalid " + valueName + ".";
	}
}
