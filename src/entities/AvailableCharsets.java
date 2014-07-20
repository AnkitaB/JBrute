package entities;

import java.util.ArrayList;
import java.util.List;

public class AvailableCharsets {
	private static final String NUMERIC = "0123456789";
	private static final String ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String ALPHA_NUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final String LOWERALPHA = "abcdefghijklmnopqrstuvwxyz";
	private static final String LOWERALPHA_NUMERIC = "abcdefghijklmnopqrstuvwxyz0123456789";
	private static final String MIXALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String MIXALPHA_NUMERIC = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	// The charset "ascii-32-95" includes all 95 characters on standard US keyboard
	private static final String ASCII_32_95 = " !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
	private static final String ASCII_32_65_123_4 = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`{|}~";
	private static final String ALPHA_NUMERIC_SYMBOL32_SPACE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_+=~`[]{}|\\:;\"'<>,.?/ ";
	private static final String ORACLE_ALPHA_NUMERIC_SYMBOL3 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789#$_";
	
	public static String getCharsetByName(String charsetName){
		String returnValue = "unknow";
		if (charsetName.equalsIgnoreCase("numeric")){
			returnValue = NUMERIC;
		}
		else if (charsetName.equalsIgnoreCase("alpha")){
			returnValue = ALPHA;
		}
		else if (charsetName.equalsIgnoreCase("alpha-numeric")){
			returnValue = ALPHA_NUMERIC;
		}
		else if (charsetName.equalsIgnoreCase("loweralpha")){
			returnValue = LOWERALPHA;
		}
		else if (charsetName.equalsIgnoreCase("loweralpha-numeric")){
			returnValue = LOWERALPHA_NUMERIC;
		}
		else if (charsetName.equalsIgnoreCase("mixalpha")){
			returnValue = MIXALPHA;
		}
		else if (charsetName.equalsIgnoreCase("mixalpha-numeric")){
			returnValue = MIXALPHA_NUMERIC;
		}
		else if (charsetName.equalsIgnoreCase("ascii-32-95")){
			returnValue = ASCII_32_95;
		}
		else if (charsetName.equalsIgnoreCase("ascii-32-65-123-4")){
			returnValue = ASCII_32_65_123_4;
		}
		else if (charsetName.equalsIgnoreCase("alpha-numeric-symbol32-space")){
			returnValue = ALPHA_NUMERIC_SYMBOL32_SPACE;
		}
		else if (charsetName.equalsIgnoreCase("oracle-alpha-numeric-symbol3")){
			returnValue = ORACLE_ALPHA_NUMERIC_SYMBOL3;
		}
		
		return returnValue;
	}
	
	public static void printAvailableCharsetNames(){
		System.out.println("");
		System.out.println("Available charsets: ");
		System.out.println("numeric");
		System.out.println("alpha");
		System.out.println("alpha-numeric");
		System.out.println("loweralpha");
		System.out.println("loweralpha-numeric");
		System.out.println("mixalpha");
		System.out.println("mixalpha-numeric");
		System.out.println("ascii-32-95 (includes all 95 characters on standard US keyboard)");
		System.out.println("ascii-32-65-123-4");
		System.out.println("alpha-numeric-symbol32-space");
		System.out.println("oracle-alpha-numeric-symbol3");
		System.out.println("");
	}
	public static List<String> getAvailableCharsetNames(){
		List<String> list = new ArrayList<String>();
		list.add("numeric");
		list.add("alpha");
		list.add("alpha-numeric");
		list.add("loweralpha");
		list.add("loweralpha-numeric");
		list.add("mixalpha");
		list.add("mixalpha-numeric");
		list.add("ascii-32-95");
		list.add("ascii-32-65-123-4");
		list.add("alpha-numeric-symbol32-space");
		list.add("oracle-alpha-numeric-symbol3");
		return list;
	}

	public static boolean validateCharset(String charsetName) {
		boolean returnValue = false;
		List<String> availableCharsets = getAvailableCharsetNames();
		for (String charset: availableCharsets){
			if (charset.equalsIgnoreCase(charsetName)){
				returnValue = true;
				break;
			}
		}
		return returnValue;
	}
	
	public static void printAvailableCharsetContents(){
		System.out.println("");
		System.out.println("Available charsets: ");
		System.out.println("numeric = ["+NUMERIC+"]");
		System.out.println("alpha = ["+ALPHA+"]");
		System.out.println("alpha-numeric = ["+ALPHA_NUMERIC+"]");
		System.out.println("loweralpha = ["+LOWERALPHA+"]");
		System.out.println("loweralpha-numeric = ["+LOWERALPHA_NUMERIC+"]");
		System.out.println("mixalpha = ["+MIXALPHA+"]");
		System.out.println("mixalpha-numeric = ["+MIXALPHA_NUMERIC+"]");
		System.out.println("ascii-32-95 = ["+ASCII_32_95+"]");
		System.out.println("ascii-32-65-123-4 = ["+ASCII_32_65_123_4+"]");
		System.out.println("alpha-numeric-symbol32-space = ["+ALPHA_NUMERIC_SYMBOL32_SPACE+"]");
		System.out.println("oracle-alpha-numeric-symbol3 = ["+ORACLE_ALPHA_NUMERIC_SYMBOL3+"]");
		System.out.println("");
	}

}
