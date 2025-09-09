# On your machine
openssl s_client -connect s2bsec-cnidexp.26066.app.standardchartered.com:443 \
  -servername s2bsec-cnidexp.26066.app.standardchartered.com -showcerts </dev/null \
  > certs.txt


keytool -importcert -alias ingress-fake \
  -file fake-cert.pem \
  -keystore src/main/resources/truststore.jks