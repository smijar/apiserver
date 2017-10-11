package com.app.apiserver.core;

import jersey.repackaged.com.google.common.base.MoreObjects;

/**
 * the location
 * @author smijar
 *
 */
public class Loc {
	private double x;
	private double y;
	
	public Loc() {
		
	}
	
	public Loc(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	
	public String toString() {
		return MoreObjects.toStringHelper(this.getClass())
				.add("x", x)
				.add("y", y)
				.toString();
	}
}
