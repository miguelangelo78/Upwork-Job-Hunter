package com.org.uphunt.bot;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.org.uphunt.data.dict.Dictionary;
import com.org.uphunt.entity.JobTile;
import com.org.uphunt.file.FileHandler;
import com.org.uphunt.gui.popup.Popup;

enum HuntMode {
	HUNT_NEW, HUNT_BROWSE
}

public class UpworkHunterBot {
	final boolean DEBUG = true;
	
	Popup pop;
	
	private HuntMode huntMode;
	
	private final String upwork_url = "https://www.upwork.com/o/jobs/browse/c";
	private int MAX_PAGECOUNT = 10;
	private int MAX_PAGE_ABSOLUTE = 700; // 700 is a big numbah m8
	private final int UPDATE_INTERVAL_BROWSE = 1; // 1 ms
	private final int UPDATE_INTERVAL_NEW = 60000; // 60K ms or 60 SECONDS
	private int UPDATE_INTERVAL = UPDATE_INTERVAL_BROWSE;
	private int last_page_read = 0;
	private int jobs_saved = 0;
	private Dictionary dict;
	
	private FileHandler fileHandler;
	
	ArrayList<String> cats;
	ArrayList<ArrayList<Document>> upwork_search_doc; // An array for all categories and an array for all pages
	ArrayList<Integer> cats_choosen;
	final int default_cat = 0; // Web dev and programming
	
	private ArrayList<JobTile> goodJobs;
	
	public UpworkHunterBot(){
		try {
			pop = new Popup();
			fileHandler = new FileHandler();
			dict = new Dictionary();
			
			huntMode = HuntMode.HUNT_BROWSE;
			goodJobs = new ArrayList<JobTile>();
			
			// Initialize categories' url:
			Elements cats_el = Jsoup.connect(upwork_url+"/").get().select("#category > li > a");
			cats = new ArrayList<String>();
			
			int first = 0;
			for(Element cat : cats_el){
				if(first++==0) continue; // The first category is empty
				cats.add(cat.attr("href"));
			}
				
			// Initialize cats choosen and upwork search document array list
			cats_choosen = new ArrayList<Integer>();
			upwork_search_doc = new ArrayList<ArrayList<Document>>();
			
			// Add default category and update the document
			add_category(default_cat, false);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}	
	
	private void DEBUG_PRINT(String debug_msg){
		if(DEBUG)
			System.out.println("> DEBUG - "+debug_msg);
	}
	
	private void ERROR_LOG(String error_msg){
		fileHandler.write(fileHandler.upwork_error_log, error_msg);
	}
	
	private void update_work_doc_single(int cat_index, int page){
		page++; // Should be  0 indexed
		
		boolean tryagain = true;
		while(tryagain){
			DEBUG_PRINT("Fetching: "+upwork_url+cats.get(cats_choosen.get(cat_index))+"?page="+page);
			
			try {
				Document pageDoc = Jsoup.connect(upwork_url+cats.get(cats_choosen.get(cat_index))+"?page="+page).get();
				upwork_search_doc.get(cat_index).set(page-1, pageDoc);
				tryagain = false;
			} catch (IOException e) {
				// Try again till it works
				e.printStackTrace();
				DEBUG_PRINT("Couldn't connect to Upwork! Trying again . . .");
				tryagain = true;
			}		
		}
	}
	
	private void update_work_doc_single(int cat_index){
		// Update single category and only first page
		update_work_doc_single(cat_index, 0);
	}
	
	private void update_work_doc_single_allpages(int cat_index){
		// Update single category but all pages
		for(int i=0; i < MAX_PAGECOUNT; i++)
			update_work_doc_single(cat_index, i);
	}
	
	private void update_upwork_doc_all(){
		DEBUG_PRINT("Updating all categories and all pages ("+MAX_PAGECOUNT+" pages) . . .");
		
		// Update all upwork documents for all categories and pages:
		for(int i=0;i<upwork_search_doc.size();i++)
			update_work_doc_single_allpages(i);
		
		last_page_read = MAX_PAGECOUNT;
		
		DEBUG_PRINT("Done updating!");
	}
	
	private Document get_cat(int cat_index, int page){
		if(cat_index < 0 || cat_index > cats.size()) return null;
		
		// Find category:
		for(int i=0;i<cats_choosen.size();i++)
			if(cats_choosen.get(i) == cat_index)
				return upwork_search_doc.get(i).get(page);
		
		// Didn't find such category
		return null;
	}
	
	public Document get_cat(int cat_index){
		return get_cat(cat_index, 0);
	}
	
	public void add_category(int cat_index){
		add_category(cat_index, true);
	}
	
	public void add_category(int cat_index, boolean fetch){
		if(cat_index < 0 || cat_index > cats.size()) return;
		
		cats_choosen.add(cat_index);
		
		// Initialize all pages and add them to the 2D array list:
		ArrayList<Document> allPages = new ArrayList<Document>();
		for(int i=0;i<MAX_PAGE_ABSOLUTE;i++)
			allPages.add(null);
		upwork_search_doc.add(allPages);

		if(fetch)
			update_work_doc_single(upwork_search_doc.size()-1);
	}
	
	public int remove_category(int cat_index){
		if(cat_index < 0 || cat_index > cats.size()) return -1;
		
		// Find the index first:
		for(int i=0;i<cats_choosen.size();i++){
			if(cats_choosen.get(i) == cat_index){
				// Remove the ith category from the list and also the document
				cats_choosen.remove(i);
				upwork_search_doc.remove(i);
				return 0;
			}
		}
		
		return -1;
	}
		
	public JobTile[] getJobTiles(int cat_index){
		ArrayList<JobTile> jobTileArrList = new ArrayList<JobTile>();
		
		// Iterate through all pages, grab all elements for each page and add them into an arraylist
		for(int i=0;i<last_page_read;i++){ // last page should be atleast equal to MAX_PAGECOUNT on the first time this runs!
			Elements allJobTiles = get_cat(cat_index,i).select(".job-tile");
			
			for(int j=0;j<allJobTiles.size();j++)
				jobTileArrList.add(new JobTile(allJobTiles.get(j)));
		}
		
		// And convert that array list into a simple array:
		JobTile jobTiles[] = new JobTile[jobTileArrList.size()];
		for(int i=0;i<jobTileArrList.size();i++)
			jobTiles[i] = jobTileArrList.get(i);
		
		return jobTiles;
	}
	
	private void load_dictionaries(){
		try {
			dict.load_dictionary(dict.lookfor, fileHandler.uphunt_dict_lookfor);
			dict.load_dictionary(dict.butavoid, fileHandler.uphunt_dict_butavoid);
		}catch(Exception e){
			ERROR_LOG(e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private void init(){
		// TODO: And don't empty the file, in the function obviously
		fileHandler.empty(fileHandler.upwork_result_path); // Clear old jobs from the result file
		
		// Load configuration files here, which include the dictionaries and categories to be used
		load_dictionaries();
		
		// Load first 10 pages:
		update_upwork_doc_all();		
	}
	
	public void save_good_jobs(){
		// Show all good jobs that were added:
		int newJobCount = goodJobs.size() - jobs_saved;
		
		String strBuild = "";
		for(int i=jobs_saved;i<goodJobs.size();i++){
			strBuild += goodJobs.get(i).jobDump()+"\n";
			System.out.println(goodJobs.get(i).jobDump());
			jobs_saved++;
		}
		
		if(newJobCount > 0){
			fileHandler.append(fileHandler.upwork_result_path, strBuild);
			pop.show("- Upwork Hunter Alert -", "Found "+newJobCount+" new jobs on Upwork");
		}
	}
	
	public boolean job_already_saved(JobTile job){
		// TODO: Go to file instead of RAM to check if the job was already saved
		for(JobTile j : goodJobs)
			if(j.jobURL.equals(job.jobURL)) 
				return true;
		return false;
	}
	
	public void run(){
		init();
		
		boolean keep = true;
		while(keep){
			try {
				// Fetch all tiles for all choosen categories
				for(int i=0;i<cats_choosen.size();i++){
					JobTile jobs[] = getJobTiles(i);
				
					// Iterate through all tiles and match the job title with a dictionary
					// TODO: In the future, match all other properties as well
				
					for(int j=0;j<jobs.length;j++) // Match job title with dictionary here
						if((dict.contains(dict.lookfor, jobs[j].jobTitle) || dict.contains(dict.lookfor, jobs[j].jobDesc)) 
								&& 
							(!dict.contains(dict.butavoid, jobs[j].jobTitle) && !dict.contains(dict.butavoid, jobs[j].jobDesc)))
						{
							if(!job_already_saved(jobs[j]))
								goodJobs.add(jobs[j]);
						}
					
				}
				
				save_good_jobs();
				
				// Wait a bit for now (could be a big bit though)
				Thread.sleep(UPDATE_INTERVAL);
				
				// Update dictionaries
				load_dictionaries();
				
				switch(huntMode){
				case HUNT_NEW: 
					// From now on update only the 1st page, no need for more
					for(int i=0;i<cats_choosen.size();i++)
						update_work_doc_single(cats_choosen.get(i));
					break;
				case HUNT_BROWSE: 
					// Enough browsing dude, now you're going to fetch the new jobs
					if(last_page_read>=MAX_PAGE_ABSOLUTE){
						huntMode = HuntMode.HUNT_NEW;
						UPDATE_INTERVAL = UPDATE_INTERVAL_NEW;
						break;
					}
					
					// Update all categories and a couple pages counting from the last search done
					for(int i=0;i<cats_choosen.size();i++)
						for(int page=last_page_read; page<last_page_read+MAX_PAGECOUNT; page++)
							update_work_doc_single(cats_choosen.get(i),page);
					
					last_page_read += MAX_PAGECOUNT;
					
					break;
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
