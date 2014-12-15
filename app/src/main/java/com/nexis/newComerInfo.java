package com.nexis;

import org.joda.time.DateTime;

public class newComerInfo {
	public String fName, lName, gen, dd, hPhone, cPhone, em, ads, pCode, ct, sch, gy, chris, bap, cYear, bYear;
	
	public newComerInfo(String firstName, String lastName, String gender, String date, String homePhone, String cellPhone, String email, String address, String postalCode, 
					    String city, String school, String gradeYear, String christian, String baptized, String christianYear,String baptizedYear) {
		fName = firstName;
		lName = lastName;
		gen = gender;
		dd = date;
		hPhone = homePhone;
		cPhone = cellPhone;
		em = email;
		ads = address;
		pCode = postalCode;
		ct = city;
		sch = school;
		gy = gradeYear;
		chris = christian;
		bap = baptized;
		cYear = christianYear;
		bYear = baptizedYear;
	}
}
