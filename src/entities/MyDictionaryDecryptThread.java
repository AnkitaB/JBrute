package entities;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import specialAlgorithm.MyMessageDigest;

public class MyDictionaryDecryptThread extends JBruteThread {
	private String message;
    private ProcessResult opResult;
    private List<StringBuilder> words;
    private int algorithmCode = 0;
    private List<DictionaryRule> rules;
    private boolean stdoutMode = false;
    
    public MyDictionaryDecryptThread(String msg, ProcessResult pr, List<StringBuilder> words, List<DictionaryRule> rules, boolean stdoutMode){
        super();
        this.message = msg;
        this.opResult = pr;
        if (opResult.getAlgorithmCodes().equalsIgnoreCase("0")){
        	this.algorithmCode = 0;
        }
        else {
        	this.algorithmCode = AvailableAlgorithms.getAlgorithmCodeFromInput(opResult.getAlgorithmCodes());
        }
        this.words = words;
        this.rules = rules;
        this.stdoutMode = stdoutMode;
    }

	public List<DictionaryRule> getRules() {
		return rules;
	}

	public void setRules(List<DictionaryRule> rules) {
		this.rules = rules;
	}

	public List<StringBuilder> getWords() {
		return words;
	}

	public void setWords(List<StringBuilder> words) {
		this.words = words;
	}

	public String getMensaje() {
		return message;
	}

	public void setMensaje(String mensaje) {
		this.message = mensaje;
	}

	public ProcessResult getResultado() {
		return opResult;
	}

	public void setResultado(ProcessResult resultado) {
		this.opResult = resultado;
	}

	public int getAlgorithmCode() {
		return algorithmCode;
	}

	public void setAlgorithmCode(int algorithmCode) {
		this.algorithmCode = algorithmCode;
	}
	
	public void run(){
		if (this.stdoutMode){
			stdoutRuleProcessing();
		}
		else {
			// first I need to validate all hashes to decrypt
			this.opResult.validateEncryptedHashes();
			if (this.opResult.getPendingEncryptedHashes().size() == 0){
				System.err.println();
				System.err.println("There are no valid hashes to decrypt!!!");
				System.err.println();
				return;
			}
			
			if (opResult.isChainedHash()){
				decryptDictionaryChained();
			}
			else {
				if (AvailableAlgorithms.isHashedSpecialAlgorithm(this.algorithmCode)){
					decryptDictionarySpecialAlgorithm();
				}
				else {
					decryptDictionary();
				}
			}
		}	
	}
	
	private void stdoutRuleProcessing() {
		for (DictionaryRule rule: rules){
			System.out.println("Processing rule \""+rule.toString()+"\"");
			for (StringBuilder word: words){
				List<StringBuilder> list = rule.aplicateRule(word);
				for (StringBuilder s: list){
					System.out.println(s);
				}	
			}	
		}
		
	}

	private void decryptDictionary() {
		List<byte[]> byteList = new ArrayList<byte[]>();
		List<byte[]> saltList = new ArrayList<byte[]>();
		boolean[] preSaltList = new boolean[opResult.getPendingEncryptedHashes().size()];
		int auxIndex = 0;
		boolean useSalt = false;
		for (HashEntry entry: opResult.getEncryptedHashes()){
			byteList.add(DatatypeConverter.parseHexBinary(entry.getHash()));
			byte[] auxSalt = AvailableAlgorithms.getSaltByteArray(entry.getSalt(), entry.getSaltType());
			if (auxSalt != null){
				useSalt = true;
			}
			saltList.add(auxSalt);
			preSaltList[auxIndex] = entry.isPreSalt();
			auxIndex = auxIndex + 1;
		}

		MessageDigest m = AvailableAlgorithms.getMessageDigest(this.algorithmCode);
		byte[] result = null;
		byte[] resultSalt = null;
		int cantEntrys = byteList.size();

		OUTERMOST:		
		for (DictionaryRule rule: rules){
			if (this.message.equals("Thread 0")){
				System.out.println("Processing rule \""+rule.toString()+"\"");
			}	
			for (StringBuilder word: words){
				if (isSomethingDifferent()){
					// recalculate hashes
					useSalt = false;
					byteList.clear();
					saltList.clear();
					preSaltList = new boolean[opResult.getPendingEncryptedHashes().size()];
					auxIndex = 0;
					for (HashEntry entry: opResult.getPendingEncryptedHashes()){
						byteList.add(DatatypeConverter.parseHexBinary(entry.getHash()));
						byte[] auxSalt = AvailableAlgorithms.getSaltByteArray(entry.getSalt(), entry.getSaltType());
						if (auxSalt != null){
							useSalt = true;
						}
						saltList.add(auxSalt);
						preSaltList[auxIndex] = entry.isPreSalt();
						auxIndex = auxIndex + 1;
					}
					cantEntrys = byteList.size();
					this.setSomethingDifferent(false);
					// get out of here if it is all resolved
					if (opResult.isResolved()){
						break OUTERMOST;
					}
				}
				List<StringBuilder> list = rule.aplicateRule(word);
				if (useSalt){
					for (StringBuilder s: list){
						for (int i = 0; i < cantEntrys; i++){
							byte[] elem = byteList.get(i);
							resultSalt = AuxiliaryForDecryptThreads.processSalt(ByteArray.toByteArray(s),saltList.get(i),preSaltList[i]);
							m.update(resultSalt);
							result = m.digest();
							if (Arrays.equals(elem,result)){
								opResult.addDecryption(AvailableAlgorithms.getLowerCaseHash(elem),s.toString());	
							}
						}
					}
				}
				else {
					for (StringBuilder s: list){
						m.update(ByteArray.toByteArray(s));
						result = m.digest();
						for (int i = 0; i < cantEntrys; i++){
							byte[] elem = byteList.get(i);
							if (Arrays.equals(elem,result)){
								opResult.addDecryption(AvailableAlgorithms.getLowerCaseHash(elem),s.toString());
							}
						}
					}
				}
			}
		}
	}
	
	private void decryptDictionarySpecialAlgorithm() {
		List<byte[]> byteList = new ArrayList<byte[]>();
		List<byte[]> saltList = new ArrayList<byte[]>();
		MyMessageDigest m = AvailableAlgorithms.getMyMessageDigest(this.algorithmCode);
		for (HashEntry he: opResult.getPendingEncryptedHashes()){
			byte[] aux = m.hashToByteArray(he.getHash());
			byteList.add(aux);
			saltList.add(m.getSaltFromHash(aux));
		}
		int cantEntrys = byteList.size();
		byte[] result = null;

		OUTERMOST:
		for (DictionaryRule rule: rules){
			if (this.message.equals("Thread 0")){
				System.out.println("Processing rule \""+rule.toString()+"\"");
			}
			for (StringBuilder word: words){
				if (isSomethingDifferent()){
					// recalculate hashes
					byteList.clear();
					saltList.clear();
					for (HashEntry entry: opResult.getPendingEncryptedHashes()){
						byte[] auxHash = m.hashToByteArray(entry.getHash()); 
						byteList.add(auxHash);
						saltList.add(m.getSaltFromHash(auxHash));
					}
					cantEntrys = byteList.size();
					this.setSomethingDifferent(false);
					// get out of here if it is all resolved
					if (opResult.isResolved()){
						break OUTERMOST;
					}
				}
				List<StringBuilder> list = rule.aplicateRule(word);
				for (StringBuilder s: list){ 
					result = ByteArray.toByteArray(s);
					for (int i = 0; i < cantEntrys; i++){
						m.update(result);
						if (Arrays.equals(byteList.get(i),m.digest(saltList.get(i)))){
							opResult.addDecryption(m.toHash(byteList.get(i)),s.toString());
						}
					}
				}	
			}	
		}
	}
	
	private void decryptDictionaryChained() {
		List<byte[]> byteList = new ArrayList<byte[]>();
		List<byte[]> saltList = new ArrayList<byte[]>();
		boolean[] preSaltList = new boolean[opResult.getPendingEncryptedHashes().size()];
		int auxIndex = 0;
		boolean useSalt = false;
		for (HashEntry entry: opResult.getEncryptedHashes()){
			byteList.add(DatatypeConverter.parseHexBinary(entry.getHash()));
			byte[] auxSalt = AvailableAlgorithms.getSaltByteArray(entry.getSalt(), entry.getSaltType());
			if (auxSalt != null){
				useSalt = true;
			}
			saltList.add(auxSalt);
			preSaltList[auxIndex] = entry.isPreSalt();
			auxIndex = auxIndex + 1;
		}
		
		MessageDigest[] ma = AvailableAlgorithms.getMessageDigestArray(opResult.getAlgorithmCodes());
		byte[] resultSalt = null;
		byte[] result = null;
		byte[] aux = null;
		int cantEntrys = byteList.size();

		OUTERMOST:
		for (DictionaryRule rule: rules){
			if (this.message.equals("Thread 0")){
				System.out.println("Processing rule \""+rule.toString()+"\"");
			}
			for (StringBuilder word: words){
				if (isSomethingDifferent()){
					// recalculate hashes
					useSalt = false;
					byteList.clear();
					saltList.clear();
					preSaltList = new boolean[opResult.getPendingEncryptedHashes().size()];
					auxIndex = 0;
					for (HashEntry entry: opResult.getPendingEncryptedHashes()){
						byteList.add(DatatypeConverter.parseHexBinary(entry.getHash()));
						byte[] auxSalt = AvailableAlgorithms.getSaltByteArray(entry.getSalt(), entry.getSaltType());
						if (auxSalt != null){
							useSalt = true;
						}
						saltList.add(auxSalt);
						preSaltList[auxIndex] = entry.isPreSalt();
						auxIndex = auxIndex + 1;
					}
					cantEntrys = byteList.size();
					this.setSomethingDifferent(false);
					// get out of here if it is all resolved
					if (opResult.isResolved()){
						break OUTERMOST;
					}
				}
				List<StringBuilder> list = rule.aplicateRule(word);
				if (useSalt){
					for (StringBuilder s: list){
						for (int i = 0; i < cantEntrys; i++){
							byte[] elem = byteList.get(i);
							resultSalt = AuxiliaryForDecryptThreads.processSalt(ByteArray.toByteArray(s),saltList.get(i),preSaltList[i]);
							for (int j = 0; j < ma.length; j++){
								ma[j].update(resultSalt);
								aux = ma[j].digest();
								if (ma.length -1 != j)
									resultSalt = AuxiliaryForDecryptThreads.getIntermediateChainedHash(aux, opResult.getChainedAlgorithmCase(), j);
							}
							resultSalt = aux;
							if (Arrays.equals(elem,resultSalt)){
								opResult.addDecryption(AvailableAlgorithms.getLowerCaseHash(elem),s.toString());
							}
						}
					}
				}
				else {
					for (StringBuilder s: list){
						result = ByteArray.toByteArray(s);
						for (int j = 0; j < ma.length; j++){
							ma[j].update(result);
							aux = ma[j].digest();
							if (ma.length -1 != j)
								result = AuxiliaryForDecryptThreads.getIntermediateChainedHash(aux, opResult.getChainedAlgorithmCase(), j);
						}
						result = aux;
						for (int i = 0; i < cantEntrys; i++){
							byte[] elem = byteList.get(i);
							if (Arrays.equals(elem,result)){
								opResult.addDecryption(AvailableAlgorithms.getLowerCaseHash(elem),s.toString());
							}
						}
					}
				}	
			}	
		}
	}

	
}
