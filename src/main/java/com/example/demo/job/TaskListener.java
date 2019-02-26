package com.example.demo.job;

import com.dangdang.ddframe.job.config.JobCoreConfiguration;
import com.dangdang.ddframe.job.config.JobRootConfiguration;
import com.dangdang.ddframe.job.config.simple.SimpleJobConfiguration;
import com.dangdang.ddframe.job.event.JobEventConfiguration;
import com.dangdang.ddframe.job.event.JobEventListener;
import com.dangdang.ddframe.job.event.JobEventListenerConfigurationException;
import com.dangdang.ddframe.job.event.rdb.JobEventRdbConfiguration;
import com.dangdang.ddframe.job.lite.api.JobScheduler;
import com.dangdang.ddframe.job.lite.api.listener.ElasticJobListener;
import com.dangdang.ddframe.job.lite.config.LiteJobConfiguration;
import com.dangdang.ddframe.job.reg.base.CoordinatorRegistryCenter;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperConfiguration;
import com.dangdang.ddframe.job.reg.zookeeper.ZookeeperRegistryCenter;
import com.google.gson.JsonObject;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by xulihua on 2018/10/30.
 */
@Service
public class TaskListener implements BeanPostProcessor,ApplicationListener<ApplicationEvent>, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private Set<TaskBean> beanSet = new HashSet<>();

    @Override
    public Object postProcessBeforeInitialization(Object o, String s) throws BeansException {
        return o;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //bean加载之后读取 @task 注解的服务进行初始化
        Object target = this.getTarget(bean);
        if (null != target) {
            Class<?> c = target.getClass();
//            if (taskDispatcher == null && bean instanceof TaskDispatcher) {
//                taskDispatcher = (TaskDispatcher) bean;
//            }
            if (!c.isAnnotationPresent(Task.class)) {
                return bean;
            }
            Annotation annotation = c.getAnnotation(Task.class);
            if (null != annotation) {
                TaskBean t = new TaskBean();
                t.setAnnotation(annotation);
                t.setBeanName(beanName);
                t.setBean(bean);
                beanSet.add(t);
            }
        }
        return bean;
    }
     @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        System.out.println("事件触发,beanset=");
        if (event instanceof ContextRefreshedEvent
                && ((ContextRefreshedEvent) event).getApplicationContext() == applicationContext) {
            if (beanSet.size() > 0) {
                for (TaskBean bean : beanSet) {
                    this.init(bean.getAnnotation(), bean.getBeanName(), bean.getBean());
                }
            }
        }
}

    //初始化任务并开启
    private void init(Annotation annotation, String beanName, Object bean) {

        //监控数据源配置
        JobEventConfiguration configuration = createjobEventConfig();
        //注册中心配置
        CoordinatorRegistryCenter registryCenter = createRegistryCenter();
        //任务配置
        LiteJobConfiguration liteJobConfiguration = this.createJobConfiguration(annotation, beanName, bean);
        Object o = this.getListener(annotation);
        //启动任务
        if (null != o) {
            new JobScheduler(registryCenter, liteJobConfiguration, (ElasticJobListener) o).init();
        } else {
            new JobScheduler(registryCenter, liteJobConfiguration, configuration).init();
        }
        System.out.println("初始化任务并开启成功");

    }

    //创建数据源
    private JobEventConfiguration createjobEventConfig() {
        BasicDataSource dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");//?&useUnicode=true&characterEncoding=utf-8
        dataSource.setUrl("jdbc:mysql://rm-bp1rzca790wavv1238o.mysql.rds.aliyuncs.com:3306/task");
        dataSource.setUsername("amm");
        dataSource.setPassword("Amm@ali2016");
//        dataSource.setUrl("jdbc:mysql://10.1.1.196:4012/rz_account");
//        dataSource.setUsername("rzp2p");
//        dataSource.setPassword("q.nzmg;R7fhfjghvgFom3>v");
        JobEventConfiguration eventConfiguration = new JobEventRdbConfiguration(dataSource);
        return eventConfiguration;
    }

    public CoordinatorRegistryCenter createRegistryCenter() {
        //这个为1个zk环境的下的1个namespace也可以有多个 1个namespace下有多个job
        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration("dev-aimama-20.sumpay.local:2181", "task1");
//        ZookeeperConfiguration zookeeperConfiguration = new ZookeeperConfiguration("127.0.0.1:2181", "task1");
//        zookeeperConfiguration.setDigest("digest");
        zookeeperConfiguration.setMaxSleepTimeMilliseconds(3000);
        zookeeperConfiguration.setMaxRetries(3);
        zookeeperConfiguration.setBaseSleepTimeMilliseconds(1000);
        CoordinatorRegistryCenter regCenter = new ZookeeperRegistryCenter(zookeeperConfiguration);
        regCenter.init();
        return regCenter;
    }

    //任务配置
    private LiteJobConfiguration createJobConfiguration(Annotation annotation, String beanName, Object bean) {
        Task task = (Task) annotation;
        if (null != task) {
            String cron = task.cron();
            int shardingTotalCount = task.shardingTotalCount();
            System.out.println("获取到的分片数="+shardingTotalCount);
            // 定义作业核心配置
            JobCoreConfiguration simpleCoreConfig = JobCoreConfiguration.newBuilder(beanName, cron, shardingTotalCount).build();
            // 定义SIMPLE类型配置
            SimpleJobConfiguration simpleJobConfig = new SimpleJobConfiguration(simpleCoreConfig, bean.getClass().getCanonicalName());
            // 定义Lite作业根配置
            JobRootConfiguration simpleJobRootConfig = LiteJobConfiguration.newBuilder(simpleJobConfig).build();

            // 创建作业配置
            return (LiteJobConfiguration) simpleJobRootConfig;
        }
        return null;
    }

    private Object getListener(Annotation annotation) {
        Task task = (Task) annotation;
        try {
            if (task.taskListener() != null && !task.taskListener().equals("")) {
                Class c = Class.forName(task.taskListener());
                Object obj = c.newInstance();
                return obj;
            }
        } catch (Exception e) {
            System.out.println("获取任务监听类出错={}"+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private Object getTarget(Object bean) {
        Object target = bean;
        while (target instanceof Advised) {
            try {
                target = ((Advised) bean).getTargetSource().getTarget();
            } catch (Exception var4) {
                target = null;
                break;
            }
        }
        return target;
    }

}
