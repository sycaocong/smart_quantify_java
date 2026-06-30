package com.smartquantify.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class DataSourceConfig {

    public static final ThreadLocal<String> DATASOURCE_KEY = ThreadLocal.withInitial(() -> "master");

    @Value("${spring.datasource.master.url:}")
    private String masterUrl;

    @Value("${spring.datasource.master.username:}")
    private String masterUsername;

    @Value("${spring.datasource.master.password:}")
    private String masterPassword;

    @Value("${spring.datasource.slave.url:}")
    private String slaveUrl;

    @Value("${spring.datasource.slave.username:}")
    private String slaveUsername;

    @Value("${spring.datasource.slave.password:}")
    private String slavePassword;

    @Bean(name = "masterDataSource")
    public DataSource masterDataSource() {
        log.info("Initializing master data source: {}", masterUrl);
        return DataSourceBuilder.create()
                .url(masterUrl)
                .username(masterUsername)
                .password(masterPassword)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }

    @Bean(name = "slaveDataSource")
    public DataSource slaveDataSource() {
        log.info("Initializing slave data source: {}", slaveUrl);
        return DataSourceBuilder.create()
                .url(slaveUrl)
                .username(slaveUsername)
                .password(slavePassword)
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }

    @Bean(name = "routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource masterDataSource,
            @Qualifier("slaveDataSource") DataSource slaveDataSource) {
        
        AbstractRoutingDataSource routingDataSource = new AbstractRoutingDataSource() {
            @Override
            protected Object determineCurrentLookupKey() {
                return DATASOURCE_KEY.get();
            }
        };

        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("master", masterDataSource);
        targetDataSources.put("slave", slaveDataSource);

        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.setDefaultTargetDataSource(masterDataSource);
        
        log.info("Routing data source configured with master/slave");
        return routingDataSource;
    }

    @Bean(name = "dataSource")
    @Primary
    public DataSource dataSource(@Qualifier("routingDataSource") DataSource routingDataSource) {
        log.info("Primary data source configured");
        return routingDataSource;
    }

    public static void useMaster() {
        DATASOURCE_KEY.set("master");
    }

    public static void useSlave() {
        DATASOURCE_KEY.set("slave");
    }

    public static void clear() {
        DATASOURCE_KEY.remove();
    }
}
