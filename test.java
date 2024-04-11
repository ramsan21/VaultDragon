package com.example.demo.service;

import com.jcraft.jsch.*;
import java.nio.file.Path;

public class ScpService {

    private String host;
    private int port;
    private String username;
    private String privateKeyPath;

    public ScpService(String host, int port, String username, String privateKeyPath) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.privateKeyPath = privateKeyPath;
    }

    private Session createSession() throws JSchException {
        JSch jsch = new JSch();
        jsch.addIdentity(privateKeyPath); // Add the private key path here

        Session session = jsch.getSession(username, host, port);
        session.setConfig("StrictHostKeyChecking", "no"); // Consider handling the host key checking differently for production
        session.connect();

        return session;
    }

    public void upload(Path localPath, String remotePath) throws JSchException, SftpException {
        Session session = createSession();
        Channel channel = session.openChannel("sftp");
        channel.connect();

        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.put(localPath.toString(), remotePath, ChannelSftp.OVERWRITE);

        sftpChannel.exit();
        channel.disconnect();
        session.disconnect();
    }

    public void download(String remotePath, Path localPath) throws JSchException, SftpException {
        Session session = createSession();
        Channel channel = session.openChannel("sftp");
        channel.connect();

        ChannelSftp sftpChannel = (ChannelSftp) channel;
        sftpChannel.get(remotePath, localPath.toString());

        sftpChannel.exit();
        channel.disconnect();
        session.disconnect();
    }
}
