package entities;

import java.util.List;

public final class DictionaryRuleProcessor {
	private int min_length = 4;
	private int max_length = 16;
	private StringBuilder memory;
	//private static DictionaryRuleProcessor instance;
	// CONSTANTS
	private static final String VOWELS = "aeiouAEIOU";
	private static final String CONSONANTS = "bcdfghjklmnñpqrstvwxyzBCDFGHJKLMNÑPQRSTVWXYZ";
	private static final String PUNCTUATION = ".,:;'?!`\"";
	private static final String SYMBOLS = "$%^&*()-_+=|\\<>[]{}#@/~";
	private static final String ED_STR = "ed";
	private static final String ING_STR = "ing";
	private static final char[] NUMERIC_CONSTANTS = {'A','B','C','D','E','F','G','H','I','J','K',
													 'L','M','N','O','P','Q','R','S','T','U','V',
													 'W','X','Y','Z'};
	
	public DictionaryRuleProcessor() {
		super();
	}

	public DictionaryRuleProcessor(int min_length, int max_length) {
		super();
		this.min_length = min_length;
		this.max_length = max_length;
	}
	/*
	public static DictionaryRuleProcessor getInstance(){
		if (instance == null){
			instance = new DictionaryRuleProcessor();
		}
		return instance;
	}
	*/
	public int getMin_length() {
		return min_length;
	}

	public void setMin_length(int min_length) {
		this.min_length = min_length;
	}

	public int getMax_length() {
		return max_length;
	}

	public void setMax_length(int max_length) {
		this.max_length = max_length;
	}
	
	public StringBuilder getMemory() {
		return memory;
	}

	public void setMemory(StringBuilder memory) {
		this.memory = new StringBuilder(memory.toString());
	}
	
	public boolean rejectConditionList(List<String> conditions, StringBuilder word){
		boolean returnValue = false;
		OUTERLOOP:
		for(String cond: conditions){
			returnValue = rejectCondition(cond, word);
			if (returnValue)
				break OUTERLOOP;
		}
		return returnValue;
	}

	public boolean rejectCondition(String condition, StringBuilder word){
		boolean returnValue = false;
		char charClass = condition.charAt(0);
		if (charClass == '-'){
			if (condition.length() > 1){
				char c2 = condition.charAt(1);
				if (c2 == ':') return false; // acepto la palabra asi como esta
				// TODO implementar estos rejects, por default los ignora!!!
				if ((c2 == 'c')
						|| (c2 == '8')
						|| (c2 == 's')
						|| (c2 == 'p'))
				{
					returnValue = false;
				}
			}
			else {
				System.out.println("Wrong length of condition!: "+condition);
				return true;
			}
		}
		String options = condition.substring(1, condition.length());
		if ((options.length() == 0) && (charClass != 'Q')){
			System.out.println("Wrong length of condition!: "+condition);
			return true;
		}
		if (charClass == 'Q'){ // reject if the word don't change
			return (AuxiliaryForDecryptThreads.equalsTo(this.memory,word));
		}
		else if (charClass == '>'){
			returnValue = rejectUnlessLengthGraterThan(options.charAt(0),word);
		}
		else if (charClass == '<'){
			returnValue = rejectUnlessLengthLessThan(options.charAt(0),word);
		}
		else if (charClass == '!'){
			returnValue = rejectContains(options,word);
		}
		else if (charClass == '/'){
			returnValue = !(rejectContains(options,word));
		}
		else if (charClass == '='){
			if (options.length() == 2 || options.length() == 3){
				int position = Character.getNumericValue(options.charAt(0));
				returnValue = rejectUnlessCharacterAtPosition(position, options.substring(1), word);
			}
			else {
				System.err.println("[rejectCondition] error in condition =");
			}
			
		}
		else if (charClass == '('){
			int position = 0;
			returnValue = rejectUnlessCharacterAtPosition(position, options, word);
		}
		else if (charClass == ')'){
			int position = word.length() - 1;
			returnValue = rejectUnlessCharacterAtPosition(position, options, word);
		}
		else if (charClass == '%'){
			returnValue = rejectUnlessContainsMoreThanOneInstanceOf(options, word);
		}
		return returnValue;
	}

	private boolean rejectUnlessContainsMoreThanOneInstanceOf(String options, StringBuilder word) {
		int instances = Character.getNumericValue(options.charAt(0));
		boolean returnValue = false;
		int count = Integer.MIN_VALUE;
		if (options.length() == 2){
			count = countInstancesOfChar(options.charAt(1), word);
		}
		else if (options.length() == 3){
			if (options.charAt(1) == '?'){
				count = countInstancesOfCharSet(options.charAt(2), word);
			}
			else
				System.err.println("[rejectUnlessContainsMoreThanOneInstanceOf] wrong rule");
		}
		else {
			System.err.println("[rejectUnlessContainsMoreThanOneInstanceOf] wrong rule");
		}
		//System.out.println("[rejectUnlessContainsMoreThanOneInstanceOf] count: "+count+" / instance: "+instances);
		returnValue = (count < instances);
		return returnValue;
	}

	private int countInstancesOfCharSet(char option, StringBuilder word) {
		String myCharset = null;
		boolean charsetIn = true;
		if (option == '?'){
			myCharset = "?";
		}
		else if (option == 'v'){
			myCharset = VOWELS;
		}
		else if (option == 'V'){
			myCharset = VOWELS;
			charsetIn = false;
		}
		else if (option == 'c'){
			myCharset = CONSONANTS;
		}
		else if (option == 'C'){
			myCharset = CONSONANTS;
			charsetIn = false;
		}
		else if (option == 'w'){
			myCharset = " ";
		}
		else if (option == 'W'){
			myCharset = " ";
			charsetIn = false;
		}
		else if (option == 'p'){
			myCharset = PUNCTUATION;
		}
		else if (option == 'P'){
			myCharset = PUNCTUATION;
			charsetIn = false;
		}
		else if (option == 's'){
			myCharset = SYMBOLS;
		}
		else if (option == 'S'){
			myCharset = SYMBOLS;
			charsetIn = false;
		}
		else if (option == 'l'){
			myCharset = AvailableCharsets.getCharsetByName("loweralpha");
		}
		else if (option == 'L'){
			myCharset = AvailableCharsets.getCharsetByName("loweralpha");
			charsetIn = false;
		}
		else if (option == 'u'){
			myCharset = AvailableCharsets.getCharsetByName("alpha");;
		}
		else if (option == 'U'){
			myCharset = AvailableCharsets.getCharsetByName("alpha");;
			charsetIn = false;
		}
		else if (option == 'd'){
			myCharset = AvailableCharsets.getCharsetByName("numeric");
		}
		else if (option == 'D'){
			myCharset = AvailableCharsets.getCharsetByName("numeric");
			charsetIn = false;
		}
		else if (option == 'a'){
			myCharset = AvailableCharsets.getCharsetByName("mixalpha");
		}
		else if (option == 'A'){
			myCharset = AvailableCharsets.getCharsetByName("mixalpha");
			charsetIn = false;
		}
		else if (option == 'x'){
			myCharset = AvailableCharsets.getCharsetByName("mixalpha-numeric");
		}
		else if (option == 'X'){
			myCharset = AvailableCharsets.getCharsetByName("mixalpha-numeric");
			charsetIn = false;
		}
		else if (option == 'z'){
			myCharset = AvailableCharsets.getCharsetByName("ascii-32-95");
		}
		else if (option == 'Z'){
			myCharset = AvailableCharsets.getCharsetByName("ascii-32-95");
			charsetIn = false;
		}
		else {
			System.err.println("[countInstancesOfCharSet] invalid charset!!!");
			return 0;
		}
		int charsetLength = myCharset.length();
		int wordLength = word.length();
		int count = 0;
		if (charsetIn){
			for (int i = 0; i < wordLength; i++){
				char aux = word.charAt(i);
				for(int j = 0; j < charsetLength; j++){
					if (aux == myCharset.charAt(j))
						count = count + 1;
				}
			}
		}	
		else {
			boolean ok = true;
			for (int i = 0; i < wordLength; i++){
				char aux = word.charAt(i);
				INNERLOOP:
				for(int j = 0; j < charsetLength; j++){
					if (aux == myCharset.charAt(j)){
						ok = false;
						break INNERLOOP;
					}	
				}
				if (ok){
					count = count + 1;
				}
				else {
					ok = true;
				}
			}	
		}
		return count;
	}

	private int countInstancesOfChar(char c, StringBuilder word) {
		int count = 0;
		int wordLength = word.length();
		for (int i=0; i < wordLength; i++){
			if (c == word.charAt(i))
				count = count + 1;
		}
		return count;
	}

	private boolean rejectUnlessCharacterAtPosition(int position, String options, StringBuilder word) {
		boolean returnValue = false;
		if (options.length() == 1){
			returnValue = (options.charAt(0) != word.charAt(position));
		}
		else if (options.length() == 2){
			if (options.charAt(0) == '?'){
				char ca = word.charAt(position);
				returnValue = !containsCharacterClass(options.charAt(1), ca);
			}	
			else {
				System.err.println("[rejectUnlessCharacterAtPosition] wrong rule!");
				returnValue = true; //wrong rule
			}	
		}
		return returnValue;
	}

	private boolean rejectContains(String options, StringBuilder word) {
		int opLength = options.length();
		boolean returnValue = false;
		if (opLength == 1){
			char c = options.charAt(0);
			returnValue = containsCharacter(c,word);
		}
		else if (opLength == 2){
			char charClass = options.charAt(0);
			char option = options.charAt(1);
			if (charClass != '?'){
				returnValue = false; // wrong rule
			}
			else {
				returnValue = containsCharacterClass(option,word);
			}
		}
		return returnValue;
	}

	private boolean containsCharacterClass(char option, StringBuilder word) {
		boolean returnValue = false;
		String charset = "";
		if (option == '?'){
			returnValue = containsCharacter('?', word);
		}
		else if (option == 'v'){
			charset = VOWELS;
			returnValue = containsCharset(charset, word);
		}
		else if (option == 'V'){
			charset = VOWELS;
			returnValue = !onlyContainsCharset(charset, word);
		}
		else if (option == 'c'){
			charset = CONSONANTS;
			returnValue = containsCharset(charset, word);
		}
		else if (option == 'C'){
			charset = CONSONANTS;
			returnValue = !onlyContainsCharset(charset, word);
		}
		else if (option == 'w'){
			returnValue = containsCharacter(' ',word);
		}
		else if (option == 'W'){
			returnValue = !containsCharacter(' ',word);
		}
		else if (option == 'p'){
			charset = PUNCTUATION;
			returnValue = containsCharset(charset, word);
		}
		else if (option == 'P'){
			charset = PUNCTUATION;
			returnValue = !onlyContainsCharset(charset, word);
		}
		else if (option == 's'){
			charset = SYMBOLS;
			returnValue = containsCharset(charset, word);
		}
		else if (option == 'S'){
			charset = SYMBOLS;
			returnValue = !onlyContainsCharset(charset, word);
		}
		else if (option == 'l'){
			charset = AvailableCharsets.getCharsetByName("loweralpha");
			returnValue = containsCharset(charset, word);
		}
		else if (option == 'L'){
			charset = AvailableCharsets.getCharsetByName("loweralpha");
			returnValue = !onlyContainsCharset(charset, word);
		}
		else if (option == 'u'){
			charset = AvailableCharsets.getCharsetByName("alpha");
			returnValue = containsCharset(charset, word);
		}
		else if (option == 'U'){
			charset = AvailableCharsets.getCharsetByName("alpha");
			returnValue = !onlyContainsCharset(charset, word);
		}
		else if (option == 'd'){
			charset = AvailableCharsets.getCharsetByName("numeric");
			returnValue = containsCharset(charset, word);
		}
		else if (option == 'D'){
			charset = AvailableCharsets.getCharsetByName("numeric");
			returnValue = !onlyContainsCharset(charset, word);
		}
		else if (option == 'a'){
			charset = AvailableCharsets.getCharsetByName("mixalpha");
			returnValue = containsCharset(charset, word);
		}
		else if (option == 'A'){
			charset = AvailableCharsets.getCharsetByName("mixalpha");
			returnValue = !onlyContainsCharset(charset, word);
		}
		else if (option == 'x'){
			charset = AvailableCharsets.getCharsetByName("mixalpha-numeric");
			returnValue = containsCharset(charset, word);
		}
		else if (option == 'X'){
			charset = AvailableCharsets.getCharsetByName("mixalpha-numeric");
			returnValue = !onlyContainsCharset(charset, word);
		}
		else if (option == 'z'){
			charset = AvailableCharsets.getCharsetByName("ascii-32-95");
			returnValue = containsCharset(charset, word);
		}
		else if (option == 'Z'){
			charset = AvailableCharsets.getCharsetByName("ascii-32-95");
			returnValue = !onlyContainsCharset(charset, word);
		}
		return returnValue;
	}
	
	private boolean containsCharacterClass(char option, char c) {
		boolean returnValue = false;
		String charset = "";
		if (option == '?'){
			returnValue = ('?' == c);
		}
		else if (option == 'v'){
			charset = VOWELS;
			returnValue = containsCharset(charset, c);
		}
		else if (option == 'V'){
			charset = VOWELS;
			returnValue = !onlyContainsCharset(charset, c);
		}
		else if (option == 'c'){
			charset = CONSONANTS;
			returnValue = containsCharset(charset, c);
		}
		else if (option == 'C'){
			charset = CONSONANTS;
			returnValue = !onlyContainsCharset(charset, c);
		}
		else if (option == 'w'){
			returnValue = (' ' == c);
		}
		else if (option == 'W'){
			returnValue = (' ' != c);
		}
		else if (option == 'p'){
			charset = PUNCTUATION;
			returnValue = containsCharset(charset, c);
		}
		else if (option == 'P'){
			charset = PUNCTUATION;
			returnValue = !onlyContainsCharset(charset, c);
		}
		else if (option == 's'){
			charset = SYMBOLS;
			returnValue = containsCharset(charset, c);
		}
		else if (option == 'S'){
			charset = SYMBOLS;
			returnValue = !onlyContainsCharset(charset, c);
		}
		else if (option == 'l'){
			charset = AvailableCharsets.getCharsetByName("loweralpha");
			returnValue = containsCharset(charset, c);
		}
		else if (option == 'L'){
			charset = AvailableCharsets.getCharsetByName("loweralpha");
			returnValue = !onlyContainsCharset(charset, c);
		}
		else if (option == 'u'){
			charset = AvailableCharsets.getCharsetByName("alpha");
			returnValue = containsCharset(charset, c);
		}
		else if (option == 'U'){
			charset = AvailableCharsets.getCharsetByName("alpha");
			returnValue = !onlyContainsCharset(charset, c);
		}
		else if (option == 'd'){
			charset = AvailableCharsets.getCharsetByName("numeric");
			returnValue = containsCharset(charset, c);
		}
		else if (option == 'D'){
			charset = AvailableCharsets.getCharsetByName("numeric");
			returnValue = !onlyContainsCharset(charset, c);
		}
		else if (option == 'a'){
			charset = AvailableCharsets.getCharsetByName("mixalpha");
			returnValue = containsCharset(charset, c);
		}
		else if (option == 'A'){
			charset = AvailableCharsets.getCharsetByName("mixalpha");
			returnValue = !onlyContainsCharset(charset, c);
		}
		else if (option == 'x'){
			charset = AvailableCharsets.getCharsetByName("mixalpha-numeric");
			returnValue = containsCharset(charset, c);
		}
		else if (option == 'X'){
			charset = AvailableCharsets.getCharsetByName("mixalpha-numeric");
			returnValue = !onlyContainsCharset(charset, c);
		}
		else if (option == 'z'){
			charset = AvailableCharsets.getCharsetByName("ascii-32-95");
			returnValue = containsCharset(charset, c);
		}
		else if (option == 'Z'){
			charset = AvailableCharsets.getCharsetByName("ascii-32-95");
			returnValue = !onlyContainsCharset(charset, c);
		}
		return returnValue;
	}
	
	private boolean containsCharset(String charset, StringBuilder word){
		boolean returnValue = false;
		int wordLength = word.length();
		int charsetLength = charset.length();
		OUTERLOOP:
		for (int i = 0; i < wordLength; i++){
			char aux1 = word.charAt(i);
			for (int j = 0; j < charsetLength; j++){
				if (aux1 == charset.charAt(j)){
					returnValue = true;
					break OUTERLOOP;
				}
			}
		}
		return returnValue;
	}
	
	private boolean containsCharset(String charset, char c){
		boolean returnValue = false;
		int charsetLength = charset.length();
		OUTERLOOP:
		for (int j = 0; j < charsetLength; j++){
			if (c == charset.charAt(j)){
				returnValue = true;
				break OUTERLOOP;
			}
		}
		return returnValue;
	}
	
	private boolean onlyContainsCharset(String charset, StringBuilder word){
		int wordLength = word.length();
		int charsetLength = charset.length();
		boolean ok = false;
		OUTERLOOP:
		for (int i = 0; i < wordLength; i++){
			ok = false;
			char aux1 = word.charAt(i);
			ok = false;
			for (int j = 0; j < charsetLength; j++){
				if (aux1 == charset.charAt(j)){
					ok = true;
					break;
				}
			}
			if (!ok)
				break OUTERLOOP;
		}
		return ok;
	}
	
	private boolean onlyContainsCharset(String charset, char c){
		int charsetLength = charset.length();
		boolean ok = false;
		OUTERLOOP:
		for (int j = 0; j < charsetLength; j++){
			if (c == charset.charAt(j)){
				ok = true;
				break OUTERLOOP;
			}
		}
		
		return ok;
	}

	private boolean containsCharacter(char c, StringBuilder word) {
		boolean returnValue = false;
		int wordLength = word.length();
		for (int i = 0; i < wordLength; i++)
			if (word.charAt(i) == c){
				returnValue = true;
				break;
			}
		return returnValue;
	}
	
	private int isNumericConstant(char c){
		int returnValue = -1;
		if (c == '*'){
			returnValue = max_length;
		}
		else if (c == '+'){
			returnValue = max_length + 1;
		}
		else if (c == '-'){
			returnValue = max_length - 1;
		}
		else if (c == 'l'){
			returnValue = this.memory.length();
		}
		else if (c == 'm'){
			returnValue = this.memory.length() - 1;
		}
		else {
			OUTERLOOP:
			for (int i = 0; i < NUMERIC_CONSTANTS.length; i++){
				if (c == NUMERIC_CONSTANTS[i]){
					returnValue = i + 10;
					break OUTERLOOP;
				}
			}
				
		}
		return returnValue;
	}

	private boolean rejectUnlessLengthLessThan(char option, StringBuilder word) {
		boolean returnValue = false;
		int lengthValue = isNumericConstant(option);
		if (lengthValue == -1){
			returnValue = !(word.length() < Character.getNumericValue(option));
		}
		else {
			returnValue = !((word.length() < lengthValue)); 
		}
		return returnValue;
	}

	private boolean rejectUnlessLengthGraterThan(char option, StringBuilder word) {
		boolean returnValue = false;
		int lengthValue = isNumericConstant(option);
		if (lengthValue == -1){
			returnValue = !(word.length() > Character.getNumericValue(option));
		}
		else {
			returnValue = !((word.length() > lengthValue)); 
		}
		return returnValue;
	}
	
	public StringBuilder applyRuleActionList(StringBuilder word, List<String> actions){
		for(String action: actions){
			word = applyAction(word, action);
		}
		return word;
	}

	public StringBuilder applyAction(StringBuilder word, String action) {
		// default validations
		if (action.length() == 0) {
			System.out.println("ERROR! Bad action definition! (ignoring it...)");
			return word;
		}
		char c = action.charAt(0);
		if (c == ':'){
			return word;
		}
		
		StringBuilder returnValue = null;
		char o = 0;
		StringBuilder options = new StringBuilder();
		if (action.length() > 1){
			o = action.charAt(1);
			options.append(action.substring(1, action.length()));
		}
		// start action processing
		if (c == 's') {
			StringBuilder aux = new StringBuilder();
			if (action.length() <= 1) {
				System.out.println("ERROR! Bad action definition! (ignoring it...)");
				return word;
			}
			char auxc = action.charAt(1);
			int wordLength= word.length();
			char ca = 0;
			if (auxc != '?' ){
				char X = action.charAt(1);
				if (action.length() <= 2) {
					System.out.println("ERROR! Bad action definition! (ignoring it...)");
					return word;
				}
				char Y = action.charAt(2);
				for (int i = 0; i < wordLength; i++){
					ca = word.charAt(i);
					if (ca == X){
						aux.append(Y);
					}
					else{
						aux.append(ca);
					}
				}
			}
			else {
				if (action.length() <= 3) {
					System.out.println("ERROR! Bad action definition! (ignoring it...)");
					return word;
				}
				char myClass = action.charAt(2);
				char Y = action.charAt(3);
				for (int i = 0; i < wordLength; i++){
					ca = word.charAt(i);
					if (containsCharacterClass(myClass, ca)){
						aux.append(Y);
					}
					else{
						aux.append(ca);
					}
				}
			}
			returnValue = aux;
		}
		else if (c == '@'){
			StringBuilder aux = new StringBuilder();
			if (action.length() <= 1) {
				System.out.println("ERROR! Bad action definition! (ignoring it...)");
				return word;
			}
			char auxc = action.charAt(1);
			int wordLength= word.length();
			char ca = 0;
			if (auxc != '?' ){
				char X = action.charAt(1);
				for (int i = 0; i < wordLength; i++){
					ca = word.charAt(i);
					if (ca != X){
						aux.append(ca);
					}
				}
			}
			else {
				if (action.length() <= 2) {
					System.out.println("ERROR! Bad action definition! (ignoring it...)");
					return word;
				}
				char myClass = action.charAt(2);
				for (int i = 0; i < wordLength; i++){
					ca = word.charAt(i);
					if (!containsCharacterClass(myClass, ca)){
						aux.append(ca);
					}
				}
			}
			returnValue = aux;
		}	
		else if (c == 'S'){
			StringBuilder aux = new StringBuilder();
			int wordLength= word.length();
			for (int i = 0; i < wordLength; i++){
				char ca = word.charAt(i);
				aux.append(getShiftCharacterCase(ca));
			}
			returnValue = aux;
		}	
		else if (c == 'V'){
			StringBuilder aux = new StringBuilder();
			int wordLength= word.length();
			for (int i = 0; i < wordLength; i++){
				char ca = word.charAt(i);
				if (containsCharset(CONSONANTS, ca)){
					aux.append(Character.toUpperCase(ca));
				}
				else if (containsCharset(VOWELS, ca)){
					aux.append(Character.toLowerCase(ca));
				}
				else{
					aux.append(ca);
				}
			}
			returnValue = aux;
		}
		else if (c == 'R'){
			StringBuilder aux = new StringBuilder();
			int wordLength= word.length();
			for (int i = 0; i < wordLength; i++){
				char ca = word.charAt(i);
				aux.append(getShiftCharacterRight(ca));
			}
			returnValue = aux;
		}
		else if (c == 'L'){
			StringBuilder aux = new StringBuilder();
			int wordLength= word.length();
			for (int i = 0; i < wordLength; i++){
				char ca = word.charAt(i);
				aux.append(getShiftCharacterLeft(ca));
			}
			returnValue = aux;
		}
		else if (c == 'p'){
			StringBuilder aux = new StringBuilder();
			aux.append(word);
			aux.append('s');
			returnValue = aux;
		}
		else if (c == 'P'){
			StringBuilder aux = new StringBuilder();
			aux.append(word);
			aux.append(ED_STR);
			returnValue = aux;
		}
		else if (c == 'I'){
			StringBuilder aux = new StringBuilder();
			aux.append(word);
			int lastCharPosition = word.length() - 1;
			char ca = word.charAt(lastCharPosition);
			if (containsCharset(VOWELS, ca)){
				aux.deleteCharAt(lastCharPosition);
			}
			aux.append(ING_STR);
			returnValue = aux;
		}
		else if (c == '\''){
			StringBuilder aux = new StringBuilder();
			int lengthValue = isNumericConstant(o);
			int pos = 0;
			if (lengthValue == -1){
				// es un numero o otra cosa
				pos = Character.getNumericValue(o);
			}
			else {
				pos = lengthValue;
			}
			if (pos <= word.length()){
				for (int i = 0; i < pos; i++ ){
					aux.append(word.charAt(i));
				}
			}
			else {
				aux.append(word);
			}
			returnValue = aux;
		}
		else if (c == 'l'){
			StringBuilder aux = new StringBuilder();
			for (int i = 0; i < word.length(); i++){
				aux.append(Character.toLowerCase(word.charAt(i)));
			}
			returnValue = aux;
		}
		else if (c == 'u'){
			StringBuilder aux = new StringBuilder();
			for (int i = 0; i < word.length(); i++){
				aux.append(Character.toUpperCase(word.charAt(i)));
			}
			returnValue = aux;
		}
		else if (c == 'c'){
			StringBuilder aux = new StringBuilder();
			aux.append(word);
			if (word.length() == 0){
				returnValue = new StringBuilder(word);
			}
			else {
				aux.setCharAt(0, Character.toUpperCase(word.charAt(0)));
				returnValue = aux;
			}	
		}
		else if (c == 'C'){
			StringBuilder aux = new StringBuilder();
			if (word.length() == 0){
				aux.append(word);
			}
			else {
				aux.append(Character.toLowerCase(word.charAt(0)));
				for (int i = 1; i < word.length(); i++){
					aux.append(Character.toUpperCase(word.charAt(i)));
				}
			}
			returnValue = aux;
		}
		else if (c == 't'){
			StringBuilder aux = new StringBuilder();
			int wordLength = word.length();
			char ca = 0;
			for (int i = 0; i < wordLength; i++){
				ca = word.charAt(i);
				if (containsCharacterClass('l', ca)){
					aux.append(Character.toUpperCase(ca));
				}
				else if (containsCharacterClass('u', ca)){
					aux.append(Character.toLowerCase(ca));
				}
				else {
					aux.append(ca);
				}
			}
			returnValue = aux;
		}
		else if (c == 'T'){
			StringBuilder aux = new StringBuilder();
			aux.append(word);
			int lengthValue = isNumericConstant(o);
			int pos = 0;
			if (lengthValue == -1){
				// es un numero o otra cosa
				pos = Character.getNumericValue(o);
			}
			else {
				pos = lengthValue;
			}
			if (pos < word.length()){
				char ca = word.charAt(pos);
				if (containsCharacterClass('l', ca)){
					aux.setCharAt(pos, Character.toUpperCase(ca));
				}
				else if (containsCharacterClass('u', ca)){
					aux.setCharAt(pos, Character.toLowerCase(ca));
				}
			}
			returnValue = aux;
		}
		else if (c == 'r'){
			StringBuilder aux = new StringBuilder();
			aux.append(word);
			aux.reverse();
			returnValue = aux;
		}
		else if (c == 'd'){
			StringBuilder aux = new StringBuilder();
			aux.append(word);
			aux.append(word);
			returnValue = aux;
		}
		else if (c == 'f'){
			StringBuilder aux1 = new StringBuilder();
			StringBuilder aux2 = new StringBuilder();
			aux1.append(word);
			aux2.append(word);
			aux1.reverse();
			aux2.append(aux1);
			returnValue = aux2;
		}
		else if (c == '{'){
			StringBuilder aux = new StringBuilder();
			aux.append(word);
			if (word.length() != 0){
				aux.deleteCharAt(0);
				aux.append(word.charAt(0));
			}
			returnValue = aux;
		}
		else if (c == '}'){
			int lastCharPosition = word.length() - 1;
			StringBuilder aux = new StringBuilder();
			aux.append(word.charAt(lastCharPosition));
			aux.append(word);
			aux.deleteCharAt(lastCharPosition+1);
			returnValue = aux;
		}
		else if (c == '$'){
			StringBuilder aux = new StringBuilder(word);
			aux.insert(aux.length(),o);
			returnValue = aux;
		}
		else if (c == '^'){
			StringBuilder aux = new StringBuilder(word);
			aux.insert(0,o);
			returnValue = aux;
		}
		else if (c == '['){
			StringBuilder aux = new StringBuilder();
			aux.append(word);
			if (word.length() != 0){
				aux.deleteCharAt(0);
			}	
			returnValue = aux;
		}
		else if (c == ']'){
			StringBuilder aux = new StringBuilder();
			aux.append(word);
			if (word.length() != 0){
				aux.deleteCharAt(word.length() - 1);
			}	
			returnValue = aux;
		}
		else if (c == 'D'){
			StringBuilder aux = new StringBuilder();
			aux.append(word);
			try {
				int pos = Integer.parseInt(options.toString());
				if (word.length() > pos){
					aux.deleteCharAt(pos);
				}	
			}
			catch (Exception e) {
			}
			returnValue = aux;
		}
		else if (c == 'M'){
			this.memory = new StringBuilder(word);
			returnValue = new StringBuilder(word);
		}
		else if (c == 'X'){
			//System.out.println("[DEBUG] options: "+options);
			if (options.length() == 3){
				int startPos = 0;
				int endPos = 0;
				int insertPos = 0;
				char myChar = options.charAt(0);
				int aux = isNumericConstant(myChar);
				if (aux == -1){
					// es un numero o otra cosa
					startPos = Character.getNumericValue(myChar);
				}
				else {
					startPos = aux;
				}
				myChar = options.charAt(1);
				aux = isNumericConstant(myChar);
				if (aux == -1){
					// es un numero o otra cosa
					endPos = Character.getNumericValue(myChar);
				}
				else {
					endPos = aux;
				}
				myChar = options.charAt(2);
				aux = isNumericConstant(myChar);
				if (aux == -1){
					// es un numero o otra cosa
					insertPos = Character.getNumericValue(myChar);
				}
				else {
					insertPos = aux;
				}
				StringBuilder pre1 = new StringBuilder();
				//System.out.println("[DEBUG][applyAction] memory   : "+this.memory);
				//System.out.println("[DEBUG][applyAction] word     : "+word);
				//System.out.println("[DEBUG][applyAction] startPos : "+startPos);
				//System.out.println("[DEBUG][applyAction] endPos   : "+endPos);
				//System.out.println("[DEBUG][applyAction] insertPos: "+insertPos);
				pre1.append(this.memory.substring(startPos, endPos));
				returnValue = new StringBuilder();
				returnValue.append(word.substring(0, insertPos));
				returnValue.append(pre1);
				returnValue.append(word.substring(insertPos));
				//System.out.println("[DEBUG] returnValue: "+returnValue);
			}
			else {//ignore error
				returnValue = new StringBuilder(word);
			}
		}
		else {
			returnValue = new StringBuilder("NULL(unknow_rule)");
		}
		return returnValue;
	}

	private char getShiftCharacterCase(char c) {
		if (c == ' ') c= ' ';
		else if (c == '!') c = '1';
		else if (c == '"') c = '\'';
		else if (c == '#') c = '3';
		else if (c == '$') c = '4';
		else if (c == '%') c = '5';
		else if (c == '&') c = '7';
		else if (c == '\'') c = '"';
		else if (c == '(') c = '9';
		else if (c == ')') c = '0';
		else if (c == '*') c = '8';
		else if (c == '+') c = '=';
		else if (c == ',') c = '<';
		else if (c == '-') c = '_';
		else if (c == '.') c = '>';
		else if (c == '/') c = '?';
		else if (c == '0') c = ')';
		else if (c == '1') c = '!';
		else if (c == '2') c = '@';
		else if (c == '3') c = '#';
		else if (c == '4') c = '$';
		else if (c == '5') c = '%';
		else if (c == '6') c = '^';
		else if (c == '7') c = '&';
		else if (c == '8') c = '*';
		else if (c == '9') c = '(';
		else if (c == ':') c = ';';
		else if (c == ';') c = ':';
		else if (c == '<') c = ',';
		else if (c == '=') c = '+';
		else if (c == '>') c = '.';
		else if (c == '?') c = '/';
		else if (c == '@') c = '2';
		else if (c == 'A') c = 'a';
		else if (c == 'B') c = 'b';
		else if (c == 'C') c = 'c';
		else if (c == 'D') c = 'd';
		else if (c == 'E') c = 'e';
		else if (c == 'F') c = 'f';
		else if (c == 'G') c = 'g';
		else if (c == 'H') c = 'h';
		else if (c == 'I') c = 'i';
		else if (c == 'J') c = 'j';
		else if (c == 'K') c = 'k';
		else if (c == 'L') c = 'l';
		else if (c == 'M') c = 'm';
		else if (c == 'N') c = 'n';
		else if (c == 'O') c = 'o';
		else if (c == 'P') c = 'p';
		else if (c == 'Q') c = 'q';
		else if (c == 'R') c = 'r';
		else if (c == 'S') c = 's';
		else if (c == 'T') c = 't';
		else if (c == 'U') c = 'u';
		else if (c == 'V') c = 'v';
		else if (c == 'W') c = 'w';
		else if (c == 'X') c = 'x';
		else if (c == 'Y') c = 'y';
		else if (c == 'Z') c = 'z';
		else if (c == '[') c = '{';
		else if (c == '\\') c = '|';
		else if (c == ']') c = '}';
		else if (c == '^') c = '6';
		else if (c == '_') c = '-';
		else if (c == '`') c = '~';
		else if (c == 'a') c = 'A';
		else if (c == 'b') c = 'B';
		else if (c == 'c') c = 'C';
		else if (c == 'd') c = 'D';
		else if (c == 'e') c = 'E';
		else if (c == 'f') c = 'F';
		else if (c == 'g') c = 'G';
		else if (c == 'h') c = 'H';
		else if (c == 'i') c = 'I';
		else if (c == 'j') c = 'J';
		else if (c == 'k') c = 'K';
		else if (c == 'l') c = 'L';
		else if (c == 'm') c = 'M';
		else if (c == 'n') c = 'N';
		else if (c == 'o') c = 'O';
		else if (c == 'p') c = 'P';
		else if (c == 'q') c = 'Q';
		else if (c == 'r') c = 'R';
		else if (c == 's') c = 'S';
		else if (c == 't') c = 'T';
		else if (c == 'u') c = 'U';
		else if (c == 'v') c = 'V';
		else if (c == 'w') c = 'W';
		else if (c == 'x') c = 'X';
		else if (c == 'y') c = 'Y';
		else if (c == 'z') c = 'Z';
		else if (c == '{') c = '[';
		else if (c == '|') c = '\\';
		else if (c == '}') c = ']';
		else if (c == '~') c = '`';

		return c;
	}

	private char getShiftCharacterRight(char c) {
		if (c == ' ') c= ' ';
		else if (c == '!') c = '@';
		else if (c == '"') c = '"';
		else if (c == '#') c = '$';
		else if (c == '$') c = '%';
		else if (c == '%') c = '^';
		else if (c == '&') c = '*';
		else if (c == '\'') c = '\'';
		else if (c == '(') c = ')';
		else if (c == ')') c = '_';
		else if (c == '*') c = '(';
		else if (c == '+') c = '|';
		else if (c == ',') c = '.';
		else if (c == '-') c = '=';
		else if (c == '.') c = '/';
		else if (c == '/') c = '\\';
		else if (c == '0') c = '-';
		else if (c == '1') c = '2';
		else if (c == '2') c = '3';
		else if (c == '3') c = '4';
		else if (c == '4') c = '5';
		else if (c == '5') c = '6';
		else if (c == '6') c = '7';
		else if (c == '7') c = '8';
		else if (c == '8') c = '9';
		else if (c == '9') c = '0';
		else if (c == ':') c = '"';
		else if (c == ';') c = '\'';
		else if (c == '<') c = '>';
		else if (c == '=') c = '\\';
		else if (c == '>') c = '?';
		else if (c == '?') c = '|';
		else if (c == '@') c = '#';
		else if (c == 'A') c = 'S';
		else if (c == 'B') c = 'N';
		else if (c == 'C') c = 'V';
		else if (c == 'D') c = 'F';
		else if (c == 'E') c = 'R';
		else if (c == 'F') c = 'G';
		else if (c == 'G') c = 'H';
		else if (c == 'H') c = 'J';
		else if (c == 'I') c = 'O';
		else if (c == 'J') c = 'K';
		else if (c == 'K') c = 'L';
		else if (c == 'L') c = ':';
		else if (c == 'M') c = '<';
		else if (c == 'N') c = 'M';
		else if (c == 'O') c = 'P';
		else if (c == 'P') c = '{';
		else if (c == 'Q') c = 'W';
		else if (c == 'R') c = 'T';
		else if (c == 'S') c = 'D';
		else if (c == 'T') c = 'Y';
		else if (c == 'U') c = 'I';
		else if (c == 'V') c = 'B';
		else if (c == 'W') c = 'E';
		else if (c == 'X') c = 'C';
		else if (c == 'Y') c = 'U';
		else if (c == 'Z') c = 'X';
		else if (c == '[') c = ']';
		else if (c == '\\') c = '\\';
		else if (c == ']') c = ']';
		else if (c == '^') c = '&';
		else if (c == '_') c = '+';
		else if (c == '`') c = '1';
		else if (c == 'a') c = 's';
		else if (c == 'b') c = 'n';
		else if (c == 'c') c = 'v';
		else if (c == 'd') c = 'f';
		else if (c == 'e') c = 'r';
		else if (c == 'f') c = 'g';
		else if (c == 'g') c = 'h';
		else if (c == 'h') c = 'j';
		else if (c == 'i') c = 'o';
		else if (c == 'j') c = 'k';
		else if (c == 'k') c = 'l';
		else if (c == 'l') c = ';';
		else if (c == 'm') c = ',';
		else if (c == 'n') c = 'm';
		else if (c == 'o') c = 'p';
		else if (c == 'p') c = '[';
		else if (c == 'q') c = 'w';
		else if (c == 'r') c = 't';
		else if (c == 's') c = 'd';
		else if (c == 't') c = 'y';
		else if (c == 'u') c = 'i';
		else if (c == 'v') c = 'b';
		else if (c == 'w') c = 'e';
		else if (c == 'x') c = 'c';
		else if (c == 'y') c = 'u';
		else if (c == 'z') c = 'x';
		else if (c == '{') c = '}';
		else if (c == '|') c = '|';
		else if (c == '}') c = '}';
		else if (c == '~') c = '!';

		return c;
	}
	
	private char getShiftCharacterLeft(char c) {
		if (c == ' ') c= ' ';
		else if (c == '!') c = '~';
		else if (c == '"') c = ':';
		else if (c == '#') c = '@';
		else if (c == '$') c = '#';
		else if (c == '%') c = '$';
		else if (c == '&') c = '^';
		else if (c == '\'') c = ';';
		else if (c == '(') c = '*';
		else if (c == ')') c = '(';
		else if (c == '*') c = '&';
		else if (c == '+') c = '_';
		else if (c == ',') c = 'm';
		else if (c == '-') c = '0';
		else if (c == '.') c = ',';
		else if (c == '/') c = '.';
		else if (c == '0') c = '9';
		else if (c == '1') c = '`';
		else if (c == '2') c = '1';
		else if (c == '3') c = '2';
		else if (c == '4') c = '3';
		else if (c == '5') c = '4';
		else if (c == '6') c = '5';
		else if (c == '7') c = '6';
		else if (c == '8') c = '7';
		else if (c == '9') c = '8';
		else if (c == ':') c = 'L';
		else if (c == ';') c = 'l';
		else if (c == '<') c = 'M';
		else if (c == '=') c = '-';
		else if (c == '>') c = '<';
		else if (c == '?') c = '>';
		else if (c == '@') c = '!';
		else if (c == 'A') c = 'A';
		else if (c == 'B') c = 'V';
		else if (c == 'C') c = 'X';
		else if (c == 'D') c = 'S';
		else if (c == 'E') c = 'W';
		else if (c == 'F') c = 'D';
		else if (c == 'G') c = 'F';
		else if (c == 'H') c = 'G';
		else if (c == 'I') c = 'U';
		else if (c == 'J') c = 'H';
		else if (c == 'K') c = 'J';
		else if (c == 'L') c = 'K';
		else if (c == 'M') c = 'N';
		else if (c == 'N') c = 'B';
		else if (c == 'O') c = 'I';
		else if (c == 'P') c = 'O';
		else if (c == 'Q') c = 'Q';
		else if (c == 'R') c = 'E';
		else if (c == 'S') c = 'A';
		else if (c == 'T') c = 'R';
		else if (c == 'U') c = 'Y';
		else if (c == 'V') c = 'C';
		else if (c == 'W') c = 'Q';
		else if (c == 'X') c = 'Z';
		else if (c == 'Y') c = 'T';
		else if (c == 'Z') c = 'Z';
		else if (c == '[') c = 'p';
		else if (c == '\\') c = '=';
		else if (c == ']') c = '[';
		else if (c == '^') c = '%';
		else if (c == '_') c = ')';
		else if (c == '`') c = '`';
		else if (c == 'a') c = 'a';
		else if (c == 'b') c = 'v';
		else if (c == 'c') c = 'x';
		else if (c == 'd') c = 's';
		else if (c == 'e') c = 'w';
		else if (c == 'f') c = 'd';
		else if (c == 'g') c = 'f';
		else if (c == 'h') c = 'g';
		else if (c == 'i') c = 'u';
		else if (c == 'j') c = 'h';
		else if (c == 'k') c = 'j';
		else if (c == 'l') c = 'k';
		else if (c == 'm') c = 'n';
		else if (c == 'n') c = 'b';
		else if (c == 'o') c = 'i';
		else if (c == 'p') c = 'o';
		else if (c == 'q') c = 'q';
		else if (c == 'r') c = 'e';
		else if (c == 's') c = 'a';
		else if (c == 't') c = 'r';
		else if (c == 'u') c = 'y';
		else if (c == 'v') c = 'c';
		else if (c == 'w') c = 'q';
		else if (c == 'x') c = 'z';
		else if (c == 'y') c = 't';
		else if (c == 'z') c = 'z';
		else if (c == '{') c = 'P';
		else if (c == '|') c = '+';
		else if (c == '}') c = '{';
		else if (c == '~') c = '~';

		return c;
	}
}

