package com.weooh;

import com.weooh.clair.AbstractDAO;
import com.weooh.clair.ClairDataSource;
import com.weooh.clair.annotation.ObjectDAO;

import java.sql.SQLException;

@ObjectDAO
public class AccountDAO extends AbstractDAO<Account> {

	public AccountDAO(ClairDataSource dataSource) throws SQLException {
		super(dataSource);
	}

}
