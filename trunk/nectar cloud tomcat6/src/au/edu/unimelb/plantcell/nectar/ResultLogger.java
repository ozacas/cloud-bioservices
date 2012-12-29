package au.edu.unimelb.plantcell.nectar;

import org.apache.commons.exec.LogOutputStream;

/**
 * Records each line of output from the command-line invocation to an internal
 * StringBuffer. The caller may retrieve this value at any time from <code>toString()</code>
 * 
 * @author andrew.cassin
 *
 */
public class ResultLogger extends LogOutputStream {
	StringBuffer sb = new StringBuffer(100 * 1024);
	
	@Override
	protected void processLine(String line, int lvl) {
		sb.append(line);
		sb.append('\n');
	}

	@Override
	public String toString() {
		return sb.toString();
	}
}
	
	