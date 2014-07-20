package entities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class IOProcessor {
	private static final String HASHFILE_COMMENT = "#!comment:";
	private static final String RULEFILE_COMMENT = "#";
	private static final String DECRYPTION_BOX_FILE_NAME = ".pandoras.box";
	
	public final static List<String> getLinesFromFile(String fileName, String comment, boolean ignoreEmpyLines){
		List<String> lineList = new ArrayList<String>();
		BufferedReader br = null;

		try {
			String sCurrentLine;
			File FileToRead = new File(fileName);
			String completeFileName = getCompleteFileName(fileName);
			
			if(FileToRead.isFile()){
				br = new BufferedReader(new FileReader(completeFileName));
				while ((sCurrentLine = br.readLine()) != null) {
					sCurrentLine = sCurrentLine.replaceAll("(\\r|\\n)", "");
					if (!sCurrentLine.startsWith(comment)){// ignore comments
						if ((ignoreEmpyLines) && (!sCurrentLine.equals(""))){// ignore empty lines
							lineList.add(sCurrentLine);
						}	
					}	
				}
  			}
  			else {
  				System.out.println("The file "+completeFileName+ " does not exists!");
  			}
		
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return lineList;
	}
	
	public final static List<HashEntry> getLinesFromHashFile(String fileName){
		List<HashEntry> hashList = new ArrayList<HashEntry>();
		BufferedReader br = null;

		try {
			String sCurrentLine;
			File FileToRead = new File(fileName);
			String completeFileName = getCompleteFileName(fileName);
			
			if(FileToRead.isFile()){
				br = new BufferedReader(new FileReader(completeFileName));
				while ((sCurrentLine = br.readLine()) != null) {
					sCurrentLine = sCurrentLine.replaceAll("(\\r|\\n)", "");
					HashEntry he = new HashEntry(sCurrentLine);
					hashList.add(he);
				}
  			}
  			else {
  				System.out.println("The file "+completeFileName+ " does not exists!");
  			}
		
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return hashList;
	}

	public static boolean fileExists(String fileName) {
		File FileToRead = new File(fileName);
		String cwd = new File(".").getAbsolutePath();
		return FileToRead.exists();
	}
	
	public final static List<StringBuilder> getStringBuilderLinesFromFile(String fileName, String comment){
		List<StringBuilder> lineList = new ArrayList<StringBuilder>();
		BufferedReader br = null;
		
		try {
			String sCurrentLine;
			File FileToRead = new File(fileName);
			String completeFileName = getCompleteFileName(fileName);
			
			if(FileToRead.isFile()){
				br = new BufferedReader(new FileReader(completeFileName));
				while ((sCurrentLine = br.readLine()) != null) {
					sCurrentLine = sCurrentLine.replaceAll("(\\r|\\n)", "");
					if (!sCurrentLine.startsWith(comment)){
						// ignore comments
						lineList.add(new StringBuilder(sCurrentLine));
					}	
				}
  			}
  			else {
  				System.out.println("The file "+completeFileName+ " does not exists!");
  			}
		
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return lineList;
	}
	
	public static final List<String> getHashesFromHashFile(String fileName){
		return getLinesFromFile(fileName, HASHFILE_COMMENT, false);
	}
	
	public static final List<StringBuilder> getStringBuilderLinesFromFile(String fileName){
		return getStringBuilderLinesFromFile(fileName, HASHFILE_COMMENT);
	}
	
	public static final List<String> getRulesFromRuleFile(String fileName){
		return getLinesFromFile(fileName, RULEFILE_COMMENT, true);
	}

	public static List<HashEntry> getHashEntrysFromTheBox() {
		List<HashEntry> hashEntryList = new ArrayList<HashEntry>();
		BufferedReader br = null;
		String fileName = DECRYPTION_BOX_FILE_NAME;

		try {
			String sCurrentLine;
			File FileToRead = new File(fileName);
			String completeFileName = getCompleteFileName(fileName);
			
			if(FileToRead.isFile()){
				br = new BufferedReader(new FileReader(completeFileName));
				while ((sCurrentLine = br.readLine()) != null) {
					sCurrentLine = sCurrentLine.replaceAll("(\\r|\\n)", "");
					int firstSpace = sCurrentLine.indexOf(' ');
					String hashEntrStr = sCurrentLine.substring(0, firstSpace);
					hashEntryList.add(new HashEntry(hashEntrStr));	
				}
  			}
  			else {
  				//System.out.println("[INFO] The file "+completeFileName+ " does not exists!");
  			}
		
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return hashEntryList;
	}
	
	public static List<String> getLinesFromTheBox() {
		List<String> list = new ArrayList<String>();
		BufferedReader br = null;
		String fileName = DECRYPTION_BOX_FILE_NAME;

		try {
			String sCurrentLine;
			File FileToRead = new File(fileName);
			String completeFileName = getCompleteFileName(fileName);
			
			if(FileToRead.isFile()){
				br = new BufferedReader(new FileReader(completeFileName));
				while ((sCurrentLine = br.readLine()) != null) {
					sCurrentLine = sCurrentLine.replaceAll("(\\r|\\n)", "");
					list.add(sCurrentLine);	
				}
  			}
  			else {
  				//System.out.println("[INFO] The file "+completeFileName+ " does not exists!");
  			}
		
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return list;
	}
	
	public static boolean isHashInTheBox(HashEntry newHash) {
		boolean isInBox = false;
		
		List<HashEntry> boxHashes = IOProcessor.getHashEntrysFromTheBox();
		OUTERMOST:
		for (HashEntry decryptedHash: boxHashes){
			if (decryptedHash.toString().equals(newHash.toString())){
				isInBox = true;
				break OUTERMOST;
			}
		}
		
		return isInBox;
	}
	
	public synchronized static final  void addHashEntryToTheBox(HashEntry hashEntry, String word) {
		String fileName = getCompleteFileName(DECRYPTION_BOX_FILE_NAME);
		File FileToRead = new File(fileName);
		boolean mustCreatesBox = false;
		if(!FileToRead.isFile()){
			try {
			      File file = new File(fileName);
			      if (file.createNewFile()){
			        System.out.println("File "+fileName+" is created!");
			        mustCreatesBox = true;
			      }else{
			        System.out.println("File "+fileName+" already exists.");
			      }
		 
		    	} catch (IOException e) {
			      e.printStackTrace();
			}
		}
		boolean isInBox = false;
		if (!mustCreatesBox){
			isInBox = isHashInTheBox(hashEntry);
		}
		if (!isInBox){
			try {
				String line = hashEntry.toString() + " --> " + word;
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, true)));
				out.println(line);
				out.close();
			} catch (IOException e) {
				//oh noes!
				System.err.println("[DEBUG] ERROR! Can not write to decryption file.");
				e.printStackTrace();
			}
		}
		else {
			System.out.println("[INFO] entry "+hashEntry.toString()+" already in the box!");
		}
	}
	
	private static final String getCompleteFileName(String fileName){
		String your_os = System.getProperty("os.name").toLowerCase();
		String workingDir = System.getProperty("user.dir");
		File FileToRead = new File(fileName);
		String completeFileName = "";
		//System.out.println("Your OS: "+your_os);
		if (FileToRead.getParent() == null){
			if(your_os.contains("windows")){
				completeFileName = workingDir + "\\" + fileName;
			}else if(your_os.contains("linux") 
					|| your_os.contains("nux")
					|| your_os.contains("os x"))
			{
				completeFileName = workingDir + "/" + fileName;	
			}else{
				System.out.println("Unknow OS: "+your_os);
				completeFileName = workingDir + "{unknowOS}" + fileName;
			}
		}
		else {
			completeFileName = fileName;
		}
		
		return completeFileName;
	}

	public static String getWordFromHashEntry(HashEntry hash) {
		String word = "";
		List<String> resolved = getLinesFromTheBox();
		OUTERMOST:
		for (String line: resolved){
			int firstSpace = line.indexOf(' ');
			String he = line.substring(0, firstSpace);
			if (he.equalsIgnoreCase(hash.toString())){
				int lastSpace = line.lastIndexOf(' ');
				word = line.substring(lastSpace + 1);
				break OUTERMOST;
			}
		}
		return word;
	}
}
