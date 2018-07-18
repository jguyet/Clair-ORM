package com.weooh;

import com.weooh.clair.AbstractDAO;
import com.weooh.clair.ClairDataSource;
import com.weooh.clair.annotation.ObjectDAO;

@ObjectDAO
public class AccountDAO extends AbstractDAO<Account> {

	public AccountDAO(ClairDataSource dataSource) {
		super(dataSource);
	}

}
