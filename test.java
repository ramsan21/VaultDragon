gpg --list-secret-keys --with-colons | grep sec | cut -d: -f5 | xargs -I {} gpg --batch --yes --delete-secret-keys {}

gpg --list-keys --with-colons | grep pub | cut -d: -f5 | xargs -I {} gpg --batch --yes --delete-keys {}

