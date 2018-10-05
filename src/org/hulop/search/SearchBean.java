package org.hulop.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.hulop.data.Directory;
import org.hulop.data.Directory.Item;
import org.hulop.data.MapGeojson.Facility;
import org.hulop.data.MapGeojson.FacilityEntry;
import org.hulop.data.Searchable;

public class SearchBean {
	
	private static SearchBean instance;
	
	public static SearchBean getInstance() {
		if (instance == null) {
			instance = new SearchBean();
		}
		return instance;
	}
	
	
	private HashMap<String, Searchable> cache = new HashMap<String, Searchable>();
	
	public void addSearchable(String id, Searchable searchable) {
		cache.put(id, searchable);
	}
	
	public class SearchResult {
		List<Item> items;
		
		SearchResult(List<Item> items) {
			this.items = items;
		}

		public JSONArray toJSON() throws JSONException {
			JSONArray array = new JSONArray();
			for(Item item: items) {
				array.add(item.toJSON());
			}
			return array;
		}
	}
	
	private HashMap<FacilityEntry, ArrayList<Facility>> index = new HashMap<FacilityEntry, ArrayList<Facility>>();
	
	public SearchBean() {	
	}
	
	private void debugprint() {
		for(FacilityEntry e:index.keySet()) {
			ArrayList<Facility> fs = index.get(e);
			
			System.out.print(e+":");
			for(Facility f:fs) {
				System.out.print(f+",");
			}
			System.out.println();
		}
	}

	public Directory search(String user, String query) {	
		Searchable s = cache.get(user);
		if (s == null) {
			return null;
		}
		return s.seacrh(query);
	}

}
