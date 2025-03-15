Since you don’t want IntelliJ to connect to the internet or its plugin manager, you can disable these automatic connections and plugin updates. Here’s how:

1. Disable Plugin Auto-Updates
	•	Open IntelliJ IDEA.
	•	Go to Settings (File > Settings on Windows/Linux or IntelliJ IDEA > Preferences on macOS).
	•	Navigate to Plugins.
	•	Click on the settings ⚙️ icon (top-right corner).
	•	Uncheck “Check for plugin updates”.

2. Disable IntelliJ from Accessing JetBrains Plugin Repository
	•	Open Settings.
	•	Go to Plugins > Marketplace.
	•	Click on the settings ⚙️ icon and select “Manage Plugin Repositories”.
	•	Remove or disable any JetBrains repository URLs listed.

3. Disable HTTP Requests to JetBrains
	•	Open Help > Diagnostic Tools > Debug Log Settings.
	•	Add:

#disable plugin updates
idea.plugins.disabled=true
#disable automatic requests to JetBrains servers
idea.connection.timeout=0

Then restart IntelliJ.

4. Block Plugin Manager Using idea.properties
	•	Locate the IntelliJ configuration directory:
	•	On Windows: C:\Users\<your-username>\.IntelliJIdea<version>\config
	•	On Linux/macOS: ~/.config/JetBrains/IntelliJIdea<version>/
	•	Open or create the file idea.properties inside this directory.
	•	Add the following:

idea.plugins.hosts=

This prevents IntelliJ from connecting to any plugin repository.

5. Modify Hosts File to Block JetBrains Plugin Manager (Optional)

If you want to ensure IntelliJ never connects to the JetBrains plugin server:
	•	Edit your system’s hosts file:
	•	Windows: Open Notepad as Administrator and edit C:\Windows\System32\drivers\etc\hosts.
	•	Linux/macOS: Edit /etc/hosts with sudo.
	•	Add:

127.0.0.1 plugins.jetbrains.com



6. Restart IntelliJ IDEA

After making these changes, restart IntelliJ. It should no longer try to connect to the JetBrains plugin repository or check for plugin updates.