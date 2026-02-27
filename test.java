Still the Same Core Issue
The chmod -R 755 /apps/logs/kong/SIT2_KONG_STARSEC_01/ you ran fixed the wrong path. The error points to a different location:

/apps/kong/logs/SIT2_KONG_STARSEC_01/error.log   ← nginx is trying to write here
/apps/logs/kong/SIT2_KONG_STARSEC_01/            ← you fixed this (wrong path)


Fix Steps (as root)
Switch to root first:

su - root
# or
sudo -i


1. Create the missing log directory and file:

mkdir -p /apps/kong/logs/SIT2_KONG_STARSEC_01/
touch /apps/kong/logs/SIT2_KONG_STARSEC_01/error.log


2. Fix ownership:

chown -R starsswb:starsswb /apps/kong/logs/SIT2_KONG_STARSEC_01/
chmod -R 755 /apps/kong/logs/SIT2_KONG_STARSEC_01/


3. Do the same for STARSECADMIN:

mkdir -p /apps/kong/logs/SIT2_KONG_STARSECADMIN_01/
touch /apps/kong/logs/SIT2_KONG_STARSECADMIN_01/error.log
chown -R starsswb:starsswb /apps/kong/logs/SIT2_KONG_STARSECADMIN_01/
chmod -R 755 /apps/kong/logs/SIT2_KONG_STARSECADMIN_01/


4. Switch back and retry:

su - starsswb
cd /apps/kong/instances/SIT2_KONG_STARSEC_01/bin/
./kong_ctl.sh start


The key insight: ls: cannot access '/apps/logs/kong/...' vs the nginx error showing /apps/kong/logs/... — the directory order is swapped. That’s why the chmod didn’t help.​​​​​​​​​​​​​​​​