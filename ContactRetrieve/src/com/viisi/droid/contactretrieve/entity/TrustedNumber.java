package com.viisi.droid.contactretrieve.entity;

public class TrustedNumber implements IEntity {

	private Long id;
	private String number;

	public String getNumber() {
		return number;
	}

	public void setNumber(String passw) {
		this.number = passw;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return number;
	}

}
