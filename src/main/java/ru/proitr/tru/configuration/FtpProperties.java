package ru.proitr.tru.configuration;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.net.ftp.FTP;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class FtpProperties {
    @Value("${ftp.user}")
    private String userName;
    @Value("${ftp.password}")
    private String password;
    @Value("${ftp.host}")
    private String host;
    @Value("${ftp.port}")
    private int port;
    @Value("${ftp.timeout}")
    private int timeout;
    private int fileType = FTP.BINARY_FILE_TYPE;
}
