package au.edu.unimelb.plantcell.nectar;


/**
 * Entry point for the TMHMM web service. Methods must exist here for the service
 * definition to exist
 * @author andrew.cassin
 *
 */
public class TMHMMService {
	
	/**
	 * Submits a small batch of PROTEIN sequences (typically no more than 100 or 100KB which
	 * ever comes first) for analysis with TMHMM. The job will compete for access with other
	 * jobs on the same server, so it may not run for some time. The jobID is returned, the caller
	 * expected to poll at infrequent intervals for completion.
	 * 
	 * @param fasta_sequences
	 * @param organism one of "plant" or "non-plant"
	 * @return
	 */
	public String submit(String fasta_sequences, String organism) {
		try {
			return ServerListener.submit(new TMHMMJob(fasta_sequences));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Return the current status of the job: one of QUEUED, RUNNING, COMPLETED, FAILED
	 * @param jobID the job id to check the status of
	 * @return
	 */
	public String getStatus(String jobID) {
		return ServerListener.getStatus(jobID);
	}
	
	public String getResult(String jobID) {
		return ServerListener.getResult(jobID);
	}
}
