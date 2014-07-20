package specialAlgorithm;

import java.security.MessageDigest;

import entities.AvailableAlgorithms;

public class MyMessageDigest extends MessageDigest {
	private SpecialAlgorithm m = null;
	private byte[] input;
	
	public MyMessageDigest() {
		super(null);
	}
	
	public MyMessageDigest(String specialAlgorithm) {
		super(null);
		int algorithmCode = AvailableAlgorithms.getAlgorithmTypeCode(specialAlgorithm);

		if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("LM")){
			m = new LM();
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("MD4")){
			m = new MD4();
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("MYSQL-322")){
			m = new MySQL322();
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("NTLM")){
			m = new NTLM();
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("MYSQL-411")){
			m = new MySQL411();
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("MD5CRYPT")){
			m = new MD5Crypt();
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("ORACLE-10G")){
			m = new Oracle10g();
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("ORACLE-11G")){
			m = new Oracle11g();
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("MSSQL-2000")){
			m = new MSSQL2000();
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("MSSQL-2005")){
			m = new MSSQL2005();
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("MSSQL-2012")){
			m = new MSSQL2012();
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("POSTGRESQL")){
			m = new PostgreSQL();
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("SYBASE-ASE1502")){
			m = new SybaseASE1502();
		}
		else if (algorithmCode == AvailableAlgorithms.getAlgorithmTypeCode("INFORMIX-1170")){
			m = new Informix1170();
		}
		else {
			System.err.println("No special algorithm...");
		}
	
	}
	
	public static boolean isUsernameSaltedHash(String algorithm){
		boolean returnValue = false;
		if (algorithm.equalsIgnoreCase("ORACLE-10G")
				|| algorithm.equalsIgnoreCase("POSTGRESQL"))
		{
			returnValue = true;
		}
		
		return returnValue;
	}
	
	public static boolean isSaltededSpecialAlgorithm(String algorithm){
		boolean returnValue = false;
		if (algorithm.equalsIgnoreCase("MD5CRYPT")
				|| algorithm.equalsIgnoreCase("ORACLE-10G")
				|| algorithm.equalsIgnoreCase("ORACLE-11G")
				|| algorithm.equalsIgnoreCase("MSSQL-2000")
				|| algorithm.equalsIgnoreCase("MSSQL-2005")
				|| algorithm.equalsIgnoreCase("MSSQL-2012")
				|| algorithm.equalsIgnoreCase("POSTGRESQL")
				|| algorithm.equalsIgnoreCase("SYBASE-ASE1502")
				|| algorithm.equalsIgnoreCase("INFORMIX-1170")){
			returnValue = true;
		}
		
		return returnValue;
	}
	
	public static final boolean isValidHash(String hash, String algorithm){
		boolean returnValue = false;
		if (hash == null){
			returnValue = false;
		}
		else if (hash.length() == 0){
			returnValue = false;
		}
		else {
			if (algorithm.equalsIgnoreCase("MD4")
					|| algorithm.equalsIgnoreCase("NTLM") 
					|| algorithm.equalsIgnoreCase("LM"))
			{
				returnValue = (hash.length() == 32 && AvailableAlgorithms.isHexHash(hash));
			}
			else if (algorithm.equalsIgnoreCase("MYSQL-411")){
				returnValue = hash.length() == 40 && AvailableAlgorithms.isHexHash(hash);
			}
			else if (algorithm.equalsIgnoreCase("MD5CRYPT")){
				if (hash.length() < 12){
					returnValue = false;
				}
				else {
					returnValue = (hash.substring(0, 3).equalsIgnoreCase("$1$") && hash.charAt(11) == '$');
					if (returnValue){
						returnValue = hash.length() == 34;
					}
				}	
			}
			else if (algorithm.equalsIgnoreCase("ORACLE-10G")){
				int index = hash.lastIndexOf('/');
				if (index == -1){
					returnValue = false;
				}
				else {
					String hash2 = hash.substring(index+1,hash.length());
					returnValue = hash2.length() == 16 && AvailableAlgorithms.isHexHash(hash2);
				}	
			}
			else if (algorithm.equalsIgnoreCase("ORACLE-11G")){
				if (hash.length() > 2){
					returnValue = (Character.toUpperCase(hash.charAt(0)) == 'S' && hash.charAt(1) == ':');
					if (returnValue){
						returnValue = hash.length() == 62 && AvailableAlgorithms.isHexHash(hash.substring(2, hash.length()));
					}
				}	
			}
			else if (algorithm.equalsIgnoreCase("MSSQL-2000")){
				if (hash.length() <= 6){
					returnValue = false;
				}
				else {
					String aux = hash.substring(0, 6);
					if (aux.equalsIgnoreCase("0x0100")){
						returnValue = (hash.length() == 94 && AvailableAlgorithms.isHexHash(hash.substring(6, hash.length())));
					}
				}	
			}
			else if (algorithm.equalsIgnoreCase("MSSQL-2005")){
				if (hash.length() <= 6){
					returnValue = false;
				}
				else {
					String aux = hash.substring(0, 6);
					if (aux.equalsIgnoreCase("0x0100")){
						returnValue = (hash.length() == 54 && AvailableAlgorithms.isHexHash(hash.substring(6, hash.length())));
					}
				}	
			}
			else if (algorithm.equalsIgnoreCase("MSSQL-2012")){
				if (hash.length() <= 6){
					returnValue = false;
				}
				else {
					String aux = hash.substring(0, 6);
					if (aux.equalsIgnoreCase("0x0200")){
						returnValue = (hash.length() == 142 && AvailableAlgorithms.isHexHash(hash.substring(6, hash.length())));
					}
				}	
			}
			else if (algorithm.equalsIgnoreCase("MYSQL-322")){
				returnValue = (hash.length() == 16 && AvailableAlgorithms.isHexHash(hash));
			}
			else if (algorithm.equalsIgnoreCase("POSTGRESQL")){
				int index = hash.lastIndexOf('/');
				if (index == -1){
					returnValue = false;
				}
				else {
					String hash2 = hash.substring(index+1,hash.length());
					if (hash2.length() > 2){
						if (hash2.substring(0, 3).equalsIgnoreCase("md5")){
							returnValue = hash2.length() == 35 && AvailableAlgorithms.isHexHash(hash2.substring(3, hash2.length()));
						}
					}	
				}	
			}
			else if (algorithm.equalsIgnoreCase("SYBASE-ASE1502")){
				if (hash.length() > 6){
					String aux = hash.substring(0, 6);
					if (aux.equalsIgnoreCase("0xc007")){
						returnValue = hash.length() == 86 && AvailableAlgorithms.isHexHash(hash.substring(6));
					}
				}
			}
			else if (algorithm.equalsIgnoreCase("INFORMIX-1170")){
				int index = hash.lastIndexOf('/');
				if (index == -1){
					returnValue = false;
				}
				else {
					String salt = hash.substring(0,index - 1);
					if (!AvailableAlgorithms.isValidStringNumber(salt)){
						returnValue = false;
					}
					else {
						String hash2 = hash.substring(index+1);
						hash2 = hash2.replace('_', '/').replace('.', '+');
						returnValue = hash2.length() == 44 && AvailableAlgorithms.isBase64Hash(hash2) && hash2.charAt(hash2.length()-1) == '=';
					}		
				}	
			}
		}	
		
		return returnValue;
	}

	@Override
	public byte[] engineDigest() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void engineReset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void engineUpdate(byte arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void engineUpdate(byte[] arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public byte[] digest(byte[] salt){
		// i will use this method to append salt to word
		return m.cryptPerf(input, salt);
	}
	
	@Override
	public byte[] digest(){
		return m.cryptPerf(input, null);
	}
	
	@Override
	public void update(byte[] input, int offset, int len){
		this.input = input;
	}
	
	@Override
	public void update(byte[] input){
		this.input = input;
	}
	
	public byte[] getSaltFromHash(byte[] hash){
		return m.getSaltFromHash(hash);
	}
	
	public byte[] hashToByteArray(String hash){
		return m.toByteArray(hash);
	}
	
	public String crypt(String word, String salt){
		return m.crypt(word, salt);
	}
	
	public String toHash(byte[] hash){
		return m.toHash(hash);
	}
}
