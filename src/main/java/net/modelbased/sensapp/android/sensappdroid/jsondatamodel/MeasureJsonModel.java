package net.modelbased.sensapp.android.sensappdroid.jsondatamodel;

import java.util.ArrayList;
import java.util.List;

public class MeasureJsonModel {

	private String bn;
	private long bt;
	private String bu;
	private List<DataJsonModel> e;
	
	public MeasureJsonModel() {
		bn = new String();
		bu = new String();
		e = new ArrayList<DataJsonModel>();
	}
	
	public MeasureJsonModel(String bn, long bt, String bu) {
		this.bn = bn;
		this.bt = bt;
		this.bu = bu;
		e = new ArrayList<DataJsonModel>();
	}
	
	public MeasureJsonModel(String bn, long bt, String bu, List<DataJsonModel> e) {
		this.bn = bn;
		this.bt = bt;
		this.bu = bu;
		this.e = e; 
	}
	
	public String getBn() {
		return bn;
	}
	public void setBn(String bn) {
		this.bn = bn;
	}
	public long getBt() {
		return bt;
	}
	public void setBt(long bt) {
		this.bt = bt;
	}
	public String getBu() {
		return bu;
	}
	public void setBu(String bu) {
		this.bu = bu;
	}
	public List<DataJsonModel> getE() {
		return e;
	}
	public void setE(List<DataJsonModel> e) {
		this.e = e;
	}
	
	public MeasureJsonModel appendMeasure(int value, long time) {
		e.add(new DataJsonModel(value, time));
		return this;
	}
	
	public MeasureJsonModel appendMeasure(Measure measure) {
		if (!measure.getSensor().equals(bn)) {
			throw new IllegalArgumentException("Incompatible sensor name: " + measure.getSensor());
		} 
		e.add(new DataJsonModel(measure.getValue(), measure.getTime()));
		return this;
	}

	@Override
	public String toString() {
		return "MEASURE MODEL/ bn: " + bn + " - bt: " + bt + " - bu: " + bu;
	}
}
