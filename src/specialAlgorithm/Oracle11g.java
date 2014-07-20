package specialAlgorithm;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import javax.xml.bind.DatatypeConverter;
import entities.ByteArray;


/*
Oracle11g.java

Created: 8 July 2013
Release: 1.0
Version: 1.3
Last Mod Date: 2013/10/19 11:20:00

About Class:
------------
This class implements the hash algorithm used to store user passwords
in Oracle 11gR1 (or higher).
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

public class Oracle11g implements SpecialAlgorithm{
	private MessageDigest m = null;
	private static final byte S_BYTE = (byte) 'S';
	private static final byte TWOPOINST_BYTE = (byte) ':';
	
	public Oracle11g() {
		super();
		try {
			m = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public final String crypt(String password, String salt){
		byte[] salt1 = null;
		if (salt == null){
			// generate a random salt of 10 bytes
			salt1 = new byte[10];
			new Random().nextBytes(salt1);
		}
		else {
			salt1 = DatatypeConverter.parseHexBinary(salt);
		}	
		 
		return toHash(cryptPerf(password.getBytes(), salt1));
	}
	
	private final String bytes2Utf8(byte[] ba){
		try {
			return new String(ba,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// I never will return null, because UTF-8 always exists
			e.printStackTrace();
			return null;
		}
	}
	
	public final String toHash(byte[] b){
		byte[] aux = new byte[b.length-2];
		int auxIndex = 0;
		for (int i = 2; i < b.length; i++){
			aux[auxIndex] = b[i];
			auxIndex = auxIndex + 1;
		}
		String returnValue1 = bytes2Utf8(b).substring(0, 2);
		String returnValue2 = DatatypeConverter.printHexBinary(aux); 
		return returnValue1 + returnValue2;
	}
	
	public byte[] cryptPerf(byte[] password, byte[] salt){
		byte[] toUpdate = new byte[password.length + salt.length];
		for (int i = 0; i < password.length; i++){
			toUpdate[i] = password[i];
		}
		int myIndex = password.length;
		for (int j = 0; j < salt.length; j++){
			toUpdate[myIndex] = salt[j];
			myIndex = myIndex + 1;
		}
		m.update(toUpdate, 0, toUpdate.length);
		byte[] mResult = m.digest(); 
		byte[] output = new byte[mResult.length + 2 + 10];
		output[0] = S_BYTE;
		output[1] = TWOPOINST_BYTE;
		for(int i = 0; i < mResult.length; i++){
			output[i+2] = mResult[i];
		}
		// append salt in hexa representation, 10 bytes
		int auxIndex = mResult.length + 2;
		for (int i = 0; i < salt.length; i++){
			output[auxIndex] = salt[i];
			auxIndex = auxIndex + 1;
		}
		
		return output;
	}
	
	public final byte[] toByteArray(String hash){
		byte[] bhash = new byte[32];
		bhash[0] = S_BYTE;
		bhash[1] = TWOPOINST_BYTE;
		byte[] auxbhash = DatatypeConverter.parseHexBinary(hash.substring(2,hash.length()));
		for (int i = 0; i < auxbhash.length; i++){
			bhash[i+2] = auxbhash[i];
		}
		
		return bhash;
	}
	
	public byte[] getSaltFromHash(byte[] hash) {
		byte[] salt = new byte[10];
		int auxIndex = 0;
		int hashLength = hash.length;
		for (int i = hashLength - 10; i < hashLength; i++){
			salt[auxIndex] = hash[i];
			auxIndex = auxIndex + 1;
		}
		return salt;
	}

	public byte[] appendSaltToWord(byte[] word, byte[] salt) {
		return ByteArray.concat(word, salt);
	}
}
