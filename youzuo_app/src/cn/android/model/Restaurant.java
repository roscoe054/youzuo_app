package cn.android.model;

public class Restaurant {
	private String name;
	private int evaluation;
	private int waitingNum;
	private String location;
	private String tel;
	private String introduct;

	public Restaurant() {
		super();
	}

	public Restaurant(String name, int eva, int wai, String loc, String tel,
			String intro) {
		super();
		this.name = name;
		this.evaluation = eva;
		this.waitingNum = wai;
		this.location = loc;
		this.tel = tel;
		this.introduct = intro;
	}

	// name
	public String getName() {
		return name;
	}

	public void setId(String name) {
		this.name = name;
	}

	// evaluation
	public int getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(int evaluation) {
		this.evaluation = evaluation;
	}

	// waitingNum
	public int getWaitingNum() {
		return waitingNum;
	}

	public void setWaitingNum(int waitingNum) {
		this.waitingNum = waitingNum;
	}

	// location
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	// tel
	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	// introduct
	public String getIntroduct() {
		return introduct;
	}

	public void setIntroduct(String introduct) {
		this.introduct = introduct;
	}
}
