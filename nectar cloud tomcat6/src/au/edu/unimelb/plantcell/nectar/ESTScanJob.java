package au.edu.unimelb.plantcell.nectar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;


/**
 * Runs ESTscan (estscan.sourceforge.net) on the specified nucleotide sequences (up to 2MB per batch)
 * and returns both the nucleotide and protein translated results. The user has some control over options,
 * but this is not the full functionality of estscan and associated programs.
 * 
 * @author andrew.cassin
 *
 */
public class ESTScanJob extends AbstractJob<String> {
	private File m_nucl_results;
	private File m_prot_results;
	private File m_score_model;	// read only
	private String m_fasta;
	
	public ESTScanJob(String fasta, String score_model) throws Exception {
		setID("");

		Pattern p = Pattern.compile("^\\w+$");
		Matcher m = p.matcher(score_model);
		if (!m.matches()) {
			throw new Exception("Invalid Score model!");
		}
		if (!score_model.endsWith(".smat"))
			score_model += ".smat";
		File f = new File("/usr/local/estscan/models/", score_model);
		if (!f.exists() || !f.canRead()) {
			throw new Exception("Unknown score model: "+score_model);
		}
		m_score_model = f;
		m_nucl_results= File.createTempFile("out_estscan_nucl", ".fasta");
		m_prot_results= File.createTempFile("out_estscan_prot", ".fasta");
		setFastaSequences(fasta);
	}
	
	public void setFastaSequences(String new_fasta) {
		m_fasta = new_fasta;
	}
	
	public String getFastaSequences() {
		return m_fasta;
	}
	
	@Override
	public String call() {
		setRunning(true);
		
		try {
			File tmp_fasta = File.createTempFile("estscan_tmp", ".fasta");	// file does not exist yet but will be populated per blast batch...
	    	PrintWriter pw = new PrintWriter(new FileWriter(tmp_fasta));
	    	pw.println(getFastaSequences());
	    	pw.close();
	    	
	    	final Map<String, File> map = new HashMap<String,File>();
	    	map.put("fasta", tmp_fasta);
	    	map.put("nucl", m_nucl_results);
	    	map.put("prot", m_prot_results);
	    	map.put("model", m_score_model);
	    	CommandLine cmdLine = new CommandLine("/usr/local/estscan/bin/estscan");
	    	cmdLine.setSubstitutionMap(map);
	    
	    	cmdLine.addArgument("-M");
	    	cmdLine.addArgument("${model}");
	    	cmdLine.addArgument("-o");
	    	cmdLine.addArgument("${nucl}");
	    	cmdLine.addArgument("-t");
	    	cmdLine.addArgument("${prot}");
	    	cmdLine.addArgument("${fasta}");
	    
	    	DefaultExecutor exe = new DefaultExecutor();
	    	exe.setExitValues(new int[] {0});
	    	exe.setStreamHandler(new PumpStreamHandler(new NullLogger(), new NullLogger()));
	    	exe.setWorkingDirectory(map.get("fasta").getParentFile());		// arbitrary choice
	    	exe.setWatchdog(new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT));
	    	Logger.getAnonymousLogger().info(cmdLine.toString());
	    	int exitCode = exe.execute(cmdLine);
			tmp_fasta.delete();
			
			if (exe.isFailure(exitCode)) {
				ServerListener.setError(getID(), new Exception("Invalid exit code from estscan: "+exitCode));
	    		return null;
	    	} 
			
			StringBuilder sb = new StringBuilder((int) (100 + m_nucl_results.length() + m_prot_results.length()));
			sb.append("# nucleotide results\n");
			append_file(sb, m_nucl_results);
			sb.append("# protein results\n");
			append_file(sb, m_prot_results);
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
	}

	private void append_file(StringBuilder sb, File f) throws IOException  {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "ASCII"));
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}
		br.close();
	}
	
	@Override
	public void cleanup() {
		try {
			m_nucl_results.delete();
			m_prot_results.delete();
		} catch (Exception e) {
			// NO-OP
		}
	}

}
