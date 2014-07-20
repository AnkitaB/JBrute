package controller;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import specialAlgorithm.MyMessageDigest;
import entities.AuxiliaryForDecryptThreads;
import entities.AvailableAlgorithms;
import entities.AvailableCharsets;
import entities.ByteArray;
import entities.DictionaryRule;
import entities.DictionaryRulePreProcessor;
import entities.HashEntry;
import entities.IOProcessor;
import entities.JBruteThread;
import entities.MyBruteDecryptThread;
import entities.MyDictionaryDecryptThread;
import entities.ProcessResult;
import entities.WordList;


public class JBrute {

	/**
	 * @param args
	 */

	public static void main(String[] args){
		int argSize = args.length;

		if (argSize <= 0 ) {
			System.out.println("");
			System.out.println("No arguments passed.");
		}
		else {
			String mainParameter = args[0];
			List<InParameter> optionalParameters = new ArrayList<InParameter>();
			for (int i = 1; i < argSize; i++){
				optionalParameters.add(new InParameter(args[i]));
			}
			if (mainParameter.equalsIgnoreCase("--decrypt")){
				mainDecrypt(optionalParameters);
				return;
			}
			else if (mainParameter.equalsIgnoreCase("--encrypt")){
				mainEncrypt(optionalParameters);
			}
			else if (mainParameter.equalsIgnoreCase("--list_charsets")){
				mainListCharsets();
			}
			else if (mainParameter.equalsIgnoreCase("--help")){
				mainHelp();
			}
			else if (mainParameter.equalsIgnoreCase("--debug")){
				System.out.println("");
				System.out.println("No hay ninguna funcion asignada al parametro --debug.");
				System.out.println("");
			}	
			else if (mainParameter.equalsIgnoreCase("--test")){
				mainTest(optionalParameters);
			}
			else if (mainParameter.equalsIgnoreCase("--optimal_threads")){
				mainOptimalThreads();
			}
			else if (mainParameter.equalsIgnoreCase("--version")){
				mainVersion();
			}
			else if (mainParameter.equalsIgnoreCase("--expected")){
				mainExpected();
			}
			else if (mainParameter.equalsIgnoreCase("--guess")){
				mainGuess(optionalParameters);
			}
			else { 
				System.out.println("");
				System.out.println("Wrong Main Parameter ()"+mainParameter+"!!! Use --help to show available parameters.");
				System.out.println("");
			}
		}
	}
	
	public static void mainGuess(List<InParameter> optionalParameters) {
		String encryptedHash;
		List<String> hashList = new ArrayList<String>();
		String hashFileName;
		boolean hashInputControl1 = false;
		boolean hashInputControl2 = false;
		boolean luckyMode = false;
		for (InParameter parameter: optionalParameters){
			if(parameter.getName().equalsIgnoreCase("--hash")){
				if (parameter.hasValue()){
					encryptedHash = parameter.getValue();
					hashList.add(encryptedHash);
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a hash!");
					System.out.println("");
					return;
				}
				hashInputControl1 = true;
			}
			else if (parameter.getName().equalsIgnoreCase("--hash_file")){
				if (parameter.hasValue()){
					hashFileName = parameter.getValue();
					encryptedHash = "";
					hashList = IOProcessor.getHashesFromHashFile(hashFileName);
					hashInputControl2 = true;
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a hash file name!");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--lucky")){
				luckyMode = true;
			}
			else {
				System.out.println("");
				System.out.println("ERROR!!! unknow parameter: "+parameter.getName());
				System.out.println("");
				return;
			}
		}
		if (hashInputControl1 && hashInputControl2){
			System.out.println("");
			System.err.println("ERROR! You must specify or --hash or --hash_file optional parameters, not both.");
			System.out.println("");
			return;
		}
		if (hashList.size() == 0){
			System.out.println("");
			System.err.println("ERROR! There are no hashes to guess.");
			System.out.println("");
			return;
		}
		System.out.println("");
		if (luckyMode){
			System.out.println("Lucky mode: ON");
		}
		System.out.println("My guess is:");
		List<String> algName = new ArrayList<String>();
		int aux = 0;
		for (String hash: hashList){
			algName = AvailableAlgorithms.guessAlgorithms(hash, luckyMode);
			if (algName.size() == 0){
				System.out.print(hash+" : UNKNOW");
			}
			else {
				System.out.print(hash+" : ");
				for (String name: algName){
					if (aux == 0){
						System.out.print(name);
						aux = 1;
					}
					else {
						System.out.print(","+name);
					}
				}	
				System.out.println("");
			}	
		}
		System.out.println("");
	}

	@SuppressWarnings("unused")
	public static void mainExpected() {
		System.out.println("");
		System.out.println("Printing examples for expected hashes of supported algorithms (no chained)");
		String word = "hola";
		System.out.println("Word to encrypt: "+word);
		System.out.println("");
		byte[] bword = word.getBytes();
		byte[] result = null;
		String resultHash = "";
		String algName = "";
		String auxSalt = "";
		String[] algorithms = AvailableAlgorithms.getAvailableAlgorithms();
		for (int i = 0; i < algorithms.length; i++){
			if (AvailableAlgorithms.isSupportedAlgorithm(i)){
				algName = AvailableAlgorithms.getAlgorithmName(i);
				if (AvailableAlgorithms.isHashedSpecialAlgorithm(i)){
					auxSalt = "username";
				}
				resultHash = encryptWord(word,i,auxSalt);
				System.out.println("Hash "+algName+": "+resultHash);
			}	
			
		}
		System.out.println("");
	}

	public static void mainDecrypt(List<InParameter> optionalParameters) {
		ProcessResult result = new ProcessResult();
		//String resultado = "";
		HashEntry encryptedHash = new HashEntry();
		List<HashEntry> hashList = new ArrayList<HashEntry>();
		//hashList.add("");
		//hashList = null;
		String hashFileName = null;
		String charsetName = null;
		String alphabet = null;
		String salt = null;
		String chainedHashCase = ""; //lower
		String decryptionMethod = "brute";
		String dictionaryFileName = "wordlist.txt";
		String rulesFileName = "rules.txt";
		String algorithmCode = "0";
		String saltType = "utf8";
		boolean hashOption = false;
		boolean preSalt = false;
		boolean stdoutMode = false;
		int from = 0;
		int until = 0;
		int numberOfThreads = 1;
		boolean isDefaultCharset = false;
		for (InParameter parameter: optionalParameters){
			if(parameter.getName().equalsIgnoreCase("--hash")){
				//encryptedHash = parameter.getValue().toLowerCase();
				if (parameter.hasValue()){
					hashOption = true;
					encryptedHash.setHash(parameter.getValue());
					hashList.add(encryptedHash);
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a hash!");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--algorithm")){
				if (parameter.hasValue()){
					algorithmCode = parameter.getValue();
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct algorithm code.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--charset")){
				if (parameter.hasValue()){
					charsetName = parameter.getValue();
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct charset.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--minlength")){
				if (parameter.hasValue()){
					if (isValidWordLengthNumber(parameter.getValue())){
						from = parameter.getNumericValue();
						if (from == Integer.MIN_VALUE){
							System.out.println("");
							System.out.println("ERROR! Invalid from number: "+parameter.getValue());
							System.out.println("");
							return;
						}
					}
					else {
						System.out.println("");
						System.out.println("ERROR! Invalid from number: "+parameter.getValue());
						System.out.println("");
						return;
					}
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct a correct from number.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--maxlength")){
				if (parameter.hasValue()){
					if (isValidWordLengthNumber(parameter.getValue())){
						until = parameter.getNumericValue();
						if (from == Integer.MIN_VALUE){
							System.out.println("");
							System.out.println("ERROR! Invalid until number: "+parameter.getValue());
							System.out.println("");
							return;
						}
					}
					else {
						System.out.println("");
						System.out.println("ERROR! Invalid until number: "+parameter.getValue());
						System.out.println("");
						return;
					}
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct until number.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--hash_file")){
				if (parameter.hasValue()){
					hashFileName = parameter.getValue();
					//hashList = IOProcessor.getHashesFromHashFile(hashFileName);
					hashList = IOProcessor.getLinesFromHashFile(hashFileName);
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a hash file name!!!");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--threads")){
				if (parameter.hasValue()){
					if (isValidStringNumber(parameter.getValue())){
						numberOfThreads = parameter.getNumericValue();
					}
					else {
						System.out.println("");
						System.out.println("ERROR! Invalid threads number: "+parameter.getValue());
						System.out.println("");
						return;
					}
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct thread number.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--presalt")){
				if (parameter.hasValue()){
					salt = parameter.getValue();
					preSalt = true;
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct presalt.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--postsalt")){
				if (parameter.hasValue()){
					salt = parameter.getValue();
					preSalt = false;
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct postsalt.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--salt_type")){
				if (parameter.hasValue()){
					String myAux = parameter.getValue(); 
					if (myAux.equalsIgnoreCase("int64")){
						saltType = myAux;
					}
					else if (myAux.equalsIgnoreCase("hex")){
						saltType = myAux;
					}
					else if (myAux.equalsIgnoreCase("utf8")){
						saltType = myAux;
					}
					else {
						System.out.println("");
						System.err.println("ERROR! You must specify a correct salt type.");
						System.out.println("");
						return;
					}
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct salt type.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--chained_case")){
				if (parameter.hasValue()){
					chainedHashCase = parameter.getValue();
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct chained case.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--method")){
				if (parameter.hasValue()){
					decryptionMethod = parameter.getValue();
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct metod.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--dict_file")){
				if (parameter.hasValue()){
					dictionaryFileName = parameter.getValue();
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct dictionary file name.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--rule_file")){
				if (parameter.hasValue()){
					rulesFileName = parameter.getValue();
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct rules file name.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--stdout")){
				stdoutMode = true;
			}
			else {
				System.out.println("");
				System.out.println("ERROR!!! unknow parameter: "+parameter.getName());
				System.out.println("");
				return;
			}
		}
		// First Validation: you can't use both --hash and --hash_file optional parameters
		if (hashFileName != null && hashOption){
			System.out.println("");
			System.out.println("You can't use --hash and --hash_file parameters together!!!");
			System.out.println("");
			return;
		}
		else {
			if (hashOption){
				if (salt != null ){
					if (preSalt){
						hashList.get(0).setPreSalt(salt);
					}
					else {
						hashList.get(0).setPostSalt(salt);
					}
					hashList.get(0).setSaltType(saltType);
				}
			}
			else {
				if (hashList.size() == 0 && !stdoutMode){
					System.out.println("");
					System.out.println("ERROR!!! You must use --hash or --hash_file parameter!!!");
					System.out.println("");
					return;
				}	
			}
		}
			
		if (!hashOption && hashFileName == null){
			if (!decryptionMethod.equalsIgnoreCase("dictionary") && !stdoutMode){
				// actually you do not need to specify hashes for dictionary decryption with stdout
				System.out.println("");
				System.out.println("You must specify a hash or a hash file!!!");
				System.out.println("");
				return;
			}	
		}
		else {
			System.out.println("");
			for(HashEntry he: hashList){
				result.addEncryptedHash(he);
			}
			hashList.clear();
		}
		if (!AvailableAlgorithms.isSupportedAlgorithm(algorithmCode)){
			//if (!algorithmCode.equalsIgnoreCase("0") || !stdoutMode || !decryptionMethod.equalsIgnoreCase("dictionary")){
			if (!stdoutMode || !decryptionMethod.equalsIgnoreCase("dictionary")){
				if (result.getEncryptedHashes().size() > 0){
					if (algorithmCode.equalsIgnoreCase("0")){
						//System.out.println("");
						System.out.println("Algorithm not selected, I will try to guess it...");
						List<String> algName = AvailableAlgorithms.guessAlgorithms(result.getEncryptedHashes().get(0).getHash(), true);
						if (algName.size() == 0 || algName.size() > 1){
							System.out.println("");
							System.out.println("ERROR! Unsupported algorithm or unknow hash!!!");
							System.out.println("");
							return;
						}
						else if (algName.size() == 1){
							System.out.println("Guessed algorithm to use: "+algName.get(0));
							int algCode = AvailableAlgorithms.getAlgorithmTypeCode(algName.get(0));
							algorithmCode = AvailableAlgorithms.getAlgorithmCodeInput(algCode);
							result.setAlgorithmCodes(algorithmCode);
						}
					}
					else {
						System.out.println("");
						System.out.println("ERROR! Unsupported algorithm!!!");
						System.out.println("");
						return;
					}
				}	
			}
			else {
				// stdout mode
			}
		}
		else {
			if(algorithmCode.length() == 1){
				result.setAlgorithmCodes(algorithmCode);
			}
			else {
				if (isValidChainedAlgorithmDefinition(algorithmCode)){
					result.setAlgorithmCodes(algorithmCode);
				}
				else {
					System.out.println("");
					System.out.println("ERROR! Special algorithms can not be chained : "+algorithmCode);
					System.out.println("");
					return;
				}
			}	
		}
		System.out.println("");
		if (charsetName == null){
			charsetName = "loweralpha";
			alphabet = AvailableCharsets.getCharsetByName(charsetName);
			isDefaultCharset = true;
			//System.out.println("Using "+charsetName+" as charset (default)");
		}
		else if (!AvailableCharsets.validateCharset(charsetName)){
			System.out.println("");
			System.out.println("Invalid charset!");
			System.out.println("You have to specify one of this:");
			AvailableCharsets.printAvailableCharsetNames();
			System.out.println("");
			return;
		}
		else {
			alphabet = AvailableCharsets.getCharsetByName(charsetName);
			System.out.println("Using "+charsetName+" as charset.");
		}
		int processorsCount = Runtime.getRuntime().availableProcessors();

		if (numberOfThreads > alphabet.length()){
			numberOfThreads = alphabet.length();
		}
		if (numberOfThreads == 0){
			numberOfThreads = 1;
		}
		if (numberOfThreads > processorsCount){
			numberOfThreads = processorsCount;
		}
		if ((algorithmCode.length() - chainedHashCase.length()) != 1){
			if ((algorithmCode.length() - chainedHashCase.length()) > 1){
				System.out.println("Bad specification of case, padding with default option (lower).");
				chainedHashCase = "";
				for (int i = 1; i < algorithmCode.length(); i++){
					chainedHashCase = chainedHashCase + "L";
				}
				System.out.println("Using default case for chained algorithms: "+chainedHashCase);
			}
			else {
				System.out.println("");
				System.out.println("ERROR! Invalid length of chained hash case : "+chainedHashCase);
				System.out.println("");
				return;
			}
		}
		else {
				// validate if each case is a valid case
				chainedHashCase = chainedHashCase.toUpperCase();
				if (!isValidChainedCase(chainedHashCase)){
					System.out.println("");
					System.out.println("ERROR! Invalid chained hash case : "+chainedHashCase);
					System.out.println("");
					return;
				}
		}
		if (!decryptionMethod.equalsIgnoreCase("brute") && !decryptionMethod.equalsIgnoreCase("dictionary")){
			System.out.println("");
			System.out.println("ERROR! Invalid decryption method : "+decryptionMethod);
			System.out.println("");
			return;
		}
		result.setChainedAlgorithmCase(chainedHashCase);
		//System.out.println("Hash to decrypt: "+encryptedHash);
		String encryptionAlgorithm = AvailableAlgorithms.getAlgorithmName(algorithmCode);
		System.out.println("Algorithm: "+encryptionAlgorithm);
		System.out.println("Number of cores detected: "+Integer.toString(processorsCount));
		System.out.println("Number of threads: "+numberOfThreads);
		if (salt != null){
			if (preSalt)
				System.out.println("Pre-Salt: "+salt);
			else
				System.out.println("Post-Salt: "+salt);
		}
		
		long start = System.currentTimeMillis();		
		int exitCode = 1;
		System.out.println("Number of hash/es to decrypt: "+result.getEncryptedHashes().size());
		if (decryptionMethod.equalsIgnoreCase("brute")){
			System.out.println("Using brute force decryption method.");
			if (isDefaultCharset)
				System.out.println("Using "+charsetName+" as charset (default).");
			else
				System.out.println("Using "+charsetName+" as charset.");
			
			if (from == 0){
				from = 1;
				System.out.println("Combinations of 1 characters min (default)");
			}
			else {
				System.out.println("Combinations of "+from+" characters min.");
			}
			if (until == 0){
				until = 7;
				System.out.println("Combinations of 7 characters max (default)");
			}
			else {
				System.out.println("Combinations of "+until+" characters max.");
			}
			System.out.println("");
			decryptBruteMethod(from, until, alphabet, result, numberOfThreads);
		}
		else if (decryptionMethod.equalsIgnoreCase("dictionary")){
			if (!dictionaryFileName.equals("")){
				if (IOProcessor.fileExists(dictionaryFileName)){
					System.out.println("Using dictionary decryption method.");
					System.out.println("Dictionary file: "+dictionaryFileName);
					if (stdoutMode){
						System.out.println("Not decrypting, just showing rules result.");
					}
					//System.out.println("");
					List<String> rules = new ArrayList<String>();
					if (!rulesFileName.equals("")){
						if (IOProcessor.fileExists(rulesFileName)){
							System.out.println("Rules file: "+rulesFileName);
							rules = processDictionaryRules(rulesFileName);
						}
						else {
							System.out.println("");
							System.out.println("ERROR! Rules file "+rulesFileName+" does not exists.");
							System.out.println("");
							return;
						}
					}
					if (stdoutMode && numberOfThreads > 1){
						numberOfThreads = 1;
						System.out.println("Using 1 thread for stdout mode.");
					}
					decryptDictionaryMethod(dictionaryFileName, result, numberOfThreads, rules, stdoutMode);
				}
				else {
					System.out.println("");
					System.out.println("ERROR! Dictionary file "+dictionaryFileName+" does not exists.");
					System.out.println("");
					return;
				}
			}
			else {
				System.out.println("");
				System.out.println("Error! You must specify a dictionary file.");
				System.out.println("");
				return;
			}
		}
		System.out.println("");
		if (result.isResolved()){
			System.out.println("All hashes decrypted!!!");
			System.out.println("Results: ");
			for(String elem: result.getDecryptedHashes()){
				System.out.println(elem);
			}
			exitCode = 0;
		}
		else {
			if (result.getDecryptedHashes().size() != 0){
				System.out.println("Some hashes decrypted, but not all...");
				System.out.println("Results: ");
				for(String elem: result.getDecryptedHashes()){
					System.out.println(elem);
				}
			}
			else {
				if (!stdoutMode){
					System.out.println("Can not crack any password, game over...");
				}	
			}	
		}
		System.out.println("");
		System.out.println("Total Seconds Elapsed: "+(System.currentTimeMillis() - start) / 1000);
		System.out.println("");
		System.exit(exitCode);
		
	}

	private static List<String> processDictionaryRules(String rulesFileName) {
		List<String> fileLines = IOProcessor.getRulesFromRuleFile(rulesFileName);
		List<String> returnValue = new ArrayList<String>();
		for (String line: fileLines){
			returnValue.add(line);
		}
		return returnValue;
	}

	public static void mainTest(List<InParameter> optionalParameters) {
		int processorsCount = Runtime.getRuntime().availableProcessors();
		int numberOfThreads = 1; // 1 thread default
		String algorithmCode = "1"; // MD5 default
		String chainedHashCase = ""; // default Lower
		int testTime = 5; //default time
		int numberOfHashes = 1;
		boolean withSalt = false;
		for (InParameter parameter: optionalParameters){
			boolean validParameter = false;
			if(parameter.getName().equalsIgnoreCase("--threads")){
				if (parameter.hasValue()){
					if (isValidStringNumber(parameter.getValue())){
						numberOfThreads = parameter.getNumericValue();
						validParameter = true;
					}
					else {
						System.out.println("");
						System.out.println("ERROR! Invalid threads number: "+parameter.getValue());
						System.out.println("");
						return;
					}
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct thread number.");
					System.out.println("");
					return;
				}
			}
			else if(parameter.getName().equalsIgnoreCase("--algorithm")){
				if (parameter.hasValue()){
					algorithmCode = parameter.getValue();
					validParameter = true;
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct algorithm code.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--chained_case")){
				if (parameter.hasValue()){
					chainedHashCase = parameter.getValue();
					validParameter = true;
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct algorithm code.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--time")){
				if (parameter.hasValue()){
					if (isValidStringNumber(parameter.getValue())){
						testTime = parameter.getNumericValue();
						validParameter = true;
					}
					else {
						System.out.println("");
						System.out.println("ERROR! Invalid time number: "+parameter.getValue());
						System.out.println("");
						return;
					}
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct time number (in seconds).");
					System.out.println("");
					return;
				}
			}
			if(parameter.getName().equalsIgnoreCase("--hashcount")){
				if (parameter.hasValue()){
					if (isValidStringNumber(parameter.getValue())){
						numberOfHashes = parameter.getNumericValue();
						validParameter = true;
					}
					else {
						System.out.println("");
						System.out.println("ERROR! Invalid hash count number: "+parameter.getValue());
						System.out.println("");
						return;
					}
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct hash count number.");
					System.out.println("");
					return;
				}
			}
			if(parameter.getName().equalsIgnoreCase("--salt")){
				withSalt = true;
				validParameter = true;
			}
			//
			if (!validParameter){
				System.out.println("");
				System.out.println("ERROR! unknow parameter: "+parameter.toString());
				System.out.println("");
				return;
			}
		}
		if (numberOfThreads == 0){
			numberOfThreads = 1;
		}
		else if (numberOfThreads > processorsCount){
			numberOfThreads = processorsCount;
		}
		if (!AvailableAlgorithms.isSupportedAlgorithm(algorithmCode)){
			System.out.println("");
			System.out.println("ERROR!!! not supported algorithm.");
			System.out.println("");
			return;
		}
		else {
			if(algorithmCode.length() != 1){
				if (!isValidChainedAlgorithmDefinition(algorithmCode)){
					System.out.println("");
					System.out.println("ERROR! Special algorithms can not be chained : "+algorithmCode);
					System.out.println("");
					return;
				}
			}	
		}
		System.out.println("");
		if ((algorithmCode.length() - chainedHashCase.length()) != 1){
			if ((algorithmCode.length() - chainedHashCase.length()) > 1){
				System.out.println("Bad specification of case, padding with default option (lower).");
				chainedHashCase = "";
				for (int i = 1; i < algorithmCode.length(); i++){
					chainedHashCase = chainedHashCase + "L";
				}
				System.out.println("Using default case for chained algorithms: "+chainedHashCase);
			}
			else {
				System.out.println("");
				System.out.println("ERROR! Invalid length of chained hash case : "+chainedHashCase);
				System.out.println("");
				return;
			}
		}
		else {
				// validate if each case is a valid case
				chainedHashCase = chainedHashCase.toUpperCase();
				if (!isValidChainedCase(chainedHashCase)){
					System.out.println("");
					System.out.println("ERROR! Invalid chained hash case : "+chainedHashCase);
					System.out.println("");
					return;
				}
		}
		//result.setChainedAlgorithmCase(chainedHashCase);
		System.out.println("Using "+numberOfHashes+" hash/es.");
		System.out.println("Using random salt: "+withSalt);
		testPerformance(numberOfThreads, algorithmCode, chainedHashCase, testTime, numberOfHashes, withSalt);
	}

	public static void mainEncrypt(List<InParameter> optionalParameters) {
		String word = "";
		String algorithmCode = "0";
		String salt = "";
		String chainedHashCase = ""; //lower
		String saltType = "utf8"; //default
		boolean preSalt = false;
		boolean upperCase = false;
		boolean printBase64 = false;
		byte[] saltByte = null;
		for (InParameter parameter: optionalParameters){
			if(parameter.getName().equalsIgnoreCase("--word")){
				if (parameter.hasValue()){
					word = parameter.getValue();
				}
				else {
					System.out.println("");
					System.out.println("ERROR!!! You must specify a correct word.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--chained_case")){
				if (parameter.hasValue()){
					chainedHashCase = parameter.getValue();
				}
				else {
					System.out.println("");
					System.out.println("ERROR!!! You must specify a correct chained case.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--algorithm")){
				if (parameter.hasValue()){
					algorithmCode = parameter.getValue();
				}
				else {
					System.out.println("");
					System.out.println("ERROR!!! You must specify a correct algorithm code.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--presalt")){
				if (parameter.hasValue()){
					salt = parameter.getValue();
					preSalt = true;
				}
				else {
					System.out.println("");
					System.out.println("ERROR!!! You must specify a correct presalt.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--postsalt")){
				if (parameter.hasValue()){
					salt = parameter.getValue();
					preSalt = false;
				}
				else {
					System.out.println("");
					System.out.println("ERROR!!! You must specify a correct postsalt.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--salt_type")){
				if (parameter.hasValue()){
					String myAux = parameter.getValue(); 
					if (myAux.equalsIgnoreCase("int64")){
						saltType = myAux;
					}
					else if (myAux.equalsIgnoreCase("hex")){
						saltType = myAux;
					}
					else if (myAux.equalsIgnoreCase("utf8")){
						saltType = myAux;
					}
					else {
						System.out.println("");
						System.err.println("ERROR! You must specify a correct salt type.");
						System.out.println("");
						return;
					}
				}
				else {
					System.out.println("");
					System.err.println("ERROR! You must specify a correct salt type.");
					System.out.println("");
					return;
				}
			}
			else if (parameter.getName().equalsIgnoreCase("--upper")){
				upperCase = true;
			}
			else if (parameter.getName().equalsIgnoreCase("--base64")){
				printBase64 = true;
			}
			else {
				System.out.println("");
				System.out.println("ERROR!!! unknow parameter: "+parameter.getName());
				System.out.println("");
				return;
			}
		}
		if (!AvailableAlgorithms.isSupportedAlgorithm(algorithmCode)){
				System.out.println("");
				System.out.println("ERROR! Unsupported algorithm!!!");
				System.out.println("");
				return;
		}
		else {
			if(algorithmCode.length() == 1){
				//result.setAlgorithmCodes(algorithmCode);
			}
			else {
				if (isValidChainedAlgorithmDefinition(algorithmCode)){
					//result.setAlgorithmCodes(algorithmCode);
				}
				else {
					System.out.println("");
					System.out.println("ERROR! Special algorithms can not be chained : "+algorithmCode);
					System.out.println("");
					return;
				}
			}	
		}
		if ((algorithmCode.length() - chainedHashCase.length()) != 1){
			if ((algorithmCode.length() - chainedHashCase.length()) > 1){
				System.out.println("Bad specification of case, padding with default option (lower).");
				chainedHashCase = "";
				for (int i = 1; i < algorithmCode.length(); i++){
					chainedHashCase = chainedHashCase + "L";
				}
				System.out.println("Using default case for chained algorithms: "+chainedHashCase);
			}
			else {
				System.out.println("");
				System.out.println("ERROR! Invalid length of chained hash case : "+chainedHashCase);
				System.out.println("");
				return;
			}
		}
		else {
				// validate if each case is a valid case
				chainedHashCase = chainedHashCase.toUpperCase();
				if (!isValidChainedCase(chainedHashCase)){
					System.out.println("");
					System.out.println("ERROR! Invalid chained hash case : "+chainedHashCase);
					System.out.println("");
					return;
				}
		}
		System.out.println("");
		if (word.equals("")){
			System.out.println("ERROR! You must specify a word!!!");
		}
		else {
			// process salt type
			if (!salt.equals("")){
				saltByte = AvailableAlgorithms.getSaltByteArray(salt, saltType);
				if (saltByte == null){
					System.out.println("");
					System.out.println("ERROR! Invalid salt type value "+salt+" for salt type "+saltType);
					System.out.println("");
					return;
				}
			}	
			encryptPlaintext(word, algorithmCode, chainedHashCase, salt, saltByte, saltType, preSalt, upperCase, printBase64);
		}
		System.out.println("");
		
	}

	public static void mainOptimalThreads() {
		// voy a validar por fuerza bruta todos los hashes de longitud 6
		int processorsCount = Runtime.getRuntime().availableProcessors();
		//String alphabet = AvailableCharsets.getCharsetByName("loweralpha-numeric");
		System.out.println("");
		System.out.println("Number of cores detected: "+Integer.toString(processorsCount));
		System.out.println("Testing performance for MD5 for 1 thread to "+processorsCount+" threads...");
		int acum = 0;
		int bestThreadsNumber = 0;
		long start = System.currentTimeMillis();
		List<MyBruteDecryptThread> listaThreads = new ArrayList<MyBruteDecryptThread>();
		ProcessResult pr = new ProcessResult();
		for (int j = 1; j <= processorsCount; j++){
			int numberOfThreads = j;
			System.out.print("Estimating performance for "+numberOfThreads+" thread/s...");
			// test MD5 performance
			pr.setAlgorithmCodes("1");
			for (int i = 1; i <= numberOfThreads; i++){
				listaThreads.add(new MyBruteDecryptThread(true,pr,1,false));
			}
			for (MyBruteDecryptThread thread: listaThreads){
				thread.start();
			}
			for (MyBruteDecryptThread thread: listaThreads){
				try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			System.out.println(" "+pr.getTryesCount()/10+" hashes per second.");
			if (acum <= pr.getTryesCount()/10){
				acum = pr.getTryesCount()/10;
				bestThreadsNumber = numberOfThreads;
			}
			listaThreads.clear();
			pr.setTestCount(0);
		}
		System.out.println("");
		System.out.println("Best threads number: "+bestThreadsNumber);
		System.out.println("You could compute "+acum+ " MD5 hashes/sec approx.");
		System.out.println("");
		int segundosProcesamiento = (int) (System.currentTimeMillis() - start) / 1000;
		if (segundosProcesamiento == 0)
			segundosProcesamiento = 1;
		System.out.println("Total time elapsed: "+segundosProcesamiento+" seconds.");
		System.out.println("");
	}

	public static void mainHelp() {
		System.out.println("");
		System.out.println("Available parameters:");
		System.out.println("	--decrypt: decrypt a hash.");
		System.out.println("		--algorithm=<CODE>: specify the code of the algoritm to use (multiple codes accepted).");
		System.out.println("			Available codes:");
		System.out.println("				1: MD5");
		System.out.println("				2: MD4");
		System.out.println("				5: SHA-256");
		System.out.println("				6: SHA-512");
		System.out.println("				8: MD5CRYPT");
		System.out.println("				9: SHA-1");
		System.out.println("				A: ORACLE-10G");
		System.out.println("				B: ORACLE-11G");
		System.out.println("				C: NTLM");
		System.out.println("				D: LM");
		System.out.println("				E: MSSQL-2000");
		System.out.println("				F: MSSQL-2005");
		System.out.println("				G: MSSQL-2012");
		System.out.println("				H: MYSQL-322");
		System.out.println("				I: MYSQL-411");
		System.out.println("				J: POSTGRESQL");
		System.out.println("				K: SYBASE-ASE1502");
		System.out.println("				L: INFORMIX-1170");
		System.out.println("				            ");
		System.out.println("		   Multiple codes accepted: combinations of codes 1, 2, 5, 6, 9.");
		System.out.println("		--chained_case=<STRING>: binary, lower case or upper case for chained hashing (multiple values accepted)");
		System.out.println("			accepted values: [R|L|U] (Raw, Lower or Upper, default L)");
		System.out.println("		--charset=<CHARSET_NAME>: specify an available charset (default loweralpha)");
		System.out.println("		--dict_file=<FILE_NAME>: specify the file name of the dictionary to use with --method=dictionary (default wordlist.txt)");
		System.out.println("		--hash or --hash_file=<FILE_NAME>: specify one hash or the name of a file containing hashes.");
		System.out.println("		--maxlength=<NUMBER>: max password length (default 7)");
		System.out.println("		--method=<STRING>: 'brute' or 'dictionary' (default brute)");
		System.out.println("		--minlength=<NUMBER>: min password length (default 1)");
		System.out.println("		--postsalt=<STRING>: specify a post-salt to use only for no-special algorithms (default empty)");
		System.out.println("		--presalt=<STRING>: specify a pre-salt to use only for no-special algorithms (default empty)");
		System.out.println("		--rule_file=<FILE_NAME>: specify the file name of the rule's file to use with --method=dictionary (default rules.txt)");
		System.out.println("			Available rules: similar to in John the Ripper (masking partially supported).");
		System.out.println("		--salt_type=<STRING>: specify salt type.");
		System.out.println("			accepted values: [hex|int64|utf8] (default utf8)");
		System.out.println("		--stdout: show rules application for --method=dictionary (default false)");
		System.out.println("		--threads=<NUMBER>: number of threads to use (default 1)");
		System.out.println("	--encrypt: encrypt a word.");
		System.out.println("		--algorithm=<CODE>: specify the code of the algoritm to use (multiple codes accepted, default 1).");
		System.out.println("			Available codes: same ones as --decrypt --algorithm option.");
		System.out.println("		--base64: specify the final hash in base64 too.");
		System.out.println("		--chained_case=<STRING>: binary, lower case or upper case for chained hashing (multiple values accepted)");
		System.out.println("			accepted values: [R|L|U] (Raw, Lower or Upper, default L)");
		System.out.println("		--presalt=<STRING>: specify a pre-salt to use only for no-special algorithms (default empty)");
		System.out.println("		--postsalt=<STRING>: specify a post-salt to use only for no-special algorithms (default empty)");
		System.out.println("		--salt_type=<STRING>: specify salt type.");
		System.out.println("			accepted values: [hex|int64|utf8] (default utf8)");
		System.out.println("		--upper: specify the final hash in uppercase.");
		System.out.println("		--word=<STRING>: specify a word to encrypt");
		System.out.println("	--expected: print hash example for each supported algorithm.");
		System.out.println("	--guess: try to identify the algorithm of a hash (can return multiple algorithms).");
		System.out.println("		--hash=<HASH> or --hash_file=<FILE_NAME>: specify one hash or the name of a file containing hashes.");
		System.out.println("		--lucky: determinate the most probably algorithm for the hash (just one).");
		System.out.println("	--list_charsets: print available charsets.");
		//System.out.println("	--optimal_threads: find optimal threads number for your hardware.");
		System.out.println("	--test: estimate number of hashes that you could process with your actual hardware.");
		System.out.println("		--algorithm=<CODE>: specify the code of the algoritm to use (multiple codes accepted, default 1).");
		System.out.println("			Available codes: same ones as --decrypt --algorithm option.");
		System.out.println("		--chained_case=<STRING>: binary, lower case or upper case for chained hashing (multiple values accepted)");
		System.out.println("			accepted values: [R|L|U] (Raw, Lower or Upper, default L)");
		System.out.println("		--hashcount=<NUMBER>: number of hashes to use (default 1)");
		System.out.println("		--time=<NUMBER>: number of seconds to use (default 5)");
		System.out.println("		--threads=<NUMBER>: number of threads to use (default 1)");
		System.out.println("		--salt: use a random salt for each hash (default false)");
		System.out.println("	--version: print current version");
		//System.out.println("	--debug:");
		System.out.println("");
	}

	public static void mainListCharsets() {
		AvailableCharsets.printAvailableCharsetContents();
	}

	public static void mainVersion() {
		System.out.println("");
		System.out.println("Current Version : 0.99 (beta)");
		System.out.println("Supported JRE   : 1.7.0_21+");
		System.out.println("Release Date    : 09/11/2013");
		System.out.println("Autor           : Gonzalo L. Camino (gonzalocamino@gmail.com)");
		System.out.println("");
	}

	/*
	 * End of main functions
	 */
	/*
	private static String next_original(String input) {
	    int length = input.length();
	    char c = input.charAt(length - 1);
	
	    if(c == 'z') 
		    return length > 1 ? next_original(input.substring(0, length - 1)) + 'a' : "aa";
	    
	    return input.substring(0, length - 1) + ++c;
	    
	}
	*/
	private static void encryptPlaintext(String plaintext, String algorithmCodes, String chainedAlgorithmCase, String salt, byte[] saltByte, String saltType, boolean preSalt, boolean upperCase, boolean printBase64){
		byte[] bword = plaintext.getBytes();
		byte[] resultSalt = bword;
		
		System.out.println("Word to hash: "+plaintext);
		String finalHash = "";
		String algorithmName = AvailableAlgorithms.getAlgorithmName(algorithmCodes);
		if (AvailableAlgorithms.isNativeAlgorithm(algorithmCodes)){
			if (saltByte != null){
				if (preSalt){
					resultSalt = ByteArray.concat(saltByte, bword);
					System.out.println("Pre-salt used: "+salt);
				}
				else { //postsalt
					resultSalt = ByteArray.concat(bword,saltByte);
					System.out.println("Post-salt used: "+salt);	
				}
				System.out.println("Salt type: "+saltType);
			}
			MessageDigest[] ma = AvailableAlgorithms.getMessageDigestArray(algorithmCodes);
			/*
			 * I need to reverse the chainedAlgorithmCase, because ma has the algorithms reversed
			 * as you can see in AvailableAlgorithms.getMessageDigestArray();
			 */
			chainedAlgorithmCase = new StringBuilder(chainedAlgorithmCase.toUpperCase()).reverse().toString();
			boolean anError = false;
			for (int i = 0; i < ma.length; i++){
				if (ma[i] == null) {
					anError = true;
				}
			}
			if (ma == null || anError){
				System.out.println("");
				System.out.println("ERROR! Algorithm not supported here.");
				System.out.println("");
				return;
			}
			
			byte[] aux = null;
			for (int i = 0; i < ma.length; i++){
				ma[i].update(resultSalt,0,resultSalt.length);
				aux = ma[i].digest();
				if (ma.length -1 != i)
					resultSalt = AuxiliaryForDecryptThreads.getIntermediateChainedHash(aux, chainedAlgorithmCase, i);
			}
			//
			finalHash = AvailableAlgorithms.getLowerCaseHash(aux);
			if (!chainedAlgorithmCase.equalsIgnoreCase("")){
				System.out.println("Chained case: "+chainedAlgorithmCase);
			}
		}
		else {
			if (algorithmCodes.length() == 1){//just in case...
				int algorithmCode = AvailableAlgorithms.getAlgorithmCodeFromInput(algorithmCodes);
				MyMessageDigest m = AvailableAlgorithms.getMyMessageDigest(algorithmCode);
				if (m == null){
					System.out.println("");
					System.out.println("ERROR! Invalid special algorithm!!!");
					System.out.println("");
				}
				else {
					//m.update(bword);
					if (AvailableAlgorithms.isUsernameSaltedHash(algorithmCode)){
						if (!saltByte.equals("")){
							System.out.println("Username (salt) used: "+salt);
							finalHash = m.crypt(plaintext, salt);
						}
						else {
							System.err.println("");
							System.err.println("ERROR! Algorithm "+algorithmName+" is user-salted. You must specify a salt (pre or post is the same).");
							System.err.println("");
							return;
						}
					}
					else {
						finalHash = m.crypt(plaintext, null);
					}	
				}
			}
		}
		
		if (upperCase){
			finalHash = finalHash.toUpperCase();
		}
		System.out.println("Hash "+algorithmName+" : "+finalHash);
		String finalHash2 = "";
		if (printBase64){
			byte[] aux = DatatypeConverter.parseHexBinary(finalHash);
			finalHash2 = DatatypeConverter.printBase64Binary(aux);
			System.out.println("Base64 "+algorithmName+" : "+finalHash2);
		}
	}
	
	private static String encryptWord(String word, int algorithmCode, String auxSalt){
		byte[] bword = word.getBytes();
		String resultHash;
		byte[] result = null;
		if (AvailableAlgorithms.isNativeAlgorithm(algorithmCode)){
			MessageDigest m = AvailableAlgorithms.getMessageDigest(algorithmCode);
			m.update(bword);
			result = m.digest();
			resultHash = DatatypeConverter.printHexBinary(result);
		}
		else {
			MyMessageDigest m = AvailableAlgorithms.getMyMessageDigest(algorithmCode);
			if (!AvailableAlgorithms.isUsernameSaltedHash(algorithmCode)){
				auxSalt = null;
			}
			resultHash = m.crypt(word, auxSalt);
		}
		
		return resultHash;
	}
	
	private static StringBuilder nextCombinationCustom(String alphabet, StringBuilder input) {
		StringBuilder returnValue = new StringBuilder();
		int inputLength = input.length();
		int alphabetLength = alphabet.length();
		char c = input.charAt(inputLength - 1);
		char nextChar = 0;

		if(c == alphabet.charAt(alphabetLength - 1)) {
			char firstAlphabetCharacter = alphabet.charAt(0);
			if (inputLength == 1) {
				returnValue = new StringBuilder();
				returnValue.append(firstAlphabetCharacter);
				returnValue.append(firstAlphabetCharacter);
			}	
			else {
				input.deleteCharAt(inputLength - 1);
				returnValue = nextCombinationCustom(alphabet,input).append(firstAlphabetCharacter);
			}	
		}		
		else {
			for (int i = 0; i < alphabetLength; i++){
				if (alphabet.charAt(i) == c){
					nextChar = alphabet.charAt(i+1);
					break;
				}
			}
			input.setCharAt(inputLength - 1,nextChar);
			returnValue = input;
		}	
		return returnValue;
	}
	
	private static void decryptBruteMethod(int longDesde, int longHasta, String alphabet, 
			ProcessResult pr, int numberOfThreads){
		StringBuilder firstCombination = new StringBuilder("");
		StringBuilder lastCombination = new StringBuilder("");
		long start = System.currentTimeMillis();
		//List<MyBruteDecryptThread> myThreads = new ArrayList<MyBruteDecryptThread>();
		List<JBruteThread> myThreads = new ArrayList<JBruteThread>();
		
		// first I need to validate all hashes to decrypt
		pr.validateEncryptedHashes();
		if (pr.getPendingEncryptedHashes().size() == 0){
			System.err.println();
			System.err.println("There are no valid hashes to decrypt!!!");
			System.err.println();
			return;
		}

		for(int i = longDesde; i <= longHasta; i++){
			List<StringBuilder> allWords = generateAllWords(alphabet,i,numberOfThreads);
			System.out.println("Starting with combinations of "+Integer.toString(i)+" characters...");
			for(int j = 0; j < numberOfThreads; j++){
				if (j == 0){
					firstCombination = allWords.get(j);
				}
				else {
					firstCombination = nextCombinationCustom(alphabet,allWords.get(j));
				}	
				
				lastCombination = allWords.get(j+1);
				/*
				System.out.println("");
				System.out.println("	Thread "+j+":");
				System.out.println("	firstCombination: "+firstCombination);
				System.out.println("	lastCombination: "+lastCombination);
				*/
				myThreads.add(new MyBruteDecryptThread("Thread "+j,alphabet,firstCombination.toString(),lastCombination.toString(),pr));
			}
			pr.setThreads(myThreads);
			
			for(JBruteThread thread: myThreads){
				thread.setPriority(Thread.MAX_PRIORITY);
				thread.start();
			}
			for(JBruteThread thread: myThreads){
				try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			myThreads.clear();
			if (pr.isResolved()){
				System.out.println("");
				break;
			}
			System.out.println("Done. (Time elapsed (sec): "+(System.currentTimeMillis() - start) / 1000+")");
			
		}
	}
	
	private static void decryptDictionaryMethod(String dictionaryFileName, ProcessResult pr,
			int numberOfThreads, List<String> ruleLines, boolean stdoutMode) {
		//
		// first I need to validate all hashes to decrypt
		pr.validateEncryptedHashes();
		if (pr.getPendingEncryptedHashes().size() == 0 && !stdoutMode){
			System.err.println();
			System.err.println("There are no valid hashes to decrypt!!!");
			System.err.println();
			return;
		}
		System.out.print("Reading words from dictionary... ");
		List<StringBuilder> words = IOProcessor.getStringBuilderLinesFromFile(dictionaryFileName);
		List<WordList> threadWords = getWordsForEachThread(words, numberOfThreads);
		System.out.println(words.size() + " words readed.");
		// por ahora uso un solo thread
		List<JBruteThread> myThreads = new ArrayList<JBruteThread>();
		boolean printRulesLoaded = true;
		//List<DictionaryRule> rules = DictionaryRulePreProcessor.getInstance().getRulesFromRuleLines(ruleLines);
		for (int i = 0; i < numberOfThreads; i++){
			List<DictionaryRule> rules = DictionaryRulePreProcessor.getInstance().getRulesFromRuleLines(ruleLines, printRulesLoaded);
			MyDictionaryDecryptThread myThread = new MyDictionaryDecryptThread("Thread "+i, pr, threadWords.get(i).getList(), rules, stdoutMode);
			printRulesLoaded = false;
			myThread.setPriority(Thread.MAX_PRIORITY);
			myThreads.add(myThread);
			pr.setThreads(myThreads);
		}
		System.out.println("");
		System.out.println("Starting decryption process (method: dictionary)");
		long start = System.currentTimeMillis();
		for(JBruteThread thread: myThreads){
			thread.setPriority(Thread.MAX_PRIORITY);
			thread.start();
		}
		for(JBruteThread thread: myThreads){
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		long totalSeconds = (System.currentTimeMillis() - start) / 1000;
		System.out.println("Decryption process finished (seconds elapsed: "+totalSeconds+")");
		myThreads.clear();
		if (pr.isResolved() && !stdoutMode){
			System.out.println("");
		}
	}
	
	private static List<WordList> getWordsForEachThread(
			List<StringBuilder> words, int numberOfThreads) {
		int numberOfWords = words.size() / numberOfThreads;
		List<WordList> returnValue = new ArrayList<WordList>();
		int start = 0;
		int end = numberOfWords;
		for (int i = 0; i < numberOfThreads; i++){
			WordList aux = new WordList();
			for (int j = start; j < end; j++){
				aux.addWord(words.get(j));
			}
			returnValue.add(aux);
			start = end;
			end = end + numberOfWords;
			if ((words.size() - end) < numberOfWords){
				end = words.size();
			}
		}
		return returnValue;
	}

	private static List<StringBuilder> generateAllWords(String alphabet, int i, int numberOfThreads) {
		List<StringBuilder> words = new ArrayList<StringBuilder>();
		// first word
		StringBuilder aux = new StringBuilder("");
		aux.append(alphabet.charAt(0));
		aux = fillWithFirstCharOfAlphabet(alphabet, aux, i);
		words.add(aux);
		// middle words
		if (numberOfThreads > 1){
			for (int j = 1; j < numberOfThreads; j++){
				aux = new StringBuilder("");
				aux.append(alphabet.charAt((alphabet.length()/numberOfThreads*j)-1));
				aux = fillWithLastCharOfAlphabet(alphabet, aux, i);
				words.add(aux);
			}
		}
		// last word
		aux = new StringBuilder("");
		aux.append(alphabet.charAt(alphabet.length()-1));
		aux = fillWithLastCharOfAlphabet(alphabet, aux, i);
		words.add(aux);
		return words;
	}

	private static StringBuilder fillWithFirstCharOfAlphabet(String alphabet, StringBuilder sb, int i){
		StringBuilder returnValue = new StringBuilder("");
		returnValue.append(sb.toString());
		for (int j = 1; j < i; j++){
			returnValue.append(alphabet.charAt(0));
		}
		return returnValue;
	}
	
	private static StringBuilder fillWithLastCharOfAlphabet(String alphabet, StringBuilder sb, int i){
		StringBuilder returnValue = new StringBuilder("");
		returnValue.append(sb.toString());
		for (int j = 1; j < i; j++){
			returnValue.append(alphabet.charAt(alphabet.length()-1));
		}
		return returnValue;
	}

	private static void testPerformance(int numberOfThreads, String algorithmCode, String chainedHashCase, int testTime, int numberOfHashes, boolean withSalt){
		//System.out.println("");
		int processorsCount = Runtime.getRuntime().availableProcessors();
		System.out.println("Number of cores detected: "+Integer.toString(processorsCount));
		System.out.println("Number of threads to use: "+numberOfThreads);
		System.out.println("");
		ProcessResult pr = new ProcessResult();
		pr.setAlgorithmCodes(algorithmCode);
		pr.setChainedAlgorithmCase(chainedHashCase);
		pr.setTestTime(testTime);
		List<MyBruteDecryptThread> listaThreads = new ArrayList<MyBruteDecryptThread>();
		String algorithmName = AvailableAlgorithms.getAlgorithmName(algorithmCode);
		System.out.println("Benchmarking "+algorithmName+" ...");
		for (int i = 0; i < numberOfThreads; i++){
			listaThreads.add(new MyBruteDecryptThread(true,pr,numberOfHashes,withSalt));
		}
		for (MyBruteDecryptThread thread: listaThreads){
			thread.start();
		}
		for (MyBruteDecryptThread thread: listaThreads){
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		listaThreads.clear();
		System.out.println("You could compute "+pr.getTestResult()+ " "+algorithmName+" words/sec approx.");
		pr.setTestCount(0);
		System.out.println("");	
	}
	
	private static boolean isValidStringNumber(String number){
		boolean returnValue = true;
		OUTERLOOP:
		for (int i = 0; i < number.length(); i++){
			if (!Character.isDigit(number.charAt(i))){
				returnValue = false;
				break OUTERLOOP;
			}
		}
		return returnValue;
	}
	
	private static boolean isValidWordLengthNumber(String number){
		return isValidStringNumber(number);
	}
	
	private static boolean isValidChainedCase(String value){
		boolean returnValue = true;
		OUTERLOOP:
		for (int i = 0; i < value.length(); i++){
			char c = value.charAt(i);
			if (c != 'L' && c!= 'U' && c != 'R'){
				returnValue = false;
				break OUTERLOOP;
			}
		}
		
		return returnValue;
	}
	
	private static boolean isValidChainedAlgorithmDefinition(String algorithmCodes){
		
		boolean returnValue = true;
		String algorithm = "";
		OUTERLOOP:
		for (int i = 0; i < algorithmCodes.length(); i++){
			char c = algorithmCodes.charAt(i);
			algorithm = algorithm + c;
			int algorithmCode = AvailableAlgorithms.getAlgorithmCodeFromInput(algorithm);
			if (AvailableAlgorithms.isHashedSpecialAlgorithm(algorithmCode)){
				returnValue = false;
				break OUTERLOOP;
			}
			algorithm = "";	
		}
		
		return returnValue;
	}
	
}
