package com.example.demo.job;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 任务注解在任务执行类中需要加上此注解用来对task 做初始化
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Task {

    /**
     * 任务名称
     *
     * @return
     */
    String taskName() default "";

    /**
     * spring 定时器配置 必填
     *
     * @return
     */
    String cron();

    /**
     * 任务class xxx.class.getCanonicalName()获取
     *
     * @return
     */
    String taskClass() default "";

    /**
     * 分片总数量
     *
     * @return
     */
    int shardingTotalCount() default 1;

    /**
     * 监听类型
     *
     * @return
     */
    String taskListener() default "";

}
