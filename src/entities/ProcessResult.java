package entities;

import java.util.ArrayList;
import java.util.List;

public class ProcessResult {
	private boolean resolved = false;
	private List<HashEntry> encryptedHashes;
	private List<HashEntry> pendingEncryptedHashes;
	private List<String> decryptedHashes;
	private int testCount = 0;
	private int testTime = 5; //in seconds
	private int hashesCount = 0;
	private String algorithmCodes = "0";
	private String chainedAlgorithmCase;
	private List<JBruteThread> threads = null;

	public ProcessResult(){
		this.resolved = false;
		this.encryptedHashes = new ArrayList<HashEntry>();
		this.pendingEncryptedHashes = new ArrayList<HashEntry>();
		this.decryptedHashes = new ArrayList<String>();
	}

	public List<HashEntry> getEncryptedHashes() {
		return encryptedHashes;
	}
	
	public List<HashEntry> getPendingEncryptedHashes(){
		return pendingEncryptedHashes;
	}
	
	public boolean isResolved() {
		return resolved;
	}

	public void setResolved(boolean resolved) {
		this.resolved = resolved;
	}

	public List<String> getDecryptedHashes() {
		return decryptedHashes;
	}
	
	public List<JBruteThread> getThreads() {
		return threads;
	}

	public void setThreads(List<JBruteThread> threads) {
		this.threads = threads;
	}
	
	private void removeFromPendingEncryptedHashes(String s) {
		int index = -1;
		OUTERLOOP:
		for(HashEntry elem: this.pendingEncryptedHashes){
			index = index + 1;
			if (elem.getHash().equalsIgnoreCase(s)){
				break OUTERLOOP;
			}
		}
		if (index >= 0){
			this.pendingEncryptedHashes.remove(index);
		}
	}

	public void addEncryptedHash(HashEntry hash) {
		if (!hashIsAdded(hash)){
			if (!IOProcessor.isHashInTheBox(hash)){
				hashesCount = hashesCount + 1;
				this.encryptedHashes.add(hash);
				this.pendingEncryptedHashes.add(hash);
			}
			else {
				System.out.println("Hash entry '"+hash.toString()+"' is in the box: "+IOProcessor.getWordFromHashEntry(hash));
			}
		}
		else {
			System.out.println("Duplicated hash entry '"+hash.toString()+"', ignoring it...");
		}
			
	}

	private boolean hashIsAdded(HashEntry newHash) {
		boolean isAdded = false;
		OUTERMOST:
		for (HashEntry addedHash: encryptedHashes){
			if (addedHash.toString().equals(newHash.toString())){
				isAdded = true;
				break OUTERMOST;
			}
		}
		return isAdded;
	}

	public synchronized void addDecryption(String hash, String word){
		decryptedHashes.add(hash + " --> " + word);
		HashEntry he = getHashEntryFromHash(hash);
		removeFromPendingEncryptedHashes(hash);
		if (pendingEncryptedHashes.size() == 0){
			this.resolved = true;
		}
		// notify the changes to threads
		for (JBruteThread myThread: threads){
			myThread.setSomethingDifferent(true);
		}
		System.out.println("Founded: "+hash+":"+word);
		// persist decryption on pandora's box file
		IOProcessor.addHashEntryToTheBox(he, word);
	}

	private HashEntry getHashEntryFromHash(String hash) {
		HashEntry returnValue = null;
		OUTERMOST:
		for (HashEntry he: this.encryptedHashes){
			if (hash.equalsIgnoreCase(he.getHash())){
				returnValue = new HashEntry();
				returnValue.setHash(he.getHash());
				returnValue.setSaltType(he.getSaltType());
				if (he.isPreSalt()){
					returnValue.setPreSalt(he.getSalt());
				}
				else {
					returnValue.setPreSalt(he.getSalt());
				}
				break OUTERMOST;
			}
		}
		if (returnValue == null){
			System.err.println("[DEBUG] error! el hash no se encuentra en encryptedHashes!!!");
			returnValue = new HashEntry(hash);
		}
		return returnValue;
	}

	public int getTryesCount() {
		return testCount;
	}

	public void setTestCount(int countTryes) {
		this.testCount = countTryes;
	}
	
	public void incrementTestCount(int count){
		testCount = testCount + count;
	}

	public String getAlgorithmCodes() {
		return algorithmCodes;
	}

	public void setAlgorithmCodes(String algorithmCodes) {
		this.algorithmCodes = algorithmCodes;
	}
	
	public String getChainedAlgorithmCase() {
		return chainedAlgorithmCase;
	}

	public void setChainedAlgorithmCase(String chainedAlgorithmCase) {
		StringBuilder aux = new StringBuilder(chainedAlgorithmCase.toUpperCase());
		this.chainedAlgorithmCase = aux.reverse().toString();
	}
	
	public boolean isChainedHash(){
		return (algorithmCodes.length() > 1);
	}

	public void clearEncryptedHashes() {
		this.encryptedHashes.clear();		
	}
	
	public void validateEncryptedHashes(){
		List<HashEntry> validatedHashes = new ArrayList<HashEntry>();
		// here does not matters if it is a chained algortithm
		// I only validate the form of the hash
		char algChar = algorithmCodes.charAt(0);
		String lastCode = "" + algChar;
		int algorithmCode = AvailableAlgorithms.getAlgorithmCodeFromInput(lastCode);
		String algorithmName = AvailableAlgorithms.getAlgorithmName(algorithmCode);
		for (HashEntry he: encryptedHashes){
			if (AvailableAlgorithms.isValidHash(he.getHash(), algorithmCode)){
				validatedHashes.add(he);
			}
			else {
				System.out.println("Not a valid "+algorithmName+" hash: "+he.getHash()+" (i will ignore it).");
			}
		}
		this.encryptedHashes = validatedHashes;
		this.pendingEncryptedHashes = validatedHashes;
	}

	public int getTestTime() {
		return testTime;
	}

	public void setTestTime(int testTime) {
		this.testTime = testTime;
	}
	
	public String getTestResult(){
		String output = new String(Integer.toString(this.testCount / this.testTime));
		if (output.length() >= 7){
			output = output.substring(0,output.length() - 3)+"K";
		}
		
		return output;
	}
}
