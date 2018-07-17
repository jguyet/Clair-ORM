package com.xxx;

import com.xxx.annotation.ObjectDAO;
import com.zaxxer.hikari.HikariDataSource;

@ObjectDAO
public class AccountDAO extends AbstractDAO<Account> {

	public AccountDAO(HikariDataSource dataSource) {
		super(dataSource);
	}

}
