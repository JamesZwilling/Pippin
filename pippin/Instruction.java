package pippin;

public interface Instruction {
	void execute(int arg, int flags);
	static int numOnes(int i){
/**		int retVal = 0;
		String number = Integer.toUnsignedString(num, 2);
		for(int i = 0; i < number.length(); i++){
			if(number.charAt(i) == '1'){retVal++;}
		}
		return retVal;
*/
		i = i - ((i >>> 1) & 0x55555555);
		i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
		return (((i + (i >>> 4)) & 0x0F0F0F0F) * 0x01010101) >>> 24;
	}
	static void checkParity(int num){
		int res = numOnes(num);
		if((res & 1) != 0){
			throw new ParityCheckException("This instruction is corrupted");
		}
	}
}
