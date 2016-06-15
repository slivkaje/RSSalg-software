package application.GUI;

public class ClassNameElement {
	private String className;
	private int noLabeled;
	
	public ClassNameElement(String className, int noLabeled) {
		this.className = className;
		this.noLabeled = noLabeled;
	}
	
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public int getNoLabeled() {
		return noLabeled;
	}
	public void setNoLabeled(int noLabeled) {
		this.noLabeled = noLabeled;
	}
}
