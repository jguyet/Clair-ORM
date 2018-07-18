package com.weooh;

import java.util.ArrayList;

public class Main {

	public static void main(String[] args) {
		
		Database data = new Database();
		
		data.initializeConnection();
		data.initializeDAO();
		
		for (Account a : data.getAccountTable().find()) {
			System.out.println("Account id=" + a.id + " account=" + a.account + " password=" + a.password);
		}
		Account a = data.getAccountTable().findOne("id", 1);
		System.out.println("Account id=" + a.id + " account=" + a.account + " password=" + a.password);
		
		Account test = new Account();
		
		test.account = "TEST";
		test.id = "eherheh69";
		test.password = "XD";
		
		ArrayList<String> errors = data.getAccountTable().save(test, false);
		System.out.println(errors.toString());
		
		if (data.getAccountTable().exists(test)) {
			System.out.println("T exists1");
		} else {
			System.out.println("T doesn't exists1");
		}
		
		data.getAccountTable().delete(test);
		
		if (data.getAccountTable().exists(test)) {
			System.out.println("T exists");
		} else {
			System.out.println("T doesn't exists");
		}
	}
	
}
