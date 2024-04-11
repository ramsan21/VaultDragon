<dependencies>
    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <!-- JSch for SSH and SCP -->
    <dependency>
        <groupId>com.jcraft</groupId>
        <artifactId>jsch</artifactId>
        <version>0.1.55</version>
    </dependency>
</dependencies>


            package com.example.demo.service;

import com.jcraft.jsch.*;
import java.nio.file.Path;
import java.util.Vector;

public class ScpService {

    private String host;
    private int port;
    private String username;
    private String password;

    public ScpService(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public void upload(Path localPath, String remotePath) throws JSchException, SftpException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.put(localPath.toString(), remotePath, ChannelSftp.OVERWRITE);
        sftpChannel.exit();
        session.disconnect();
    }

    public void download(String remotePath, Path localPath) throws JSchException, SftpException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        Channel channel = session.openChannel("sftp");
        channel.connect();
        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.get(remotePath, localPath.toString());
        sftpChannel.exit();
        session.disconnect();
    }
}
