package com.hospital.appointment.config;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 核心：ShardingSphere-JDBC 编程式路由配置
 * 将逻辑表 biz_order 动态路由到物理表 biz_order_0 和 biz_order_1
 */
@Configuration
public class ShardingDataSourceConfig {

    // 动态读取原本写在 application.properties 里的配置
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    @Primary // 【必须】强制 MyBatis 优先使用我们接管的这个代理数据源
    public DataSource shardingDataSource() throws Exception {

        // 1. 配置物理数据源 (保留原有的 HikariCP 优良性能)
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMinimumIdle(5);
        dataSource.setMaximumPoolSize(20);
        dataSourceMap.put("ds0", dataSource); // 给数据源起个别名 ds0

        // 2. 配置针对 biz_order 表的分表规则：数据节点为 ds0.biz_order_0 到 ds0.biz_order_1
        ShardingTableRuleConfiguration orderTableRuleConfig =
                new ShardingTableRuleConfiguration("biz_order", "ds0.biz_order_${0..1}");

        // 3. 配置分片策略：指定参与分片的列为 patient_id
        orderTableRuleConfig.setTableShardingStrategy(
                new StandardShardingStrategyConfiguration("patient_id", "order_inline"));

        // 4. 配置分片算法：对 patient_id 取模 (奇数进表1，偶数进表0)
        Properties inlineProps = new Properties();
        inlineProps.setProperty("algorithm-expression", "biz_order_${patient_id % 2}");
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("INLINE", inlineProps);

        // 5. 组装整体分片规则
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(orderTableRuleConfig);
        shardingRuleConfig.getShardingAlgorithms().put("order_inline", algorithmConfig);

        // 6. 生成并返回被 ShardingSphere 代理的终极数据源！
        return ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, Collections.singleton(shardingRuleConfig), new Properties());
    }
}