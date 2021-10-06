package com.example.shardingjdbc.service;

import com.example.shardingjdbc.model.Order;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;

import java.util.List;

public interface OrderService {

    void createSubTableIfAbsent(String logicTableName, List<String> collect, ShardingDataSource shardingDataSource);

    void inser(Order order);
}
