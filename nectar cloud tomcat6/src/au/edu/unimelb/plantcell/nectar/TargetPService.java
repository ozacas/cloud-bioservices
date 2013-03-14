package au.edu.unimelb.plantcell.nectar;

import java.io.File;

public class TargetPService {

	/**
	 * Invokes TargetP with the specified arguments. Returns a jobID
	 */
	public String submit(boolean is_plant, double cp_cutoff, double sp_cutoff, double m_cutoff, double o_cutoff, String fasta) {
		try {
			// we must do this here in case Linux boot scripts automagically delete aging folders
			// in the targetp configuration, otherwise targetp will fail when the job is run
			
			// HACK TODO FIXME: this must match the targetp script
			File tmpdir = new File("/tmp/targetp");
			if (!tmpdir.exists() || !tmpdir.isDirectory()) {
				tmpdir.delete();		// remove a plain file or link or whatever...
				tmpdir.mkdir();			// re-create it as a directory
			}
			
			// add the job
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
