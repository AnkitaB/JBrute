package sharedByLicense;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*

Based on the work of Jonathan Abbey
Adopted for Director by Thomas Aeby, 2004

MD5Crypt.java

Created: 3 November 1999
Release: $Name:  $
Version: $Revision: 1.1 $
Last Mod Date: $Date: 2004/02/04 08:10:35 $
Java Code By: Jonathan Abbey, jonabbey@arlut.utexas.edu
Original C Version:
----------------------------------------------------------------------------
"THE BEER-WARE LICENSE" (Revision 42):
<phk@login.dknet.dk> wrote this file.  As long as you retain this notice you
can do whatever you want with this stuff. If we meet some day, and you think
this stuff is worth it, you can buy me a beer in return.   Poul-Henning Kamp
----------------------------------------------------------------------------

-----------------------------------------------------------------------
            
Ganymede Directory Management System

Copyright (C) 1996, 1997, 1998, 1999  The University of Texas at Austin.

Contact information

Author Email: ganymede_author@arlut.utexas.edu
Email mailing list: ganymede@arlut.utexas.edu

US Mail:

Computer Science Division
Applied Research Laboratories
The University of Texas at Austin
PO Box 8029, Austin TX 78713-8029

Telephone: (512) 835-3200

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

*/

/*------------------------------------------------------------------------------
                                                                        class
                                                                     MD5Crypt

------------------------------------------------------------------------------*/

/**
* <p>This class defines a method,
* {@link MD5Crypt#crypt(java.lang.String, java.lang.String) crypt()}, which
* takes a password and a salt string and generates an OpenBSD/FreeBSD/Linux-compatible
* md5-encoded password entry.</p>
*
* <p>Created: 3 November 1999</p>
* <p>Release: $Name:  $</p>
* <p>Version: $Revision: 1.1 $</p>
* <p>Last Mod Date: $Date: 2004/02/04 08:10:35 $</p>
* <p>Java Code By: Jonathan Abbey, jonabbey@arlut.utexas.edu</p>
* <p>Original C Version:<pre>
* ----------------------------------------------------------------------------
* "THE BEER-WARE LICENSE" (Revision 42):
* <phk@login.dknet.dk> wrote this file.  As long as you retain this notice you
* can do whatever you want with this stuff. If we meet some day, and you think
* this stuff is worth it, you can buy me a beer in return.   Poul-Henning Kamp
* ----------------------------------------------------------------------------
* </pre></p>
*/

public final class MD5Crypt {

/**
*
* Command line test rig.
* @throws NoSuchAlgorithmException 
* @throws UnsupportedEncodingException 
*
*/

	static public void main(String argv[]) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
	 if ((argv.length < 1) || (argv.length > 2))
	   {
	        System.err.println("Usage: MD5Crypt password salt");
	        System.exit(1);
	   }
	
	 if (argv.length == 2)
	   {
	        System.err.println(MD5Crypt.crypt(argv[0], argv[1]));
	   }
	 else
	   {
	        System.err.println(MD5Crypt.crypt(argv[0]));
	   }
	 
	 System.exit(0);
	}

	static private final String SALTCHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	
	static private final String itoa64 = "./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	static private final StringBuffer to64sb(long v, int size){
		StringBuffer result = new StringBuffer();
	
		while (--size >= 0){
			result.append(itoa64.charAt((int) (v & 0x3f)));
			v >>>= 6;
		}
		return result;
	}

	static private final void clearbits(byte bits[])
	{
	 for (int i = 0; i < bits.length; i++)
	   {
	        bits[i] = 0;
	   }
	}

/**
* convert an encoded unsigned byte value into a int
* with the unsigned value.
*/

	static private final int bytes2u(byte inp)
	{
	 return (int) inp & 0xff;
	}
	
	static public final String bytes2Utf8(byte[] ba)
	{
		try {
			return new String(ba,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// I never will return null, because UTF-8 always exists
			e.printStackTrace();
			return null;
		}
	}

/**
* <p>This method actually generates a OpenBSD/FreeBSD/Linux PAM compatible
* md5-encoded password hash from a plaintext password and a
* salt.</p>
*
* <p>The resulting string will be in the form '$1$&lt;salt&gt;$&lt;hashed mess&gt;</p>
*
* @param password Plaintext password
*
* @return An OpenBSD/FreeBSD/Linux-compatible md5-hashed password field.
* @throws NoSuchAlgorithmException 
* @throws UnsupportedEncodingException 
*/

	static public final String crypt(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException
	{
	 StringBuffer salt = new StringBuffer();
	 java.util.Random randgen = new java.util.Random();
	
	 /* -- */
	
	  while (salt.length() < 8)
	    {
	      int index = (int) (randgen.nextFloat() * SALTCHARS.length());
	      salt.append(SALTCHARS.substring(index, index+1));
	    }
	
	  return MD5Crypt.crypt(password, salt.toString());
	}

/**
* <p>This method actually generates a OpenBSD/FreeBSD/Linux PAM compatible
* md5-encoded password hash from a plaintext password and a
* salt.</p>
*
* <p>The resulting string will be in the form '$1$&lt;salt&gt;$&lt;hashed mess&gt;</p>
*
* @param password Plaintext password
* @param salt A short string to use to randomize md5.  May start with $1$, which
*             will be ignored.  It is explicitly permitted to pass a pre-existing
*             MD5Crypt'ed password entry as the salt.  crypt() will strip the salt
*             chars out properly.
*
* @return An OpenBSD/FreeBSD/Linux-compatible md5-hashed password field.
* @throws NoSuchAlgorithmException 
*/
	
	static public final String crypt(String password, String salt) throws NoSuchAlgorithmException {
		return bytes2Utf8(MD5Crypt.crypt(password.getBytes(), salt.getBytes()));
	}

	static public final byte[] crypt(byte[] password, byte[] salt) throws NoSuchAlgorithmException
	{
		 /* This string is magic for this algorithm.  Having it this way,
		  * we can get get better later on */
		
		String magic = "$1$";
		byte[] magicByte = magic.getBytes();
		byte finalState[];
		MessageDigest ctx, ctx1;
		long l; 
		
		if ((char) salt[0] == '$'){
			salt = removeFirstChar(salt);
		}

		if (salt.length > 8){
			salt = truncateSalt(salt);
		}
		
		ctx = MessageDigest.getInstance("MD5");
		
		ctx.update(password);    // The password first, since that is what is most unknown
		ctx.update(magicByte);   // Then our magic string
		ctx.update(salt);        // Then the raw salt
		
		/* Then just as many characters of the MD5(pw,salt,pw) */
		
		ctx1 = MessageDigest.getInstance("MD5");
		ctx1.update(password);
		ctx1.update(salt);
		ctx1.update(password);
		finalState = ctx1.digest();
		
		int passwordLength = password.length; 
		for (int pl = passwordLength; pl > 0; pl -= 16){
			 for( int i=0; i< (pl > 16 ? 16 : pl); i++ ){
				 ctx.update(finalState[i] );
			 }	 
		}
		
		/* the original code claimed that finalState was being cleared
		   to keep dangerous bits out of memory, but doing this is also
		   required in order to get the right output. 
		*/
		
		clearbits(finalState);
		 
		/* Then something really weird... */
		
		for (int i = passwordLength; i != 0; i >>>=1){
			if ((i & 1) != 0){
			    ctx.update(finalState[0]);
			}
			else {
			    ctx.update(password[0]);
			}
		}
		
		finalState = ctx.digest();
		
		/*
		 * and now, just to make sure things don't run too fast
		 * On a 60 Mhz Pentium this takes 34 msec, so you would
		 * need 30 seconds to build a 1000 entry dictionary...
		 *
		 * (The above timings from the C version)
		 */
		ctx1 = MessageDigest.getInstance("MD5");
		for (int i = 0; i < 1000; i++){
		
		        if ((i & 1) != 0){
		            ctx1.update(password);
		        }
		        else {
		            for( int c=0; c<16; c++ ){
		              ctx1.update(finalState[c]);
		            }  
		        }
		
		        if ((i % 3) != 0){
		            ctx1.update(salt);
		        }
		
		        if ((i % 7) != 0){
		            ctx1.update(password);
		        }
		
		        if ((i & 1) != 0){
		            for( int c=0; c<16; c++ ){
		            	ctx1.update(finalState[c]);
		            }  
		        }
		        else {
		            ctx1.update(password);
		        }
		
		        finalState = ctx1.digest();
		}
		
		/* Now make the output string */
		
		StringBuffer result = new StringBuffer();
		
		result.append(magic);
		result.append(bytes2Utf8(salt));
		result.append('$');
		
		l = (bytes2u(finalState[0]) << 16) | (bytes2u(finalState[6]) << 8) | bytes2u(finalState[12]);
		result.append(to64sb(l, 4));
		
		l = (bytes2u(finalState[1]) << 16) | (bytes2u(finalState[7]) << 8) | bytes2u(finalState[13]);
		result.append(to64sb(l, 4));
		
		l = (bytes2u(finalState[2]) << 16) | (bytes2u(finalState[8]) << 8) | bytes2u(finalState[14]);
		result.append(to64sb(l, 4));
		
		l = (bytes2u(finalState[3]) << 16) | (bytes2u(finalState[9]) << 8) | bytes2u(finalState[15]);
		result.append(to64sb(l, 4));
		
		 l = (bytes2u(finalState[4]) << 16) | (bytes2u(finalState[10]) << 8) | bytes2u(finalState[5]);
		 result.append(to64sb(l, 4));
		
		 l = bytes2u(finalState[11]);
		 result.append(to64sb(l, 2));
		
		 /* Don't leave anything around in vm they could use. */
		 clearbits(finalState);
		
		 return result.toString().getBytes();
	}

	private static byte[] removeFirstChar(byte[] salt) {
		int saltLength = salt.length;
		byte[] returnValue = new byte[saltLength - 1];
		
		for (int i = 1; i < saltLength; i++){
			returnValue[i - 1] = salt[i];
		}
		return returnValue;
	}

	private static byte[] truncateSalt(byte[] salt) {
		int maxSaltLength = 8;
		byte[] returnValue = new byte[maxSaltLength];
		for (int i = 0; i < maxSaltLength; i++){
			returnValue[i] = salt[i];
		}	
		
		return returnValue;
	}
}