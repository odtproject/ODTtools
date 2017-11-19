package odt.lib.db;

public class TagNameClass {
	
	private String tagname;
	private String name;
	private String email1;
	private String email2;
	private String email3;
	private String moderator;
	private String status;
	private String notes;

	public String getTagname() {
		return tagname;
	}
	public void setTagname(String tagname) {
		this.tagname = tagname;
	}
	public String getModerator() {
		return moderator;
	}	
	public void setModerator(String moderator) {
		this.moderator = moderator;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail1() {
		return email1;
	}
	public void setEmail1(String email1) {
		this.email1 = email1;
	}
	public String getEmail2() {
		return email2;
	}
	public void setEmail2(String email2) {
		this.email2 = email2;
	}
		public String getEmail3() {
		return email3;
	}
	public void setEmail3(String email3) {
		this.email3 = email3;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}

}
