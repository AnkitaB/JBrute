package entities;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import specialAlgorithm.MD5Crypt;
import specialAlgorithm.MyMessageDigest;


public final class AvailableAlgorithms {
	private static String[] ALGORITHMS = {"unknow","MD5","MD4","unknow","unknow",
											"SHA-256","SHA-512","unknow","MD5CRYPT","SHA-1",
											"ORACLE-10G","ORACLE-11G","NTLM","LM","MSSQL-2000",
											"MSSQL-2005","MSSQL-2012","MYSQL-322","MYSQL-411","POSTGRESQL",
											"SYBASE-ASE1502","INFORMIX-1170"};
	//private static int ALGORITHMS_LENGTH = 21;
	private static char[] ALGORITHMS_CODES = {'0','1','2','3','4','5','6','7','8','9',
											  'A','B','C','D','E','F','G','H','I','J',
											  'K', 'L'};
	private static char[] HEX_CHARS = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	private static char[] BASE64_CHARS = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
										  'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
										  '0','1','2','3','4','5','6','7','8','9','/','+','='};
	/*
	 * MD5
	 */
	
	public final static String MD5(String s){
        MessageDigest m = null;
		try {
			m = MessageDigest.getInstance("MD5");
			m.update(s.getBytes(),0,s.length());
			return DatatypeConverter.printHexBinary(m.digest()).toLowerCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * MD5Crypt
	 */
	
	public final static String MD5Crypt(String password, String salt){
    	MD5Crypt m = new MD5Crypt();
		return m.crypt(password,salt);
	}
	
	public final static byte[] MD5_byteArray(byte[] s){
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("MD5");
			m.update(s,0,s.length);
			return m.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * SHA1
	 */
	
	public final static String SHA1(String s){
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("SHA1");
			m.update(s.getBytes(),0,s.length());
			return DatatypeConverter.printHexBinary(m.digest()).toLowerCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
    }
	
	public final static byte[] SHA1_byteArray(byte[] s){
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("SHA1");
			m.update(s,0,s.length);
			return m.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
    }
    
	/*
	 * SHA-256
	 */
	
	public final static String SHA256(String s) {
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("SHA-256");
			m.update(s.getBytes(),0,s.length());
			return DatatypeConverter.printHexBinary(m.digest()).toLowerCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public final static byte[] SHA256_byteArray(byte[] s) {
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("SHA-256");
			m.update(s,0,s.length);
			return m.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * SHA-512
	 */
	
	public final static String SHA512(String s) {
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("SHA-512");
			m.update(s.getBytes(),0,s.length());
			return DatatypeConverter.printHexBinary(m.digest()).toLowerCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public final static byte[] SHA512_byteArray(byte[] s) {
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("SHA-512");
			m.update(s,0,s.length);
			return m.digest();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String decryptCisco7(byte[] hash){
		byte[] xarr = {0x64, 0x73, 0x66, 0x64, 0x3b, 0x6b,
	                   0x66, 0x6f, 0x41, 0x2c, 0x2e, 0x69,
	                   0x79, 0x65, 0x77, 0x72, 0x6b, 0x6c,
	                   0x64, 0x4a, 0x4b, 0x44, 0x48, 0x53,
	                   0x55, 0x42};
		System.out.println("hash len: "+hash.length);
		int i = (int) hash[0];                         
		StringBuilder result = new StringBuilder(""); 
		for (int c = 1 ; c < hash.length; c++){
			result.append((char) (hash[c] ^ xarr[i++]));                            
			i %= 53;  
		}
		return result.toString();
	}
	
	/*
	 * Various functions
	 */

	public static int getAlgorithmTypeCode(String algorithm) {
		int hashTypeNumber = 0; // unknow
		
		for (int i = 0; i < ALGORITHMS.length; i++){
			if (algorithm.equalsIgnoreCase(ALGORITHMS[i])){
				hashTypeNumber = i;
			}
		}
		
	    return hashTypeNumber;
	}

	public static String getAlgorithmName(int algorithmCode){
		String returnValue = "UNKNOW";
		if (algorithmCode > 0 && algorithmCode < ALGORITHMS.length){
			returnValue = ALGORITHMS[algorithmCode];
		}
		
		return returnValue;
	}
	
	public static String getAlgorithmName(String algorithmCode){
		String returnValue = "UNKNOW";
		int wordLength = algorithmCode.length();
		if (wordLength == 1){
			returnValue = getAlgorithmName(Character.getNumericValue(algorithmCode.charAt(0)))+"()";
		}
		else if (wordLength > 1){
			returnValue = "";
			for (int i = 0; i < wordLength; i++){
				returnValue = returnValue + getAlgorithmName(Character.getNumericValue(algorithmCode.charAt(i))) + "(";
			}
			for (int i = 0; i < wordLength; i++){
				returnValue = returnValue + ")";
			}
			
		}
		
		return returnValue;
	}

	public static boolean isSupportedAlgorithm(String algorithmCode) {
		boolean returnValue = true;
		int wordLength = algorithmCode.length(); 
		for (int i = 0; i < wordLength; i++){
			int j = Character.getNumericValue(algorithmCode.charAt(i)); 
			if(!isSupportedAlgorithm(j)){
				returnValue = false;
				break;
			}
		 }
		
		return returnValue;
	}
	
	public static boolean isNativeAlgorithm(int algorithmCode){
		boolean returnValue = false;
		if (algorithmCode == 1 || algorithmCode == 5 || algorithmCode == 6 || algorithmCode == 9){
			returnValue = true;
		}
		
		return returnValue;
	}
	
	public static boolean isNativeAlgorithm(String algorithmCodes){
		boolean returnValue = true;
		int wordLength = algorithmCodes.length();
		int ac = 0;
		OUTERMOST:
		for (int i = 0; i < wordLength; i++){ 
			ac = Character.getNumericValue(algorithmCodes.charAt(i));
			// SpecialAlgorithms not supported in chained algorithms
			if (!isNativeAlgorithm(ac)){
				returnValue = false;
				break OUTERMOST;
			}
		}
		
		return returnValue;
	}
	
	public static boolean isSupportedAlgorithm(int algorithmCode) {
		boolean returnValue = false;
		if (!getAlgorithmName(algorithmCode).equalsIgnoreCase("UNKNOW")){
			returnValue = true;
		}
		
		return returnValue;
	}
	
	public static boolean isUsernameSaltedHash(int algorithmCode){
		return MyMessageDigest.isUsernameSaltedHash(getAlgorithmName(algorithmCode));
	}
	
	public static boolean isHashedSpecialAlgorithm(int algorithmCode){
		
		return MyMessageDigest.isSaltededSpecialAlgorithm(getAlgorithmName(algorithmCode));
	}
	
	public static String getUTF8String(byte[] b){
		String aux = "";
		try {
			aux = new String(b,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return aux;
	}
	
	public static String getUpperCaseHash(byte[] b){
		return DatatypeConverter.printHexBinary(b);
	}
	
	public static String getLowerCaseHash(byte[] b){
		return DatatypeConverter.printHexBinary(b).toLowerCase();
	}
	
	public static MyMessageDigest getMyMessageDigest(int algorithmCode){
		MyMessageDigest m = null;
		String algName = getAlgorithmName(algorithmCode);
		if (!isNativeAlgorithm(algorithmCode)){
			m = new MyMessageDigest(algName);
		}
		else {
			try {
				m = (MyMessageDigest) MessageDigest.getInstance(algName);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		
		
		return m;
	}
	
	public MessageDigest getMessageDigest(String algorithm){
		return getMessageDigest(getAlgorithmTypeCode(algorithm));
	}
	
	public static MessageDigest getMessageDigest(int algorithmCode){
		MessageDigest m = null;
		String algName = getAlgorithmName(algorithmCode);
		try {
			if (isNativeAlgorithm(algorithmCode)){
				m = MessageDigest.getInstance(algName);
			}
			else {
				m = new MyMessageDigest(algName);
			}	
		}	
		catch (NoSuchAlgorithmException e) {
			m = null;
		}
		
		return m;
	}
	
	public static MessageDigest[] getMessageDigestArray(String algorithmCodes){
		int wordLength = algorithmCodes.length();
		MessageDigest[] ma = new MessageDigest[wordLength];
		int ac = 0;
		algorithmCodes = new StringBuilder(algorithmCodes.toUpperCase()).reverse().toString();
		for (int i = 0; i < wordLength; i++){
			ac = Character.getNumericValue(algorithmCodes.charAt(i));
			// SpecialAlgorithms not supported in chained algorithms
			if (isNativeAlgorithm(ac)){
				ma[i] = getMessageDigest(ac);
			}
		}		
		
		return ma;
	}

	public static int getAlgorithmCodeFromInput(String algorithmCodes) {
		int returnValue = 0;
		if (algorithmCodes.length() == 1){
			algorithmCodes = algorithmCodes.toUpperCase();
			char c = algorithmCodes.charAt(0);
			OUTERLOOP:
			for (int i = 0; i < ALGORITHMS.length; i++){
				if (c == ALGORITHMS_CODES[i]){
					returnValue = i;
					break OUTERLOOP;
				}
			}
		}	
		else {
			returnValue = 0;
		}
		
		return returnValue;
	}
	
	private static final boolean isHexChar(char c){
		boolean returnValue = false;
		
		char cu = Character.toUpperCase(c);
		OUTERLOOP:
		for(int i = 0; i < HEX_CHARS.length; i++){
			if (cu == HEX_CHARS[i]){
				returnValue = true;
				break OUTERLOOP;
			}
		}
		
		return returnValue;
	}
	
	public static final boolean isHexHash(String hash){
		boolean returnValue = true;
		if (hash.length()%2 == 0){
			OUTERLOOP:
			for (int i = 0; i < hash.length(); i++){
				returnValue = isHexChar(hash.charAt(i));
				if (!returnValue){
					break OUTERLOOP;
				}
			}
		}
		else {
			returnValue = false;
		}
		return returnValue;
	}
	
	public static final boolean isValidHash(String hash, int algorithmCode){
		boolean returnValue = false;
		if (hash == null){
			returnValue = false;
		}
		else if (hash.length() == 0){
			returnValue = false;
		}
		else if (isSupportedAlgorithm(algorithmCode)){
			if (isNativeAlgorithm(algorithmCode)){
				if (algorithmCode == 1){
					//MD5
					returnValue = (hash.length() == 32 && isHexHash(hash));
				}
				else if (algorithmCode == 5){//SHA-256
					returnValue = hash.length() == 64 && isHexHash(hash);
				}
				else if (algorithmCode == 6){//SHA-512
					returnValue = hash.length() == 128 && isHexHash(hash);
				}
				else if (algorithmCode == 9){//SHA1
					returnValue = hash.length() == 40 && isHexHash(hash);
				}
			}
			else {
				returnValue = MyMessageDigest.isValidHash(hash, getAlgorithmName(algorithmCode));
			}
		}	
		
		return returnValue;
	}

	public static List<String> guessAlgorithms(String hash, boolean luckyMode) {
		List<String> returnValue = new ArrayList<String>();
		for (int i = 0; i < ALGORITHMS.length; i++){
			if (isValidHash(hash, i)){
				returnValue.add(ALGORITHMS[i]);
			}
		}
		if (returnValue.size() > 1 && luckyMode){
			List<String> aux = new ArrayList<String>();
			OUTERMOST:
			for (String algName: returnValue){
				if (algName.equalsIgnoreCase("MD5")){
					// actually prefer MD5 over MD4, NTLM or LM
					aux.add(algName);
					break OUTERMOST;
				}
				else if (algName.equalsIgnoreCase("SHA-1")){
					// actually prefer SHA-1 over MYSQL-411
					aux.add(algName);
					break OUTERMOST;
				}
			}
			if (aux.size() == 0){
				// for future implementations, just choose the first of the list
				aux.add(returnValue.get(0));
			}
			returnValue = aux;
		}
		return returnValue;
	}

	public static String[] getAvailableAlgorithms() {
		return ALGORITHMS;
	}

	public static String getAlgorithmCodeInput(int algCode) {
		char c = ALGORITHMS_CODES[algCode];
		String returnValue = new String("");
		returnValue = returnValue + c;
		
		return returnValue;
	}
	
	private static final boolean isBase64Char(char c){
		boolean returnValue = false;
		
		OUTERLOOP:
		for(int i = 0; i < BASE64_CHARS.length; i++){
			if (c == BASE64_CHARS[i]){
				returnValue = true;
				break OUTERLOOP;
			}
		}
		
		return returnValue;
	}

	public static boolean isBase64Hash(String hash) {
		boolean returnValue = true;
		if (hash.length()%4 == 0){
			OUTERLOOP:
			for (int i = 0; i < hash.length(); i++){
				returnValue = isBase64Char(hash.charAt(i));
				if (!returnValue){
					break OUTERLOOP;
				}
			}
		}
		else {
			returnValue = false;
		}
		return returnValue;
	}
	
	public static boolean isValidStringNumber(String number){
		boolean returnValue = true;
		OUTERLOOP:
		for (int i = 0; i < number.length(); i++){
			if (!Character.isDigit(number.charAt(i))){
				returnValue = false;
				break OUTERLOOP;
			}
		}
		return returnValue;
	}
	
	public static byte[] getSaltByteArray(String salt, String saltType) {
		// salt can not be null here
		byte[] saltByte = null;
		if (salt != null){
			if (saltType.equals("int64")){
				if (isValidStringNumber(salt)){
					saltByte = new BigInteger(salt).toByteArray();
				}	
			}
			else if (saltType.equals("hex")){
				if (AvailableAlgorithms.isHexHash(salt)){
					saltByte = DatatypeConverter.parseHexBinary(salt);
				}	
			}
			else if (saltType.equals("utf8")){
				saltByte = salt.getBytes();
			}
		}
		return saltByte;
	}
}
