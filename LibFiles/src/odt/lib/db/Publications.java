package odt.lib.db;

public class Publications {

	private String pubAbbrev;
	private String pubTitle;
	private String condition; /* tagname, defunct, Adopt me */
	private String pubLocation;
	private String pubURL;
	private String redirect;

	public String getPubAbbrev() {
		return pubAbbrev;
	}
	public void setPubAbbrev(String pubAbbrev) {
		this.pubAbbrev = pubAbbrev;
	}
	public String getPubTitle() {
		return pubTitle;
	}
	public void setPubTitle(String pubTitle) {
		this.pubTitle = pubTitle;
	}
	public String getPubLocation() {
		return pubLocation;
	}
	public void setPubLocation(String pubLocation) {
		this.pubLocation = pubLocation;
	}
	public String getCondition() {
		return condition;
	}
	public void setCondition(String condition) {
		this.condition = condition;
	}
	public String getRedirect() {
		return redirect;
	}
	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}
	public String getPubURL() {
		return pubURL;
	}
	public void setPubURL(String pubURL) {
		this.pubURL = pubURL;
	}
}
