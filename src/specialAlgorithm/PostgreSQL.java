package specialAlgorithm;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;

import entities.ByteArray;


/*
PostgreSQL.java

Created: 9 July 2013
Release: 1.0
Version: 1.1
Last Mod Date: 2013/09/11 17:00:00

About Class:
------------
This class implements the hash algorithm used to store user passwords
in PostgreSQL.
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

public class PostgreSQL implements SpecialAlgorithm{
	@SuppressWarnings("unused")
	private static byte[] magicNumber = DatatypeConverter.parseHexBinary("0123456789ABCDEF");
	private MessageDigest m = null;
	private byte[] hashPrefix = null;
	
	public PostgreSQL(){
		try {
			m = MessageDigest.getInstance("MD5");
			hashPrefix = new byte[4];
			hashPrefix[0] = (byte) '/';
			hashPrefix[1] = (byte) 'm';
			hashPrefix[2] = (byte) 'd';
			hashPrefix[3] = (byte) '5';
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}	
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

	public final String toHash(byte[] hash) {
		byte[] salt = getSaltFromHash(hash);
		int auxIndex = 0;
		OUTERLOOP:
		for (int i = 0; i < hash.length; i++){
			if (hash[i] == (byte) '/'){
				auxIndex = i;
				break OUTERLOOP;
			}
		}
		byte[] rest = ByteArray.getSubArray(hash, auxIndex + 4, hash.length - 1);
		
		return bytes2Utf8(salt) + "/md5" + DatatypeConverter.printHexBinary(rest).toLowerCase();
	}
	
	public final String crypt(String password, String salt){
		return toHash(cryptPerf(password.getBytes(), salt.getBytes()));
	}
	
	public byte[] cryptPerf(byte[] password, byte[] salt){
		byte[] lowerSalt = ByteArray.toLowerCase(salt);
		byte[] prepared = ByteArray.concat(password, lowerSalt);
		m.update(prepared);
		
		return insertPreSaltForCompatibilityWithJBrute(m.digest(), lowerSalt);
	}
	
	private byte[] insertPreSaltForCompatibilityWithJBrute(byte[] b, byte[] username) {
		byte[] returnValue = new byte[username.length];
		for (int i = 0; i < username.length; i++){
			returnValue[i] = username[i];
		}
		returnValue = ByteArray.concat(returnValue, hashPrefix);
		returnValue = ByteArray.concat(returnValue, b);
		
		return returnValue;
	}

	public byte[] getSaltFromHash(byte[] hash) {
		int myIndex = 0;
		OUTERLOOP:
		for (int i = 0; i < hash.length; i++){
			if ((char) hash[i] == '/'){
				myIndex = i;
				break OUTERLOOP;
			}
		}
		byte[] lowerSalt = ByteArray.toLowerCase(ByteArray.getSubArray(hash, 0, myIndex - 1)); 
		
		return lowerSalt;
	}

	public byte[] appendSaltToWord(byte[] word, byte[] salt) {
		// +1 here is for the '/' character
		byte[] saltedWord = new byte[salt.length + 1];
		for (int i = 0; i < salt.length; i++){
			saltedWord[i] = salt[i];
		}
		saltedWord[salt.length] = (byte) '/';
		saltedWord = ByteArray.concat(saltedWord, word);
		
		return saltedWord;
	}

	public final byte[] toByteArray(String hash){
		int separatorPos = hash.indexOf('/');
		String salt = hash.substring(0, separatorPos);
		byte[] bsalt = salt.getBytes();
		//remove md5 word
		String rest = hash.substring(separatorPos + 1 + 3, hash.length());
		byte[] brest = DatatypeConverter.parseHexBinary(rest);
		byte[] bhash = new byte[bsalt.length + 1 + 3];
		for (int i = 0; i <bsalt.length; i++){
			bhash[i] = bsalt[i];
		}
		bhash[bsalt.length] = (byte) '/';
		bhash[bsalt.length + 1] = (byte) 'm';
		bhash[bsalt.length + 2] = (byte) 'd';
		bhash[bsalt.length + 3] = (byte) '5';
		bhash = ByteArray.concat(bhash, brest);
		
		return bhash;
	}
}
