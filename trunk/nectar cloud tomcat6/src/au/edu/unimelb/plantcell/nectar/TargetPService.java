package au.edu.unimelb.plantcell.nectar;

public class TargetPService {

	/**
	 * Invokes TargetP with the specified arguments. Returns a jobID
	 */
	public String submit(boolean is_plant, double cp_cutoff, double sp_cutoff, double m_cutoff, double o_cutoff, String fasta) {
		try {
			return ServerListener.submit(new TargetPJob(is_plant, cp_cutoff, sp_cutoff, m_cutoff, o_cutoff, fasta));
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
