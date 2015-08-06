package com.org.uphunt.entity;

import org.jsoup.nodes.Element;

public class JobTile {
	public String jobTitle;
	public String jobDesc;
	public String jobURL;
	public String payType;
	public String budget; // If paytype is hourly this field MIGHT be empty
	public String estimateTime; // This one might also be empty
	
	public Element jobElement;
	
	private final String UNKNOWN_STR = "Unknown";
	
	public JobTile(Element jobTileElement){
		jobElement = jobTileElement;
		
		jobTitle = jobTileElement.select("header > h2 > a").text();
		jobURL = jobTileElement.select("header > h2 > a").attr("abs:href");
		jobDesc = jobTileElement.select(".description").text();
		payType = jobTileElement.select(".js-type").text();
		budget = jobTileElement.select(".js-budget").text();
		estimateTime = jobTileElement.select(".js-duration").text();
		
		// In case the information is not found on the job tile:
		if(budget.length() == 0) budget = UNKNOWN_STR;
		if(estimateTime.length() == 0) estimateTime = UNKNOWN_STR;
	}
	
	public Element getElement(){
		return jobElement;
	}
	
	public String jobDump(){
		return String.format("************\n- Job title: %s\n- Description: %s\n- Pay Type: %s\n- Budget: %s\n- Duration: %s\n- URL: %s\n************", jobTitle, jobDesc, payType, budget, estimateTime, jobURL);
	}
}