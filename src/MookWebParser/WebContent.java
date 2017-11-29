package MookWebParser;

import java.util.ArrayList;
import java.util.HashMap;

public class WebContent {

	private String headline;
	private String URL;
	private String content;
	private String date;
	private ArrayList<String> photoURLs;
	private HashMap<String, String> tags;
	
	public WebContent() {
		photoURLs = new ArrayList<String>();
		tags = new HashMap<String, String>();
	}
	
	public String getHeadline() {
		return headline;
	}

	public void setHeadline(String headline) {
		this.headline = headline;
	}

	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public ArrayList<String> getPhotoURLs() {
		return photoURLs;
	}

	public void addPhotoURLs(String url) {
		if(photoURLs.isEmpty()) photoURLs.add(url);
		if(photoURLs.contains(url)) return;
		photoURLs.add(url);
	}

	public HashMap<String, String> getTags() {
		return tags;
	}

	public void addTags(String name, String uRL) {
		tags.put(name, uRL);
	}
	
	
	
	
}
