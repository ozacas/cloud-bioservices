package au.edu.unimelb.plantcell.nectar;

import java.io.File;
import java.util.ArrayList;

public class ESTScanService {
	/**
	 * Returns a list of scoring models as supported by the service. These should
	 * be constructed by the procedure in the ESTScan manual. The server must be configured
	 * with at least one model!
	 * 
	 * @return
	 */
	public String[] getAvailableScoreModels() {
		File[] smats = new File("/usr/local/estscan/models/").listFiles();
		ArrayList<String> good = new ArrayList<String>();
		for (File f : smats) {
			if (f.getName().toLowerCase().endsWith(".smat")) {
				good.add(f.getName().substring(0, f.getName().length()-5));
			}
		}
		if (good.size() < 1) {
			return new String[] { "Oryza_Sativa" };
		} else {
			return good.toArray(new String[0]);
		}
	}
	
	/**
	 * Returns the specified jobs status
	 */
	public String getStatus(String jobID) {
		return ServerListener.getStatus(jobID);
	}
	
	/**
	 * Returns the result of scanning execution
	 */
	public String getResult(String job_id) {
		return ServerListener.getResult(job_id);
	}
	
	/**
	 * Submit a set of nucleotide sequences (in FASTA format) for analysis by ESTscan using
	 * the specified score model ({@link getAvailableScoreModels}).
	 * @param nucleotide_sequence_as_fasta
	 * @return JobID if successful, null otherwise
	 */
	public String submit(String nucleotide_sequence_as_fasta, String scoring_model) {
		try {
			return ServerListener.submit(new ESTScanJob(nucleotide_sequence_as_fasta, scoring_model));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
