package com.messaging.rcs.configuration;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfiguration {

	Logger logger = LoggerFactory.getLogger(getClass());

	@Bean(name = "processExecutor")
	public TaskExecutor workExecutor() {
		logger.info("Message from the que");
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setThreadNamePrefix("Async-Queue-");
		threadPoolTaskExecutor.setCorePoolSize(3);
		threadPoolTaskExecutor.setMaxPoolSize(50);
		threadPoolTaskExecutor.setQueueCapacity(25000);
		threadPoolTaskExecutor.afterPropertiesSet();
		threadPoolTaskExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				logger.info("Message from the que rejected");
			}
		});
		threadPoolTaskExecutor.initialize();
		logger.info("ThreadPoolTaskExecutor set for the Application.");
		return threadPoolTaskExecutor;
	}

	@Bean(name = "leadUploadExecutor")
	public TaskExecutor leadUploadExecutor() {
		logger.info("Message from the que");
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setThreadNamePrefix("LeadUpload-Async-Queue-");
		threadPoolTaskExecutor.setCorePoolSize(3);
		threadPoolTaskExecutor.setMaxPoolSize(50);
		threadPoolTaskExecutor.setQueueCapacity(25000);
		threadPoolTaskExecutor.afterPropertiesSet();
		threadPoolTaskExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				logger.info("Message from the que rejected");
			}
		});
		threadPoolTaskExecutor.initialize();
		logger.info("ThreadPoolTaskExecutor set for the Application.");
		return threadPoolTaskExecutor;
	}

	@Bean(name = "blackListUploadFileExecutor")
	public TaskExecutor blackListUploadFileExecutor() {
		logger.info("Message from the que");
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setThreadNamePrefix("Blacklist-Async-Queue-");
		threadPoolTaskExecutor.setCorePoolSize(3);
		threadPoolTaskExecutor.setMaxPoolSize(50);
		threadPoolTaskExecutor.setQueueCapacity(25000);
		threadPoolTaskExecutor.afterPropertiesSet();
		threadPoolTaskExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				logger.info("Message from the que rejected");
			}
		});
		threadPoolTaskExecutor.initialize();
		logger.info("ThreadPoolTaskExecutor set for the Application.");
		return threadPoolTaskExecutor;
	}

    @Bean(name = "checkRcsBulkTaskExecutor")
    TaskExecutor checkRcsBulkExecutor() {
		ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
		threadPoolTaskExecutor.setThreadNamePrefix("checkRcsBulkTaskExecutor-Queue");
		threadPoolTaskExecutor.setCorePoolSize(3);
		threadPoolTaskExecutor.setMaxPoolSize(20);
		threadPoolTaskExecutor.setQueueCapacity(600);
		threadPoolTaskExecutor.afterPropertiesSet();
		logger.info("ThreadPoolTaskExecutor set for the Application.");
		return threadPoolTaskExecutor;
	}
}