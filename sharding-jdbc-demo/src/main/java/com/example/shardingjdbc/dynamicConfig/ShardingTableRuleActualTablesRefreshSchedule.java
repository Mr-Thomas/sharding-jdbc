package com.example.shardingjdbc.dynamicConfig;

import com.example.shardingjdbc.service.OrderService;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author ：Administrator
 * @description：TODO动态刷新数据节点
 * @date ：2021/9/27 14:58
 */
@Slf4j
@Component
@EnableScheduling
public class ShardingTableRuleActualTablesRefreshSchedule implements InitializingBean {
    @Autowired
    private DynamicTablesProperties dynamicTables;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private OrderService orderService;

    @Scheduled(cron = "0/1 * * * * ?")
    public void refreshActualDataNodes() {
        try {
            ShardingDataSource shardingDataSource = (ShardingDataSource) this.dataSource;
            if (dynamicTables.getNames() == null || dynamicTables.getNames().length == 0) {
                log.warn("dynamic.table.names为空");
                return;
            }
            for (int i = 0; i < dynamicTables.getNames().length; i++) {
                Set<String> actualTables = new HashSet<>();
                Map<DataNode, Integer> dataNodeIndexMap = Maps.newHashMap();
                String dynamicTableName = dynamicTables.getNames()[i];
                ShardingRule shardingRule = shardingDataSource.getRuntimeContext().getRule();
                TableRule tableRule = shardingRule.getTableRule(dynamicTableName);
                List<DataNode> dataNodes = tableRule.getActualDataNodes();
                AtomicInteger index = new AtomicInteger(0);
                dataNodes.forEach(dataNode -> {
                    actualTables.add(dataNode.getTableName());
                    if (index.intValue() == 0) {
                        dataNodeIndexMap.put(dataNode, 0);
                    } else {
                        dataNodeIndexMap.put(dataNode, index.intValue());
                    }
                    index.incrementAndGet();
                });

                Field actualDataNodesField = TableRule.class.getDeclaredField("actualDataNodes");
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(actualDataNodesField, actualDataNodesField.getModifiers() & ~Modifier.FINAL);
                actualDataNodesField.setAccessible(true);
                LocalDateTime localDateTime = LocalDateTime.of(2021, 10, 4, 0, 0, new Random().nextInt(59));
                LocalDateTime now = LocalDateTime.now();
                String dataSourceName = dataNodes.get(0).getDataSourceName();
                String logicTableName = tableRule.getLogicTable();
                List<DataNode> newDataNodes = new ArrayList<>();
                while (true) {
                    StringBuilder stringBuilder = new StringBuilder()
                            .append(dataSourceName)
                            .append(".")
                            .append(logicTableName)
                            .append("_")
                            .append(localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                    DataNode dataNode = new DataNode(stringBuilder.toString());
                    newDataNodes.add(dataNode);
                    localDateTime = localDateTime.plusDays(1L);
                    if (localDateTime.isAfter(now)) {
                        StringBuilder stringBuilder1 = new StringBuilder()
                                .append(dataSourceName)
                                .append(".")
                                .append(logicTableName)
                                .append("_")
                                .append(localDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                        DataNode dataNode1 = new DataNode(stringBuilder1.toString());
                        newDataNodes.add(dataNode1);
                        break;
                    }
                }
                actualDataNodesField.set(tableRule, newDataNodes);
                // 动态刷新：actualTablesField
                Field actualTablesField = TableRule.class.getDeclaredField("actualTables");
                actualTablesField.setAccessible(true);
                actualTablesField.set(tableRule, actualTables);
                // 动态刷新：dataNodeIndexMapField
                Field dataNodeIndexMapField = TableRule.class.getDeclaredField("dataNodeIndexMap");
                dataNodeIndexMapField.setAccessible(true);
                dataNodeIndexMapField.set(tableRule, dataNodeIndexMap);
                // 动态刷新：datasourceToTablesMapField
                Map<String, Collection<String>> datasourceToTablesMap = Maps.newHashMap();
                datasourceToTablesMap.put(dataSourceName, actualTables);
                Field datasourceToTablesMapField = TableRule.class.getDeclaredField("datasourceToTablesMap");
                datasourceToTablesMapField.setAccessible(true);
                datasourceToTablesMapField.set(tableRule, datasourceToTablesMap);

                createSubTableIfAbsent(logicTableName, newDataNodes, shardingDataSource);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSubTableIfAbsent(String logicTableName, List<DataNode> dataNodes, ShardingDataSource shardingDataSource) {
        LocalDateTime localDateTime = LocalDateTime.now();
        LocalDate localDate = localDateTime.toLocalDate();
        String yyyyMMdd = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<String> collect = dataNodes.stream().map(DataNode::getTableName)
                .filter(s -> Long.parseLong(s.substring(s.lastIndexOf("_") + 1)) >= Long.parseLong(yyyyMMdd))
                .collect(Collectors.toList());
        orderService.createSubTableIfAbsent(logicTableName, collect, shardingDataSource);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        refreshActualDataNodes();
    }
}
