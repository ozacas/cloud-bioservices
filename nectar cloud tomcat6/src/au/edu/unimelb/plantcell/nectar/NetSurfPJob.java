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


/**
 * Runs the CBS netsurfp secondary structure prediction algorithm over the specified
 * FASTA sequences (which must be protein sequences!)
 * 
 * @author andrew.cassin
 *
 */
public class NetSurfPJob extends AbstractJob<String> {
	private String m_fasta;
	
	public NetSurfPJob(String fasta_sequences) {
		setID("");
		setFastaSequences(fasta_sequences);
	}
	
	private void setFastaSequences(String fasta) {
		m_fasta = fasta;
	}
	
	private String getFastaSequences() {
		return m_fasta;
	}
	
	@Override
	public String call() {
		CommandLine cmdLine = new CommandLine("/usr/local/netsurfp-1.0/netsurfp");
		setRunning(true);
		
    	// this temp file must be setup
		try {
	    	File tmp_fasta = File.createTempFile("netsurfp_tmp", ".fasta");	// file does not exist yet but will be populated per blast batch...
	    	PrintWriter pw = new PrintWriter(new FileWriter(tmp_fasta));
	    	pw.println(getFastaSequences());
	    	pw.close();
	    	
	    	final Map<String, File> map = new HashMap<String,File>();
	    	map.put("fasta", tmp_fasta);
	    	cmdLine.setSubstitutionMap(map);
	    	cmdLine.addArgument("-a");			// 10 column output format IS REQUIRED
	    	cmdLine.addArgument("-i");
	    	cmdLine.addArgument("${fasta}");
	    
	    	DefaultExecutor exe = new DefaultExecutor();
	    	exe.setExitValues(new int[] {0, 1});
	    	ResultLogger rl = new ResultLogger();
	    	exe.setStreamHandler(new PumpStreamHandler(rl, new NullLogger()));
	    	exe.setWorkingDirectory(map.get("fasta").getParentFile());		// arbitrary choice
	    	exe.setWatchdog(new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT));
	    	
	    	int exitCode = exe.execute(cmdLine);
			tmp_fasta.delete();
			
			if (exe.isFailure(exitCode)) {
				ServerListener.setError(getID(), new Exception("Invalid exit code from netsurfp: "+exitCode));
	    		return null;
	    	} 
			return rl.toString();
		} catch (Exception e) {
			Logger.getAnonymousLogger().severe(e.getMessage());
			e.printStackTrace();
			ServerListener.setError(getID(), e);
			return null;
		} 
	}

	@Override
	public void cleanup() {
		// no temp files to cleanup
	}

}
