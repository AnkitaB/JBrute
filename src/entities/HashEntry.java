package entities;

public class HashEntry {
	private String hash;
	private String salt = null;
	private String saltType = "utf8";
	private boolean preSalt = true;
	private static final String SEPARATOR = ":";
	private static final String[] SALT_TYPES = {"utf8", "hex", "int64"};
	
	public HashEntry() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public HashEntry(String line) {
		super();
		String aux = gerFirstField(line);
		if (aux != null) this.hash = aux;
		line = removeFirstField(line);
		if (line != null){
			aux = gerFirstField(line);
			if (aux != null) this.salt = aux;
			line = removeFirstField(line);
			if (line != null){
				aux = gerFirstField(line);
				if (aux != null) {
					if (aux.equalsIgnoreCase("pre")){
						this.preSalt = true;
					}
					else if (aux.equalsIgnoreCase("pos")){
						this.preSalt = false;
					}
				}	
				line = removeFirstField(line);
				if (line != null){
					aux = gerFirstField(line);
					if (aux != null) {
						if (isValidSaltType(aux)){
							this.saltType = aux;
						}
					}
				}
			}
		}
	}

	private static final String removeFirstField(String s) {
		String returnValue = null;
		int idx = s.indexOf(SEPARATOR, 0);
		if (idx != -1){
			returnValue = s.substring(idx+1, s.length());
		}
		return returnValue;
	}

	private static final String gerFirstField(String s) {
		String returnValue = null;
		int idx = s.indexOf(SEPARATOR, 0);
		if (idx == -1){//the line should contains only the hash
			idx = s.length();
		}
		returnValue = s.substring(0,idx); 
		if (returnValue.equalsIgnoreCase("")){
			returnValue = null;
		}
		return returnValue;
	}

	public HashEntry(String hash, String salt, String saltType, boolean preSalt) {
		super();
		this.hash = hash;
		this.salt = salt;
		this.saltType = saltType;
		this.preSalt = preSalt;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getSaltType() {
		return saltType;
	}

	public void setSaltType(String saltType) {
		this.saltType = saltType;
	}

	public boolean isPreSalt() {
		return preSalt;
	}

	public void setPreSalt(String salt) {
		this.salt = salt;
		this.preSalt = true;
	}
	
	public void setPostSalt(String salt) {
		this.salt = salt;
		this.preSalt = false;
	}
	
	public String getSalt(){
		return this.salt;
	}
	
	public String toString(){
		String s = "";
		s = s + hash + SEPARATOR + salt + SEPARATOR;
		if (preSalt) s = s + "pre" + SEPARATOR;
		else s = s + "pos" + SEPARATOR;
		s = s + saltType;
		return s;
	}
	
	public static final boolean isValidSaltType(String saltType){
		boolean returnValue = false;
		OUTERMOST:
		for (int i = 0; i < SALT_TYPES.length; i++){
			if (saltType.equalsIgnoreCase(SALT_TYPES[i])){
				returnValue = true;
				break OUTERMOST;
			}
		}
		return returnValue;
	}
	
}
