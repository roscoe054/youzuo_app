package cn.android.model;

public class Reservation {
	private String name;
	private String key;
	private int frontNum;

	public Reservation() {
		super();
	}

	public Reservation(String key,String name,int frontNum) {
		super();
		this.name = name;
		this.key = key;
		this.frontNum = frontNum;
	}

	// name
	public String getName() {
		return name;
	}

	public void setId(String name) {
		this.name = name;
	}

	// key
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	// evaluation
	public int getFrontNum() {
		return frontNum;
	}

	public void setFrontNum(int frontNum) {
		this.frontNum = frontNum;
	}
}
