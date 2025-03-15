Yes, instead of e.getStackTrace(), which returns an array of stack trace elements that might not be formatted correctly for logging, you should use:

log.info("Exception occurred in getDataForAuditLog: ", e);

Why?
	•	The logger will automatically format the stack trace properly.
	•	You will get the full stack trace without manually converting the array.

Alternatively, if you need to log it explicitly as a string, use:

log.info("Exception occurred in getDataForAuditLog: {}", ExceptionUtils.getStackTrace(e));

For this, you need to include Apache Commons Lang:

<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.12.0</version>
</dependency>

This ensures that the entire stack trace is printed in a human-readable format.