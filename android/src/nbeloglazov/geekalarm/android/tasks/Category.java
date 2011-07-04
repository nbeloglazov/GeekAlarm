package nbeloglazov.geekalarm.android.tasks;

public class Category {
	
	private String code;
	private String name;
	
	public Category(String code, String description) {
		this.code = code;
		this.name = description;
	}
	
	public String getCode() {
		return code;
	}
	public String getName() {
		return name;
	}
	
	
}
