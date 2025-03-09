If System.exit(0) was the only way to stop your Hazelcast instance, it means that something in your system was preventing the JVM from shutting down properly. Let’s debug the possible causes.

⸻

🔍 Possible Reasons Why Hazelcast Didn’t Shutdown Properly

Even after calling instance.shutdown(), Hazelcast.shutdownAll(), or HazelcastClient.shutdownAll(), if the process was still alive, it suggests:

1. Background Threads Keeping JVM Alive

Hazelcast runs several background non-daemon threads that might be keeping the process alive:
	•	IO Threads (Networking, Discovery)
	•	Heartbeat/Monitoring Threads
	•	Executor Services (Scheduled Tasks, Event Listeners)
	•	Cluster Rebalancing Tasks

🛠 How to Detect Hazelcast Threads

Before calling System.exit(0), print all running threads:

Thread.getAllStackTraces().keySet()
    .forEach(thread -> log.info("Active Thread: " + thread.getName() + " | State: " + thread.getState()));

Look for any Hazelcast-related threads that are still active.

⸻

2. Hazelcast Client Still Connected

If you are using Hazelcast in client mode (HazelcastClient.newHazelcastClient()), it may not be shutting down properly.

✅ Fix: Ensure client shutdown explicitly

if (instance instanceof HazelcastInstance) {
    instance.shutdown();
} else if (instance instanceof HazelcastClientInstance) {
    ((HazelcastClientInstance) instance).shutdown();
}

HazelcastClient.shutdownAll();
Hazelcast.shutdownAll();



⸻

3. Shutdown Hook Not Triggering

If your shutdown hook is not getting executed on JVM exit, it can leave Hazelcast running.

✅ Fix: Manually add a shutdown hook

Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    log.info("JVM Shutdown Hook: Stopping Hazelcast...");
    Hazelcast.shutdownAll();
}));



⸻

4. Stale Network Connections Preventing Shutdown

If Hazelcast has open network sockets (e.g., TCP connections to other nodes), it can prevent the JVM from stopping.

✅ Fix: Enable aggressive shutdown mode

hazelcastInstance.getLifecycleService().terminate();

This forces immediate shutdown without waiting for operations to complete.

⸻

5. Force Shutdown Hazelcast Executors

If Hazelcast is running executor services, they might still be active.

✅ Fix: Shut down executors manually

HazelcastInstance hzInstance = instance;
if (hzInstance != null) {
    hzInstance.getExecutorService("default").shutdown();
}



⸻

🛠 Debugging Next Steps

1️⃣ Print all active threads before shutdown

Thread.getAllStackTraces().keySet()
    .forEach(thread -> log.info("Thread: " + thread.getName()));

2️⃣ Check if Hazelcast.getAllHazelcastInstances() is empty after shutdown

log.info("Remaining Hazelcast instances: " + Hazelcast.getAllHazelcastInstances());

3️⃣ Ensure client and member shutdown separately

instance.shutdown();
Hazelcast.shutdownAll();
HazelcastClient.shutdownAll();

4️⃣ Force terminate if shutdown fails

System.exit(0);



⸻

Final Thoughts

🚀 Since System.exit(0) worked for you, it’s clear that some Hazelcast threads were still running. Try the debugging steps above to pinpoint the issue.

Would you like me to help analyze the running threads further?