package au.edu.unimelb.plantcell.nectar;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.soap.SOAPException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * An abstraction for running a small series of sequences, in a single invocation,
 * to Wolf PSort (http://www.wolfpsort.org). This implementation handles passing
 * the right arguments from the SOAP client to a wrapper script which then invokes
 * PSORT. The results, as a single string, are returned to the client.
 * 
 * @author andrew.cassin
 *
 */
public class PSortJob extends  AbstractJob<String> {
	private final static HashSet<String> acceptable_organism = new HashSet<String>();
	static {
		acceptable_organism.add("plant");
		acceptable_organism.add("animal");
		acceptable_organism.add("fungi");
	}
	private String        m_fasta;
	private String        m_organism;
	private boolean       m_validate = false;
	
	public PSortJob(String fasta) throws SOAPException {
		this(fasta, "plant");
	}
	
	public PSortJob(String fasta, String organism) throws SOAPException {
		assert(fasta != null && organism != null);
	
		setID("");
		setFastaSequences(fasta);
		setOrganism(organism);
	}
	
	/**
	 * Set the batch of sequences to run thru PSort. The FASTA data is checked
	 * carefully to avoid hackers...
	 * 
	 * @param fasta
	 * @throws SOAPException
	 */
	public void setFastaSequences(String fasta) throws SOAPException {
		if (m_validate && !isValidFasta(fasta)) {
			throw new SOAPException("Invalid FASTA data -- job rejected");
		}
		m_fasta = fasta;
	}

	public String getFastaSequences() {
		return m_fasta;
	}
	
	public String getOrganism() {
		return m_organism;
	}

	public void setOrganism(String new_organism) throws SOAPException {
		if (new_organism == null || !acceptable_organism.contains(new_organism)) {
			throw new SOAPException("Invalid organism: "+new_organism);
		}
		m_organism = new_organism;
	}
	
	@Override
	public String call() {
		CommandLine cmdLine = new CommandLine("/usr/local/WoLFPSORT_package_v0.2/bin/run.sh");
		setRunning(true);
		
    	// this temp file must be setup
		try {
	    	File tmp_fasta = File.createTempFile("psort_tmp", ".fasta");	// file does not exist yet but will be populated per blast batch...
	    	PrintWriter pw = new PrintWriter(new FileWriter(tmp_fasta));
	    	pw.println(getFastaSequences());
	    	pw.close();
	    	
	    	final Map<String, File> map = new HashMap<String,File>();
	    	map.put("fasta", tmp_fasta);
	    	cmdLine.setSubstitutionMap(map);
	    	cmdLine.addArgument("${fasta}");
	    	cmdLine.addArgument(getOrganism());
	    
	    	DefaultExecutor exe = new DefaultExecutor();
	    	exe.setExitValues(new int[] {0, 1});
	    	ResultLogger rl = new ResultLogger();
	    	exe.setStreamHandler(new PumpStreamHandler(rl, new ErrorLogger(Logger.getAnonymousLogger())));
	    	exe.setWorkingDirectory(map.get("fasta").getParentFile());		// arbitrary choice
	    	exe.setWatchdog(new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT));
	    	
	    	int exitCode = exe.execute(cmdLine);
			tmp_fasta.delete();

			if (exe.isFailure(exitCode)) {
				ServerListener.setError(getID(), new Exception("Invalid exit code from PSort: "+exitCode));
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
		// no-temp files to cleanup at the moment...
	}

}
