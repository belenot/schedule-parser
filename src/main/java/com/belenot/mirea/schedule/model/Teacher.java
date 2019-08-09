package com.belenot.mirea.schedule.model;

public class Teacher {
    private int id;
    private String name;
    private String surname;
    private String patronymic;
    private String shortName;
    public int getId() {
	return id;
    }
    public Teacher setId(int id) {
	this.id = id;
	return this;
    }
    public String getName() {
	return name;
    }
    public Teacher setName(String name) {
	this.name = name;
	return this;
    }
    public String getSurname() {
	return surname;
    }
    public Teacher setSurname(String surname) {
	this.surname = surname;
	return this;
    }
    public String getPatronymic() {
	return patronymic;
    }
    public Teacher setPatronymic(String patronymic) {
	this.patronymic = patronymic;
	return this;
    }
    public String getShortName() {
	return shortName;
    }
    public Teacher setShortName(String shorName) {
	this.shortName = shorName;
	return this;
    }
    
}
