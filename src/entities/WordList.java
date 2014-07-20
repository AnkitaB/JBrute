package entities;

import java.util.ArrayList;
import java.util.List;

public class WordList {
	List<StringBuilder> list;

	public WordList() {
		super();
		// TODO Auto-generated constructor stub
		this.list = new ArrayList<StringBuilder>();
	}

	public WordList(List<StringBuilder> list) {
		super();
		this.list = list;
	}

	public List<StringBuilder> getList() {
		return list;
	}

	public void setList(List<StringBuilder> list) {
		this.list = list;
	}
	
	public void addWord(StringBuilder sb){
		this.list.add(sb);
	}
	
	public int size(){
		return list.size();
	}
}
