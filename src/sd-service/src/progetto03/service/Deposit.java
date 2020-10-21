package progetto03.service;


import java.util.Date;

public class Deposit {

	private final double weight;
	private final Date date;
	
	
	public Deposit(double weight, Date date) {
		super();
		this.weight = weight;
		this.date = date;
	}

	public double getWeight() {
		return weight;
	}

	public Date getDate() {
		return date;
	}

}
