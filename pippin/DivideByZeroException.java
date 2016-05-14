package pippin;

@SuppressWarnings("serial")
public class DivideByZeroException extends RuntimeException {
	public DivideByZeroException(){
	}
	public DivideByZeroException(String s){
		super(s);
	}
}
