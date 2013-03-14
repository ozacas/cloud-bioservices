package au.edu.unimelb.plantcell.nectar;

import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.soap.SOAPException;

/**
 * Responsible for managing the job queue and thread(s) involved in processing the jobs.
 * An instance of this class <b>must</b> be registered as a listener to the servlet container
 * (eg. tomcat) instance so that it correct starts and stops the job scheduling queue when
 * tomcat is started/shutdown. In the event of shutdown, any running jobs will be forcibly terminated,
 * losing results.
 * 
 * @author http://www.plantcell.unimelb.edu.au/bioinformatics
 *
 */
public class ServerListener implements ServletContextListener {
	private final static String JOB_QUEUE = "production-job-queue";

	private static ServletContext sc;
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		sc = arg0.getServletContext();
		Object servicer = sc.getAttribute(JOB_QUEUE);
		if (servicer != null) {
			sc.removeAttribute(JOB_QUEUE);
			sc = null;
			((JobQueue)servicer).ShutdownNow();
			Logger.getAnonymousLogger().info("Stopped job servicer: "+servicer);
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		sc = arg0.getServletContext();
		Object servicer = sc.getAttribute(JOB_QUEUE);
		if (servicer == null) {
			servicer = new JobQueue();
			sc.setAttribute(JOB_QUEUE, servicer);
			Logger.getAnonymousLogger().info("Starting job servicer: "+servicer);
		}
	}

	/**
	 * Adds the specified job to the servicer. At this time, there is only one
	 * nectar server, so only one job is run at a time.
	 * 
	 * @param j
	 * @return
	 */
	public static String submit(AbstractJob<String> j) {
		if (sc == null)
			return "NO QUEUE";
		JobQueue servicer = (JobQueue) sc.getAttribute(JOB_QUEUE);
		if (servicer == null)
			return "NO QUEUE";
		return servicer.submit(j);
	}

	/**
	 * Is the job queued, running or completed? This method may also return unknown job
	 * for all jobs which have completed and results have been returned.
	 * @param jobID
	 * @return
	 */
	public static String getStatus(String jobID) {
		if (sc == null)
			return "NO QUEUE";
		JobQueue servicer = (JobQueue) sc.getAttribute(JOB_QUEUE);
		if (servicer == null)
			return "NO QUEUE";
		return servicer.getStatus(jobID);
	}
	
	/**
	 * Returns the result associated with the specified job. This method may be called
	 * only once per job, subsequent calls will return <code>null</code>
	 * @param jobID
	 * @return
	 */
	public static String getResult(String jobID) {
		if (sc == null)
			return null;
		JobQueue servicer = (JobQueue) sc.getAttribute(JOB_QUEUE);
		if (servicer == null)
			return null;
		
		try {
			return servicer.getResult(jobID);
		} catch (SOAPException e) {
			e.printStackTrace();
		}
	
		return null;
	}

	public static void setError(String id, Exception e) {
		if (sc == null)
			return;
		JobQueue servicer = (JobQueue) sc.getAttribute(JOB_QUEUE);
		if (servicer == null)		// should not happen!
			return;
		servicer.setError(id, e);
	}

	public static void setRunning(AbstractJob<String> j, boolean is_running) {
		if (sc == null)
			return;
		JobQueue servicer = (JobQueue) sc.getAttribute(JOB_QUEUE);
		servicer.setRunning(j, is_running);
	}
}
