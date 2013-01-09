package com.viisi.droid.contactretrieve.entity;

public class Password implements IEntity {

	private Long id;
	private String passw;

	public String getPassw() {
		return passw;
	}

	public void setPassw(String passw) {
		this.passw = passw;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return passw;
	}

}
