package pippin;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Observable;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MachineView extends Observable {
	static final int TICK = 500;
	private MachineModel model;
	private String defaultDir;
	private String sourceDir;
	private String dataDir;
	private String executableDir;
	private Properties properties;
	private CodeViewPanel codeViewPanel;
	private MemoryViewPanel memoryViewPanel1;
	private MemoryViewPanel memoryViewPanel2;
	private MemoryViewPanel memoryViewPanel3;
	private ControlPanel controlPanel;
	private CPUViewPanel cpuViewPanel;
	private MenuBarBuilder menuBarBuilder;
	private JFrame frame;
	private Timer timer;
	private States state;
	private File currentProgramFile;
	private File currentDataFile;
	private boolean running = false;
	private boolean programLoaded = false;
	private boolean noDataNeeded = false;
	private boolean autoStepOn = false;
	public boolean isAutoStepOn() {
		return autoStepOn;
	}
	public void setAutoStepOn(boolean autoStepOn) {
		this.autoStepOn = autoStepOn;
	}
	public States getState() {
		return state;
	}
	public void setRunning(boolean running) {
		this.running = running;
		if(running){
			state = States.PROGRAM_LOADED_NOT_AUTOSTEPPING;
		} else{
			autoStepOn = false;
			state = States.PROGRAM_HALTED;
		}
		state.enter();
		setChanged();
		notifyObservers();
	}
	public int getPC() {
		return model.getPC();
	}
	public int getAccum() {
		return model.getAccum();
	}
	public int getData(int i) {
		return model.getData(i);
	}
	public Code getCode() {
		return model.getCode();
	}
	public int getChangedIndex() {
		return model.getChangedIndex();
	}	
	private void locateDefaultDirectory() {
		//CODE TO DISCOVER THE ECLIPSE DEFAULT DIRECTORY:
		File temp = new File("propertyfile.txt");
		if(!temp.exists()) {
			PrintWriter out;
			try {
				out = new PrintWriter(temp);
				out.close();
				defaultDir = temp.getAbsolutePath();
				temp.delete();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			defaultDir = temp.getAbsolutePath();
		}
		// change to forward slashes
		defaultDir = defaultDir.replace('\\','/');
		int lastSlash = defaultDir.lastIndexOf('/');
		defaultDir  = defaultDir.substring(0, lastSlash + 1);
	}
	private void loadPropertiesFile() {
		try { // load properties file "propertyfile.txt", if it exists
			properties = new Properties();
			properties.load(new FileInputStream("propertyfile.txt"));
			sourceDir = properties.getProperty("SourceDirectory");
			executableDir = properties.getProperty("ExecutableDirectory");
			dataDir = properties.getProperty("DataDirectory");
			// CLEAN UP ANY ERRORS IN WHAT IS STORED:
			if (sourceDir == null || sourceDir.length() == 0 
					|| !new File(sourceDir).exists()) {
				sourceDir = defaultDir;
			}
			if (executableDir == null || executableDir.length() == 0 
					|| !new File(executableDir).exists()) {
				executableDir = defaultDir;
			}
			if (dataDir == null || dataDir.length() == 0 
					|| !new File(dataDir).exists()) {
				dataDir = defaultDir;
			}
		} catch (Exception e) {
			// PROPERTIES FILE DID NOT EXIST
			sourceDir = defaultDir;
			executableDir = defaultDir;
		}		
	}
	public void createAndShowGUI(){
		frame = new JFrame("Pippin Simulator");
		codeViewPanel = new CodeViewPanel(this);
		memoryViewPanel1 = new MemoryViewPanel(this, 0, 160);
		memoryViewPanel2 = new MemoryViewPanel(this, 160, 240);
		memoryViewPanel3 = new MemoryViewPanel(this, 240, Memory.DATA_SIZE);
		frame.add(codeViewPanel.createCodeDisplay(),BorderLayout.LINE_START);
		cpuViewPanel = new CPUViewPanel(this);
		controlPanel = new ControlPanel(this);
		menuBarBuilder = new MenuBarBuilder(this);
		JPanel center = new JPanel();
		center.setLayout(new GridLayout(1,3));
		frame.add(center,BorderLayout.CENTER);
		center.add(memoryViewPanel1.createMemoryDisplay());
		center.add(memoryViewPanel2.createMemoryDisplay());
		center.add(memoryViewPanel3.createMemoryDisplay());
		frame.add(cpuViewPanel.createCPUDisplay(),BorderLayout.PAGE_START);
		frame.add(controlPanel.createControlDisplay(),BorderLayout.PAGE_END);
		JMenuBar bar = new JMenuBar();
		frame.setJMenuBar(bar);
		bar.add(menuBarBuilder.createFileMenu());
		bar.add(menuBarBuilder.createExecuteMenu());
		frame.setSize(1200,600);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(WindowListenerFactory.windowClosingFactory(e -> exit()));
		state = States.NOTHING_LOADED;
		state.enter();
		setChanged();
		notifyObservers();
		timer = new Timer(TICK, e -> {if(autoStepOn) step();});
		timer.start();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	public MachineView(MachineModel m){
		model = m;
		locateDefaultDirectory();
		loadPropertiesFile();
		createAndShowGUI();
	}
	void clearAll(){
		model.clear();
		programLoaded = false;
		noDataNeeded = false;
		state = States.NOTHING_LOADED;
		state.enter();
		setChanged();
		notifyObservers("Clear");
	}
	void halt(){
		setRunning(false);
	}
	void setPeriod(int period){
		if(timer != null){
			timer.setDelay(period);
		}
	}
	public void exit() { // method executed when user exits the program
	    int decision = JOptionPane.showConfirmDialog(
	            frame, "Do you really wish to exit?",
	            "Confirmation", JOptionPane.YES_NO_OPTION);
	    if (decision == JOptionPane.YES_OPTION) {
	        System.exit(0);
	    }
	}
	public void step(){
		if(model.isRunning()){
			try{
				model.step();
			} catch (CodeAccessException e) {
				JOptionPane.showMessageDialog(
					frame, 
					"Illegal access to code from line " + getPC() + "\n"
					+ "Exception message: " + e.getMessage(),
					"Run time error",
					JOptionPane.OK_OPTION);
				halt();
			} catch (ArrayIndexOutOfBoundsException e) {
				JOptionPane.showMessageDialog(
					frame, 
					"Illegal access to data from line " + getPC() + "\n"
					+ "Exception message: " + e.getMessage(),
					"Run time error",
					JOptionPane.OK_OPTION);
				halt();
			} catch (NullPointerException e) {
				JOptionPane.showMessageDialog(
					frame, 
					"Null pointer exception from line " + getPC() + "\n"
					+ "Exception message: " + e.getMessage(),
					"Run time error",
					JOptionPane.OK_OPTION);
				halt();
			} catch (IllegalArgumentException e) {
				JOptionPane.showMessageDialog(
					frame, 
					"Program Error from line " + getPC() + "\n"
					+ "Exception message: " + e.getMessage(),
					"Run time error",
					JOptionPane.OK_OPTION);
				halt();
			} catch (IllegalInstructionException e) {
				JOptionPane.showMessageDialog(
					frame, 
					"Illegal flags in the instruction" + "\n"
					+ "Exception message: " + e.getMessage(),
					"Run time error",
					JOptionPane.OK_OPTION);
				halt();
			} catch (DivideByZeroException e) {
				JOptionPane.showMessageDialog(
					frame, 
					"Divide by zero from line " + getPC() + "\n"
					+ "Exception message: " + e.getMessage(),
					"Run time error",
					JOptionPane.OK_OPTION);
				halt();
			} catch (ParityCheckException e) {
				JOptionPane.showMessageDialog(
					frame, 
					"Parity error from line " + getPC() + "\n"
					+ "Exception message: " + e.getMessage(),
					"Run time error",
					JOptionPane.OK_OPTION);
				halt();
			} catch (IndexOutOfBoundsException e) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Program error from line " + getPC() + "\n"
                        + "Exception message: " + e.getMessage(),
                        "Run time error",
                        JOptionPane.OK_OPTION);
                halt();
            }
			setChanged();
			notifyObservers();
		} else{
			halt();
		}
	}
	public void execute(){
		while(running){
			if(model.isRunning()){
				try{
					model.step();
				} catch (CodeAccessException e) {
					halt();
					JOptionPane.showMessageDialog(
						frame, 
						"Illegal access to code from line " + getPC() + "\n"
						+ "Exception message: " + e.getMessage(),
						"Run time error",
						JOptionPane.OK_OPTION);
				} catch (ArrayIndexOutOfBoundsException e) {
					halt();
					JOptionPane.showMessageDialog(
						frame, 
						"Illegal access to data from line " + getPC() + "\n"
						+ "Exception message: " + e.getMessage(),
						"Run time error",
						JOptionPane.OK_OPTION);
				} catch (NullPointerException e) {
					halt();
					JOptionPane.showMessageDialog(
						frame, 
						"Null pointer exception from line " + getPC() + "\n"
						+ "Exception message: " + e.getMessage(),
						"Run time error",
						JOptionPane.OK_OPTION);
				} catch (IllegalArgumentException e) {
					halt();
					JOptionPane.showMessageDialog(
						frame, 
						"Program Error from line " + getPC() + "\n"
						+ "Exception message: " + e.getMessage(),
						"Run time error",
						JOptionPane.OK_OPTION);
				} catch (IllegalInstructionException e) {
					halt();
					JOptionPane.showMessageDialog(
						frame, 
						"Illegal flags in the instruction" + "\n"
						+ "Exception message: " + e.getMessage(),
						"Run time error",
						JOptionPane.OK_OPTION);
				} catch (DivideByZeroException e) {
					halt();
					JOptionPane.showMessageDialog(
						frame, 
						"Divide by zero from line " + getPC() + "\n"
						+ "Exception message: " + e.getMessage(),
						"Run time error",
						JOptionPane.OK_OPTION);
				} catch (ParityCheckException e) {
					halt();
					JOptionPane.showMessageDialog(
						frame, 
						"Parity error from line " + getPC() + "\n"
						+ "Exception message: " + e.getMessage(),
						"Run time error",
						JOptionPane.OK_OPTION);
				}
			} else{
				halt();
			}
		}
		setChanged();
		notifyObservers();
	} 
	public void toggleAutoStep(){
		setAutoStepOn(!autoStepOn);
	}
	public void loadCode() {
		programLoaded = false;
        noDataNeeded = false;
		JFileChooser chooser = new JFileChooser(executableDir);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Pippin Executable Files", "pexe");
		chooser.setFileFilter(filter);
		// CODE TO LOAD DESIRED FILE
		int openOK = chooser.showOpenDialog(null);
		if(openOK == JFileChooser.APPROVE_OPTION) {
			currentProgramFile = chooser.getSelectedFile();
		}
		if(currentProgramFile != null && currentProgramFile.exists()) {
			// CODE TO REMEMBER WHICH DIRECTORY HAS THE pexe FILES
			executableDir = currentProgramFile .getAbsolutePath();
			executableDir = executableDir.replace('\\','/');
			int lastSlash = executableDir.lastIndexOf('/');
			executableDir = executableDir.substring(0, lastSlash + 1);
			try { 
				properties.setProperty("SourceDirectory", sourceDir);
				properties.setProperty("ExecutableDirectory", executableDir);
				properties.setProperty("DataDirectory", dataDir);
				properties.store(new FileOutputStream("propertyfile.txt"), 
						"File locations");
			} catch (Exception e) {
				System.out.println("Error writing properties file");
			}			
			programLoaded = true;
			int needData = JOptionPane.showConfirmDialog(
					frame, 
					"Data may be needed before the program can execute\n"
							+ "Do you need to load data?",
							"Program Load",
							JOptionPane.YES_NO_OPTION);
			if(needData == JOptionPane.NO_OPTION) {
				noDataNeeded = true;
				finalLoad_ReloadStep();			
			} else {
				loadData();
			}
		}
	}

	public void loadData() {
		noDataNeeded = false;
		if(!programLoaded) {
			JOptionPane.showMessageDialog(
					frame, 
					"No program loaded. Please load a program",
					"Error",
					JOptionPane.OK_OPTION);
			return;
		}
		JFileChooser chooser = new JFileChooser(dataDir);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Pippin Data Files", "dat");
		chooser.setFileFilter(filter);
		// CODE TO LOAD DESIRED FILE
		int openOK = chooser.showOpenDialog(null);
		if(openOK == JFileChooser.APPROVE_OPTION) {
			currentDataFile = chooser.getSelectedFile();
		}
		if(currentDataFile != null && currentDataFile.exists()) {
			// CODE TO REMEMBER WHICH DIRECTORY HAS THE dat FILES
			dataDir = currentDataFile .getAbsolutePath();
			dataDir = dataDir.replace('\\','/');
			int lastSlash = dataDir.lastIndexOf('/');
			dataDir = dataDir.substring(0, lastSlash + 1);
			try { 
				properties.setProperty("SourceDirectory", sourceDir);
				properties.setProperty("ExecutableDirectory", executableDir);
				properties.setProperty("DataDirectory", dataDir);
				properties.store(new FileOutputStream("propertyfile.txt"), 
						"File locations");
			} catch (Exception e) {
				System.out.println("Error writing properties file");
			}			
			finalLoad_ReloadStep();
		} 
	}		

	public void finalLoad_ReloadStep() {
		boolean temp1 = programLoaded;
		boolean temp2 = noDataNeeded;
	    clearAll();
	    programLoaded = temp1;
	    noDataNeeded = temp2;
		String str = "";
		if(noDataNeeded)
			str = Loader.load(model, currentProgramFile);
		else
			str = Loader.load(model, currentProgramFile, currentDataFile);
		if(str.equals("success")) {
			model.setRunning(true);
			setRunning(true);
			setAutoStepOn(false);
			setChanged();
			notifyObservers("Load Code");
		}
		else {
			JOptionPane.showMessageDialog(
					frame, 
					"The file being selected has problems.\n" +
							str + "\n" +
							"Cannot load the program",
							"Warning",
							JOptionPane.OK_OPTION);
		}
	}
/**	public void reload(){
		clearAll();
		finalLoad_ReloadStep();
	} */
	void assembleFile(){
		File source = null;
		File outputExe = null;
		JFileChooser chooser = new JFileChooser(sourceDir);
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Pippin Source Files", "pasm");
		chooser.setFileFilter(filter);
		int openOK = chooser.showOpenDialog(null);
		if (openOK == JFileChooser.APPROVE_OPTION){ source = chooser.getSelectedFile();}
		else if (openOK == JFileChooser.CANCEL_OPTION) // Added to prevent additional dialogs if canceled
		{
		    return;
		}
		if(source != null && source.exists()){
			// CODE TO REMEMBER WHICH DIRECTORY HAS THE pexe FILES
			// WHICH WE WILL ALLOW TO BE DIFFERENT FROM sourceDir
			sourceDir = source.getAbsolutePath();
			sourceDir = sourceDir.replace('\\','/');
			int lastDot = sourceDir.lastIndexOf('.');
			String outName = sourceDir.substring(0, lastDot + 1) + "pexe";            
			int lastSlash = sourceDir.lastIndexOf('/');
			sourceDir = sourceDir.substring(0, lastSlash + 1);
			outName = outName.substring(lastSlash+1); 
			filter = new FileNameExtensionFilter(
			            "Pippin Executable Files", "pexe");
			if(executableDir.equals(defaultDir)) {
			    chooser = new JFileChooser(sourceDir);
			} else {
			    chooser = new JFileChooser(executableDir);
			}
			chooser.setFileFilter(filter);
			chooser.setSelectedFile(new File(outName));
			int saveOK = chooser.showSaveDialog(null);
			if(saveOK == JFileChooser.APPROVE_OPTION) {
			    outputExe = chooser.getSelectedFile();
			}
			if(outputExe != null){
				// CODE TO REMEMBER WHICH DIRECTORY HAS THE pexe FILES
				executableDir = source.getAbsolutePath();
				executableDir = executableDir.replace('\\','/');
				lastSlash = executableDir.lastIndexOf('/');
				executableDir = executableDir.substring(0, lastSlash + 1);
				try { 
					properties.setProperty("SourceDirectory", sourceDir);
					properties.setProperty("ExecutableDirectory", executableDir);
					properties.setProperty("DataDirectory", dataDir);
					properties.store(new FileOutputStream("propertyfile.txt"), 
							"File locations");
				} catch (Exception e) {
					System.out.println("Error writing properties file");
				}
				StringBuilder builder = new StringBuilder();
				int ret = Assembler.assemble(source, outputExe, builder);
				if(ret == 0){
					JOptionPane.showMessageDialog(frame, "The source was assembled to an executable", "Success", JOptionPane.INFORMATION_MESSAGE);
				} else{
					JOptionPane.showMessageDialog(
						      frame, 
						      builder.toString(),
						      "Failure on line " + ret,
						      JOptionPane.OK_OPTION);
				}
			}else{
				JOptionPane.showMessageDialog(
					      frame, 
					      "The output file has problems \n" + "Cannot assemble the program",
					      "Warning",
					      JOptionPane.OK_OPTION);
			}
		} else{
			JOptionPane.showMessageDialog(
				      frame, 
				      "The source file has problems \n" + "Cannot assemble the program",
				      "Warning",
				      JOptionPane.OK_OPTION);
		}
	}
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new MachineView(new MachineModel(true)); 
			}
		});
	}
}