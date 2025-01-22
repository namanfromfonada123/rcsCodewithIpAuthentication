package com.messaging.rcs.configuration;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestTemplate;


/**
 * Created by sbsingh on Nov/15/2020.
 */

@Configuration
public class JDBCConfig {

    @Autowired
    private Environment env;

    @Value("${dnd.data.driver-class-name:com.mysql.cj.jdbc.Driver}")
    private String driverName;

    @Value("${spring.datasource.url}")
    private String mysqlDatabaseUrl;
    @Value("${spring.datasource.username:root}")
    private String mysqlDatabaseUsername;

    @Value("${spring.datasource.password}")
    private String mysqlPassword;

    @Value("${spring.datasource.driver-class-name:com.mysql.jdbc.Driver}")
    private String mysqldriverName;


    @Bean(name = "mySqlDataSourceTemplate")
    public JdbcTemplate jdbcTemplate(@Qualifier("mySqlDataSource") DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "mySqlDataSource")
    @Primary
    public DataSource mySqlDataSource()
    {
        DataSource ds = new DataSource();
        ds.setDriverClassName(mysqldriverName);
        ds.setUrl(mysqlDatabaseUrl);
        ds.setUsername(mysqlDatabaseUsername);
        ds.setPassword(mysqlPassword);
        ds.setValidationQuery("select 1 from dual");
        ds.setTestOnBorrow(true);
        return ds;
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
