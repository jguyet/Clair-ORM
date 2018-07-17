package com.xxx;

import com.xxx.annotation.Column;
import com.xxx.annotation.Primary;
import com.xxx.annotation.Table;

@Table("accounts")
public class Account {
	
	@Column("id")
	@Primary(autoIncrement = true)
	public String id;
	
	@Column("account")
	public String account;
	
	@Column("password")
	public String password;

}
