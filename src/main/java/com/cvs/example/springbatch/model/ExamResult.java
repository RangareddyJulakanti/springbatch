package com.cvs.example.springbatch.model;

import org.joda.time.LocalDate;


public class ExamResult {
	
	private String studentName;
	private LocalDate dob;
	private Integer percentage;

	public String getStudentName() {
		return studentName;
	}

	public void setStudentName(String studentName) {
		this.studentName = studentName;
	}

	public LocalDate getDob() {
		return dob;
	}

	public void setDob(LocalDate dob) {
		this.dob = dob;
	}

	public Integer getPercentage() {
		return percentage;
	}

	public void setPercentage(Integer percentage) {
		this.percentage = percentage;
	}
}
