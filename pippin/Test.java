package pippin;

public class Test {
	public static void main(String[] args){
		System.out.println(Instruction.numOnes(3));
		Instruction.checkParity(3);
		System.out.println(Instruction.numOnes(35));
		Instruction.checkParity(35);
		System.out.println(Instruction.numOnes(9));
		Instruction.checkParity(9);
	}
}

