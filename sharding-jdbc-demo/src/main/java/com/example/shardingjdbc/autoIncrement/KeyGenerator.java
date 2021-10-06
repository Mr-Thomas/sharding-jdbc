package com.example.shardingjdbc.autoIncrement;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.spi.keygen.ShardingKeyGenerator;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * @author ：Administrator
 * @description：TODO
 * @date ：2021/9/27 10:46
 */
@Component
@Slf4j
public class KeyGenerator implements ShardingKeyGenerator, ApplicationContextAware {

    private static IdWorker worker;

    @Override
    public Comparable<?> generateKey() {
        return worker.nextId();
    }

    @Override
    public String getType() {
        return "auto_increment";
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public void setProperties(Properties properties) {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        worker = applicationContext.getBean(IdWorker.class);
    }
}
