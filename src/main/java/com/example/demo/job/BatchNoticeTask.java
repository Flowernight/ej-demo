package com.example.demo.job;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import org.springframework.stereotype.Service;

/**
 * @author xulihua
 */
@Service
@Task(taskName="BatchNoticeTask", cron = "0 0/2 * * * ?", shardingTotalCount = 3)
public class BatchNoticeTask implements SimpleJob{

	@Override
	public void execute(ShardingContext shardingContext) {
		int count = shardingContext.getShardingTotalCount();
		System.out.println("分片数"+count);
		System.out.println(shardingContext.getShardingItem());
		try {
			System.out.println("----------BatchNoticeTask start");

			System.out.println("BatchNoticeTask end");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
