package specialAlgorithm;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import entities.ByteArray;


/*
LM.java

Created: 1 July 2013
Release: 1.0
Version: 1.1
Last Mod Date: 2013/09/11 17:00:00

About Class:
------------
This class implements the hash algorithm LM, used by Microsoft to store passwords.
I´ve got details of implementation from several web pages.

About License:
--------------
This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

public class LM implements SpecialAlgorithm {
	private static byte[] MAGIC_STRING = new String("KGS!@#$%").getBytes();
	private static int PWD_FIXED_SIZE = 14;
	private static byte NULL_BYTE = (byte) '\0';
	private Cipher myCipher1 = null;
	private Cipher myCipher2 = null;
	
	public LM(){
		try {
			myCipher1 = Cipher.getInstance("DES/ECB/NoPadding");
			myCipher2 = Cipher.getInstance("DES/ECB/NoPadding");
			//magicString = new String("KGS!@#$%").getBytes();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	static private final String bytes2Utf8(byte[] ba){
		try {
			return new String(ba,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// I never will return null, because UTF-8 always exists
			e.printStackTrace();
			return null;
		}
	}
	
	static public final byte[] prepare(byte[] password){
		byte [] bpwd = null;
		// uppercase password
		password = ByteArray.toUpperCase(password);
		// correct password length
		if (password.length < PWD_FIXED_SIZE){
			// padding with null bytes
			int difference = PWD_FIXED_SIZE - password.length;
			byte[] aux = new byte[difference];
			for (int i = 0; i < aux.length; i++){
				aux[i] = NULL_BYTE;
			}
			bpwd = ByteArray.concat(password, aux);
		}
		else if (password.length > PWD_FIXED_SIZE){
			bpwd = ByteArray.getSubArray(password, 0, PWD_FIXED_SIZE - 1);
		}
		else {// password length equal to PWD_FIXED_SIZE
			bpwd = password;
		}

		return bpwd;
	}

	public String toHash(byte[] hash) {
		return DatatypeConverter.printHexBinary(hash);
	}
	
	private final static byte[] toDESKey(byte[] password){
	    /* make room for parity bits */
		byte[] key = new byte[8];
	    key[0] =                         (byte) (password[0] >> 0);
	    key[1] = (byte) (((password[0]) << 7) | (password[1] >> 1));
	    key[2] = (byte) (((password[1]) << 6) | (password[2] >> 2));
	    key[3] = (byte) (((password[2]) << 5) | (password[3] >> 3));
	    key[4] = (byte) (((password[3]) << 4) | (password[4] >> 4));
	    key[5] = (byte) (((password[4]) << 3) | (password[5] >> 5));
	    key[6] = (byte) (((password[5]) << 2) | (password[6] >> 6));
	    key[7] = (byte) (password[6] << 1);
	    
	    return key;
	}
	
	public String crypt(String password, String salt){
		return toHash(cryptPerf(password.getBytes(), null));
	}
	
	public byte[] cryptPerf(byte[] password, byte[] salt){
		byte[] returnValue = null;
		byte[] prepared = prepare(password);
		byte[] part1 = ByteArray.getSubArray(prepared, 0, 6);
		byte[] part2 = ByteArray.getSubArray(prepared, 7, 13);
		SecretKey keySpec1 = new SecretKeySpec(LM.toDESKey(part1), "DES");
		SecretKey keySpec2 = new SecretKeySpec(LM.toDESKey(part2), "DES");
		try {
			myCipher1.init(Cipher.ENCRYPT_MODE, keySpec1);
			byte[] result1 = myCipher1.doFinal(MAGIC_STRING);
			myCipher2.init(Cipher.ENCRYPT_MODE, keySpec2);
			byte[] result2 = myCipher2.doFinal(MAGIC_STRING);
			returnValue = ByteArray.concat(result1, result2);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return returnValue;
	}
	
	/*private static byte[] getSaltFromHash(byte[] hash) {
		return salt;
	}*/

	public byte[] appendSaltToWord(byte[] word, byte[] hash) {
		return word;
	}

	public final byte[] toByteArray(String hash){
		return DatatypeConverter.parseHexBinary(hash);
	}

	@Override
	public byte[] getSaltFromHash(byte[] hash) {
		return null;
	}
}
