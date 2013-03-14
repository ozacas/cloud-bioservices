package au.edu.unimelb.plantcell.nectar;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FailableFuture<T> implements Future<T> {
	private Future<T> m_future;
	private Throwable m_failure;
	private boolean   m_running;	// true if the job is currently running, false otherwise
	
	public FailableFuture(Future<T> f) {
		assert(f != null);
		m_future = f;
		setError(null);
		setRunning(false);
	}
	
	public boolean isRunning() {
		return m_running;
	}
	
	public void setRunning(boolean is_running) {
		m_running = is_running;
	}
	
	public void setError(Throwable th) {
		m_failure = th;
	}
	
	public boolean isError() {
		return (m_failure != null);
	}
	
	public Throwable getError() {
		return m_failure;
	}
	
	@Override
	public boolean cancel(boolean arg0) {
		return m_future.cancel(arg0);
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		if (m_failure != null || m_future == null) {
			return null;
		}
		return m_future.get();
	}

	@Override
	public T get(long arg0, TimeUnit arg1) throws InterruptedException,
			ExecutionException, TimeoutException {
		if (m_failure != null || m_future == null)
			return null;
		return m_future.get(arg0, arg1);
	}

	@Override
	public boolean isCancelled() {
		return m_future.isCancelled();
	}

	@Override
	public boolean isDone() {
		return m_future.isDone();
	}

}
