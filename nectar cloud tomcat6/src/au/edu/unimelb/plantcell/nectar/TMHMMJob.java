package au.edu.unimelb.plantcell.nectar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
 * Runs TMHMM (a trans-membrane helice prediction program) for a given set of sequences.
 * It is not recommended to have more than 200 sequences per batch, but it may work depending on
 * available disk space...
 * 
 * @author andrew.cassin
 *
 */
public class TMHMMJob extends AbstractJob<String> {
	private String  m_fasta;
	private File    m_result_file;
	private boolean m_validate = false;
	
	public TMHMMJob(String fasta) throws SOAPException {
		setID("");
		setFastaSequences(fasta);
	}

	public String getFastaSequences() {
		return m_fasta;
	}
	
	public void setFastaSequences(String fasta) throws SOAPException {
		if (m_validate && !isValidFasta(fasta)) {
			throw new SOAPException("Invalid FASTA format sequences - job rejected!");
		}
		m_fasta = fasta;
	}
	
	public String getResult() {
		if (m_result_file == null) {
			return null;
		}
		try {
			StringBuilder   sb= new StringBuilder((int) m_result_file.length());
			BufferedReader rdr= new BufferedReader(new FileReader(m_result_file));
			String line;
			while ((line = rdr.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			rdr.close();
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	@Override
	public String call() {
		CommandLine cmdLine = new CommandLine("/usr/local/tmhmm-2.0c/bin/run.sh");
		setRunning(true);
		
    	// this temp file must be setup
		try {
	    	File tmp_dir = File.createTempFile("tmhmm", ".dir");	// file does not exist yet but will be populated per blast batch...
	    	tmp_dir.delete();
	    	tmp_dir.mkdir();
	    	File tmp_fasta = File.createTempFile("tmhmm", ".fasta", tmp_dir);
	    	
	    	PrintWriter pw = new PrintWriter(new FileWriter(tmp_fasta));
	    	pw.println(getFastaSequences());
	    	pw.close();
	    	
	    	final Map<String, File> map = new HashMap<String,File>();
	    	map.put("fasta", tmp_fasta);
	    	map.put("output_dir", tmp_dir);
	    	cmdLine.setSubstitutionMap(map);
	    	cmdLine.addArgument("${fasta}");
	    	cmdLine.addArgument("${output_dir}");
	    
	    	DefaultExecutor exe = new DefaultExecutor();
	    	exe.setExitValues(new int[] {0, 1});
	    	exe.setStreamHandler(new PumpStreamHandler(new NullLogger(), new ErrorLogger(Logger.getAnonymousLogger())));
	    	exe.setWorkingDirectory(map.get("output_dir"));		// MUST be this folder!
	    	exe.setWatchdog(new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT));
	    	
	    	int exitCode = exe.execute(cmdLine);
			if (exe.isFailure(exitCode)) {
				ServerListener.setError(getID(), new Exception("Invalid exit code from "+exitCode));
				return null;
	    	} else {
	    		m_result_file = new File(tmp_dir, "predictions.txt");
	    		Logger.getAnonymousLogger().info("Result file for TMHMM job: "+getID()+" is "+m_result_file.getAbsolutePath());
	    		return getResult();
	    	}
		} catch (Exception e) {
			Logger.getAnonymousLogger().severe(e.getMessage());
			e.printStackTrace();
			ServerListener.setError(getID(), e);
			return null;
		} 
	}

	@Override
	public void cleanup() {
		if (m_result_file != null) {
			File parent_folder = m_result_file.getParentFile();
			try {
				Logger.getAnonymousLogger().info("Cleaning up job folder: "+parent_folder.getAbsolutePath());
				String parent_folder_path = parent_folder.getAbsolutePath();
				if (parent_folder_path.indexOf("..") >= 0)
					throw new Exception("Cowardly refusing to delete folder with .. in path");
				int slash_cnt = 0;
				for (int i=0; i<parent_folder_path.length(); i++) {
					if (parent_folder_path.charAt(i) == File.separatorChar) {
						slash_cnt++;
					}
				}
				if (slash_cnt < 3) 
					throw new Exception("Cowardly refusing to delete folder with fewer than three "+File.separatorChar+"'s in it.");
				delete(parent_folder);
			} catch (Exception e) {
				Logger.getAnonymousLogger().warning("Unable to delete job folder: "+parent_folder.getName());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * The <code>run()</code> method arranges via a wrapper shell script to store all prediction
	 * related data in a single folder. This folder must be cleaned up if we are no longer going
	 * to store the results. So this method exists to delete the parent folder. <b>WARNING:</b> this
	 * method could delete large parts of your files if you are not careful!
	 * 
	 * @param file
	 * @throws IOException
	 */
	public static void delete(File file) throws IOException {
		if(file.isDirectory()){
			//directory is empty, then delete it
			if (file.list().length==0){
			   file.delete();
			} else{
			   //list all the directory contents
	    	   String files[] = file.list();
	
	    	   for (String temp : files) {
	    	      //construct the file structure
	    	      File fileDelete = new File(file, temp);
	
	    	      //recursive delete
	    	      delete(fileDelete);
	    	   }
	
	    	   //check the directory again, if empty then delete it
	    	   if(file.list().length==0){
	       	     file.delete();
	    	   }
			}
		} else {
			//if file, then delete it
			file.delete();
		}
	}
}
