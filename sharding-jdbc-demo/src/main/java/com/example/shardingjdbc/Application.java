package com.example.shardingjdbc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportResource;

/**
 * @author ：Administrator
 * @description：TODO
 * @date ：2021/6/27 15:37
 */
@SpringBootApplication
//@ImportResource("classpath*:sharding-jdbc.xml")/*spring引入sharding-jdbc配置文件*/
@MapperScan("com.example.shardingjdbc.dao")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
