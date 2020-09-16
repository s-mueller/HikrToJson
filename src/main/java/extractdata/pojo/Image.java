package extractdata.pojo;

import extractdata.extractors.ImageWaypoint;

import java.util.List;

public class Image {

	private String id;
	private String url;
	private String description;
	private List<ImageWaypoint> waypoints;
	private String fileName;
	
	public Image(String id, String url, String fileName, String description, List<ImageWaypoint> waypoints) {
		this.id = id;
		this.url = url;
		this.description = description;
		this.waypoints = waypoints;
		this.fileName = fileName;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getId() {
		return id;
	}

	public String getFileName() {
		return fileName;
	}

}