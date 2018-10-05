package org.hulop.data;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

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
			this.setLandmakrks(map.getLandmarks());
		}catch(Exception e) {
			e.printStackTrace();
			return;
		}
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
	public Directory seacrh(String query) {
		Directory result = new Directory();
		Section section = result.add(new Section());
		walk(query, section.items);
		return result;
	}
	
}
