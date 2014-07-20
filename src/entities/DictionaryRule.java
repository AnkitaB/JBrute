package entities;

import java.util.ArrayList;
import java.util.List;

public class DictionaryRule {
	private List<String> conditions = new ArrayList<String>();
	private List<String> actions = new ArrayList<String>();
	private List<String> members = new ArrayList<String>();
	private DictionaryRuleProcessor rp;
	private DictionaryRulePreProcessor rpp;
	private String oneLineDefinition;
	private List<DictionaryRule> subRules;

	public DictionaryRule() {
		super();
		this.rp = new DictionaryRuleProcessor();
		this.rpp = DictionaryRulePreProcessor.getInstance();
	}

	public DictionaryRule(String rule) {
		super();
		//this.rp = DictionaryRuleProcessor.getInstance();
		this.rp = new DictionaryRuleProcessor();
		this.rpp = DictionaryRulePreProcessor.getInstance();
		this.oneLineDefinition = rule;
		this.conditions = rpp.getRuleConditions(oneLineDefinition);
		this.actions = rpp.getRuleActions(oneLineDefinition);
		this.members = rpp.getWordList(oneLineDefinition);
		//this.subRules = rpp.generateSubRules(this.actions);
		this.subRules = rpp.generateSubRules(this.members);
		
	}

	public List<String> getConditions() {
		return conditions;
	}

	public void setConditions(List<String> conditions) {
		this.conditions = conditions;
	}

	public List<String> getActions() {
		return actions;
	}

	public void setActions(List<String> actions) {
		this.actions = actions;
	}
	
	public List<String> getMembers() {
		return members;
	}

	public void setMembers(List<String> members) {
		this.members = members;
	}

	public boolean rejectWord(StringBuilder word){
		return rp.rejectConditionList(conditions, word);
	}
		
	private StringBuilder aplicateOneAction(StringBuilder word){
		return rp.applyRuleActionList(word, actions);
	}
	
	public String toString(){
		StringBuilder aux = new StringBuilder();
		for (String member: members){
			aux.append(member);
			aux.append(" ");
		}
		int myLength = aux.length();
		if (myLength > 0){
			if (aux.charAt(myLength - 1) == ' '){
				aux.deleteCharAt(aux.length() - 1);
			}	
		}
		return aux.toString();
	}
	
	public void addAction(String action){
		this.actions.add(action);
	}
	
	public void addMember(String action){
		this.members.add(action);
	}
	
	public List<String> getSubRules(){
		List<String> list = new ArrayList<String>();
		for (DictionaryRule dr: subRules){
			list.add(dr.toString());
		}
		
		return list;
	}
	
	public List<StringBuilder> aplicateAction(StringBuilder word){
		List<StringBuilder> list = new ArrayList<StringBuilder>();
		for (DictionaryRule dr: subRules){
			list.add(dr.aplicateOneAction(word));
		}
		return list;
	}
	
	public List<StringBuilder> aplicateRule(StringBuilder word){
		List<StringBuilder> list = new ArrayList<StringBuilder>();
		StringBuilder aux = null;
		for (DictionaryRule dr: subRules){
			aux = dr.aplicateOneRule(word);
			if (aux != null){
				list.add(aux);
			}	
		}
		
		return list;
	}

	/*
	private StringBuilder aplicateOneRule(StringBuilder word) {
		StringBuilder memory = new StringBuilder(word);
		StringBuilder returnValue = new StringBuilder(word);
		for (String member: this.members){
			if (rpp.isCondition(member)){
				if (rpp.mustQueryMemory(member)){
					if (AuxiliaryForDecryptThreads.equalsTo(memory,returnValue)){
						return null;
					}
				}
				else if (rp.rejectCondition(member, returnValue)){
					return null;
				}
			}
			else if (rpp.isAction(member)){
				if (rpp.mustMemorizeWord(member)){
					memory = new StringBuilder(returnValue);
				}
				else {
					returnValue = rp.applyAction(returnValue, member);
				}
			}
		}
		return returnValue;
	}
	*/
	private StringBuilder aplicateOneRule(StringBuilder word) {
		rp.setMemory(new StringBuilder(word));
		StringBuilder returnValue = new StringBuilder(word);
		for (String member: this.members){
			try {
				if (rpp.isCondition(member)){
					if (rp.rejectCondition(member, returnValue)){
						return null;
					}
				}
				else if (rpp.isAction(member)){
					returnValue = rp.applyAction(returnValue, member);
				}
				else {
					System.err.println("ERROR!!! unknow member: "+member);
					return null; // ignore it
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				System.err.println("[DEBUG][aplicateOneRule] rule   : " + toString());
				System.err.println("[DEBUG][aplicateOneRule] member : " + member);
				System.err.println("[DEBUG][aplicateOneRule] word   : " + word);
				System.err.println("[DEBUG][aplicateOneRule] memory : " + rp.getMemory());
			}
		}
		return returnValue;
	}
}