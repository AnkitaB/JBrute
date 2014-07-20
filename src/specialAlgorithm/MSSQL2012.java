package specialAlgorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;

import entities.ByteArray;


/*
MSSQL2012.java

Created: 5 July 2013
Release: 1.0
Version: 1.2
Last Mod Date: 2013/09/23 12:00:00

About Class:
------------
This class implements the hash algorithm used by Microsoft to store user passwords
in SQLServer2012.
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

public class MSSQL2012 implements SpecialAlgorithm{
	private MessageDigest m = null;
	
	public MSSQL2012() {
		try {
			this.m = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public String toHash(byte[] b){
		return "0x" + DatatypeConverter.printHexBinary(b);
	}
	
	public final String crypt(String password, String salt){
		byte[] salt1 = null;
		if (salt == null){
			salt1 = new byte[4];
			new Random().nextBytes(salt1);
		}
		else {
			salt1 = salt.getBytes();
		}
		return toHash(cryptPerf(password.getBytes(), salt1));
	}
	
	public final byte[] cryptPerf(byte[] password, byte[] salt){
		byte[] bhash = null;
		byte[] saltedPassword = null;
		//byte[] bpassword = MSSQL2012.bytes2Utf8(password).getBytes("UTF-16LE");
		byte[] bpassword = ByteArray.toUTF16LE(password);
		saltedPassword = ByteArray.concat(bpassword, salt); 
		m.update(saltedPassword);
		byte[] bhashIntermediate = m.digest();
		byte[] constant = new byte[2];
		constant[0] = 2;
		constant[1] = 0;
		bhash = new byte[2];
		bhash[0] = constant[0];
		bhash[1] = constant[1];
		bhash = ByteArray.concat(bhash, salt);
		bhash = ByteArray.concat(bhash, bhashIntermediate);	
	
		return bhash;
	}
	
	public  byte[] getSaltFromHash(byte[] hash) {
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
	
	public final byte[] toByteArray(String hash){
		// expect a string with form: 0x200 + HEX_SALT + HEX_SHA_HASH
		int separatorPos = hash.indexOf('x'); // this should be 1 ...
		String hexHash = hash.substring(separatorPos + 1, hash.length());
		
		return DatatypeConverter.parseHexBinary(hexHash);
	}
}
