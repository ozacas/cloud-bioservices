package au.edu.unimelb.plantcell.nectar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

/**
 * 
 * @author andrew.cassin
 *
 */
public class SignalPJob extends AbstractJob<String> {
	private String m_fasta;
	private double m_tm, m_no_tm;
	private boolean m_use_best_method;
	private int    m_length;
	private String m_organism;
	private File   m_result_gff, m_fasta_file;
	
	public SignalPJob(String fasta, double tm_cutoff, double no_tm_cutoff, 
									boolean best_method, int length, String euk_plus_neg) throws Exception {
		super();
		m_fasta = fasta;
		m_fasta_file = null;
		m_tm    = tm_cutoff;
		m_no_tm = no_tm_cutoff;
		m_length= length;
		m_result_gff = null;
		String s = euk_plus_neg.toLowerCase();
		if (s.startsWith("euk")) {
			m_organism = "euk";
		} else if (s.startsWith("plus")) {
			m_organism = "gram+";
		} else if (s.startsWith("neg")) {
			m_organism = "gram-";
		} else
			throw new Exception("Illegal parameter: must be one of euk, plus or neg");
		m_use_best_method = best_method;
	}
	
	@Override
	public String call() {
		CommandLine cmdLine = new CommandLine("/usr/local/signalp-4.0/signalp");
		setRunning(true);
		cmdLine.addArgument("-t");
		cmdLine.addArgument(m_organism);
		cmdLine.addArgument("-f");
		cmdLine.addArgument("short");
		try {
			m_result_gff = File.createTempFile("insignalp", "out.gff");
			m_fasta_file = File.createTempFile("insignalp", "in.fasta");
			PrintWriter pw = new PrintWriter(m_fasta_file);
			pw.print(m_fasta);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		if (m_length >= 0) {
			cmdLine.addArgument("-c");
			cmdLine.addArgument(""+m_length);
		}
		cmdLine.addArgument("-s");
		if (m_use_best_method) {
			cmdLine.addArgument("best");
		} else {
			cmdLine.addArgument("notm");
		}
		if (m_no_tm >= 0.0 && !Double.isNaN(m_no_tm)) {
			cmdLine.addArgument("-u");
			cmdLine.addArgument(Double.toString(m_no_tm));
		}
		if (m_tm >= 0.0 && !Double.isNaN(m_tm)) {
			cmdLine.addArgument("-U");
			cmdLine.addArgument(Double.toString(m_tm));
		}
		
		final Map<String, File> map = new HashMap<String,File>();
    	map.put("fasta", m_fasta_file);
    	map.put("gff", m_result_gff);
    	cmdLine.setSubstitutionMap(map);
    	cmdLine.addArgument("-n");
		cmdLine.addArgument("${gff}");
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
				ServerListener.setError(getID(), new Exception("Invalid exit code from SignalP: "+exitCode));
	    		return null;
	    	} 
			BufferedReader rdr = new BufferedReader(new InputStreamReader(new FileInputStream(m_result_gff)));
			String line;
			StringBuilder sb = new StringBuilder(rl.toString());
			sb.append("#\n");
			while ((line = rdr.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			return sb.toString();
    	} catch (Exception e) {
    		e.printStackTrace();
    		return null;
    	}
	}

	@Override
	public void cleanup() {
		if (m_result_gff != null) 
			m_result_gff.delete();
		if (m_fasta_file != null)
			m_fasta_file.delete();
	}

}
