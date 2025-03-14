In Spring Batch, if your batch job runs multiple times and encounters an exception repeatedly, but you want to log the exception only once, you can handle it in several ways:

⸻

1. Use a Global Flag (Singleton) to Log Exception Once

You can maintain a static flag in a singleton ErrorHandler class to track whether the exception has been logged.

Create a Singleton Error Logger

import java.util.concurrent.atomic.AtomicBoolean;

public class ErrorLogger {
    private static final AtomicBoolean hasLogged = new AtomicBoolean(false);

    public static void logOnce(Logger logger, String message, Throwable ex) {
        if (hasLogged.compareAndSet(false, true)) {
            logger.error(message, ex);
        }
    }
}

Use it in Your StepListener or JobListener

@Component
public class BatchJobListener extends JobExecutionListenerSupport {
    private static final Logger logger = LoggerFactory.getLogger(BatchJobListener.class);

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus() == BatchStatus.FAILED) {
            jobExecution.getAllFailureExceptions().forEach(ex -> 
                ErrorLogger.logOnce(logger, "Batch job failed!", ex)
            );
        }
    }
}

✅ This ensures that the exception is logged only once, even if the job fails multiple times.

⸻

2. Use a Custom ExceptionHandler in StepListener

If you want to handle it at the step level, implement StepExecutionListener:

@Component
public class StepExceptionListener implements StepExecutionListener {
    private static final Logger logger = LoggerFactory.getLogger(StepExceptionListener.class);
    private static boolean hasLogged = false;

    @Override
    public void afterStep(StepExecution stepExecution) {
        if (!hasLogged && stepExecution.getStatus() == BatchStatus.FAILED) {
            stepExecution.getFailureExceptions().forEach(ex -> 
                ErrorLogger.logOnce(logger, "Step failed!", ex)
            );
            hasLogged = true; // Prevent further logging
        }
    }
}

✅ This ensures that the exception is logged only once per step execution.

⸻

3. Use a RetryTemplate with CircuitBreaker to Prevent Repeated Logging

If the exception is being thrown due to a retry mechanism, you can use Spring Retry with a Circuit Breaker:

Add Spring Retry Dependency

<dependency>
    <groupId>org.springframework.retry</groupId>
    <artifactId>spring-retry</artifactId>
</dependency>

Wrap Your Logic with RetryTemplate

import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.policy.SimpleRetryPolicy;

public class BatchRetryHandler {
    private static final Logger logger = LoggerFactory.getLogger(BatchRetryHandler.class);

    public static void executeWithRetry(Runnable task) {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(1)); // Retry only once

        try {
            retryTemplate.execute(context -> {
                task.run();
                return null;
            });
        } catch (Exception ex) {
            ErrorLogger.logOnce(logger, "Exception occurred during batch execution!", ex);
        }
    }
}

✅ This ensures that even if the exception keeps occurring, it will only be logged once due to the retry policy.

⸻

4. Use ItemProcessor to Filter Out Repeated Errors

If the exception happens in an ItemProcessor, you can filter duplicate failures using a Set:

@Component
public class UniqueErrorItemProcessor implements ItemProcessor<MyEntity, MyEntity> {
    private static final Set<String> loggedErrors = Collections.synchronizedSet(new HashSet<>());
    private static final Logger logger = LoggerFactory.getLogger(UniqueErrorItemProcessor.class);

    @Override
    public MyEntity process(MyEntity item) throws Exception {
        try {
            // Your processing logic
            return item;
        } catch (Exception ex) {
            String errorKey = ex.getClass().getName() + ":" + ex.getMessage();
            if (loggedErrors.add(errorKey)) {
                logger.error("Processing error: " + errorKey, ex);
            }
            throw ex;
        }
    }
}

✅ This ensures that each unique exception is logged only once.

⸻

Which Solution Should You Choose?

Scenario	Solution
Exception should be logged once per job	Use JobExecutionListener with a static flag.
Exception should be logged once per step	Use StepExecutionListener.
Exception occurs in retried operations	Use RetryTemplate.
Exception occurs in ItemProcessor	Use a Set to track logged exceptions.

Would you like help integrating any of these solutions into your existing Spring Batch job?