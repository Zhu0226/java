package com.hospital.appointment.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 核心：ShardingSphere-JDBC 编程式路由配置
 */
@Configuration
public class ShardingDataSourceConfig {

    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource shardingDataSource() throws Exception {

        // 1. 配置物理数据源 (HikariCP)
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMinimumIdle(5);
        dataSource.setMaximumPoolSize(20);
        dataSourceMap.put("ds0", dataSource);

        // 2. 配置针对 biz_order 表的分表规则
        ShardingTableRuleConfiguration orderTableRuleConfig =
                new ShardingTableRuleConfiguration("biz_order", "ds0.biz_order_${0..1}");
        orderTableRuleConfig.setTableShardingStrategy(
                new StandardShardingStrategyConfiguration("patient_id", "order_inline"));

        Properties inlineProps = new Properties();
        inlineProps.setProperty("algorithm-expression", "biz_order_${patient_id % 2}");
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("INLINE", inlineProps);

        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(orderTableRuleConfig);
        shardingRuleConfig.getShardingAlgorithms().put("order_inline", algorithmConfig);

        // 3. 配置单表规则 (扫描所有节点的所有普通表)
        SingleRuleConfiguration singleRuleConfig = new SingleRuleConfiguration(Collections.singletonList("*.*"), "ds0");

        // 4. 【终极修复】：必须传入真实的物理库名！并开启 SQL 打印
        Properties props = new Properties();
        props.setProperty("sql-show", "true"); // 开启真实的 SQL 打印，方便你观察它的路由轨迹

        return ShardingSphereDataSourceFactory.createDataSource(
                "hospital_appointment_pro", // <-- 关键修复：打破 logic_db 魔咒，指定真实库名
                dataSourceMap,
                Arrays.asList(shardingRuleConfig, singleRuleConfig),
                props
        );
    }
}