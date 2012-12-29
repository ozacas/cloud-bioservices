package au.edu.unimelb.plantcell.nectar;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javax.xml.soap.SOAPException;

/**
 * Represents a non-persistent job queue: jobs are submitted and maintained through to completion.
 * This implementation implements a simple-minded "keep last 10 job results" policy, jobs older that
 * the last ten are discarded to save disk space (configured via a constant in this class).
 * 
 * @author andrew.cassin
 *
 */
public class JobQueue {
	private String    m_basename  = "";
	private ExecutorService servicer;
	
	public final HashMap<String,Job<String>> m_jobs = new HashMap<String,Job<String>>(1027);
	public final HashMap<String,FailableFuture<String>> m_results = new HashMap<String,FailableFuture<String>>();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4720785533574473806L;

	protected JobQueue() {
		this("nectar");
	}
	
	protected JobQueue(String basename) {
		m_basename = basename;
		servicer   = Executors.newSingleThreadExecutor();
	}
	
	public synchronized String submit(Job<String> j) {
		String id = m_basename + Long.toHexString(System.currentTimeMillis());
		j.setID(id);
		m_jobs.put(id, j);
		Future<String> s = servicer.submit(j);
		m_results.put(id, new FailableFuture<String>(s));
		return id;
	}
	
	public String getStatus(String jobID) {
		FailableFuture<String> j = m_results.get(jobID);
		if (j == null) {
			return "UNKNOWN JOB";
		} else if (j.isCancelled()) {
			return "CANCELLED";
		} else if (!j.isDone()) {
			if (j.isRunning())
				return "RUNNING";
			// else...
			return "QUEUED";
		} else if (j.isError()) {
			return "FAILED";
		} else {
			return "COMPLETED";
		}
	}

	/**
	 * Returns true if the specified job is *present* in the completed list, false otherwise.
	 * A job may have completed but have been discarded as well, so false does not indicate
	 * that the job is still running.
	 * 
	 * @param jobID
	 * @return
	 */
	public synchronized boolean hasCompletedJob(String jobID) {
		FailableFuture<String> j = m_results.get(jobID);
		if (j == null || j.isError()) {
			Logger.getAnonymousLogger().info("Job "+jobID+" does not exist or has failed to run");
			return false;
		}
		boolean ret = j.isDone();
		Logger.getAnonymousLogger().info("Job "+jobID+" has completion status: "+ret);
		return ret;
	}

	/**
	 * Returns the results from Wolf PSort as specified by the caller's <code>jobID</code>.
	 * An exception is thrown if the specified job results are not available.
	 * 
	 * @param jobID
	 * @return
	 * @throws SOAPException
	 */
	public synchronized String getResult(String jobID) throws SOAPException {
		if (!hasCompletedJob(jobID))
			return null;
		Logger.getAnonymousLogger().info("Fetching results for "+jobID);
		FailableFuture<String> result = m_results.get(jobID);
		m_jobs.remove(jobID);	// one shot result get (object is trashed)
		m_results.remove(jobID);
		try {
			String ret = result.get();
			if (ret != null)
				Logger.getAnonymousLogger().info("Got "+ret.length()+" bytes of results for job "+jobID);
			return ret;
		} catch (Exception e) {
			result.setError(e);
			e.printStackTrace();
			Logger.getAnonymousLogger().warning(e.getMessage());
			return null;
		} 
	}
	
	public void ShutdownNow() {
		if (servicer != null)
			servicer.shutdownNow();
	}

	public void setError(String id, Exception e) {
		FailableFuture<String> result = m_results.get(id);
		if (result != null)
			result.setError(e);
	}

	public void setRunning(AbstractJob<String> j, boolean is_running) {
		if (j == null)
			return;
		FailableFuture<String> result = m_results.get(j.getID());
		if (result == null)
			return;
		result.setRunning(is_running);
	}
}
