package au.edu.unimelb.plantcell.nectar;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

public class NetPhosJob extends AbstractJob<String> {
	private String  m_fasta;
	private double  m_cutoff;
	private boolean m_generic, m_best_only, m_kinase;
	
	public NetPhosJob(String fasta, boolean generic, boolean best_only, boolean kinase, double cutoff) {
		setID("");
		setFastaSequences(fasta);
		setCutoff(cutoff);
		setFlags(generic, best_only, kinase);
	}
	
	private void setCutoff(double cutoff) {
		m_cutoff = cutoff;
	}
	
	private void setFlags(boolean generic, boolean best_only, boolean kinase) {
		m_generic   = generic;
		m_best_only = best_only;
		m_kinase    = kinase;
	}
	
	private void setFastaSequences(String fasta) {
		m_fasta = fasta;
	}
	
	private String getFastaSequences() {
		return m_fasta;
	}
	
	private boolean useGenericPredictions() {
		return m_generic;
	}
	
	private boolean useBestOnly() {
		return m_best_only;
	}
	
	private boolean useNetPhosK() {
		return m_kinase;
	}
	
	@Override
	public String call() {
		CommandLine cmdLine = new CommandLine("/usr/local/netphos/ape-1.0/netphos-3.1");
		setRunning(true);
    	Logger l = Logger.getAnonymousLogger();
		
    	// this temp file must be setup
		try {
	    	File tmp_fasta = File.createTempFile("netphos_tmp", ".fasta");	// file does not exist yet but will be populated per blast batch...
	    	PrintWriter pw = new PrintWriter(new FileWriter(tmp_fasta));
	    	pw.println(getFastaSequences());
	    	pw.close();
	    	
	    	final Map<String, File> map = new HashMap<String,File>();
	    	map.put("fasta", tmp_fasta);
	    	cmdLine.setSubstitutionMap(map);
	    	cmdLine.addArgument("-f");			
	    	cmdLine.addArgument("gff");
	    	if (useNetPhosK()) {
	    		cmdLine.addArgument("-k");
	    	}
	    	if (useGenericPredictions()) {		// version 2.0 of NetPhos
	    		cmdLine.addArgument("-2");
	    	}
	    	if (useBestOnly()) {
	    		cmdLine.addArgument("-b");
	    	}
	    	cmdLine.addArgument("-c");
	    	cmdLine.addArgument(new Double(getCutoff()).toString());
	    	cmdLine.addArgument("${fasta}");
	    
	    	l.info("Running: "+cmdLine);
	    	DefaultExecutor exe = new DefaultExecutor();
	    	exe.setExitValues(new int[] {0, 1});
	    	ResultLogger rl = new ResultLogger();
	    	exe.setStreamHandler(new PumpStreamHandler(rl, new NullLogger()));
	    	exe.setWorkingDirectory(map.get("fasta").getParentFile());		// arbitrary choice
	    	exe.setWatchdog(new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT));
	    	
	    	int exitCode = exe.execute(cmdLine);
			tmp_fasta.delete();
			
			l.info("NetPhos process exit code is: "+exitCode);
			
			//if (exe.isFailure(exitCode)) {
			//	ServerListener.setError(getID(), new Exception("Invalid exit code from netphos: "+exitCode));
	    	//	return null;
	    	//} 
			return rl.toString();
		} catch (Exception e) {
			Logger.getAnonymousLogger().severe(e.getMessage());
			e.printStackTrace();
			ServerListener.setError(getID(), e);
			return null;
		} 
	}

	private double getCutoff() {
		return m_cutoff;
	}

	@Override
	public void cleanup() {
		// NO-OP for now
	}

}
