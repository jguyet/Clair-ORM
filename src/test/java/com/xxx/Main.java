package com.xxx;

public class Main {

	public static void main(String[] args) {
		
		Database data = new Database();
		
		data.initializeConnection();
		data.initializeDAO();
		
		System.out.println("COUCOU");
		
		for (Account a : data.getAccountTable().find()) {
			System.out.println("Account id=" + a.id + " account=" + a.account + " password=" + a.password);
		}
		Account a = data.getAccountTable().findOne("id", 1);
		System.out.println("Account id=" + a.id + " account=" + a.account + " password=" + a.password);
	}
	
}
