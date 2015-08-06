package com.org.uphunt.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

enum FileMode{
	FILE_READ, FILE_WRITE, FILE_EMPTY, FILE_EXISTS
}

public class FileHandler {
	
	private final String TRUE_STR = "true";
	private final String FALSE_STR = "false";
	
	public String upwork_result_path = "upworkhunt_results.log";
	public String uphunt_dict_lookfor = "uphunt_dict_lookfor";
	public String uphunt_dict_butavoid = "uphunt_dict_butavoid";
	public String upwork_error_log = "upwork_error_log.log";
	
	public FileHandler(){}
	
	private String file_func(String filePath, String data, FileMode mode, boolean append){
		String res = "";
		
		switch(mode){
		case FILE_EXISTS: 
			File f = new File(filePath);
			if(f.exists() && !f.isDirectory())
				res = TRUE_STR;
			else 
				res = FALSE_STR;
			break;
		case FILE_READ: 
			BufferedReader br = null;
			StringBuilder sb = null;
			try {
				br = new BufferedReader(new FileReader(filePath));
				sb = new StringBuilder();
				String line = br.readLine();
				while(line != null){
					sb.append(line);
					sb.append(System.lineSeparator());
					line = br.readLine();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} finally {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			res = sb.toString();
			break;
		case FILE_WRITE:
			try {
				FileWriter fw = new FileWriter(filePath, true);
				fw.write(data);
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case FILE_EMPTY: 
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(filePath);
				writer.print("");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			writer.close();
			break;
		default: break;
		}
		
		return res;
	}
	
	public void append(String filePath, String data){
		file_func(filePath, data, FileMode.FILE_WRITE, true);
	}
	
	public void write(String filePath, String data){
		file_func(filePath, data, FileMode.FILE_WRITE, false);	
	}
	
	public void empty(String filePath){
		file_func(filePath, "", FileMode.FILE_EMPTY, false);
	}
	
	public boolean exists(String filePath){
		return file_func(filePath, "", FileMode.FILE_EXISTS, false).equals(TRUE_STR);
	}
	
	public String readAll(String filePath){
		return file_func(filePath, "", FileMode.FILE_READ, false);
	}
}
