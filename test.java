Here’s how to tar and untar a folder in Linux:

⸻

🔹 To tar a folder (i.e., compress it into a .tar archive):

tar -cvf archive_name.tar /path/to/folder

Explanation:
	•	c = create archive
	•	v = verbose (shows progress, optional)
	•	f = filename of archive
	•	Example:

tar -cvf backup.tar /home/user/myfolder



⸻

🔹 To untar (extract) a .tar archive:

tar -xvf archive_name.tar

Explanation:
	•	x = extract
	•	v = verbose (optional)
	•	f = filename of archive
	•	Example:

tar -xvf backup.tar



By default, it extracts in the current directory.

⸻

🔸 For tar.gz (compressed with gzip):

Create:

tar -czvf archive_name.tar.gz /path/to/folder

Extract:

tar -xzvf archive_name.tar.gz


⸻

Let me know if you need to extract to a specific directory or use .bz2, .xz, etc.