package pippin;

@SuppressWarnings("serial")
public class IllegalInstructionException extends RuntimeException {
	public IllegalInstructionException(){
	}
	public IllegalInstructionException(String s){
		super(s);
	}
}
