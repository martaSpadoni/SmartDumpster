package progetto03.service;

import java.util.Date;

public class DailyLog {
	
	private final Date date;
	private double totalWeight;
	private int nDeposit;
	
	public DailyLog(Date date, double totalWeight, int nDeposit) {
		super();
		this.date = date;
		this.totalWeight = totalWeight;
		this.nDeposit = nDeposit;
	}
	
	public Date getDate() {
		return date;
	}
	public double getTotalWeight() {
		return totalWeight;
	}
	public int getnDeposit() {
		return nDeposit;
	}
	public void setTotalWeight(double totalWeight) {
		this.totalWeight = totalWeight;
	}
	public void setnDeposit(int nDeposit) {
		this.nDeposit = nDeposit;
	}
	
}
