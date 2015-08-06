package com.org.uphunt.data.dict;

import java.util.ArrayList;

import com.org.uphunt.file.FileHandler;

public class Dictionary {
	
	private FileHandler fileHandler;

	// ! means word boundary, ? means it could be a match which belongs to a word
	public ArrayList<String> lookfor, butavoid;
	
	public Dictionary(){
		fileHandler = new FileHandler();
		lookfor = new ArrayList<String>();
		butavoid = new ArrayList<String>();
	}
	
	public void load_dictionary(ArrayList<String> dictArray, String dictionary_name) throws Exception{
		String data = "";
		
		if(fileHandler.exists(dictionary_name))
			data = fileHandler.readAll(dictionary_name);
		else throw new Exception("Couldn't load dictionary '"+dictionary_name+"'");
		
		String [] dataSplit = data.split("\n");
		
		dictArray.clear();
		for(int i=0;i<dataSplit.length;i++)
			dictArray.add(dataSplit[i]);
	}
	
	public boolean contains(ArrayList<String> dict, String word){
		for(int i=0;i<dict.size();i++){
			String dict_fix = dict.get(i).substring(0,0) + dict.get(i).substring(1);
			
			if(dict.get(i).charAt(0)=='!')
				if(word.matches("(?i).*\b"+dict_fix+"\b.*")) return true;
			 else
				 if(word.matches("(?i).*"+dict_fix+".*")) return true;
		}
		
		return false;
	}
}
