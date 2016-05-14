package pippin;

public class Factorial {
	public static void main(String[] args){
		MachineModel test = new MachineModel();
		test.setData(0, 8);
		test.setRunning(true);
		int op = 0;
		int arg = 0;
		//LOD using direct addressing (flags 0) plus the parity bit
		op = 1*8 + 1;
		arg = 0;
		test.setCode(op, arg);
		
		// STO, using direct addressing (parity bit is 1); arq is 1
		op = 2*8 +1;
		arg = 1;
		test.setCode(op, arg);
		
		//LOD  using direct addressing (parity bit is 1); arq is 0
		op = 1*8 +1;
		arg = 0;
		test.setCode(op, arg);
		
		//SUB, using immediate addressing (flags 2, parity bit is 1); arq is 1
		op = 6*8 + 1 + 2;
		arg = 1;
		test.setCode(op, arg);
		
		//STO using direct addressing (parity bit is 1); arq is 0
		op = 2*8 + 1;
		arg = 0;
		test.setCode(op, arg);

		//CMPZ flags and parity bit 0; arg is 0
		op = 12*8;
		arg = 0;
		test.setCode(op, arg);
				
		//SUB  using immediate addressing (flags 2, parity bit is 1); arq is 1
		op = 6*8 + 1 + 2;
		arg = 1;
		test.setCode(op, arg);
		
		//JMPZ, using flags 0 (relative conditional jump), parity bit is 1, arg is 4
		op = 4*8 + 1;
		arg = 4;
		test.setCode(op, arg);
		
		//LOD, using direct addressing (parity bit is 1); arq is 0
		op = 1*8 +1;
		arg = 0;
		test.setCode(op, arg);
		
		//MUL, using direct addressing (parity bit is 1); arq is 1
		op = 7*8 + 1;
		arg = 1;
		test.setCode(op, arg);
		
		//JUMP, using flags 2 and parity bit 1; arg is 1
		op = 3*8 + 2 + 1;
		arg = 1;
		test.setCode(op, arg);
		
		// HALT, using flags 0, parity bit 0 and arg = 0.
		op = 15*8;
		arg = 0;
		test.setCode(op, arg);
		
		int result = 0;
		while(test.isRunning()) {
			if(result != test.getData(1)){
				result = test.getData(1);
				System.out.println("0 => " + test.getData(0) + 
						"; 1 => " + result);
			}
			test.step();
		}
	}
}
