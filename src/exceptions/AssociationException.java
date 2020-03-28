package exceptions;

public class AssociationException extends Exception {
	private String obj1, obj2;
	
	public AssociationException(String obj1, String obj2) {
		this.obj1 = obj1;
		this.obj2 = obj2;
	}
	
	@Override
	public String getLocalizedMessage() {
		return obj1 + " is not associated with " + obj2;
	}
}