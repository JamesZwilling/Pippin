package pippin;

import java.util.Observable;

public class MachineModel extends Observable {
	private class CPU{
		private int accum;
		private int pc;
	}
	public Instruction[] INSTRUCTIONS = new Instruction[0x10];
	private CPU cpu = new CPU();
	private Memory memory = new Memory();
	private boolean withGUI = false;
	private Code code = new Code();
	private boolean running = false;
	
	public void halt(){
		if(!withGUI){System.exit(0);}
		running = false;
	}
	
	public MachineModel(){
		this(false);
	}
	
	public MachineModel(boolean b){
		withGUI = b;
		//INSTRUCTION_entry for "NOP (no operation)"
		INSTRUCTIONS[0] = (arg, flags) -> {
			flags = flags & 0x6; // remove parity bit that will have been verified
			if(flags != 0) {
				String fString = "(" + (flags%8 > 3?"1":"0") + 
							(flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException(
					"Illegal flags for this instruction: " + fString);
			}
			cpu.pc++;			
		};
		//INSTRUCTION entry for LOD (load accumulator)
		INSTRUCTIONS[0x1] = (arg, flags) -> {
			flags = flags & 0x6; // remove parity bit that will have been verified
			if(flags == 0){ //direct addressing
				cpu.accum = memory.getData(arg);
			} else if(flags == 2){ //immediate addressing
				cpu.accum = arg;
			}else if(flags == 4){ //indirect addressing
				cpu.accum = memory.getData(memory.getData(arg));
			} else {
				String fString = "(" + (flags%8 > 3?"1":"0") + 
						(flags%4 > 1?"1":"0") + ")";
			throw new IllegalInstructionException(
				"Illegal flags for this instruction: " + fString);
			}
			cpu.pc++;
		};
		//INSTRUCTION entry for STO (store accumulator in memory)
		INSTRUCTIONS[0x2] = (arg, flags) -> {
			flags = flags & 0x6; // remove parity bit that will have been verified
			if(flags == 0) { //direct addressing
				memory.setData(arg, cpu.accum);
			} else if(flags == 4) { //indirect addressing
				memory.setData(memory.getData(arg), cpu.accum);
			} else {
				String fString = "(" + (flags%8 > 3?"1":"0") + 
						(flags%4 > 1?"1":"0") + ")";
			throw new IllegalInstructionException(
				"Illegal flags for this instruction: " + fString);
			}
			cpu.pc++;
		};
		//INSTRUCTION entry for JUMP(jump the program counter to a new location in the code)
		INSTRUCTIONS[0x3] =(arg, flags) ->{
			flags =flags & 0x6; // remove the parity bit that will have been verified
			if(flags == 0) { //direct addressing
				cpu.pc += arg;
			} else if(flags == 2){ //immediate addressing
				cpu.pc = arg;
			} else if(flags == 4){ //indirect addressing
				cpu.pc += memory.getData(arg);
			} else{
				cpu.pc = memory.getData(arg);
			}
		};
		//INSTRUCTION entry for JMPZ(conditionally jump the program counter to a new location in the code)
		INSTRUCTIONS[0x4] = (arg, flags)->{
			flags = flags & 0x6; // remove parity bit that will have been verified
			if(cpu.accum == 0){
				if(flags == 0) { //direct addressing
					cpu.pc += arg;
				} else if(flags == 2){ //immediate addressing
					cpu.pc = arg;
				} else if(flags == 4){ //indirect addressing
					cpu.pc += memory.getData(arg);
				} else{
					cpu.pc = memory.getData(arg);
				}
			} else {cpu.pc++;}
		};
		//INSTRUCTION entry for ADD (add)
		INSTRUCTIONS[0x5] = (arg, flags) -> {
			flags = flags & 0x6; // remove parity bit that will have been verified
			if(flags == 0) { // direct addressing
				cpu.accum += memory.getData(arg);
			} else if(flags == 2) { // immediate addressing
				cpu.accum += arg;
			} else if(flags == 4) { // indirect addressing
				cpu.accum += memory.getData(memory.getData(arg));				
			} else { // here the illegal case is "11"
				String fString = "(" + (flags%8 > 3?"1":"0") 
							+ (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException(
					"Illegal flags for this instruction: " + fString);
			}
			cpu.pc++;			
		};
		//INSTRUCTION entry for SUB (subtract)
		INSTRUCTIONS[0x6] = (arg, flags) -> {
			flags = flags & 0x6; // remove parity bit that will have been verified
			if(flags == 0) { // direct addressing
				cpu.accum -= memory.getData(arg);
			} else if(flags == 2) { // immediate addressing
				cpu.accum -= arg;
			} else if(flags == 4) { // indirect addressing
				cpu.accum -= memory.getData(memory.getData(arg));				
			} else { // here the illegal case is "11"
				String fString = "(" + (flags%8 > 3?"1":"0") 
							+ (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException(
					"Illegal flags for this instruction: " + fString);
			}
			cpu.pc++;			
		};
		//INSTRUCTION entry for MUL (multiply)
		INSTRUCTIONS[0x7] = (arg, flags) -> {
			flags = flags & 0x6; // remove parity bit that will have been verified
			if(flags == 0) { // direct addressing
				cpu.accum *= memory.getData(arg);
			} else if(flags == 2) { // immediate addressing
				cpu.accum *= arg;
			} else if(flags == 4) { // indirect addressing
				cpu.accum *= memory.getData(memory.getData(arg));				
			} else { // here the illegal case is "11"
				String fString = "(" + (flags%8 > 3?"1":"0") 
							+ (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException(
					"Illegal flags for this instruction: " + fString);
			}
			cpu.pc++;			
		};		
		//INSTRUCTION entry for DIV (division)
		INSTRUCTIONS[0x8] = (arg, flags) -> {
			flags = flags & 0x6; // remove parity bit that will have been verified
			if(flags == 0) { // direct addressing
				if(memory.getData(arg) != 0){cpu.accum /= memory.getData(arg);}
				else{throw new DivideByZeroException();}
			} else if(flags == 2) { // immediate addressing
				if(arg != 0){cpu.accum /= arg;}
				else{throw new DivideByZeroException();}
			} else if(flags == 4) { // indirect addressing
				if(memory.getData(memory.getData(arg)) != 0){cpu.accum /= memory.getData(memory.getData(arg));}
				else{throw new DivideByZeroException();}
			} else { // here the illegal case is "11"
				String fString = "(" + (flags%8 > 3?"1":"0") 
							+ (flags%4 > 1?"1":"0") + ")";
				throw new IllegalInstructionException(
					"Illegal flags for this instruction: " + fString);
			}
			cpu.pc++;			
		};
		//INSTRUCTION entry for AND(logical and with the accumulator--not 0 means F, non-zero means T)
		INSTRUCTIONS[0x9] = (arg, flags) -> {
			flags = flags & 0x6; // remove parity bit that will have been verified
			if(flags == 0){ //direct addressing
				if(cpu.accum != 0 && memory.getData(arg) != 0){
					cpu.accum = 1;
				} else {cpu.accum = 0;}
			} else if(flags == 2){ //immediate addressing
				if(cpu.accum != 0 && arg != 0){
					cpu.accum = 1;
				} else {cpu.accum = 0;}
			} else { // here the illegal case is "11"
				String fString = "(" + (flags%8 > 3?"1":"0") 
						+ (flags%4 > 1?"1":"0") + ")";
			throw new IllegalInstructionException(
				"Illegal flags for this instruction: " + fString);
		}
		cpu.pc++;	
		};
		//INSTRUCTION entry for NOT(logical not operation on the accumulator)
		INSTRUCTIONS[0xA]= (arg,flags) ->{
			flags = flags & 0x6; // remove parity bit that will have been verified
			if(flags == 0){ //direct addressing
				if(cpu.accum == 0){
					cpu.accum = 1;
				} else {cpu.accum = 0;}
			} else { // here the illegal case is "11"
				String fString = "(" + (flags%8 > 3?"1":"0") 
						+ (flags%4 > 1?"1":"0") + ")";
			throw new IllegalInstructionException(
				"Illegal flags for this instruction: " + fString);
		}
		cpu.pc++;	
		};
		//INSTRUCTION entry for CMPL(compare less than 0)
		INSTRUCTIONS[0xB] = (arg, flags) -> {
			flags = flags & 0x6; // remove parity bit that will have been verified
			if(flags ==0){ //direct addressing
				if(memory.getData(arg) < 0){
					cpu.accum = 1;
				} else {cpu.accum = 0;}
			} else { // here the illegal case is "11"
				String fString = "(" + (flags%8 > 3?"1":"0") 
						+ (flags%4 > 1?"1":"0") + ")";
			throw new IllegalInstructionException(
				"Illegal flags for this instruction: " + fString);
		}
		cpu.pc++;	
		};
		//INSTRUCTION entry for CMPZ(compare to 0)
		INSTRUCTIONS[0xC] = (arg, flags) -> {
			flags = flags & 0x6; // remove parity bit that will have been verified
			if(flags == 0){ //direct addressing
				if(memory.getData(arg) == 0){
					cpu.accum = 1;
				} else {cpu.accum = 0;}
			} else { // here the illegal case is "11"
				String fString = "(" + (flags%8 > 3?"1":"0") 
						+ (flags%4 > 1?"1":"0") + ")";
			throw new IllegalInstructionException(
				"Illegal flags for this instruction: " + fString);
		}
		cpu.pc++;
		};
		//INSTRUCION entry for FOR(loop)
		INSTRUCTIONS[0xD] = (arg, flags) -> {
			flags = flags & 0x6; // remove parity bit that will have been verified
			int counter = cpu.pc + 1;
			if(flags == 0){ //direct addressing
				int num = memory.getData(arg);
				for(int i = 0; i < num%0x1000; i++){
					cpu.pc = counter;
					for(int j = 0; j < num/0x1000; j++){
						step();
					}
				}
			} else if(flags == 2){ //immediate addressing
				if(arg > 0){
					for(int i = 0; i < arg%0x1000; i++){
						cpu.pc = counter;
						for(int j = 0; j < arg/0x1000; j++){
							step();
						}
					}
				}
			} else { // here the illegal case is "11"
				String fString = "(" + (flags%8 > 3?"1":"0") 
						+ (flags%4 > 1?"1":"0") + ")";
			throw new IllegalInstructionException(
				"Illegal flags for this instruction: " + fString);
			}
		};
		//INSTRUCTION entry for HALT(halt execution)
		INSTRUCTIONS[0xF] = (arg, flags) -> {
			flags = flags & 0x6; // remove parity bit that will have been verified
			if(flags == 0){
				halt();
			}else { // here the illegal case is "11"
				String fString = "(" + (flags%8 > 3?"1":"0") 
						+ (flags%4 > 1?"1":"0") + ")";
				running = false;
			throw new IllegalInstructionException(
				"Illegal flags for this instruction: " + fString);
			}
		};
	}
	
	public void setData(int i, int j) {
		memory.setData(i, j);		
	}
	public Instruction get(int i) {
		return INSTRUCTIONS[i];
	}
	int[] getData() {
		return memory.getData();
	}
	public int getPC() {
		return cpu.pc;
	}
	public int getAccum() {
		return cpu.accum;
	}
	public void setAccum(int i) {
		cpu.accum = i;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
	
	public void setCode(int op, int arg){
		code.setCode(op, arg);
	}
	
	public int getData(int i){
		return memory.getData(i);
	}
	
	public void clear(){
		memory.clear();
		code.clear();
		cpu.accum = 0;
		cpu.pc = 0;
	}
	
	public void step(){
		try{
			int opPart = code.getOpPart(cpu.pc);
			int arg = code.getArg(cpu.pc);
			Instruction.checkParity(opPart);
			INSTRUCTIONS[opPart/8].execute(arg, opPart%8);
		}catch(Exception e){
			halt();
			throw e;
		}
	}
	
	public Code getCode(){
		return code;
	}
	public int getChangedIndex(){
		return memory.getChangedIndex();
	}
	public void setPC(int i){
		cpu.pc = i;
	}
}
