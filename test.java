Session session = jsch.getSession(username, host, port);
session.setConfig("StrictHostKeyChecking", "no");

// Specify the preferred encryption algorithms
session.setConfig("kex", "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256");
session.setConfig("server_host_key", "ssh-rsa,ssh-dss");
session.setConfig("cipher.s2c", "aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-ctr,aes192-cbc,aes256-ctr,aes256-cbc");
session.setConfig("cipher.c2s", "aes128-ctr,aes128-cbc,3des-ctr,3des-cbc,blowfish-cbc,aes192-ctr,aes192-cbc,aes256-ctr,aes256-cbc");
session.setConfig("mac.s2c", "hmac-md5,hmac-sha1,hmac-sha2-256,hmac-sha1-96,hmac-md5-96");
session.setConfig("mac.c2s", "hmac-md5,hmac-sha1,hmac-sha2-256,hmac-sha1-96,hmac-md5-96");
session.setConfig("compression.s2c", "none");
session.setConfig("compression.c2s", "none");

session.connect();
