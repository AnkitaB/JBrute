package specialAlgorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

/*
MySQL411.java

Created: 5 July 2013
Release: 1.0
Version: 1.1
Last Mod Date: 2013/09/11 17:00:00

About Class:
------------
This class implements the hash algorithm used to store user passwords
in MySQLServer v4.1.1 (and higher).
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

public class MySQL411 implements SpecialAlgorithm{
	private MessageDigest m = null;
	
	public MySQL411() {
		try {
			this.m = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	public final String toHash(byte[] b){
		return DatatypeConverter.printHexBinary(b);
	}
	
	public final String crypt(String password, String salt){
		return toHash(cryptPerf(password.getBytes(), null));
	}
	
	public final byte[] cryptPerf(byte[] password, byte[] salt){
		m.update(password);
		m.update(m.digest());
		return m.digest();
	}
	public byte[] appendSaltToWord(byte[] word, byte[] hash) {
		return word;
	}
	
	public final byte[] toByteArray(String hash){
		// expect a string with form: <SHA1_HASH>
		return DatatypeConverter.parseHexBinary(hash);
	}

	@Override
	public byte[] getSaltFromHash(byte[] hash) {
		return null;
	}
}
