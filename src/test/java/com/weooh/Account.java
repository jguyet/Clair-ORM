package com.weooh;

import com.weooh.clair.annotation.Column;
import com.weooh.clair.annotation.Primary;
import com.weooh.clair.annotation.Table;
import com.weooh.clair.validation.annotation.ClairValidation;

@Table("accounts")
public class Account {
	
	@Column("id")
	@Primary(autoIncrement = true)
	public String id;
	
	@Column("account")
	@ClairValidation(method = CheckEmailValidation.class)
	public String account;
	
	@Column("password")
	public String password;
	
	@Column("right")
	public int right;

}
