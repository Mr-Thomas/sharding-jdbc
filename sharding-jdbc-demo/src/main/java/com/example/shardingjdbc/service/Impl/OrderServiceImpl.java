package com.example.shardingjdbc.service.Impl;

import com.example.shardingjdbc.dao.OrderMapper;
import com.example.shardingjdbc.model.Order;
import com.example.shardingjdbc.service.OrderService;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.List;

/**
 * @author ：Administrator
 * @description：TODO
 * @date ：2021/10/6 15:11
 */
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Value(value = "${spring.shardingsphere.datasource.ds01.driver-class-name}")
    private String driver;
    @Value(value = "${spring.shardingsphere.datasource.ds01.jdbcUrl}")
    private String url;
    @Value(value = "${spring.shardingsphere.datasource.ds01.username}")
    private String userName;
    @Value(value = "${spring.shardingsphere.datasource.ds01.password}")
    private String passWord;

    @Override
    public void createSubTableIfAbsent(String logicTableName, List<String> partitionTableName, ShardingDataSource shardingDataSource) {
        try {
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, userName, passWord);
            for (String tableName : partitionTableName) {
                ResultSet resultSet = connection.getMetaData().getTables(null, null, tableName, null);
                if (resultSet.next()) {
                    continue;
                }
                Statement statement = connection.createStatement();
                try {
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + "(LIKE " + logicTableName + ");");
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    statement.close();
                    connection.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void inser(Order order) {
        orderMapper.insertSelective(order);
    }
}
