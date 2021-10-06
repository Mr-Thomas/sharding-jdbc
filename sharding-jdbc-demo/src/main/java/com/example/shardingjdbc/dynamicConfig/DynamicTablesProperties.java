package com.example.shardingjdbc.dynamicConfig;

import lombok.Data;
import org.springframework.boot.SpringBootConfiguration;

/**
 * @author ：Administrator
 * @description：TODO
 * @date ：2021/9/27 15:00
 */
@Data
@SpringBootConfiguration
public class DynamicTablesProperties {
    String[] names = {"t_order"};
}
