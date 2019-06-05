package com.weooh;

import com.weooh.clair.Clair;

public class Database {

	private Clair clair;
	
	private AccountDAO accountTable;
	
	public Database() {
		this.clair = new Clair();
	}
	
	public void initializeConnection() {
		this.clair.buildCredentials("localhost", 3306, "test", "jguyet", "xxx");
		this.clair.initializeConnection();
	}
	
	public void initializeDAO() {
		this.accountTable = this.clair.buildDAO(AccountDAO.class);
	}

	public AccountDAO getAccountTable() {
		return accountTable;
	}

	public void setAccountTable(AccountDAO accountTable) {
		this.accountTable = accountTable;
	}
	
}
