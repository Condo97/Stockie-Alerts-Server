package exceptions;

public class MissingKeyException extends Exception {
	private String keyName;
	
	public MissingKeyException(String keyName) {
		this.keyName = keyName;
	}
	
	@Override
	public String getLocalizedMessage() {
		return "Missing " + keyName + ".";
	}
}
