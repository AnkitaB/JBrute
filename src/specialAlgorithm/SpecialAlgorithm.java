package specialAlgorithm;

public interface SpecialAlgorithm {
	
	public  byte[] getSaltFromHash(byte[] hash);
	
	public byte[] appendSaltToWord(byte[] word, byte[] hash);
	
	public byte[] toByteArray(String hash);
	
	public byte[] cryptPerf(byte[] password, byte[] salt);
	
	public String crypt(String word, String salt);

	public String toHash(byte[] hash);
}
