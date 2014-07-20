package specialAlgorithm;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import javax.xml.bind.DatatypeConverter;
import entities.ByteArray;

/*
Informix1170.java

Created: 18 October 2013
Release: 1.0
Version: 1.0
Last Mod Date: 2013/10/18 21:00:00

About Class:
------------
This class implements the hash algorithm INFORMIX-1170, used by IBM to store Informix´s passwords.
I discover the algorithm using the IBM´s documentation and coffee.

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

public class Informix1170 implements SpecialAlgorithm{
	private MessageDigest m = null;
	
	public Informix1170() {
		try {
			this.m = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	@Override
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
		for (int i = 0; i < myIndex; i++){
			salt[i] = hash[i];
		}

		return salt;
	}

	@Override
	public byte[] appendSaltToWord(byte[] word, byte[] hash) {
		byte[] salt = getSaltFromHash(hash);
		return ByteArray.concat(salt, word);
	}

	@Override
	public byte[] toByteArray(String hash) {
		int index = hash.indexOf('/');
		String salt = hash.substring(0, index);
		
		String pureHash = hash.substring(index +1);
		pureHash = pureHash.replace('.', '+').replace('_', '/');
		byte[] aux = new BigInteger(salt).toByteArray();
		byte[] bsalt = new byte[1];
		bsalt[0] = '/';
		bsalt = ByteArray.concat(aux, bsalt);
		byte[] bpureHash = DatatypeConverter.parseBase64Binary(pureHash);
		return ByteArray.concat(bsalt, bpureHash);
	}

	@Override
	public byte[] cryptPerf(byte[] password, byte[] salt) {
		m.update(ByteArray.concat(salt, password));
		byte[] returnValue = m.digest();
		// i need to concatenate the salt as presalt and the "/" character
		returnValue = insertPreSaltForCompatibilityWithJBrute(returnValue, salt);
		return returnValue;
	}

	@Override
	public String crypt(String word, String salt) {
		byte[] salt1 = null;
		if (salt == null){
			salt1 = new byte[8];
			new Random().nextBytes(salt1);
		}
		else {
			salt1 = new BigInteger(salt).toByteArray();
		}
		
		return toHash(cryptPerf(word.getBytes(), salt1));
	}

	@Override
	public String toHash(byte[] hash) {
		String returnValue = "";
		byte[] salt = getSaltFromHash(hash);
		returnValue = returnValue + new BigInteger(salt).toString() + "/";
		byte aux[] = new byte[hash.length - salt.length - 1];
		int auxIndex = salt.length + 1;
		for (int i = 0; i < hash.length - salt.length - 1; i++){
			aux[i] = hash[auxIndex];
			auxIndex = auxIndex + 1;
		}
		returnValue = returnValue + DatatypeConverter.printBase64Binary(aux).replace('/', '_').replace('+', '.');
		//if (returnValue.charAt(0) == '-'){
		//	returnValue = returnValue.substring(1);
		//}
		return returnValue;
	}
	
	private static byte[] insertPreSaltForCompatibilityWithJBrute(byte[] b, byte[] salt) {
		byte[] returnValue = new byte[b.length + 1 + salt.length];
		for (int i = 0; i < salt.length; i++){
			returnValue[i] = salt[i];
		}
		returnValue[salt.length] = (byte) '/';
		int auxIndex = salt.length + 1;
		for (int i = 0; i < b.length; i++){
			returnValue[auxIndex] = b[i];
			auxIndex = auxIndex + 1;
		}
		return returnValue;
	}

}
