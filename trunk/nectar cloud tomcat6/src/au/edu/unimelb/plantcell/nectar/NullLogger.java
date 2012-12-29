package au.edu.unimelb.plantcell.nectar;

import org.apache.commons.exec.LogOutputStream;

/**
 * Sends output to /dev/null
 * @author andrew.cassin
 *
 */
public class NullLogger extends LogOutputStream {

	public NullLogger() {
	}
	
	@Override
	protected void processLine(String arg0, int arg1) {
		// NO-OP
	}

}
