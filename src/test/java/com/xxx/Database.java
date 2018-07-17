package com.xxx;

public class Database {

	private Clair clair;
	
	private AccountDAO accountTable;
	
	public Database() {
		this.clair = new Clair();
	}
	
	public void initializeConnection() {
		this.clair.buildCredentials("localhost", 3306, "test", "root", "");
		this.clair.initializeConnection();
	}
	
	public void initializeDAO() {
		this.accountTable = (AccountDAO)this.clair.buildDAO(AccountDAO.class);
	}

	public AccountDAO getAccountTable() {
		return accountTable;
	}

	public void setAccountTable(AccountDAO accountTable) {
		this.accountTable = accountTable;
	}
	
}
