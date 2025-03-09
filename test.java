Issues with Hazelcast Shutdown in Your Code

Your current shutdown method calls:

instance.shutdown();
Hazelcast.shutdownAll();
HazelcastClient.shutdownAll();

However, the process is still lingering, meaning something is keeping the JVM alive.

Potential Reasons
	1.	Hazelcast Client Doesn’t Shutdown Completely
	•	If instance is a Hazelcast client, calling instance.shutdown() only disconnects it but does not stop cluster members.
	•	Hazelcast.shutdownAll() works for Hazelcast members but not for clients.
	•	HazelcastClient.shutdownAll() is the correct way to ensure all clients are shutdown.
	2.	Hazelcast Threads Keeping JVM Alive
	•	Hazelcast may have lingering threads (e.g., heartbeat, event listener, async tasks) preventing JVM shutdown.
	3.	Shutdown Hook Not Triggered Properly
	•	The JVM may still be waiting for background Hazelcast tasks.
	4.	Lingering Hazelcast Resources (IO, Networking)
	•	Open sockets, listeners, or Hazelcast executors might still be running.

⸻

✔️ Corrected Shutdown Code

To properly shut down all Hazelcast clients and members, modify your shutdown method as follows:

@Override
public void shutdown() {
    log.info("Shutting down Hazelcast...");

    if (instance != null) {
        try {
            log.info("Shutting down Hazelcast instance...");
            instance.shutdown();
        } catch (Exception e) {
            log.error("Error shutting down Hazelcast instance", e);
        }
    }

    try {
        log.info("Shutting down all Hazelcast members...");
        Hazelcast.shutdownAll();
    } catch (Exception e) {
        log.error("Error shutting down Hazelcast members", e);
    }

    try {
        log.info("Shutting down all Hazelcast clients...");
        HazelcastClient.shutdownAll();
    } catch (Exception e) {
        log.error("Error shutting down Hazelcast clients", e);
    }

    log.info("Hazelcast shutdown complete.");
}



⸻

✔️ Steps to Verify Proper Shutdown

After calling shutdown(), check:
	1.	Confirm No Active Hazelcast Instances

if (Hazelcast.getAllHazelcastInstances().isEmpty()) {
    log.info("All Hazelcast instances have been shut down.");
} else {
    log.warn("Some Hazelcast instances are still running: " + Hazelcast.getAllHazelcastInstances());
}

This will print remaining instances if any are still running.

	2.	Ensure JVM Exits
If your process is still running after shutdown(), manually force JVM termination as a last resort:

System.exit(0);

However, if System.exit(0); is required, it means Hazelcast isn’t fully releasing resources, and further debugging is needed.

	3.	Check for Hazelcast Threads
Run:

Thread.getAllStackTraces().keySet()
      .forEach(thread -> log.info("Thread: " + thread.getName()));

If you see Hazelcast-related threads still running, some services might not have shut down.

⸻

✔️ Additional Fixes

1. Add a JVM Shutdown Hook

Ensure Hazelcast shuts down properly on JVM termination:

Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    log.info("JVM Shutdown Hook: Stopping Hazelcast...");
    shutdown();
}));

2. Set Client Shutdown Mode

If using Hazelcast clients, add:

ClientConfig clientConfig = new ClientConfig();
clientConfig.setProperty("hazelcast.client.shutdownhook.enabled", "true");
clientConfig.setProperty("hazelcast.client.shutdownhook.policy", "GRACEFUL");

This ensures clients exit properly when the JVM shuts down.

⸻

Final Checklist

✅ Use Hazelcast.shutdownAll() and HazelcastClient.shutdownAll()
✅ Check for active instances after shutdown
✅ Ensure Hazelcast threads are not blocking JVM exit
✅ Add a JVM shutdown hook

Try these fixes and let me know if Hazelcast still keeps the JVM alive! 🚀