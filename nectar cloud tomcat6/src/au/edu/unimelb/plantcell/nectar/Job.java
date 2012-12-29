package au.edu.unimelb.plantcell.nectar;

import java.util.concurrent.Callable;

/**
 * An abstraction to represent a single invocation of a given bioinformatics program a single
 * set of data provided by a web service. Each job maintains its own state: current status, ID,
 * results etc. which are invoked at the appropriate time by the framework.
 * 
 * @author andrew.cassin
 *
 */
public interface Job<T> extends Callable<T> {
	/**
	 * Return the current job status
	 * 
	 * @return one of QUEUED, BUSY, FAILED, COMPLETE and RUNNING
	 */
	public String getStatus();
	
	/**
	 * Set the job ID to the specified value
	 * @param string
	 */
	public void setID(String string);
	
	/**
	 * Returns the job ID associated with the job
	 */
	public String getID();
	
	/**
	 * Equivalent to <code>getID().equals(jobID)</code>
	 */
	public boolean hasID(String jobID);
	
	/**
	 * Run the specified job
	 */
	public T call();
	
	/**
	 * Remove any trace (eg. temporary files) from the system, when called the results for the 
	 * invoking job will no longer be available.
	 */
	public void cleanup();
	
	/**
	 * Hashcode is the hashcode of the ID (which must be unique for each job)
	 * @return
	 */
	public int hashCode();
}
