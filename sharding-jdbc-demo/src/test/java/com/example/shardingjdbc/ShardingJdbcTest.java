package com.example.shardingjdbc;

import cn.hutool.json.JSONUtil;
import com.example.shardingjdbc.dao.OrderMapper;
import com.example.shardingjdbc.model.Order;
import com.example.shardingjdbc.model.OrderExample;
import com.example.shardingjdbc.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * @author ：Administrator
 * @description：TODO
 * @date ：2021/6/27 21:34
 */
@SpringBootTest
@Slf4j
public class ShardingJdbcTest {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    @Test
    public void dynamicTest() {
        Order order = new Order();
        order.setUserId(18);
        order.setOrderAmount(BigDecimal.TEN);
        order.setOrderStatus(1);

        LocalDateTime now = LocalDateTime.now().plusDays(1);
        Date date = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        order.setCreateTime(date);
        orderService.inser(order);
    }

    @Test
    public void test() {
        Order order = new Order();
        order.setUserId(18);
        order.setOrderAmount(BigDecimal.TEN);
        order.setOrderStatus(1);
        orderMapper.insertSelective(order);
    }

    @Test
    public void test1() {
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria().andUserIdEqualTo(18);
        List<Order> orders = orderMapper.selectByExample(orderExample);
//        Order order = orderMapper.selectByPrimaryKey(652512927563120640L);
        log.info("orders:{},order:{}", JSONUtil.toJsonStr(orders)/*, JSONUtil.toJsonStr(order)*/);
    }

    @Test
    public void queryTest() {
        LocalDateTime now = LocalDateTime.now().plusDays(-1);
        Date date = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        List<Order> orders = orderMapper.queryByCreateTime(date, new Date());
        log.info("orders:{}", JSONUtil.toJsonStr(orders));
    }
}
