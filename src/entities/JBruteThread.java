package entities;

public class JBruteThread extends Thread {
	private boolean somethingDifferent = false;

	public JBruteThread() {
		super();
		this.somethingDifferent = false;
	}

	public boolean isSomethingDifferent() {
		return somethingDifferent;
	}

	public void setSomethingDifferent(boolean somethingDifferent) {
		this.somethingDifferent = somethingDifferent;
	}
	
	
}
