package org.hulop.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.hulop.data.MapGeojson.Facility;
import org.hulop.data.i18n.Messages;

public class Directory implements Searchable, Cloneable {
	
	private ArrayList<Section> sections = new ArrayList<Section>();
	public Boolean showSectionIndex = false;
	private JSONArray landmarks;

	public Directory() {
		// TODO Auto-generated constructor stub
	}
	
	public Directory(URL url, Locale locale) {
		MapGeojson map = null;
		try {
			map = MapGeojson.load(url, locale);
			setLandmakrks(map.getLandmarks());
		}catch(Exception e) {
			e.printStackTrace();
			return;
		}

		Comparator<Directory.Item> itemComparator = new Comparator<Directory.Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				return o1.compare(o2);
			}
		};
		FirstLetterIndex firstLetterIndex = new FirstLetterIndex();
		
		if (map.getBuildings().length > 1) {
			Section buildingsSection = this.add(new Section(Messages.get(locale, "buildings")));		
			for(String building:map.getBuildings()) {
				if (building == null) continue;
				List<Facility> facilities = map.getFacilitiesByBuilding(building);
				Item i = buildingsSection.add(new Item(building, null));
				
				Directory buildingDirectory = i.setContent(new Directory());
				Section buildingSection = buildingDirectory.add(new Section(building));
				for(Facility f:facilities) {
					try {
						buildingSection.add(new Item(f.getName(), f.getNamePron(), f.getNodeID(), buildingFloorString(f), buildingFloorPronString(f)));
					} catch(Exception e) {
						System.err.println(f);
					}
				}
				buildingDirectory.sortAndDevide(itemComparator, firstLetterIndex);
				buildingDirectory.showSectionIndex = true;
			}
			buildingsSection.sort(itemComparator);		
		} 
		if (map.getMajorCategories().length > 1) {
			Section categoriesSection = this.add(new Section(Messages.get(locale, "categories")));		
			for(String category:map.getMajorCategories()) {
				if (category == null) continue;
				List<Facility> facilities = map.getFacilitiesByMajorCategory(category);
				Item i = categoriesSection.add(new Item(category, null));
				
				Directory categoryDirectory = i.setContent(new Directory());
				Section categorySection = categoryDirectory.add(new Section(category));
				for(Facility f:facilities) {
					try {
						categorySection.add(new Item(f.getName(), f.getNamePron(), f.getNodeID(), buildingFloorString(f), buildingFloorPronString(f)));
					} catch(Exception e) {
						System.err.println(f);
					}
				}
				categoryDirectory.sortAndDevide(itemComparator, firstLetterIndex);
				categoryDirectory.showSectionIndex = true;
			}
			categoriesSection.sort(itemComparator);		
		}
		
		Section serviceSection = this.add(new Section(Messages.get(locale, "nearby_facility")));		
		
		for(Facility service:map.getServices()) {
			serviceSection.add(new Item(service.getName(), service.getNamePron(), service.getNodeID()));
		}
		serviceSection.sort(itemComparator);
	}
	
	protected String buildingFloorString(Facility facility) {
		try {
			Double f = Double.parseDouble(facility.getFloor());
			if (f < 0) {
				return facility.getBuilding()+" - B"+(-f.intValue())+"F";			
			} else {
				return facility.getBuilding()+" - "+f.intValue()+"F";			
			}
		} catch (Exception e) {
			return facility.getBuilding();
		}
	}
	protected String buildingFloorPronString(Facility facility) {
		try {
			Double f = Double.parseDouble(facility.getFloor());
			if (f < 0) {
				return facility.getBuilding()+" - B"+(-f.intValue())+"F";			
			} else {
				return facility.getBuilding()+" - "+f.intValue()+"F";			
			}
		} catch (Exception e) {
			return facility.getBuilding();
		}
	}
	protected String buildingRoomFloorString(Facility facility) {
		return facility.getName()+" ("+buildingFloorString(facility)+")";
	}
	protected String buildingRoomFloorPronString(Facility facility) {
		return facility.getName()+" ("+buildingFloorPronString(facility)+")";
	}
	

	public void setLandmakrks(JSONArray landmarks) {
		this.landmarks = landmarks;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		Directory dir = (Directory) super.clone();
		dir.sections = new ArrayList<Section>();
		for(Section s:sections) {
			dir.sections.add((Section)s.clone());
		}
		return dir;
	}

	public void sortAndDevide(Comparator<Item> compare, IndexOperator indexOperator) {
		if (sections.size() > 1) {
			throw new RuntimeException("section size should be one to operate");
		}
		Section section = sections.get(0);
		section.sort(compare);
		ArrayList<Section> tempSections = new ArrayList<Section>();
		Section tempSection = null;
		String lastIndex = null;
		for(Item item: section.items) {
			String index = indexOperator.getIndex(item);
			if (!index.equals(lastIndex)) {
				tempSection = new Section(index);
				tempSections.add(tempSection);
			}
			tempSection.add(item);
			lastIndex = index;
		}
		sections = tempSections;
	}

	public interface IndexOperator {
		public String getIndex(Item item);
	}
	
	public class FirstLetterIndex implements IndexOperator {
		public String getIndex(Item item) {
			if (item.sortKey != null) {
				return item.sortKey.substring(0, 1).toUpperCase();
			}
			if (item.title != null) {
				return item.title.substring(0, 1).toUpperCase();
			}
			return "*";
		}
	}
	
	public Section add(Section section) {
		this.sections.add(section);
		return section;
	}

	public class Section implements Cloneable {
		@Override
		protected Object clone() throws CloneNotSupportedException {
			Section section = (Section) super.clone();
			section.items = new ArrayList<Item>();
			for(Item i:items) {
				section.items.add((Item)i.clone());
			}
			return section;
		}

		public Section() {			
		}
		public void sort(Comparator<Item> compare) {
			items.sort(compare);
		}
		public Section(String title) {
			this.title = title;
		}
		public Section(String title, String titlePron, String indexTitle) {
			this(title);
			this.titlePron = titlePron;
			this.indexTitle = indexTitle;
		}
		
		private String title;
		private String titlePron;
		private String indexTitle;
		private ArrayList<Item> items = new ArrayList<Item>();
		
		public Item add(Item item) {
			items.add(item);
			return item;
		}
		
		public JSONObject toJSON() throws JSONException {
			JSONObject section = new JSONObject();
			if (title != null) section.put("title", title);
			if (titlePron != null) section.put("pron", titlePron);
			if (indexTitle != null) section.put("indexTitle", indexTitle);

			for(Item i: this.items) {
				section.append("items", i.toJSON());
			}
			return section;
		}

		public void walk(String query, ArrayList<Item> buffer) {
			for(Item item:items) {
				item.walk(query, buffer);
			}
		}
	}
	
	public class Item implements Cloneable{
		@Override
		protected Object clone() throws CloneNotSupportedException {
			Item item = (Item) super.clone();
			item.content = (Directory)content.clone();
			return item;
		}

		public void walk(String query, ArrayList<Item> buffer) {
			if (content != null) {
				content.walk(query, buffer);
			} else {
				if (matches(query)) {
					buffer.add(this);
				}
			}
		}

		private boolean matches(String query) {
			String lower = query.toLowerCase();
			return (title != null && title.toLowerCase().indexOf(lower) > -1) ||
					(titlePron != null && titlePron.toLowerCase().indexOf(lower) > -1) ||
					(subtitle != null && subtitle.toLowerCase().indexOf(lower) > -1);
		}

		public Item(String title, String titlePron) {
			if (title == null) { throw new RuntimeException("no title"); }
			this.title = title;
			this.titlePron = titlePron;
		}
		public Item(String title, String titlePron, String nodeID) {
			this(title, titlePron);
			this.nodeID = nodeID;
		}
		public Item(String title, String titlePron, String nodeID, String subtitle, String subtitlePron) {
			this(title, titlePron, nodeID);
			this.subtitle = subtitle;
			this.subtitlePron = subtitlePron;
		}
		public int compare(Item item) {
			if (sortKey != null && item.sortKey != null) {
				return sortKey.compareToIgnoreCase(item.sortKey);
			}
			return title.compareToIgnoreCase(item.title);
		}
		private String title;
		private String titlePron;
		private Directory content;
		private String nodeID;
		private String subtitle;
		private String subtitlePron;
		
		public String sortKey;
		
		public Directory setContent(Directory content) {
			this.content = content;
			return content;
		}
		
		public JSONObject toJSON() throws JSONException {
			JSONObject item = new JSONObject();
			if (title != null) item.put("title", title);
			if (titlePron != null) item.put("titlePron", titlePron);
			if (nodeID != null) item.put("nodeID", nodeID);
			if (content != null) item.put("content", content.toJSON());
			if (subtitle != null) item.put("subtitle", subtitle);
			if (subtitlePron != null) item.put("subtitlePron", subtitlePron);

			return item;
		}
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject directory = new JSONObject();
		for(Section s: this.sections) {
			directory.append("sections", s.toJSON());
		}
		directory.put("showSectionIndex", showSectionIndex);
		if (landmarks != null) directory.put("landmarks", landmarks);
		return directory;
	}

	public void walk(String query, ArrayList<Item> buffer) {
		for(Section s:sections) {
			s.walk(query, buffer);
		}
	}

	@Override
	public Directory search(String query) {
		Directory result = new Directory();
		Section section = result.add(new Section());
		walk(query, section.items);
		return result;
	}
	

	public static void main(String[] args) throws MalformedURLException, JSONException {
		URL url = new URL("http://cmu-demo-hotel.mybluemix.net/query/directory?lng=-80.05914149539372&lat=40.41837024971599&user=test");			
		Directory d = new Directory(url, new Locale("en"));
		Directory d2 = d.search("room");
		System.out.println(d2.toJSON().toString());
	}
}
