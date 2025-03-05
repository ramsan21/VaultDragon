It looks like your logback.xml configuration is not directing logs to a separate file as expected. Here are some possible reasons and steps to troubleshoot:

1. Check If the Logback Configuration Is Loaded
	•	You have set -Dlogback.configurationFile=/apps/tomcat/instances/TEST/logback.xml in your startup script.
	•	To verify if Logback is loading the correct file, add the following to your logback.xml:

<statusListener class="ch.qos.logback.core.status.OnConsoleStatusListener" />


	•	Restart your Tomcat instance and check if Logback logs any errors in the console.

2. Verify File Path Permissions
	•	Ensure that /apps/support/logs/tomcat/TEST/ exists and the user running Tomcat has write permissions to this directory.
	•	Try creating a test file manually:

touch /apps/support/logs/tomcat/TEST/test.log


	•	If you get a permission error, adjust the folder permissions:

chmod -R 755 /apps/support/logs/tomcat/TEST/
chown -R tomcat_user:tomcat_group /apps/support/logs/tomcat/TEST/



3. Ensure Environment Variables Are Set
	•	Your logback.xml uses ${LOG_DIR} and ${APP_LOG_FILE}.
	•	Before starting Tomcat, check if these variables are set:

echo $LOG_DIR
echo $APP_LOG_FILE


	•	If they are missing, export them in your setenv.sh:

export LOG_DIR="/apps/support/logs/tomcat/TEST/"
export APP_LOG_FILE="test"



4. Check for Logback Errors in Catalina Logs
	•	If Logback fails to initialize, errors may appear in catalina.out:

tail -f /apps/tomcat/instances/TEST/logs/catalina.out



5. Try a Basic Logback Configuration
	•	To isolate the issue, temporarily replace your logback.xml with this minimal configuration:

<configuration>
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>/apps/support/logs/tomcat/TEST/logs/app.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>


	•	Restart Tomcat and check if /apps/support/logs/tomcat/TEST/logs/app.log is created.

6. Check for Conflicting Logging Implementations
	•	If you have both Logback and another logging framework (e.g., Log4j, JUL), they might conflict.
	•	Run:

find /apps/tomcat/instances/TEST/lib -name "logback*.jar"
find /apps/tomcat/instances/TEST/lib -name "log4j*.jar"


	•	If both exist, ensure Logback is the only active logger.

Try these steps and let me know if the issue persists.