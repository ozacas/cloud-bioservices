package au.edu.unimelb.plantcell.nectar;


/**
 * Runs the prediction of subcellular localisation for the specified sequences (FASTA file)
 * using wolf psort and returns the results to the caller.
 * 
 * @author andrew.cassin
 *
 */
public class WolfPSortService {
	
	/**
	 * Submits a PSort job to the server. The job is immediately placed into the queue,
	 * but depending on other activities it may be some time before it is run. PSort jobs
	 * require only two parameters: the model used to make the prediction and the set
	 * of sequences to predict. No more than 2MB of protein sequence may be specified in
	 * a single submission.
	 * 
	 * @param fasta_sequences
	 * @param organism
	 * @return
	 */
	public String submit(String fasta_sequences, String organism) {
		try {
			return ServerListener.submit(new PSortJob(fasta_sequences, organism));
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
	
	/**
	 * Returns the textual output from the specified job. Care must be taken to ensure that
	 * the String is not so large as to overrun an internal server limit. Each service will
	 * place constraints on the size of the job which may be submitted.
	 * 
	 * @param jobID
	 * @return
	 */
	public String getResult(String jobID) {
		return ServerListener.getResult(jobID);
	}
}
