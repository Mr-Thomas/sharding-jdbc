package com.example.shardingjdbc.dynamicConfig;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

/**
 * @author ：Administrator
 * @description：TODO精准分表算法
 * @date ：2021/9/27 11:44
 */
@Slf4j
public class DatePreciseAlgorithm implements PreciseShardingAlgorithm<Date> {

    @SneakyThrows
    @Override
    public String doSharding(Collection<String> collection, PreciseShardingValue<Date> preciseShardingValue) {
        Date value = preciseShardingValue.getValue();
        String suffix = dateFormat("yyyyMMdd", value);
        StringBuffer tableName = new StringBuffer();
        String s = tableName.append(preciseShardingValue.getLogicTableName()).append("_").append(suffix).toString();
        log.warn("执行操作表名：{}", s);
        return s;
    }

    public static String dateFormat(String format, Date date) {
        return new SimpleDateFormat(format).format(date);
    }
}
