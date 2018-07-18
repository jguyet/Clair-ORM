package com.weooh;

import com.weooh.clair.AbstractDAO;
import com.weooh.clair.annotation.ObjectDAO;
import com.zaxxer.hikari.HikariDataSource;

@ObjectDAO
public class AccountDAO extends AbstractDAO<Account> {

	public AccountDAO(HikariDataSource dataSource) {
		super(dataSource);
	}

}
