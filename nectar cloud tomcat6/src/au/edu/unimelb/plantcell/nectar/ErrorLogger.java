package au.edu.unimelb.plantcell.nectar;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.exec.LogOutputStream;

/**
 * Sends an output from a command line invocation (typically stderr of the
 * program) to the specified logger (at <code>Level.SEVERE</code> log level).
 * Useful for ensuring that any errors during the Wolf PSort prediction process
 * get logged somewhere.
 * 
 * @author andrew.cassin
 *
 */
public class ErrorLogger extends LogOutputStream {
	private Logger m_logger;
	
	public ErrorLogger(Logger l) {
		m_logger = l;
	}
	
	@Override
	protected void processLine(String line, int lvl) {
		m_logger.log(Level.SEVERE, line);
	}

}
