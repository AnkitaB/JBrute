package entities;

public class ByteArray {
	private static final byte[] SIGNS_UPPER = {(byte)'0',(byte)'1',(byte)'2',(byte)'3'
		,(byte)'4',(byte)'5',(byte)'6',(byte)'7'
		,(byte)'8',(byte)'9',(byte)'A',(byte)'B'
		,(byte)'C',(byte)'D',(byte)'E',(byte)'F'};
	private static final byte[]  SIGNS_LOWER= {(byte)'0',(byte)'1',(byte)'2',(byte)'3'
		,(byte)'4',(byte)'5',(byte)'6',(byte)'7'
		,(byte)'8',(byte)'9',(byte)'a',(byte)'b'
		,(byte)'c',(byte)'d',(byte)'e',(byte)'f'};

	static public final byte[] concat(byte[] source, byte[] append){
		byte[] result = new byte[source.length + append.length];
		for (int i = 0; i < source.length; i++){
			result[i] = source[i];
		}
		int auxIndex = source.length;
		for (int i = 0; i < append.length; i++){
			result[auxIndex] = append[i];
			auxIndex = auxIndex + 1;
		}
		
		return result;
	}
	
	static public final byte[] getCopyOf(byte[] source){
		byte[] result = new byte[source.length];
		for (int i = 0; i < source.length; i++){
			result[i] = source[i];
		}
		
		return result;
		
	}
	
	static public final byte[] getSubArray(byte[] source, int indexFrom, int indexTo){
		byte[] result = null;
		if ((indexTo < indexFrom) 
			|| (indexTo > source.length)
			|| (indexFrom < 0))
		{
			result = null;
		}
		else {
			 result = new byte[indexTo - indexFrom + 1];
		}	
		int auxIndex = 0;
		for (int i = indexFrom; i <= indexTo; i++){
			result[auxIndex] = source[i];
			auxIndex = auxIndex + 1;
		}
		return result;
	}
	
	/*static public final byte[] toByteArray(int value) {
	     return  ByteBuffer.allocate(4).putInt(value).array();
	}*/

	static public final byte[] toByteArray(int value) {
	    return new byte[] {
	        (byte) (value >> 24),
	        (byte) (value >> 16),
	        (byte) (value >> 8),
	        (byte) value};
	}

	/*static public final int fromByteArray(byte[] bytes) {
	     return ByteBuffer.wrap(bytes).getInt();
	}*/

	static public final int fromByteArray(byte[] bytes) {
	     return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}

	public static final byte[] truncate(byte[] source, int newSize) {
		byte[] result = new byte[newSize];
		for(int i = 0; i < newSize; i++){
			result[i] = source[i];
		}
		return result;
	}
	
	public static final byte[] toUpperCase(byte[] source) {
		byte[] returnValue = new byte[source.length];
		for (int i = 0; i < source.length; i++){
			returnValue[i] = (byte) Character.toUpperCase(source[i]); 
		}
		return returnValue;
		
	}
	
	public static final byte[] toLowerCase(byte[] source) {
		byte[] returnValue = new byte[source.length];
		for (int i = 0; i < source.length; i++){
			returnValue[i] = (byte) Character.toLowerCase(source[i]); 
		}
		return returnValue;
		
	}
	
	public static final byte[] toUpperCase(String source) {
		int strSize = source.length();
		byte[] returnValue = new byte[strSize];
		for (int i = 0; i < strSize; i++){
			returnValue[i] = (byte) Character.toUpperCase(source.charAt(i)); 
		}
		return returnValue;
		
	}
	
	public static final byte[] toLowerCase(String source) {
		int strSize = source.length();
		byte[] returnValue = new byte[strSize];
		for (int i = 0; i < strSize; i++){
			returnValue[i] = (byte) Character.toLowerCase(source.charAt(i)); 
		}
		return returnValue;
		
	}
	
	public static final byte[] toByteArray(StringBuilder sb){
		int myLength = sb.length();
		byte[] returnValue = new byte[myLength];
		
		for (int i = 0; i < myLength; i++){
			returnValue[i] = (byte) sb.charAt(i);
		}
		return returnValue;
	}
	
	public static final byte[] toUTF16LE(byte[] b){
		int auxLength = b.length * 2;
		byte[] returnValue = new byte[auxLength];
		int auxIndex = 0;
		for (int i = 0; i < auxLength; i++){
			if (i%2 == 0){
				returnValue[i] = b[auxIndex];
				auxIndex = auxIndex + 1;
			}
			else {
				returnValue[i] = 0x00;
			}
		}
		
		return returnValue;
	}
	
	public static final byte[] toUTF16BE(byte[] b){
		int auxLength = b.length * 2;
		byte[] returnValue = new byte[auxLength];
		int auxIndex = 0;
		for (int i = 0; i < auxLength; i++){
			if (i%2 == 0){
				returnValue[i] = 0x00;
			}
			else {
				returnValue[i] = b[auxIndex];
				auxIndex = auxIndex + 1;
			}
		}
		
		return returnValue;
	}
	
	public static final byte[] toByteArrayUpper(byte[] source){
		int myLength = source.length;
		byte[] returnValue = new byte[myLength*2];
		int auxIndex = 0;
		int aux = 0;
		int left = 0;
		int right = 0;
		for (int i = 0; i < myLength; i++){
			aux = source[i] & 0xFF;
			left = aux >>> 4 & 0x1F;
			right = ((aux & 0x0F) & 0x00FF);
			returnValue[auxIndex] = SIGNS_UPPER[left];
			auxIndex = auxIndex + 1;
			returnValue[auxIndex] = SIGNS_UPPER[right];
			auxIndex = auxIndex + 1;
		}
		
		return returnValue;
	}
	
	public static final byte[] toByteArrayLower(byte[] source){
		int myLength = source.length;
		byte[] returnValue = new byte[myLength*2];
		int auxIndex = 0;
		int aux = 0;
		int left = 0;
		int right = 0;
		for (int i = 0; i < myLength; i++){
			aux = source[i] & 0xFF;
			left = aux >>> 4 & 0x1F;
			right = ((aux & 0x0F) & 0x00FF);
			returnValue[auxIndex] = SIGNS_LOWER[left];
			auxIndex = auxIndex + 1;
			returnValue[auxIndex] = SIGNS_LOWER[right];
			auxIndex = auxIndex + 1;
		}
		
		return returnValue;
	}
}
