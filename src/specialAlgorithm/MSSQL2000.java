package specialAlgorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import javax.xml.bind.DatatypeConverter;

import entities.ByteArray;


/*
MSSQL2000.java

Created: 4 July 2013
Release: 1.0
Version: 1.2
Last Mod Date: 2013/09/23 12:00:00

About Class:
------------
This class implements the hash algorithm used by Microsoft to store user passwords
in SQLServer2000 (or earlier).
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

public class MSSQL2000 implements SpecialAlgorithm{
	private MessageDigest m = null;
	
	public MSSQL2000() {
		try {
			this.m = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public String toHash(byte[] b){
		return "0x" + DatatypeConverter.printHexBinary(b);
	}
	
	public String crypt(String password, String salt){
		byte[] salt1 = null;
		if (salt == null){
			salt1 = new byte[4];
			new Random().nextBytes(salt1);
		}
		
		return toHash(cryptPerf(password.getBytes(), salt1));
	}
	
	public byte[] crypt(byte[] password, byte[] salt){
		
		return cryptPerf(password, salt);
	}
	
	public byte[] cryptPerf(byte[] password, byte[] salt){
		byte[] bhash = null;
		byte[] bpasswordLower = ByteArray.toUTF16LE(password);
		byte[] bpasswordUpper = ByteArray.toUTF16LE(ByteArray.toUpperCase(password));
		bpasswordLower = ByteArray.concat(bpasswordLower, salt);
		bpasswordUpper = ByteArray.concat(bpasswordUpper, salt); 
		m.update(bpasswordLower);
		byte[] bhashIntermediate = m.digest();
		m.update(bpasswordUpper);
		bhashIntermediate = ByteArray.concat(bhashIntermediate, m.digest());
		bhash = new byte[2];
		// constant part of the hash
		bhash[0] = 0x01;
		bhash[1] = 0x00;
		bhash = ByteArray.concat(bhash, salt);
		bhash = ByteArray.concat(bhash, bhashIntermediate);

		return bhash;
	}
	
	public byte[] getSaltFromHash(byte[] hash) {
		byte[] salt = new byte[4];
		int auxIndex = 0;
		for (int i = 2; i < 6; i++){
			salt[auxIndex] = hash[i];
			auxIndex = auxIndex + 1;
		}
		return salt;
	}

	public byte[] appendSaltToWord(byte[] word, byte[] salt) {
		return ByteArray.concat(word, salt);
	}
	
	public byte[] toByteArray(String hash){
		// expect a string with form: 0x100 + HEX_SALT + HEX_SHA_HASH
		int separatorPos = hash.indexOf('x'); // this should be 1 ...
		String hexHash = hash.substring(separatorPos + 1, hash.length());
		
		return DatatypeConverter.parseHexBinary(hexHash);
	}
}
