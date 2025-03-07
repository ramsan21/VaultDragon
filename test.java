It looks like Hazelcast is creating threads within Tomcat, and when Tomcat shuts down, some Hazelcast-related threads are not being cleaned up properly, causing Tomcat to remain running even after a shutdown attempt.

Steps to Properly Destroy Hazelcast Threads on Tomcat Shutdown:
	1.	Ensure Hazelcast Instance is Properly Shutdown
Explicitly shut down the Hazelcast instance during Tomcat shutdown. If you are not already doing this, add it in the destroy() method of CustomDispatcherServlet.

@Override
public void destroy() {
    logger.info("CustomDispatcherServlet is being destroyed");

    logger.info("Shutting down Cache");
    Cache.getInstance().shutdown(); // Ensure cache shutdown

    logger.info("Destroy All Executor Services");
    ExecutorServiceFactory.destroyAll(); // Ensure executor services are shut down

    logger.info("Shutting down Hazelcast");
    HazelcastInstance hazelcastInstance = Hazelcast.getHazelcastInstanceByName("your-hazelcast-instance-name");
    if (hazelcastInstance != null) {
        hazelcastInstance.shutdown();
    }

    super.destroy();
}

Replace "your-hazelcast-instance-name" with the actual Hazelcast instance name.

	2.	Stop Hazelcast Managed Executors
Hazelcast may be creating thread pools internally. Shut them down explicitly:

HazelcastInstance hazelcastInstance = Hazelcast.getHazelcastInstanceByName("your-hazelcast-instance-name");
if (hazelcastInstance != null) {
    hazelcastInstance.getExecutorService("default").shutdown();
}


	3.	Ensure Tomcat Stops the Process
If Tomcat is not shutting down properly, force the process to terminate using:

ps -ef | grep tomcat
kill -9 <tomcat_process_id>

But ideally, resolving the thread leaks properly should prevent this.

	4.	Check for Thread Leaks using Apache Tomcat Leak Prevention
Add the following to your catalina.sh or catalina.bat to enable Tomcat thread leak detection:

-Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=true

This ensures that on shutdown, all references are cleared.

	5.	Set Hazelcast Property to Clean Up Threads
Try setting this Hazelcast property to ensure proper cleanup:

System.setProperty("hazelcast.shutdownhook.enabled", "true");


	6.	Check for Custom Thread Pools
If you are using Hazelcast’s ExecutorService, ensure all thread pools are shutdown before stopping Tomcat:

hazelcastInstance.getExecutorService("my-executor").shutdown();


	7.	Check for Hazelcast Client Thread Leaks
If using a Hazelcast client inside Tomcat, explicitly close it:

hazelcastClient.shutdown();



After applying these steps, try stopping Tomcat and see if the process exits cleanly. If it still hangs, check the logs for remaining active threads.