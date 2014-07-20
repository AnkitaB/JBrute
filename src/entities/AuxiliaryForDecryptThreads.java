package entities;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AuxiliaryForDecryptThreads {
	
	public static final byte[] getIntermediateChainedHash(byte[] b, String chainedAlgorithmCase, int pos) {
		byte[] returnValue = null;
		if (chainedAlgorithmCase.charAt(pos) == 'L'){
			//returnValue = DatatypeConverter.printHexBinary(b).toLowerCase().getBytes();
			returnValue = ByteArray.toByteArrayLower(b);
			
		}
		else if (chainedAlgorithmCase.charAt(pos) == 'U'){
			//returnValue = DatatypeConverter.printHexBinary(b).getBytes();
			returnValue = ByteArray.toByteArrayUpper(b);
		}
		else if (chainedAlgorithmCase.charAt(pos) == 'R'){
			returnValue = b;
		}
		return returnValue;
	}
	
	public static final List<byte[]> removeFromEncryptedHashes(List<byte[]> myByteList, byte[] ba) {
		int index = -1;
		OUTERLOOP:
		for(byte[] elem: myByteList){
			index = index + 1;
			if (Arrays.equals(elem, ba)){
				break OUTERLOOP;
			}
		}
		if (index >= 0){
			myByteList.remove(index);
		}
		return myByteList;
	}
	
	public static final byte[] processSalt(byte[] result, byte[] salt, boolean preSalt) {
		byte[] resultSalt = null;
		
		if (salt == null){
			resultSalt = result;
		}
		else {
			if (preSalt){
				resultSalt = ByteArray.concat(salt, result);
			}
			else { //post salt
				resultSalt = ByteArray.concat(result, salt);
			}
		}
		
		return resultSalt;
	}
	
	public static final byte[] getRandom(int length) {
		byte[] returnValue = new byte[length];
		new Random().nextBytes(returnValue);
		
		return returnValue;
	}
	
	public static final String bytes2Utf8(byte[] ba){
		try {
			return new String(ba,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// I never will return null, because UTF-8 always exists
			e.printStackTrace();
			return null;
		}
	}
	
	public static final String getRandomString(int length) {
		return bytes2Utf8(getRandom(length));
	}
	
	public static final boolean equalsTo(StringBuilder source, StringBuilder destination){
		boolean returnValue = true;
		if (source == null || destination == null) return false;
		if (source.length() != destination.length()) return false;
		int myLength = source.length();
		OUTERMOST:
		for (int i = 0; i < myLength; i ++){
			if (source.charAt(i) != destination.charAt(i)){
				returnValue = false;
				break OUTERMOST;
			}
		}
		
		return returnValue;
	}
}
