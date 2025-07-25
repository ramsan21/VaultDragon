git checkout catalyst/main
git pull origin catalyst/main
git checkout -b hard-reset-main


git reset --hard b458c3e5

git add -A
git commit -m "Restoring catalyst/main to content of commit b458c3e5"

git push origin hard-reset-main