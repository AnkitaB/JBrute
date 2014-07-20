package entities;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import specialAlgorithm.MyMessageDigest;


public class MyBruteDecryptThread extends JBruteThread {
    private String mensaje;
    private int longDesde;
    private int longHasta;
    private String firstCombination;
    private String lastCombination;
    private String alphabet;
    private ProcessResult resultado;
    private boolean testMode = false;
	private int numberOfHashes = 1;
	private boolean withSalt = false;

    public MyBruteDecryptThread(String msg, String alphabet, int desde, int hasta, ProcessResult pr)
    {
        super();
        this.mensaje = msg;
        this.longDesde = desde;
        this.longHasta = hasta;
        this.alphabet = alphabet;
        this.resultado = pr;
    }
    
    public MyBruteDecryptThread(String msg, String alphabet, ProcessResult pr)
    {
        super();
        this.mensaje = msg;
        this.resultado = pr;
        this.alphabet = alphabet;
    }

    public MyBruteDecryptThread(String msg, String alphabet, String firstCombination2,
			String lastCombination2, ProcessResult pr) {
    	super();
        this.mensaje = msg;
        this.firstCombination = firstCombination2;
        this.lastCombination = lastCombination2;
        this.resultado = pr;
        this.alphabet = alphabet;
	}
    
    public MyBruteDecryptThread(boolean testMode, ProcessResult pr, int numberOfHashes, boolean withSalt){
    	this.testMode = testMode;
    	this.resultado = pr;
    	this.numberOfHashes = numberOfHashes;
    	this.withSalt = withSalt;
    }
    
    public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String msj)
    {
        this.mensaje = msj;
    }
    
    public int getLongDesde() {
		return longDesde;
	}

	public void setLongDesde(int longDesde) {
		this.longDesde = longDesde;
	}

	public int getLongHasta() {
		return longHasta;
	}

	public void setLongHasta(int longHasta) {
		this.longHasta = longHasta;
	}

	public String getFirstCombination() {
		return firstCombination;
	}

	public void setFirstCombination(String firstCombination) {
		this.firstCombination = firstCombination;
	}

	public String getLastCombination() {
		return lastCombination;
	}

	public void setLastCombination(String lastCombination) {
		this.lastCombination = lastCombination;
	}

	public void run()
    {
		
		if (testMode) {
			if (resultado.isChainedHash()){
				testBruteForceChained();
			}
			else {
				int algorithm = AvailableAlgorithms.getAlgorithmCodeFromInput(resultado.getAlgorithmCodes());
				if (AvailableAlgorithms.isHashedSpecialAlgorithm(algorithm)){
					testBruteForceSpecial();
				}
				else {
					testBruteForce();
				}
			}
		}
		else {
			// first I need to validate all hashes to decrypt
			this.resultado.validateEncryptedHashes();
			if (this.resultado.getPendingEncryptedHashes().size() == 0){
				System.err.println();
				System.err.println("There are no valid hashes to decrypt!!!");
				System.err.println();
				return;
			}
			if (resultado.isChainedHash()){
				// special algorithms not supported here...
				decryptBruteForceChained();
			}
			else {
				int algorithm = AvailableAlgorithms.getAlgorithmCodeFromInput(resultado.getAlgorithmCodes());
				if (AvailableAlgorithms.isHashedSpecialAlgorithm(algorithm)){
					decryptBruteForceSpecialAlgorithm();
				}
				else {
					decryptBruteForce();
				}
			}
		}
	}
	
	private void testBruteForce() {
		firstCombination = "aaaaaaaa";
		lastCombination = "99999999";
		boolean useSalt = this.withSalt;
		byte [] lastCombinationByte = lastCombination.getBytes();
		byte[] result = firstCombination.getBytes();
		alphabet = AvailableCharsets.getCharsetByName("loweralpha-numeric");
		int algorithmCode = AvailableAlgorithms.getAlgorithmCodeFromInput(resultado.getAlgorithmCodes());
		MessageDigest m = null;
		m = AvailableAlgorithms.getMessageDigest(algorithmCode);
		// i will never found this password...
		String auxHashToDecrypt = new String("alagrandelepusecuca"); 
		byte[] hashToDecrypt = auxHashToDecrypt.getBytes();
		m.update(hashToDecrypt,0,hashToDecrypt.length);
		hashToDecrypt = m.digest();
		String hashPrintable = AvailableAlgorithms.getLowerCaseHash(hashToDecrypt);
		// starting test
		List<byte[]> byteList = new ArrayList<byte[]>();
		List<byte[]> saltList = new ArrayList<byte[]>();
		boolean[] preSaltList = new boolean[this.numberOfHashes];
		byte[] aux = null;
		byte[] resultSalt = null;
		for (int i = 0; i < this.numberOfHashes; i++){
			HashEntry he = new HashEntry();
			he.setHash(hashPrintable);
			if (this.withSalt){
				he.setPreSalt(AuxiliaryForDecryptThreads.getRandomString(8));
			}
			else {
				he.setPreSalt(null);
			}
			byteList.add(DatatypeConverter.parseHexBinary(he.getHash()));
			saltList.add(AvailableAlgorithms.getSaltByteArray(he.getSalt(), he.getSaltType()));
			preSaltList[i] = true;
		}	
		long start = System.currentTimeMillis();
		int count = 0;
		int auxIndex = 0;
		int myEndTime = this.resultado.getTestTime() * 1000;
		int cantEntrys = byteList.size();
		
		OUTERMOST:
		while(!Arrays.equals(result, lastCombinationByte)){
			if (isSomethingDifferent()){
				// recalculate hashes
				useSalt = false;
				byteList.clear();
				saltList.clear();
				preSaltList = new boolean[resultado.getPendingEncryptedHashes().size()];
				auxIndex = 0;
				for (HashEntry entry: resultado.getPendingEncryptedHashes()){
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
				if (resultado.isResolved()){
					break OUTERMOST;
				}
			}
			if (useSalt){
				for (int i = 0; i < cantEntrys; i++){
					byte[] elem = byteList.get(i);
					resultSalt = AuxiliaryForDecryptThreads.processSalt(result,saltList.get(i),preSaltList[i]);
					m.update(resultSalt);
					aux = m.digest();
					if (Arrays.equals(elem,aux)){
						resultado.addDecryption(AvailableAlgorithms.getLowerCaseHash(elem),byteArray2String(result));
					}
				}
			}
			else {
				m.update(result);
				aux = m.digest();
				for (int i = 0; i < cantEntrys; i++){
					byte[] elem = byteList.get(i);
					if (Arrays.equals(elem,aux)){
						resultado.addDecryption(AvailableAlgorithms.getLowerCaseHash(elem),byteArray2String(result));
					}
				}
			}	
			result = nextCombinationCustomBytes(result);
			count = count + 1;
			if ((System.currentTimeMillis() - start) >= myEndTime){
				break OUTERMOST;
			}
		}
		resultado.incrementTestCount(count);
	}
	
	@SuppressWarnings("unused")
	private void testBruteForceSpecial() {
		firstCombination = "aaaaaaaa";
		lastCombination = "99999999";
		byte [] lastCombinationByte = lastCombination.getBytes();
		byte[] result = firstCombination.getBytes();
		alphabet = AvailableCharsets.getCharsetByName("loweralpha-numeric");
		String saltStr = null;
		byte[] salt = null;
		// i will never found this password...
		int algorithmCode = AvailableAlgorithms.getAlgorithmCodeFromInput(resultado.getAlgorithmCodes());
		MyMessageDigest m = null;
		m = AvailableAlgorithms.getMyMessageDigest(algorithmCode);
		String auxHashToDecrypt = new String("alagrandelepusecuca"); 
		if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("ORACLE-10G")
				|| (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("POSTGRESQL"))) {
			saltStr = "SYSTEM/";
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("ORACLE-11G")) {
			saltStr = "AAAAAAAAAA";
			auxHashToDecrypt = auxHashToDecrypt + saltStr;
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("MD5CRYPT")) {
			saltStr = "AAAAAAAA";
			auxHashToDecrypt = auxHashToDecrypt + saltStr;
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("INFORMIX-1170")) {
			saltStr = "3561468224442120539";
			auxHashToDecrypt = auxHashToDecrypt + saltStr;
		}
		else {
			salt = null;
		}
		String aux2 = null;
		if ((salt == null) 
				||algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("POSTGRESQL")
				||algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("MD5CRYPT"))
		{
			aux2 = m.crypt(auxHashToDecrypt, saltStr);
		}
		else {
			if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("ORACLE-11G")){
				saltStr = null;
			}
		}	
		aux2 = m.crypt(auxHashToDecrypt, saltStr);
		byte[] hashToDecrypt = m.hashToByteArray(aux2);
		byte[] aux = null;
		// starting test
		List<byte[]> byteList = new ArrayList<byte[]>();
		List<byte[]> saltList = new ArrayList<byte[]>();
		for (int i = 0; i < this.numberOfHashes; i++){
			byteList.add(hashToDecrypt);
			saltList.add(m.getSaltFromHash(hashToDecrypt));
		}	
		long start = System.currentTimeMillis();
		int count = 0;
		int myEndTime = this.resultado.getTestTime() * 1000;
		int cantEntrys = byteList.size();
		
		OUTERMOST:
		while(!Arrays.equals(result, lastCombinationByte)){
			if (isSomethingDifferent()){
				// recalculate hashes
				byteList.clear();
				saltList.clear();
				for (HashEntry entry: resultado.getPendingEncryptedHashes()){
					byte[] auxHash = m.hashToByteArray(entry.getHash()); 
					byteList.add(auxHash);
					saltList.add(m.getSaltFromHash(auxHash));
				}
				cantEntrys = byteList.size();
				this.setSomethingDifferent(false);
				// get out of here if it is all resolved
				if (resultado.isResolved()){
					break OUTERMOST;
				}
			}
			for (int i = 0; i < cantEntrys; i++){
				m.update(result);
				aux = m.digest(saltList.get(i));
				if (Arrays.equals(byteList.get(i),aux)){
					resultado.addDecryption(m.toHash(byteList.get(i)),byteArray2String(result));
					//break;
				}
			}
			result = nextCombinationCustomBytes(result);
			count = count + 1;
			if ((System.currentTimeMillis() - start) >= myEndTime){
				break OUTERMOST;
			}
		}
		resultado.incrementTestCount(count);
	}
	
	private void testBruteForceChained() {
		// special algorithms not supported here
		firstCombination = "aaaaaaaa";
		lastCombination = "99999999";
		boolean useSalt = this.withSalt;
		byte [] lastCombinationByte = lastCombination.getBytes();
		alphabet = AvailableCharsets.getCharsetByName("loweralpha-numeric");
		MessageDigest[] ma = AvailableAlgorithms.getMessageDigestArray(resultado.getAlgorithmCodes());
		// i will never find this password...
		byte[] aux = new String("alagrandelepusecuca").getBytes();
		for (int i = 0; i < ma.length; i++){
			ma[i].update(aux,0,aux.length);
			aux = ma[i].digest();
			if (ma.length -1 != i)
				aux = AuxiliaryForDecryptThreads.getIntermediateChainedHash(aux, resultado.getChainedAlgorithmCase(), i);
		}
		// starting test
		byte[] result = firstCombination.getBytes();
		List<byte[]> byteList = new ArrayList<byte[]>();
		List<byte[]> saltList = new ArrayList<byte[]>();
		boolean[] preSaltList = new boolean[this.numberOfHashes];
		int auxIndex = 0;
		for (int i = 0; i < this.numberOfHashes; i++){
			byteList.add(aux);
			if (this.withSalt){
				saltList.add(AuxiliaryForDecryptThreads.getRandom(8));
			}
			else {
				saltList.add(null);
			}
			preSaltList[i] = true;
		}	
		aux = null;
		byte[] resultSalt = null;
		
		long start = System.currentTimeMillis();
		int count = 0;
		int myEndTime = this.resultado.getTestTime() * 1000;
		int cantEntrys = byteList.size();
		
		OUTERMOST:
		while(!Arrays.equals(result, lastCombinationByte)){
			if (isSomethingDifferent()){
				// recalculate hashes
				useSalt = false;
				byteList.clear();
				saltList.clear();
				preSaltList = new boolean[resultado.getPendingEncryptedHashes().size()];
				auxIndex = 0;
				for (HashEntry entry: resultado.getPendingEncryptedHashes()){
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
				if (resultado.isResolved()){
					break OUTERMOST;
				}
			}
			if (useSalt){
				for (int i = 0; i < cantEntrys; i++){
					byte[] elem = byteList.get(0);
					resultSalt = AuxiliaryForDecryptThreads.processSalt(result,saltList.get(i),preSaltList[i]);
					for (int j = 0; j < ma.length; j++){
						ma[j].update(resultSalt);
						aux = ma[j].digest();
						if (ma.length -1 != j)
							resultSalt = AuxiliaryForDecryptThreads.getIntermediateChainedHash(aux, resultado.getChainedAlgorithmCase(), j);
					}
					resultSalt = aux;
					if (Arrays.equals(elem,resultSalt)){
						resultado.addDecryption(AvailableAlgorithms.getLowerCaseHash(elem),byteArray2String(result));
					}
				}
			}
			else {
				for (int j = 0; j < ma.length; j++){
					ma[j].update(result);
					aux = ma[j].digest();
					if (ma.length -1 != j)
						resultSalt = AuxiliaryForDecryptThreads.getIntermediateChainedHash(aux, resultado.getChainedAlgorithmCase(), j);
				}
				result = aux;
				for (int i = 0; i < cantEntrys; i++){
					byte[] elem = byteList.get(i);
					if (Arrays.equals(elem,result)){
						resultado.addDecryption(AvailableAlgorithms.getLowerCaseHash(elem),byteArray2String(result));
					}
				}
			}
			result = nextCombinationCustomBytes(result);
			count = count + 1;
			if ((System.currentTimeMillis() - start) >= myEndTime){
				break OUTERMOST;
			}
		}
		resultado.incrementTestCount(count);
	}

	public byte[] nextCombinationCustomBytes(byte[] input) {
		int inputLength = input.length;
		int alphabetLength = alphabet.length();
		byte[] returnValue = null;
		char c = (char) input[inputLength - 1];
		char nextChar = 0;

		if(c == alphabet.charAt(alphabetLength - 1)) {
			char firstAlphabetCharacter = alphabet.charAt(0);
			if (inputLength == 1) {
				returnValue = new byte[2];
				returnValue[0] = (byte) firstAlphabetCharacter;
				returnValue[1] = (byte) firstAlphabetCharacter;
			}	
			else {
				byte[] aux1 = new byte[inputLength-1];
				for (int i = 0; i < inputLength-1; i++){
					aux1[i] = input[i];
				}
				byte[] aux2 = nextCombinationCustomBytes(aux1);
				returnValue = new byte[aux2.length+1];
				for (int i = 0; i < aux2.length; i++){
					returnValue[i] = aux2[i];
				}
				returnValue[aux2.length] = (byte) firstAlphabetCharacter;
			}	
		}		
		else {
			for (int i = 0; i < alphabetLength; i++){
				if (alphabet.charAt(i) == c){
					nextChar = alphabet.charAt(i+1);
					break;
				}
			}
			returnValue = new byte[inputLength];
			for (int i = 0; i < inputLength -1; i++){
				returnValue[i] = input[i];
			}
			returnValue[inputLength -1] = (byte) nextChar;
		}	
		return returnValue;
	}
	
	private void decryptBruteForce(){
		
		byte[] result = firstCombination.getBytes();
		List<byte[]> byteList = new ArrayList<byte[]>();
		List<byte[]> saltList = new ArrayList<byte[]>();
		boolean[] preSaltList = new boolean[resultado.getPendingEncryptedHashes().size()];
		int auxIndex = 0;
		boolean useSalt = false;
		for (HashEntry entry: resultado.getPendingEncryptedHashes()){
			byteList.add(DatatypeConverter.parseHexBinary(entry.getHash()));
			byte[] auxSalt = AvailableAlgorithms.getSaltByteArray(entry.getSalt(), entry.getSaltType());
			if (auxSalt != null){
				useSalt = true;
			}
			saltList.add(auxSalt);
			preSaltList[auxIndex] = entry.isPreSalt();
			auxIndex = auxIndex + 1;
		}
		byte [] lastCombinationByte = lastCombination.getBytes();
		int algorithmCode = AvailableAlgorithms.getAlgorithmCodeFromInput(resultado.getAlgorithmCodes());
		MessageDigest m = AvailableAlgorithms.getMessageDigest(algorithmCode);
		byte[] resultSalt = null;
		byte[] aux = null;
		
		int cantEntrys = byteList.size();
		// verify the last combination, it will not be processed inside the while
		
		for (int i = 0; i < cantEntrys; i++){
			byte[] elem = byteList.get(i);
			resultSalt = AuxiliaryForDecryptThreads.processSalt(result,saltList.get(i),preSaltList[i]);
			m.update(resultSalt);
			aux = m.digest();
			if (Arrays.equals(elem,aux)){
				resultado.addDecryption(AvailableAlgorithms.getLowerCaseHash(elem),lastCombination);	
			}
		}
		
		OUTERMOST:
		while(!Arrays.equals(result, lastCombinationByte)){
			//System.out.println("    combination: "+byteArray2String(resultSalt));
			if (isSomethingDifferent()){
				// recalculate hashes
				useSalt = false;
				byteList.clear();
				saltList.clear();
				preSaltList = new boolean[resultado.getPendingEncryptedHashes().size()];
				auxIndex = 0;
				for (HashEntry entry: resultado.getPendingEncryptedHashes()){
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
				if (resultado.isResolved()){
					break OUTERMOST;
				}
			}
			if (useSalt){
				for (int i = 0; i < cantEntrys; i++){
					byte[] elem = byteList.get(i);
					resultSalt = AuxiliaryForDecryptThreads.processSalt(result,saltList.get(i),preSaltList[i]);
					m.update(resultSalt);
					aux = m.digest();
					if (Arrays.equals(elem,aux)){
						resultado.addDecryption(AvailableAlgorithms.getLowerCaseHash(elem),byteArray2String(result));
					}
				}
			}
			else {
				m.update(result);
				aux = m.digest();
				for (int i = 0; i < cantEntrys; i++){
					byte[] elem = byteList.get(i);
					if (Arrays.equals(elem,aux)){
						resultado.addDecryption(AvailableAlgorithms.getLowerCaseHash(elem),byteArray2String(result));
					}
				}
			}
			result = nextCombinationCustomBytes(result);
		}
	}

	private void decryptBruteForceSpecialAlgorithm(){
		// I assume that there is no salt because it is a salted algorithm
		byte[] result = firstCombination.getBytes();
		int algorithmCode = AvailableAlgorithms.getAlgorithmCodeFromInput(resultado.getAlgorithmCodes());
		MyMessageDigest m = AvailableAlgorithms.getMyMessageDigest(algorithmCode);
		List<byte[]> byteList = new ArrayList<byte[]>();
		List<byte[]> saltList = new ArrayList<byte[]>();
		for (HashEntry entry: resultado.getEncryptedHashes()){
			byte[] auxHash = m.hashToByteArray(entry.getHash()); 
			byteList.add(auxHash);
			saltList.add(m.getSaltFromHash(auxHash));
		}
		
		byte[] lastCombinationByte = lastCombination.getBytes();
		byte[] aux = null;
		int cantEntrys = byteList.size();
		// salvo el ultimo caso que no entra en el while
		for (int i = 0; i < cantEntrys; i++){
			m.update(lastCombinationByte);
			aux = m.digest(saltList.get(i));
			if (Arrays.equals(byteList.get(i),aux)){
				resultado.addDecryption(m.toHash(byteList.get(i)),lastCombination);	
			}
		}

		OUTERMOST:
		while(!Arrays.equals(result, lastCombinationByte)){
			if (isSomethingDifferent()){
				// recalculate hashes
				byteList.clear();
				saltList.clear();
				for (HashEntry entry: resultado.getPendingEncryptedHashes()){
					byte[] auxHash = m.hashToByteArray(entry.getHash()); 
					byteList.add(auxHash);
					saltList.add(m.getSaltFromHash(auxHash));
				}
				cantEntrys = byteList.size();
				this.setSomethingDifferent(false);
				// get out of here if it is all resolved
				if (resultado.isResolved()){
					break OUTERMOST;
				}
			}
			for (int i = 0; i < cantEntrys; i++){
				m.update(result);
				aux = m.digest(saltList.get(i));
				if (Arrays.equals(byteList.get(i),aux)){
					resultado.addDecryption(m.toHash(byteList.get(i)),byteArray2String(result));
				}
			}
			result = nextCombinationCustomBytes(result);
		}
	}

	private void decryptBruteForceChained() {
		List<byte[]> byteList = new ArrayList<byte[]>();
		List<byte[]> saltList = new ArrayList<byte[]>();
		boolean[] preSaltList = new boolean[resultado.getPendingEncryptedHashes().size()];
		int auxIndex = 0;
		boolean useSalt = false;
		for (HashEntry entry: resultado.getEncryptedHashes()){
			byteList.add(DatatypeConverter.parseHexBinary(entry.getHash()));
			byte[] auxSalt = AvailableAlgorithms.getSaltByteArray(entry.getSalt(), entry.getSaltType());
			if (auxSalt != null){
				useSalt = true;
			}
			saltList.add(auxSalt);
			preSaltList[auxIndex] = entry.isPreSalt();
			auxIndex = auxIndex + 1;
		}
		MessageDigest[] ma = AvailableAlgorithms.getMessageDigestArray(resultado.getAlgorithmCodes());
		byte [] lastCombinationByte = lastCombination.getBytes();
		int cantEntrys = byteList.size();
		byte[] resultSalt = lastCombinationByte;
		byte[] aux = null;
		// salvo el ultimo caso que no entra en el while
		for (int i = 0; i < cantEntrys; i++){
			byte[] elem = byteList.get(0);
			if (useSalt){
				resultSalt = AuxiliaryForDecryptThreads.processSalt(resultSalt,saltList.get(i),preSaltList[i]);
			}
			for (int j = 0; j < ma.length; j++){
				ma[j].update(resultSalt);
				aux = ma[j].digest();
				if (ma.length -1 != j)
					resultSalt = AuxiliaryForDecryptThreads.getIntermediateChainedHash(aux, resultado.getChainedAlgorithmCase(), j);
			}
			
			resultSalt = aux;
			if (Arrays.equals(elem,resultSalt)){
				resultado.addDecryption(AvailableAlgorithms.getLowerCaseHash(elem),lastCombination);
			}
		}
		byte[] result = firstCombination.getBytes();
		
		OUTERMOST:
		while(!Arrays.equals(result, lastCombinationByte)){
			//System.out.println("Combinacion: "+byteArray2String(result));
			if (isSomethingDifferent()){
				// recalculate hashes
				useSalt = false;
				byteList.clear();
				saltList.clear();
				preSaltList = new boolean[resultado.getPendingEncryptedHashes().size()];
				auxIndex = 0;
				for (HashEntry entry: resultado.getPendingEncryptedHashes()){
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
				if (resultado.isResolved()){
					break OUTERMOST;
				}
			}
			if (useSalt){
				for (int i = 0; i < cantEntrys; i++){
					byte[] elem = byteList.get(0);
					resultSalt = AuxiliaryForDecryptThreads.processSalt(result,saltList.get(i),preSaltList[i]);
					for (int j = 0; j < ma.length; j++){
						ma[j].update(resultSalt);
						aux = ma[j].digest();
						if (ma.length -1 != j)
							resultSalt = AuxiliaryForDecryptThreads.getIntermediateChainedHash(aux, resultado.getChainedAlgorithmCase(), j);
					}
					resultSalt = aux;
					if (Arrays.equals(elem,resultSalt)){
						resultado.addDecryption(AvailableAlgorithms.getLowerCaseHash(elem),byteArray2String(result));
					}
				}
			}
			else {
				resultSalt = result;
				for (int j = 0; j < ma.length; j++){
					ma[j].update(resultSalt);
					aux = ma[j].digest();
					if (ma.length -1 != j)
						resultSalt = AuxiliaryForDecryptThreads.getIntermediateChainedHash(aux, resultado.getChainedAlgorithmCase(), j);
				}
				resultSalt = aux;
				for (int i = 0; i < cantEntrys; i++){
					byte[] elem = byteList.get(i);
					if (Arrays.equals(elem,resultSalt)){
						resultado.addDecryption(AvailableAlgorithms.getLowerCaseHash(elem),byteArray2String(result));
					}
				}
			}
			//System.out.println("Combinacion antes   : "+byteArray2String(result));
			result = nextCombinationCustomBytes(result);
			//System.out.println("Combinacion despues : "+byteArray2String(result));
		}
		
	}
	
	public String byteArray2String(byte[] b){
		StringBuilder s = new StringBuilder("");
		char c = 0;
		for (int i = 0; i < b.length; i++){
			c = (char) b[i];
			for (int j = 0; j < alphabet.length(); j++){
				if (c == alphabet.charAt(j)){
					s.append(alphabet.charAt(j));
					break;
				}
			}
		}
		return s.toString();
	}

}
