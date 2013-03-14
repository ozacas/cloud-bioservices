package au.edu.unimelb.plantcell.nectar;

public class SignalPService {
	
	/**
	 * Runs the SignalP method for the specified type of organism with the cutoffs over the
	 * FASTA-formatted sequences. There should be no more than 1MB of sequence data per invocation
	 * of the service (100 sequences is ok)
	 * 
	 * @param fasta			fasta format data to analyze
	 * @param tm_cutoff		cutoff for TM-prediction method
	 * @param notm_cutoff	cutoff for no TM-prediction method
	 * @param best_or_notm	use best prediction method or no-TM prediction (signalP v3) only? true means best
	 * @param length		0 means no cutoff, default is 70 N-terminal residues only
	 * @param euk_plus_neg  One of euk, plus, neg
	 * @return
	 */
	public String submit(String fasta, 
			double tm_cutoff, double notm_cutoff, 
			boolean best_or_notm, 
			int length, String euk_plus_neg) {
		
		try {
			return ServerListener.submit(new SignalPJob(fasta, tm_cutoff, notm_cutoff, best_or_notm, length, euk_plus_neg));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getResult(String job_id) {
		return ServerListener.getResult(job_id);
	}
	
	public String getStatus(String job_id) {
		return ServerListener.getStatus(job_id);
	}
}
