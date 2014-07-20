package controller;

public class InParameter {
	private String name;
	private String value;
	private static final char nameSeparator = '=';
	
	public InParameter(String parameter){
		//StringBuilder aux = new StringBuilder();
		//name = "";
		int endName = -1;
		for (int i = 0; i < parameter.length(); i++){
			if (parameter.charAt(i) == nameSeparator){
				endName = i;
				value = "";
				for (int j = i+1; j < parameter.length(); j++){
					value = value + parameter.charAt(j);
				}
				break;
			}
		}
		if (endName != -1){
			name = parameter.substring(0, endName);
			if (value.equalsIgnoreCase("")){
				value = null;
			}
		}
		else {
			name = parameter;
			value = null;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public char getNameSeparator() {
		return nameSeparator;
	}
	
	public boolean hasValue(){
		return (value != null);
	}
	
	public int getNumericValue(){
		if (isDigitString()){
			return Integer.parseInt(this.value);
		}
		else {
			return Integer.MIN_VALUE;
		}
	}
	
	private boolean isDigitString(){
		boolean returnValue = true;
		OUTERLOOP:
		for (int i = 0; i < this.value.length(); i++){
			if (!Character.isDigit(this.value.charAt(i))){
				returnValue = false;
				break OUTERLOOP;
			}
		}
		return returnValue;
	}
	
	public String toString(){
		return name + nameSeparator + value;
	}

}

