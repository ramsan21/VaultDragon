Issues Found
You’re hitting two related errors when starting Kong via kong_ctl.sh:
Error 1: Permission Denied on error.log

nginx: [emerg] open() "/apps/kong/instances/SIT2_KONG_STARSECADMIN_01/error.log" failed (13: Permission denied)


Error 2: Invalid nginx Configuration

nginx: configuration file .../local/nginx.conf test failed


Root Cause
The script is running as starsswb user but nginx needs to write to log files owned by root or requires elevated privileges. Also, the .env file has /home/starsswb set as a directory path (line 11 issue).

Fix Steps
1. Fix log directory permissions:

sudo chown -R starsswb:starsec /apps/kong/instances/SIT2_KONG_STARSECADMIN_01/logs/
sudo chmod -R 755 /apps/kong/instances/SIT2_KONG_STARSECADMIN_01/logs/


2. Create the log file if missing:

sudo touch /apps/kong/instances/SIT2_KONG_STARSECADMIN_01/error.log
sudo chown starsswb /apps/kong/instances/SIT2_KONG_STARSECADMIN_01/error.log


3. Fix the .env file — check line 11:

vi /apps/kong/instances/SIT2_KONG_STARSECADMIN_01/app/.env


Make sure the prefix path points to the local directory, not /home/starsswb:

prefix=/apps/kong/instances/SIT2_KONG_STARSECADMIN_01/local


4. Create the local directory if missing:

mkdir -p /apps/kong/instances/SIT2_KONG_STARSECADMIN_01/local


5. Retry:

./kong_ctl.sh start


The warning about "user" directive being ignored is non-fatal (since nginx isn’t running as root), but the permission denial on the log file is blocking startup.​​​​​​​​​​​​​​​​