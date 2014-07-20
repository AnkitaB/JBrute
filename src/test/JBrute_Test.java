package test;

import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.Assert;
import org.junit.Test;

import controller.JBrute;

public class JBrute_Test {

	ByteArrayOutputStream dislayResult = new ByteArrayOutputStream();
		
	@Test
	public void encrypt_Test(){
		
		try{
			String [] args = {"--encrypt", "--word=Ankita", "--algorithm=1"};
			System.setOut(new PrintStream(dislayResult));
			JBrute.main(args);
			String obtained= dislayResult.toString();
			String expected="Word to hash: Ankita\n"+
			"Hash MD5() : d265e24340d83487e7740d67927e4003";
			Assert.assertEquals(expected, obtained.trim());
			dislayResult.close();
			
		} catch (Exception e) {
				// TODO Auto-generated catch block
				fail("Not yet implemented");
			}
		
		
		}
	
	@Test
	public void SaltEncrypt_Test(){
		
		try{
			String [] args = {"--encrypt", "--word=Anki", "--algorithm=2", "--presalt=abc"};
			System.setOut(new PrintStream(dislayResult));
			JBrute.main(args);
			String obtained= dislayResult.toString();
			String expected="Word to hash: Anki\n"+
			"Hash MD4() : 4DDEB0C673A669C261FA40A1B47C2FA9";
			Assert.assertEquals(expected, obtained.trim());
			dislayResult.close();
			
		} catch (Exception e) {
				// TODO Auto-generated catch block
				fail("Not yet implemented");
			}
			
		}
	
	@Test
	public void postSalt_Test(){
		
		try{
			String [] args = {"--encrypt", "--word=Anki", "--algorithm=5", "--postsalt=abc"};
			System.setOut(new PrintStream(dislayResult));
			JBrute.main(args);
			String obtained= dislayResult.toString();
			String expected="56c6221de297e6604627797c4519111afb19232d0068c3c333ca38278c8bdd97";
			if(!obtained.contains(expected)){
				fail("Failed to display");
			}
			dislayResult.close();
			
		} catch (Exception e) {
				// TODO Auto-generated catch block
				fail("Not yet implemented");
			}
			
		}
	
	@Test
	public void version_Test(){
		
		try {
			String [] args = {"--version"};
			System.setOut(new PrintStream(dislayResult));
			JBrute.main(args);
			String obtained= dislayResult.toString();
			String expected="Current Version";
			if(!obtained.contains(expected)){
				fail("Failed to display");
			}
			dislayResult.close();
			 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		
	}

	@Test
	public void help_Test(){
		
		try {
			String [] args = {"--help"};
			System.setOut(new PrintStream(dislayResult));
			JBrute.main(args);
			String obtained= dislayResult.toString();
			String expected="Available parameters:";
			if(!obtained.contains(expected)){
				fail("Failed to help");
			}
			dislayResult.close();
			 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		
	}
	
	
	@Test
	public void chainedEncrypt(){
		try {
		String [] args = {"--encrypt", "--word=Anki" ,"--algorithm=569", "--chained_case=UU"};
		System.setOut(new PrintStream(dislayResult));
		JBrute.main(args);
		String obtained= dislayResult.toString();
		System.out.println("Str is "+ obtained);
		String expected="3194017aad2e4ee827fd2016dee8be233c394f05c494c40f1557657e0e9fba9c";
		if(!obtained.contains(expected)){
			fail("Failed to encrypt");
		}
		
			dislayResult.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
		
 	@Test
	public void lucky_test(){
		
		String [] args = {"--guess", "--lucky", "--hash=4d186321c1a7f0f354b297e8914ab240"};
		System.setOut(new PrintStream(dislayResult));
		JBrute.main(args);
		String obtained=dislayResult.toString();
		if(!obtained.contains("MD5")){
			fail("Failed to Guess");
		}
		dislayResult.reset();
	}

		
	@Test
	public void chainEncrypt(){
		String [] args = {"--encrypt", "--word=Anki", "--algorithm=15"};
		System.setOut(new PrintStream(dislayResult));
		JBrute.main(args);
		String obtained=dislayResult.toString();
        String expected="5bb17acebf687959d27bc7b8da06bfa0";
		if(!obtained.contains(expected)){
			fail("Failed to encrypt");
		}
	}
	
	
	@Test
	public void chainDecrypt(){
		try {
		String [] args = {"--decrypt", "--method=brute", "--algorithm=15", 
		"--hash=37d2de01465e1b56cb88b65d136f1a94"};
		System.setOut(new PrintStream(dislayResult));
		JBrute.main(args);
		String obtained=dislayResult.toString();
		String expected="hola";
		if(!obtained.contains(expected)){
			fail("Failed to decrypt");
		}
		dislayResult.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void decrypt_Test(){
		
		try {
			String [] args = {"--decrypt","--algorithm=2","--method=dictionary","--hash=4DDEB0C673A669C261FA40A1B47C2FA9"};  
			System.setOut(new PrintStream(dislayResult));
			JBrute.main(args);
			String obtained= dislayResult.toString();
			String expected="Anki";
			if(!obtained.contains(expected)){
				fail("Failed to decrypt");
			}
			dislayResult.close();
			 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		
	}
	

	@Test
	public void saltDecrypt_Test(){
		try {
		String [] args = {"--decrypt", "--method=brute", "--presalt=abc","--algorithm=2", "--hash=0EF546118008F6FB7EBAB60C7E3CEE2F"};	
		System.setOut(new PrintStream(dislayResult));
		JBrute.main(args);
		String obtained=dislayResult.toString();
		String input="Hola";
		if(!obtained.contains(input)){
			fail("Failed to decrypt");
			
		}	
		dislayResult.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
}



	@Test
	public void chainedDecrypt(){
	
	try {
		String [] args = {"--decrypt", "--method=brute", "--algorithm=191", "--chained_case=UU",
		"--hash=6cd73d33441970bcd7691f36f65782ac"};
		System.setOut(new PrintStream(dislayResult));
		JBrute.main(args);
		String obtained= dislayResult.toString();
		String expected="Anki";
		if(! obtained.contains(expected)){
			fail("Failed to decrypt");
		}
		dislayResult.close();
		 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	
}

	@Test
	public void chainedR(){
		try {
		String [] args = {"--encrypt", "--word=Anki", "--algorithm=19", "--chained_case=R"};
		System.setOut(new PrintStream(dislayResult));
		JBrute.main(args);
		String obtained= dislayResult.toString();
		String expected="16e58800476373b54eabb17dbda67772";
		if(!obtained.contains(expected)){
			fail("Failed to Encrypt");
		}

	
			dislayResult.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	@Test
	public void ChainedR_decrypt(){
	
		try {
		String [] args = {"--decrypt", "--method=brute", "--algorithm=19", "--chained_case=R",
		"--hash=16e58800476373b54eabb17dbda67772"};
		JBrute.main(args);
		String output=dislayResult.toString();
		String expected="Anki";
		if(!output.contains(expected)){
			fail("Failed to decrypt");
		}
	
		dislayResult.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void chainedUpper(){
		
		String [] args = {"--encrypt", "--word=Anki", "--algorithm=16", "--chained_case=R", 
		"--upper"};
		System.setOut(new PrintStream(dislayResult));
		JBrute.main(args);
		String expected="25F4FEE1F802A9A50668F883AB2D1198";
		String output=dislayResult.toString();
		if(!output.contains(expected)){
			fail("Failed to decrypt");
		}

		dislayResult.reset();
	}
	
	
	@Test
	public void chainedUpper_decrypt(){
		
		String [] args = {"--decrypt", "--method=brute", "--algorithm=16", "--chained_case=R",
		"--hash=25F4FEE1F802A9A50668F883AB2D1198"};
		System.setOut(new PrintStream(dislayResult));
		JBrute.main(args);
		String expected="Anki";
		String output=dislayResult.toString();
		if(!output.contains(expected)){
			fail("Failed to decrypt");
		}

		dislayResult.reset();
	}


@Test
	public void minMax(){
		String [] args = {"--decrypt", "--method=brute", "--algorithm=1", "--hash=4d186321c1a7f0f354b297e8914ab240", "--minlength=3", "--maxlength=5"};
		System.setOut(new PrintStream(dislayResult));
		JBrute.main(args);
		String output=dislayResult.toString();
		String expected="hola";
		if(!output.contains(expected)){
			fail("Failed to decrypt");
		}
		dislayResult.reset();
	}
	
	

@Test
	public void multipleEncrypt(){
		int i = 0;
		String[] algorithm = {"--algorithm=1","--algorithm=2","--algorithm=5","--algorithm=6",
				"--algorithm=9","--algorithm=C",
				"--algorithm=D","--algorithm=E","--algorithm=F","--algorithm=G","--algorithm=H",
				"--algorithm=I","--algorithm=I","--algorithm=K","--algorithm=L"};

		
		while(i < algorithm.length){
			String [] args = {"--encrypt", "--word=hola", algorithm[i]};
			System.setOut(new PrintStream(dislayResult));
			JBrute.main(args);
			dislayResult.reset();
			i++;
		}

		
	}
	

	@Test
	public void rule_test(){
		String [] args = {"--decrypt", "--method=dictionary", "--dict_file=wordlist.txt","--rule_file=rules.txt", 
		"--stdout"};
		System.setOut(new PrintStream(dislayResult));
		JBrute.main(args);
		String output=dislayResult.toString();
		if(!output.contains("hola")){
			fail("Failed to decrypt");
		}

		dislayResult.reset();
	}

	
	@Test
	public void alpha_test(){
				
		String [] args = {"--decrypt", "--method=brute", "--charset=loweralpha-numeric", 
		"--hash=37d2de01465e1b56cb88b65d136f1a94", "--algorithm=19"};
		System.setOut(new PrintStream(dislayResult));
		JBrute.main(args);
		String output=dislayResult.toString();

		if(!output.contains("hola")){
			fail("Failed to decrypt");
		}

		dislayResult.reset();
	}

	
	
}


