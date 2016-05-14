package pippin;

@SuppressWarnings("serial")
public class ParityCheckException extends RuntimeException {
	public ParityCheckException(){
	}
	public ParityCheckException(String s){
		super(s);
	}
}
