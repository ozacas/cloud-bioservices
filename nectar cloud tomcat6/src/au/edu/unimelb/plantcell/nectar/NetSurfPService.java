package au.edu.unimelb.plantcell.nectar;

/**
 * Describes a job
 * @author andrew.cassin
 *
 */
public class NetSurfPService {

	/**
	 * Sequences supplied must be protein sequences (no ambiguous residues) in FASTA format.
	 * Each sequence must have a unique identifier.
	 * 
	 * @param fasta_sequences
	 * @return
	 */
	public String submit(String fasta_sequences) {
		try {
			return ServerListener.submit(new NetSurfPJob(fasta_sequences));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Is the job queued, running or completed?
	 * 
	 * @param jobID
	 * @return
	 */
	public String getStatus(String jobID) {
		return ServerListener.getStatus(jobID);
	}
	
	/**
	 * Returns the result of the specified job. This may be called only once per job: subsequent
	 * calls will return <code>null</code>
	 * 
	 * @param jobID
	 * @return
	 */
	public String getResult(String jobID) {
		return ServerListener.getResult(jobID);
	}
}
