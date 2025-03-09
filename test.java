Yes! You can iterate through all active threads obtained from Thread.getAllStackTraces() and attempt to stop or interrupt them. However, Java does not provide a safe way to forcibly kill a thread, because stopping a thread without proper handling can lead to corrupted data or inconsistent states.

⸻

🚀 How to Stop/Kill All Running Threads in Java

1️⃣ List All Running Threads

First, print all active threads before attempting to stop them:

Thread.getAllStackTraces().keySet()
    .forEach(thread -> log.info("Active Thread: " + thread.getName() + " | State: " + thread.getState()));



⸻

2️⃣ Interrupt Running Threads (Safe Approach)

You can interrupt all non-daemon threads that are blocking or waiting.

public void stopAllThreads() {
    log.info("Stopping all active threads...");
    for (Thread thread : Thread.getAllStackTraces().keySet()) {
        if (!thread.isDaemon() && !thread.getName().contains("main")) { // Avoid killing main thread
            log.info("Attempting to stop thread: " + thread.getName());
            thread.interrupt();
        }
    }
}

✅ This is the safest approach because it asks the threads to stop rather than killing them forcibly.

⸻

3️⃣ Forcefully Stop Hazelcast Threads (Unsafe Approach)

Since Thread.stop() is deprecated, you cannot directly stop a thread safely. However, if you’re sure Hazelcast is causing the issue, you can terminate its executors.

for (Thread thread : Thread.getAllStackTraces().keySet()) {
    if (thread.getName().contains("hz.") || thread.getName().contains("hazelcast")) {
        log.info("Forcibly stopping Hazelcast thread: " + thread.getName());
        thread.interrupt(); // Ask the thread to stop
    }
}



⸻

4️⃣ Shutdown Hazelcast Executors Manually

If Hazelcast is keeping the JVM alive, you should shut down Hazelcast executors manually:

HazelcastInstance hzInstance = instance;
if (hzInstance != null) {
    hzInstance.getExecutorService("default").shutdownNow();
}



⸻

5️⃣ Force Kill the JVM (Last Resort)

If nothing works, you can forcefully terminate the JVM, but this should be your last option:

System.exit(0);

⚠️ Warning: This will immediately stop the process without cleanup.

⸻

🔍 Next Steps

1️⃣ List all active threads before killing them (Thread.getAllStackTraces()).
2️⃣ Interrupt Hazelcast-related threads (thread.interrupt()).
3️⃣ Manually stop Hazelcast executors (shutdownNow()).
4️⃣ Only use System.exit(0) if no other method works.

Would you like me to help analyze the list of running threads to pinpoint the issue? 🚀