package au.edu.unimelb.plantcell.nectar;

import javax.xml.soap.SOAPException;

/**
 * An abstraction to provide common code to all jobs. Most of the bioinformatics programs 
 * work with FASTA files, so there is code here for that.
 * 
 * @author andrew.cassin
 *
 */
public abstract class AbstractJob<T> implements Job<T> {
	private String m_id;
	
	@Override
	public final String getStatus() {
		return ServerListener.getStatus(getID());
	}
	
	protected void setID(String prefix, String new_id) {
		if (new_id == null || new_id.length() < 1) {
			new_id = "nectar" + Long.toHexString(System.currentTimeMillis());
		}
		m_id = new_id;
	}
	
	@Override
	public void setID(String new_id) {
		setID("nectar", new_id);
	}

	@Override
	public String getID() {
		return m_id;
	}
	
	@Override
	public boolean hasID(String jobID) {
		return getID().equals(jobID);
	}

	public boolean isValidFasta(String fasta) throws SOAPException {
		if (fasta == null || fasta.length() < 1) {
			throw new SOAPException("Invalid sequence data");
		}
		if (fasta.length() > 2 * 1024 * 1024) {
			throw new SOAPException("You must submit less than 2MB of sequence data - job failed!");
		}
		
		try {
			int cur_idx = 0;
			int next_idx= fasta.indexOf('\n');
			boolean expect_hdr = true;
			boolean expect_seq = true;
			do {
				if (next_idx < 0)
					throw new SOAPException("Invalid FASTA data");
				String line = fasta.substring(cur_idx, next_idx);
				boolean done = false;
				if (expect_hdr) {
					if (!line.startsWith(">"))
						throw new SOAPException("Header line expected");
					else {
						expect_seq = true;
						expect_hdr = false;
					}
					if (line.length() > 10 * 1024) {
						throw new SOAPException("Header line too long");
					}
					done = true;
				}
				
				if (expect_seq && !done) {
					for (int i=0; i<line.length(); i++) {
						if (Character.isWhitespace(line.charAt(i))) {
							continue;
						}
						int cp = line.codePointAt(i);
						if ((cp >= 65 && cp <= 90) || 		// uppercase
								(cp >= 97 && cp <= 122)) {  // lowercase
							continue;
						}
						throw new SOAPException("invalid sequence residue: "+line.charAt(i));
					}
					
					if (done) {
						expect_hdr = true;
					}
				}
				
				if (!done) {
					throw new SOAPException("Invalid FASTA data");
				}
				cur_idx = next_idx + 1;
				next_idx= fasta.indexOf('\n', cur_idx);
			} while (next_idx >= 0);
		} catch (SOAPException e) {
			return false;
		}
		
		return true;
	}

	public String getStatus(String jobID) {
		return ServerListener.getStatus(jobID);
	}
	
	public String getResult(String jobID) {
		return ServerListener.getResult(jobID);
	}
	
	@SuppressWarnings("unchecked")
	public void setRunning(boolean is_running) {
		ServerListener.setRunning((AbstractJob<String>)this, is_running);
	}
}
