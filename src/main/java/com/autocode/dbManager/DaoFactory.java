package com.autocode.dbManager;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
public class DaoFactory {



	@Bean
	@Primary
    public Dao primaryDao(@Autowired DataSource dataSource) {
        return new DbManager(dataSource);
    }

}
