package entities;

import java.util.ArrayList;
import java.util.List;

public class DictionaryRulePreProcessor {
	private int min_length = 4;
	private int max_length = 16;
	private static DictionaryRulePreProcessor instance;
	
	private DictionaryRulePreProcessor() {
		super();
	}

	private DictionaryRulePreProcessor(int min_length, int max_length) {
		super();
		this.min_length = min_length;
		this.max_length = max_length;
	}
	
	public static DictionaryRulePreProcessor getInstance(){
		if (instance == null){
			instance = new DictionaryRulePreProcessor();
		}
		return instance;
	}
	
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
	
	public List<String> getRuleConditions(String s){
		List<String> returnValue = new ArrayList<String>();
		List<String> myWords = getWordList(s);
		for (String word: myWords){
			if (isCondition(word)){
				returnValue.add(word);
			}
		}
		return returnValue;
	}
	
	public List<String> getRuleActions(String s){
		List<String> returnValue = new ArrayList<String>();
		List<String> myWords = getWordList(s);
		for (String word: myWords){
			if (isAction(word)){
				returnValue.add(word);
			}
		}
		return returnValue;
	}
	
	public final boolean mustMemorizeWord(String action){
		return action.equals("M");
	}
	
	public final boolean isAction(String s) {
		boolean returnValue = false;
		char c = s.charAt(0);
		if ((c == 's')
			|| (c == '@')
			|| (c == 'S')
			|| (c == 'V')
			|| (c == 'R')
			|| (c == 'L')
			|| (c == 'p')
			|| (c == 'P')
			|| (c == 'I')
			|| (c == '\'')
			|| (c == ':')
			|| (c == 'l')
			|| (c == 'u')
			|| (c == 'c')
			|| (c == 'C')
			|| (c == 't')
			|| (c == 'T')
			|| (c == 'r')
			|| (c == 'd')
			|| (c == 'f')
			|| (c == '{')
			|| (c == '}')
			|| (c == '$')
			|| (c == '^')
			|| (c == '[')
			|| (c == ']')
			|| (c == 'D')
			|| (c == 'M')
			|| (c == 'X')){
			returnValue = true;
		}
		return returnValue;
	}

	public final boolean isCondition(String s){
		boolean returnValue = false;
		char c1 = s.charAt(0);
		if ((c1 == '>')
			|| (c1 == '<')
			|| (c1 == '!')
			|| (c1 == '/')
			|| (c1 == '=')
			|| (c1 == '(')
			|| (c1 == ')')
			|| (c1 == '%')
			|| (c1 == 'Q')){
			returnValue = true;
		}
		else if ((c1 == '-') && (s.length() > 1)){
			char c2 = s.charAt(1);
			if ((c2 == ':')
					|| (c2 == 'c')
					|| (c2 == '8')
					|| (c2 == 's')
					|| (c2 == 'p')){
				returnValue = true;
			}
		}
		return returnValue;
	}
	
	public boolean isValidRule(String s){
		//System.out.println("[isValidRule] word: "+s);
		boolean returnValue = true;
		List<String> words = getWordList(s);
		for(String elem: words){
			if (!isCondition(elem) && !isAction(elem)){
				returnValue = false;
				break;
			}
		}
		return returnValue;
	}
	
	public List<String> getWordList(String s){
		//System.out.println("[getWordList] line: "+s);
		List<String> list = new ArrayList<String>();
		int count = 0;
		int lineLength = s.length();
		StringBuilder aux = new StringBuilder("");
		while (count < lineLength){
			if (s.charAt(count) == ' '){
				list.add(aux.toString());
				aux = new StringBuilder("");
				while (count < lineLength && s.charAt(count) == ' '){
					count = count + 1;
				}
			}
			else {
				aux.append(s.charAt(count));
				count = count + 1;
			}
		}
		if (aux.length() > 0)
			list.add(aux.toString());
		return list;
	}
	
	public boolean isMaskedAction(String action) {
		boolean returnValue = false;
		//
		if (isAction(action)){
			String mask = removeAction(action);
			int lastCharPosition = mask.length() - 1;
			if (mask.charAt(0) == '[' && mask.charAt(lastCharPosition) == ']'){
				returnValue = true;
			}
		}
		
		return returnValue;
	}
	
	private String removeAction(String action) {
		// mask´s accepted only for $ and ^ actions
		char c = action.charAt(0);
		String returnValue = action;
		if (c == '$' || c == '^'){
			returnValue = action.substring(1, action.length());
		}
		else {
			returnValue = action; // do nothing
		}
		return returnValue;
	}

	public char[] getTargets(String action) {
		char[] returnValue = null; 
		int maskSize = action.length() - 2;
		if (action.length() == 0 || action.charAt(0) != '[' || action.charAt(maskSize+1) != ']'){
			return returnValue;
		}
		StringBuilder aux = new StringBuilder();
		aux.append(action);
		// remove ']'
		aux.deleteCharAt(action.length() - 1);
		// remove '['
		aux.deleteCharAt(0);
		String mask = aux.toString();
		if (mask.equals("a-z") && mask.length() == 3){
			returnValue = AvailableCharsets.getCharsetByName("loweralpha").toCharArray();
		}
		else if (mask.equals("A-Z") && mask.length() == 3){
			returnValue = AvailableCharsets.getCharsetByName("alpha").toCharArray();
		}
		else if (mask.equals("0-9") && mask.length() == 3){
			returnValue = AvailableCharsets.getCharsetByName("numeric").toCharArray();
		}
		else if (mask.equals("a-zA-Z") && mask.length() == 6){
			returnValue = AvailableCharsets.getCharsetByName("mixalpha").toCharArray();
		}
		else if (mask.equals("a-z0-9") && mask.length() == 6){
			returnValue = AvailableCharsets.getCharsetByName("loweralpha-numeric").toCharArray();
		}
		else if (mask.equals("A-Z0-9") && mask.length() == 6){
			returnValue = AvailableCharsets.getCharsetByName("alpha-numeric").toCharArray();
		}
		else if (mask.equals("a-zA-Z0-9") && mask.length() == 9){
			returnValue = AvailableCharsets.getCharsetByName("mixalpha-numeric").toCharArray();
		}
		else {
			// not a standard mask, it is a custom user-defined mask
			int auxIndex = 0;
			returnValue = new char[maskSize];
			for (int i = 0; i < mask.length(); i++){
				returnValue[auxIndex] = mask.charAt(i);
				auxIndex = auxIndex + 1;
			}
		}
		
		return returnValue;
	}

	public List<DictionaryRule> generateSubRules(List<String> operands) {
		List<DictionaryRule> returnValue = new ArrayList<DictionaryRule>();
		DictionaryRule dr = new DictionaryRule();
		returnValue.add(dr);
		List<DictionaryRule> aux = null;
		for (String operand: operands){
			if (isMaskedAction(operand)){
				//System.out.println("[generateSubRules] Es una masked action: "+operand);
				String mask = removeAction(operand);
				//System.out.println("[generateSubRules] Action removida: "+mask);
				char c = operand.charAt(0);
				char[] maskMembers = getTargets(mask);
				aux = new ArrayList<DictionaryRule>();
				for (DictionaryRule rule: returnValue){
					for (int i = 0; i < maskMembers.length; i++){
						String actionsLine = rule.toString();
						dr = new DictionaryRule(actionsLine);
						StringBuilder auxsb = new StringBuilder();
						auxsb.append(c);
						auxsb.append(maskMembers[i]);
						dr.addMember(auxsb.toString());
						aux.add(dr);
					}	
				}
				returnValue = aux;
			}
			else {
				//System.out.println("No es una masked action: "+operand);
				for (DictionaryRule rule: returnValue){
					rule.addMember(operand);
				}
			}
		}
		return returnValue;
	}
	
	public final List<DictionaryRule> getRulesFromRuleLines(List<String> ruleLines, boolean printRulesLoaded) {
		List<DictionaryRule> rules = new ArrayList<DictionaryRule>();
		if (printRulesLoaded){
			System.out.print("Loading rules... ");
		}
		for (String rule: ruleLines){
			if (isValidRule(rule)){
				rules.add(new DictionaryRule(rule));
			}
			else {
				if (printRulesLoaded){
					System.out.println("Error! invalid rule: "+rule+" (ignored)");
				}
			}
		}
		if (rules.size() == 0){ // load defualt rule: use only words on dictionary file
			rules.add(new DictionaryRule(":"));
			if (printRulesLoaded){
				System.out.println("Using default rule \":\" (just use words in dictionary)");
			}
		}
		else {
			if (printRulesLoaded){
				System.out.println(rules.size()+" rules loaded.");
			}
			
		}
			
		return rules;
	}

	public boolean mustQueryMemory(String condition) {
		return condition.equals("Q");
	}
	
}