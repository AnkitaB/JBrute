package specialAlgorithm;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.xml.bind.DatatypeConverter;

import entities.ByteArray;


/*
Cisco5.java

Created: 2 July 2013
Release: 1.0
Version: 1.0
Last Mod Date: 2013/07/02 18:00:00

About Class:
------------
This class implements the hash algorithm type 5 used by Cisco to store passwords.
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

public class Cisco5 {
private MessageDigest m = null;
	
	public Cisco5() {
		super();
		try {
			m = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	static public final String crypt(String password){
		// generate a random salt of 10 bytes
		byte[] b = new byte[10];
		new Random().nextBytes(b);
		String salt = DatatypeConverter.printHexBinary(b);
		return crypt(password, salt);
	}
	
	static private final String bytes2Utf8(byte[] ba){
		try {
			return new String(ba,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// I never will return null, because UTF-8 always exists
			e.printStackTrace();
			return null;
		}
	}
	
	static public final String toHash(byte[] b){
		return DatatypeConverter.printHexBinary(b).toLowerCase(); 
	}
	
	static public final String crypt(String password, String salt){
		return toHash(crypt(password.getBytes(), DatatypeConverter.parseHexBinary(salt)));
	}
	
	private static byte[] prepare(byte[] password) {
		byte[] result = null;
		int fixedSize = 510;
		try {
			result = bytes2Utf8(password).getBytes("UTF-16BE");
			if (result.length < fixedSize){
				// padding with zeros
				byte[] bpadding = new byte[fixedSize - result.length];
				result = ByteArray.concat(result,bpadding);
			}
			else if (result.length > fixedSize){
				// truncate
				result = ByteArray.truncate(result, 510);
			}//else password has exactly 510 bytes, i dont need to do something
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return result;
	}

	static public final byte[] crypt(byte[] password, byte[] salt){
		// convert password
		byte[] bpassword = prepare(password);
		// append salt
		bpassword = ByteArray.concat(bpassword, salt);
		MessageDigest m1;
		byte[] output = null;
		try {
			m1 = MessageDigest.getInstance("SHA-256");
			if (bpassword.length < 518){
				System.err.println("[Sysbase1502.crypt]prepared password has length < 518 bytes");
			}
			m1.update(bpassword); 
			//output = new byte[2 + 8 + 32];
			output = new byte[2];
			output[0] = (byte) 0xC0;
			output[1] = (byte) 0x07;
			output = ByteArray.concat(output, salt);
			output = ByteArray.concat(output, m1.digest());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return output;
	}

	public byte[] cryptPerf(byte[] password, byte[] salt){
		// convert password
		byte[] bpassword = prepare(password);
		// append salt
		bpassword = ByteArray.concat(bpassword, salt);
		//m = MessageDigest.getInstance("SHA-256");
		if (bpassword.length < 518){
			System.err.println("[Sysbase1502.crypt]prepared password has length < 518 bytes");
		}
		m.update(bpassword); 
		//output = new byte[2 + 8 + 32];
		byte[] output = new byte[2];
		output[0] = (byte) 0xC0;
		output[1] = (byte) 0x07;
		output = ByteArray.concat(output, salt);
		output = ByteArray.concat(output, m.digest());
		
		return output;
	}
	
	static public final byte[] toByteArray(String hash){
		return DatatypeConverter.parseHexBinary(hash);
	}
	
	private static byte[] getSaltFromHash(byte[] hash) {
		// salt are 8 bytes, after the first 2
		return ByteArray.getSubArray(hash, 2, 9);
	}

	public static byte[] appendSaltToWord(byte[] word, byte[] hash) {
		return ByteArray.concat(getSaltFromHash(hash), word);
	}
}
