package com.example.carpooling.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@PropertySource("classpath:application.properties")
public class HibernateConfig {
    private final String dbURL, dbUsername, dbPassword;

    @Autowired
    public HibernateConfig(Environment env) {
        this.dbURL = env.getProperty("database.url");
        this.dbUsername = env.getProperty("database.username");
        this.dbPassword = env.getProperty("database.password");
    }

    @Bean(name = "entityManagerFactory")
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(getDatasource());
        sessionFactory.setPackagesToScan("com.example.carpooling.models");
        sessionFactory.setHibernateProperties(getProperties());
        return sessionFactory;
    }

    @Bean
    public DataSource getDatasource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setDriverClassName("org.mariadb.jdbc.Driver");
        dataSource.setUrl(this.dbURL);
        dataSource.setUsername(this.dbUsername);
        dataSource.setPassword(this.dbPassword);

        return dataSource;
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
        properties.setProperty("hibernate.show_SQL", "true");
        properties.setProperty("hibernate.format_SQL", "true");

        return properties;
    }
}
