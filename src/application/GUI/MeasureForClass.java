package application.GUI;

public class MeasureForClass {
	private String measure;
	private String className;
	public String getMeasure() {
		return measure;
	}
	public void setMeasure(String measure) {
		this.measure = measure;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public MeasureForClass(String measure, String className) {
		super();
		this.measure = measure;
		this.className = className;
	}
	
	@Override
	public String toString() {
		return "MeasureForClass [measure=" + measure + ", className="
				+ className + "]";
	}
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(! (obj instanceof MeasureForClass))
			return false;
		MeasureForClass otherMeas = (MeasureForClass) obj;
		boolean equal = otherMeas.className.equals(className)&&otherMeas.measure.equals(measure);
		return equal;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((measure == null) ? 0 : measure.hashCode());
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());		
		return result;
	}
	
	
	
	
}
