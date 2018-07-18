package com.weooh.clair;

import com.zaxxer.hikari.HikariDataSource;

public class ClairDataSource {

	public HikariDataSource dataSource;
	
	public ClairDataSource(HikariDataSource dataSource) {
		this.dataSource = dataSource;
	}
}
