package specialAlgorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import javax.xml.bind.DatatypeConverter;
import entities.ByteArray;

/*
Sysbase1502.java

Created: 9 July 2013
Release: 1.0
Version: 1.2
Last Mod Date: 2013/09/23 12:00:00

About Class:
------------
This class implements the hash algorithm used to store user passwords
in Sysbase v15.0.2 (or higher).
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

public class SybaseASE1502 implements SpecialAlgorithm{
	private MessageDigest m = null;
	private static final int FIXED_SIZE = 510;
	
	public SybaseASE1502() {
		super();
		try {
			m = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public final String crypt(String password, String salt){
		byte[] salt1 = null;
		if (salt == null){
			// generate a random salt of 8 bytes
			salt1 = new byte[8];
			new Random().nextBytes(salt1);
		}
		else {
			salt1 = DatatypeConverter.parseHexBinary(salt);
		}

		return toHash(cryptPerf(password.getBytes(), salt1));
	}
	
	public final String toHash(byte[] b){
		return "0x" + DatatypeConverter.printHexBinary(b).toLowerCase(); 
	}
	
	private final byte[] prepare(byte[] password) {
		byte[] result = null;
		byte[] finalResult = new byte[518];
		//result = toBytesUtf16(toStringUtf8(password));
		result = ByteArray.toUTF16BE(password);
		if (result.length > FIXED_SIZE){
			finalResult = ByteArray.truncate(result, FIXED_SIZE);
		}
		else { // size <= fixedSize
			for (int i = 0; i < result.length; i++){
				finalResult[i] = result[i];
			}
		}
		
		return finalResult;
	}

	public final byte[] cryptPerf(byte[] password, byte[] salt){
		// convert password
		byte[] bpassword = prepare(password);
		// append salt
		bpassword = ByteArray.concat(bpassword, salt);
		m.update(bpassword);
		byte[] hashPrefix = new byte[2];
		hashPrefix[0] = (byte) 0xC0;
		hashPrefix[1] = (byte)0x07;
		byte[] output = ByteArray.concat(hashPrefix, salt); 
		
		return ByteArray.concat(output, m.digest());
	}
	
	public final byte[] toByteArray(String hash){
		int separatorPos = hash.indexOf('x'); // this should be 1 ...
		String hexHash = hash.substring(separatorPos + 1, hash.length());
		return DatatypeConverter.parseHexBinary(hexHash);
	}
	
	public byte[] getSaltFromHash(byte[] hash) {
		// salt are 8 bytes, after the first 2
		return ByteArray.getSubArray(hash, 2, 9);
	}

	public byte[] appendSaltToWord(byte[] word, byte[] salt) {
		return ByteArray.concat(salt, word);
	}
}
