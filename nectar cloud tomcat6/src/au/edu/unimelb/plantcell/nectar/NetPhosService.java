package au.edu.unimelb.plantcell.nectar;

public class NetPhosService {
	
	/**
	 * 
	 * @param fasta
	 * @param generic
	 * @param cutoff
	 * @return the job id
	 */
	public String submit(String fasta, boolean generic, boolean best_only, boolean kinase, double cutoff) {
		return ServerListener.submit(new NetPhosJob(fasta, generic, best_only, kinase, cutoff));
	}
	
	public String getResult(String job_id) {
		return ServerListener.getResult(job_id);
	}
	
	public String getStatus(String job_id) {
		return ServerListener.getStatus(job_id);
	}
}
