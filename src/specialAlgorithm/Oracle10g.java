package specialAlgorithm;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import entities.ByteArray;


/*
Oracle10g.java

Created: 7 July 2013
Release: 1.0
Version: 1.3
Last Mod Date: 2013/10/23 19:00:00

About Class:
------------
This class implements the hash algorithm used to store user´s passwords
in Oracle from version 7 to 10gR2 (inclusive).
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

public class Oracle10g implements SpecialAlgorithm{
	private static final byte[] magicNumber = DatatypeConverter.parseHexBinary("0123456789ABCDEF");
	private static final SecretKey keySpec = new SecretKeySpec(magicNumber, "DES");
	private static final IvParameterSpec iv = new IvParameterSpec(new byte[8]);
	private Cipher myCipher = null;
	
	public Oracle10g(){
		try {
			myCipher = Cipher.getInstance("DES/CBC/NoPadding");
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}
	
	private static final byte[] prepare(byte[] username, byte[] password){
		//byte[] nusername = bytes2Utf8(username).toUpperCase().getBytes();
		//byte[] npassword = bytes2Utf8(password).toUpperCase().getBytes();
		byte[] nusername = ByteArray.toUpperCase(username);
		byte[] npassword = ByteArray.toUpperCase(password);
		int nplength = npassword.length;
		int nulength = nusername.length;
		byte[] result = new byte[(nulength + nplength)*2];
		int auxIndex = 0;
		for (int i = 0; i < nulength; i++){
			result[auxIndex] = 0;
			auxIndex = auxIndex + 1;
			result[auxIndex] = nusername[i];
			auxIndex = auxIndex + 1;
		}
		
		for (int i = 0; i < nplength; i++){
			result[auxIndex] = 0;
			auxIndex = auxIndex + 1;
			result[auxIndex] = npassword[i];
			auxIndex = auxIndex + 1;
		}
		
		// if it is not a multiple of 8 bytes, then i need to pad with zeros at the end
		byte[] paddedResult = result;
		int aux2Index = result.length;
		if (aux2Index % 8 != 0){
			int cantPadding =  8 - (aux2Index % 8);
			int newLength = aux2Index + cantPadding;
			paddedResult = new byte[newLength];
			for (int i = 0; i < aux2Index; i++){
				paddedResult[i] = result[i];
			}
			
			for (int i = 0; i < cantPadding; i++){
				paddedResult[aux2Index] = 0;
				aux2Index = aux2Index + 1;
			}
		}

		return paddedResult;
	}
	
	private static final String bytes2Utf8(byte[] ba){
		try {
			return new String(ba,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// I never will return null, because UTF-8 always exists
			e.printStackTrace();
			return null;
		}
	}

	public String toHash(byte[] hash) {
		String returnValue = "";
		byte[] salt = getSaltFromHash(hash);
		returnValue = returnValue + bytes2Utf8(salt).toUpperCase() + "/";
		byte aux[] = new byte[hash.length - salt.length - 1];
		int auxIndex = salt.length + 1;
		for (int i = 0; i < hash.length - salt.length - 1; i++){
			aux[i] = hash[auxIndex];
			auxIndex = auxIndex + 1;
		}
		return returnValue + DatatypeConverter.printHexBinary(aux);
	}

	private static final byte[] getIn1(byte[] bprepared) {
		byte[] in2 = new byte[8];
		int auxIndex = 0;
		int myLength = bprepared.length; 
		for (int i =  myLength - 8; i < myLength; i++){
			in2[auxIndex] = bprepared[i];
			auxIndex = auxIndex + 1;
		}
		return in2;
	}
	
	public String crypt(String word, String salt){
		return toHash(cryptPerf(word.getBytes(), salt.getBytes()));
	}
	
	public byte[] cryptPerf(byte[] password, byte[] salt){
		byte[] returnValue = null;
		byte[] prepared = prepare(salt, password);
		
		try {
			myCipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);
			returnValue = myCipher.doFinal(prepared);
			//
			byte[] newKey = getIn1(returnValue);
			SecretKeySpec keySpec2 = new SecretKeySpec(newKey, "DES");
			myCipher.init(Cipher.ENCRYPT_MODE, keySpec2, iv);
			returnValue = myCipher.doFinal(prepared);
			returnValue = getIn1(returnValue);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		// i need to concatenate the salt as presalt and the "/" character
		returnValue = insertPreSaltForCompatibilityWithJBrute(returnValue, salt);
		return returnValue;
	}
	
	private static final byte[] insertPreSaltForCompatibilityWithJBrute(byte[] b, byte[] username) {
		byte[] returnValue = new byte[b.length + 1 + username.length];
		for (int i = 0; i < username.length; i++){
			returnValue[i] = username[i];
		}
		returnValue[username.length] = (byte) '/';
		int auxIndex = username.length + 1;
		for (int i = 0; i < b.length; i++){
			returnValue[auxIndex] = b[i];
			auxIndex = auxIndex + 1;
		}
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
		byte[] salt = new byte[myIndex];
		salt = new byte[myIndex];
		for (int i = 0; i < myIndex; i++){
			salt[i] = hash[i];
		}

		return salt;
	}

	public byte[] appendSaltToWord(byte[] word, byte[] salt) {
		// +1 here is for the '/' character
		byte[] saltedWord = new byte[word.length + 1 + salt.length];
		for (int i = 0; i < salt.length; i++){
			saltedWord[i] = salt[i];
		}
		saltedWord[salt.length] = (byte) '/';
		int auxIndex = salt.length + 1;
		for (int i = 0; i < word.length; i++){
			saltedWord[auxIndex] = word[i];
			auxIndex = auxIndex + 1;
		}
		
		return saltedWord;
	}

	public byte[] toByteArray(String hash){
		int separatorPos = hash.indexOf('/');
		String salt = hash.substring(0, separatorPos);
		String rest = hash.substring(separatorPos + 1, hash.length());
		byte[] bsalt = salt.getBytes();
		byte[] brest = DatatypeConverter.parseHexBinary(rest);
		byte[] bhash = new byte[bsalt.length + 1 + brest.length];
		int auxIndex = 0;
		for (int i = 0; i <bsalt.length; i++){
			bhash[i] = bsalt[i];
		}
		bhash[bsalt.length] = (byte) '/';
		auxIndex = bsalt.length + 1;
		for (int i = 0; i < brest.length; i++){
			bhash[auxIndex] = brest[i];
			auxIndex = auxIndex + 1;
		}
		
		return bhash;
	}
	
}