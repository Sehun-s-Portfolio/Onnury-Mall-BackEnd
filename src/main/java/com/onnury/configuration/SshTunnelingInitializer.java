/*
package com.onnury.configuration;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.annotation.PreDestroy;
import java.util.Properties;

import static java.lang.System.exit;

@Slf4j
@Component
//@Profile("ssh")
//@ConfigurationProperties(prefix = "ssh")
@Validated
@Setter
public class SshTunnelingInitializer {

    @Value("${ssh.remote_jump_host}")
    private String remoteJumpHost;
    @Value("${ssh.user}")
    private String user;
    @Value("${ssh.port}")
    private int sshPort;
    @Value("${ssh.database.port}")
    private int databasePort;

    private String privateKey;

    private Session session;

    @PreDestroy
    public void closeSSH() {
        if (session.isConnected())
            session.disconnect();
    }

    public Integer buildSshConnection() {

        Integer forwardedPort = null;

        try {
            log.info("{}@{}:{}:{} with privateKey",user, remoteJumpHost, sshPort, databasePort);

            log.info("start ssh tunneling..");
            JSch jSch = new JSch();

            log.info("creating ssh session");

            //jSch.addIdentity(privateKey);  // 개인키
            session = jSch.getSession(user, remoteJumpHost, sshPort);  // 세션 설정
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            log.info("complete creating ssh session");

            log.info("start connecting ssh connection");
            session.connect();  // ssh 연결
            log.info("success connecting ssh connection ");

            // 로컬pc의 남는 포트 하나와 원격 접속한 pc의 db포트 연결
            log.info("start forwarding");

            forwardedPort = session.setPortForwardingL(0, "localhost", databasePort);
            log.info("successfully connected to database");

        } catch (Exception e){
            log.error("fail to make ssh tunneling");
            this.closeSSH();

            e.printStackTrace();
            exit(1);
        }

        return forwardedPort;
    }
}*/
