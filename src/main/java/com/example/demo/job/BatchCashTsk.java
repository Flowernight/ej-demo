package com.example.demo.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import org.springframework.stereotype.Service;

/**
 * Created by xulh on 2018/11/6.
 */
@Service
@Task(taskName = "BatchCashTsk", cron="0 0/2 * * * ?")
public class BatchCashTsk implements SimpleJob {
    @Override
    public void execute(ShardingContext shardingContext) {
        System.out.println("cash任务开始--");
    }
}
