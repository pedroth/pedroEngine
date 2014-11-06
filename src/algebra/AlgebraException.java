package algebra;

/**
 * 
 * @author pedro
 * Serves to throw Exceptions of algebra classes
 */
public class AlgebraException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public AlgebraException(String error) {
		super(error);
	}
}
