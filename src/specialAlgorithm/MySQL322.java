package specialAlgorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import entities.ByteArray;


/*
MySQL322.java

Created: 5 July 2013
Release: 1.0
Version: 1.1
Last Mod Date: 2013/09/11 17:00:00

About Class:
------------
This class implements the hash algorithm used to store user passwords
in MySQLServer v3.2.2 (or earlier).
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

public class MySQL322 implements SpecialAlgorithm{
@SuppressWarnings("unused")
private MessageDigest m = null;
	
	public MySQL322() {
		try {
			this.m = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public final String toHash(byte[] b){
		return DatatypeConverter.printHexBinary(b).toLowerCase();
	}
	
	public final String crypt(String password, String salt){
		return toHash(cryptPerf(password.getBytes(), null));
	}
	
	public final byte[] cryptPerf(byte[] password, byte[] salt){
		int nr = 1345345333; 
		int add = 7;
		int nr2 = 0x12345671;
		int tmp = 0;
		int inlen = password.length;
		int cblank = (int) ' ';
		int ccarry = (int) '\t';
		for (int i = 0; i < inlen; i++) {
			int b = (int) password[i];
			if (b != cblank && b != ccarry){
				tmp = b;
				nr ^= (((nr & 63) + add) * tmp) + ((nr << 8) & 0xFFFFFFFF);
				nr2 += ((nr2 << 8) & 0xFFFFFFFF) ^ nr;
				add += tmp;
			}	
		}
		int firstPart = nr & ((1 << 31) - 1);
		int lastPart = nr2 & ((1 << 31) - 1);
		return ByteArray.concat(ByteArray.toByteArray(firstPart), ByteArray.toByteArray(lastPart));
	}
	
	public byte[] getSaltFromHash(byte[] hash) {
		return null;
	}

	public byte[] appendSaltToWord(byte[] word, byte[] hash) {
		return word;
	}
	
	public final byte[] toByteArray(String hash){
		// expect a string with form: <MYSQL32_HASH>
		return DatatypeConverter.parseHexBinary(hash);
	}
}
