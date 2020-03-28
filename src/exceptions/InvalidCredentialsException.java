package exceptions;

public class InvalidCredentialsException extends Exception {
	
	public InvalidCredentialsException() {
	}
	
	@Override
	public String getLocalizedMessage() {
		return "Invalid Username/Password combination.";
	}
}
