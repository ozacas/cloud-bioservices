package au.edu.unimelb.plantcell.nectar;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.soap.SOAPException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * 
 * @author andrew.cassin
 *
 */
public class TargetPJob extends AbstractJob<String> {
	private String  m_fasta;
	private double  cp_cutoff, m_cutoff, sp_cutoff, o_cutoff;
	private boolean m_plant;
	private File    m_fasta_file;
	
	public TargetPJob(boolean is_plant, double cp_cutoff, double sp_cutoff, double m_cutoff, double o_cutoff, String fasta) throws Exception {
		super();
		if (cp_cutoff < 0 || cp_cutoff > 1 ||
				sp_cutoff < 0 || sp_cutoff > 1 ||
				m_cutoff < 0 || m_cutoff > 1 ||
				o_cutoff < 0 || o_cutoff > 1)
			throw new SOAPException("Cutoff values must be in the range [0,1]... aborting!");
		m_fasta = fasta;
		m_fasta_file = null;
		m_plant = is_plant;
		this.cp_cutoff = cp_cutoff;
		this.m_cutoff  = m_cutoff;
		this.sp_cutoff = sp_cutoff;
		this.o_cutoff  = o_cutoff;
	}
	
	@Override
	public String call() {
		CommandLine cmdLine = new CommandLine("/usr/local/targetp-1.1/targetp");
		setRunning(true);
		cmdLine.addArgument(m_plant ? "-P" : "-N");
		cmdLine.addArgument("-c");		// always run chlorop/signalp for now...
		cmdLine.addArgument("-p");
		cmdLine.addArgument(Double.toString(cp_cutoff));
		cmdLine.addArgument("-t");
		cmdLine.addArgument(Double.toString(m_cutoff));
		cmdLine.addArgument("-s");
		cmdLine.addArgument(Double.toString(sp_cutoff));
		cmdLine.addArgument("-o");
		cmdLine.addArgument(Double.toString(o_cutoff));
		
		try {
			m_fasta_file = File.createTempFile("intargetp", "in.fasta");
			PrintWriter pw = new PrintWriter(m_fasta_file);
			pw.print(m_fasta);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		final Map<String, File> map = new HashMap<String,File>();
    	map.put("fasta", m_fasta_file);
    	cmdLine.setSubstitutionMap(map);
    	cmdLine.addArgument("${fasta}");
    	
		DefaultExecutor exe = new DefaultExecutor();
    	exe.setExitValues(new int[] {0});
    	ResultLogger rl = new ResultLogger();
    	exe.setStreamHandler(new PumpStreamHandler(rl, new ErrorLogger(Logger.getAnonymousLogger())));
    	exe.setWorkingDirectory(map.get("fasta").getParentFile());		// arbitrary choice
    	exe.setWatchdog(new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT));
    	
    	try {
    		Logger.getAnonymousLogger().info("Running: "+cmdLine.toString());
	    	int exitCode = exe.execute(cmdLine);
	
			if (exe.isFailure(exitCode)) {
				ServerListener.setError(getID(), new Exception("Invalid exit code from TargetP: "+exitCode));
	    		return null;
	    	} 
			
			return rl.toString();
    	} catch (Exception e) {
    		e.printStackTrace();
    		return null;
    	}
	}

	@Override
	public void cleanup() {
		if (m_fasta_file != null)
			m_fasta_file.delete();
	}

}

