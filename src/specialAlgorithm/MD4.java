package specialAlgorithm;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import javax.xml.bind.DatatypeConverter;

/*
MD4.java

Created: 1 July 2013
Release: 1.0
Version: 1.1
Last Mod Date: 2013/09/11 17:00:00

About Class:
------------
This class implements the hash algorithm MD4, actually with weird support on java.

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

public class MD4 implements SpecialAlgorithm{
	private MessageDigest m = null;
	private static Provider md4Provider = null;
	
	@SuppressWarnings("serial")
	public MD4() {
		super();
		try {
			md4Provider = new Provider("MD4Provider", 1.0d, "MD4 MessageDigest") {};
			md4Provider.put("MessageDigest.MD4", "sun.security.provider.MD4");
			m = MessageDigest.getInstance("MD4", md4Provider);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	static private final String bytes2Utf8(byte[] ba){
		try {
			return new String(ba,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// I never will return null, because UTF-8 always exists
			e.printStackTrace();
			return null;
		}
	}
	
	public String toHash(byte[] b){
		return DatatypeConverter.printHexBinary(b);
	}
	
	public String crypt(String password, String salt){
		return toHash(cryptPerf(password.getBytes(), null));
	}
	
	public byte[] cryptPerf(byte[] password, byte[] salt){
		m.update(password);
		
		return m.digest();
	}

	@Override
	public byte[] getSaltFromHash(byte[] hash) {
		return null;
	}

	@Override
	public byte[] appendSaltToWord(byte[] word, byte[] hash) {
		return word;
	}

	@Override
	public byte[] toByteArray(String hash) {
		return DatatypeConverter.parseHexBinary(hash);
	}
}
