package com.example.shardingjdbc.dynamicConfig;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author ：Administrator
 * @description：TODO
 * @date ：2021/9/27 11:44
 */
@Slf4j
public class DateRangeAlgorithm implements RangeShardingAlgorithm<Date> {

    @Override
    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Date> rangeShardingValue) {
        Collection<String> tableList = new ArrayList<>();
        Range<Date> valueRange = rangeShardingValue.getValueRange();
        Date lowerEndpoint = valueRange.lowerEndpoint();
        Date upperEndpoint = valueRange.upperEndpoint();
        Date now = new Date();
        if (lowerEndpoint.after(now)) {
            lowerEndpoint = now;
        }
        if (upperEndpoint.after(now)) {
            upperEndpoint = now;
        }
        Collection<String> tables = getRoutTable(rangeShardingValue.getLogicTableName(), lowerEndpoint, upperEndpoint);
        if (CollectionUtils.isNotEmpty(tables)) {
            tableList.addAll(tables);
        }
        return tableList;
    }

    private Collection<String> getRoutTable(String logicTableName, Date startTime, Date endTime) {
        Set<String> rouTables = new HashSet<>();
        if (startTime != null && endTime != null) {
            List<String> rangeNameList = getRangeNameList(startTime, endTime);
            for (String YearMonth : rangeNameList) {
                rouTables.add(logicTableName + "_" + YearMonth);
            }
        }
        return rouTables;
    }

    private static List<String> getRangeNameList(Date startTime, Date endTime) {
        List<String> result = Lists.newArrayList();
        // 定义日期实例
        Calendar dd = Calendar.getInstance();

        dd.setTime(startTime);

        while (dd.getTime().before(endTime)) {
            result.add(dateFormat("yyyyMMdd", dd.getTime()));
            // 进行当前日期按月份 + 1
//            dd.add(Calendar.MONTH, 1);
            dd.add(Calendar.DAY_OF_MONTH, 1);
        }
        return result;
    }


    private boolean containTableName(Set<String> suffixList, String tableName) {
        boolean flag = false;
        for (String s : suffixList) {
            if (tableName.endsWith(s)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public static TreeSet<String> suffixListForRange(String lowerSuffix, String upperSuffix) {
        TreeSet<String> suffixList = new TreeSet<>();
        if (lowerSuffix.equals(upperSuffix)) { //上下界在同一张表
            suffixList.add(lowerSuffix);
        } else {  //上下界不在同一张表  计算间隔的所有表
            String tempSuffix = lowerSuffix;
            while (!tempSuffix.equals(upperSuffix)) {
                suffixList.add(tempSuffix);
                String[] ym = tempSuffix.split("_");
                Date tempDate = parse(ym[0] + (ym[1].length() == 1 ? "0" + ym[1] : ym[1]), "yyyyMM");
                Calendar cal = Calendar.getInstance();
                cal.setTime(tempDate);
                cal.add(Calendar.MONTH, 1);
                tempSuffix = dateFormat("yyyyMMdd", cal.getTime());
            }
            suffixList.add(tempSuffix);
        }
        return suffixList;
    }

    public static Date parse(String date, String pattern) {
        try {
            return org.apache.commons.lang3.time.DateUtils.parseDate(date, pattern);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String dateFormat(String format, Date date) {
        return new SimpleDateFormat(format).format(date);
    }

}
