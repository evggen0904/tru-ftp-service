package ru.proitr.tru.controllers;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import ru.proitr.tru.configuration.FtpProperties;
import ru.proitr.tru.enums.TruFileType;
import ru.proitr.tru.utils.FileUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Controller
public class FtpController {
    private static final Logger logger = LoggerFactory.getLogger(FtpController.class);

    @Autowired
    private FtpProperties ftpProperties;

    @Value("${ftp.directory.msp}")
    private String FILE_DIRECTORY_MSP;
    @Value("${ftp.directory.ip}")
    private String FILE_DIRECTORY_IP;
    @Value("${upload.path}")
    private String LOCAL_DIRECTORY;

    @Nullable
    public String downloadFromFtp(String currentDate, TruFileType truFileType) {
        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.setConnectTimeout(ftpProperties.getTimeout());
            ftpClient.connect(ftpProperties.getHost(), ftpProperties.getPort());
            ftpClient.login(ftpProperties.getUserName(), ftpProperties.getPassword());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(ftpProperties.getFileType());

            DateFormat df = new SimpleDateFormat("yyyyMMdd");
            Calendar cal = Calendar.getInstance();
            String dateStr = df.format(Calendar.getInstance().getTime());
            if (currentDate != null) {
                cal.setTime(df.parse(currentDate));
                dateStr = df.format(cal.getTime());
            } else {
                cal.setTime(new Date());
            }
            String hash = FileUtils.getHash();

            String fileName;
            String fileDirectory;
            if (truFileType.getId() == TruFileType.TRU_MSP.getId()) {
                fileName = "ListsGWS_Moskva_" + dateStr + "_000000_" + dateStr;
                fileDirectory = FILE_DIRECTORY_MSP;
            } else {
                fileName = "ListsInnov_Moskva_" + dateStr + "_000000_" + dateStr;
                fileDirectory = FILE_DIRECTORY_IP;
            }

            String dirPath = LOCAL_DIRECTORY + "/" + hash + "/";
            Files.createDirectories(Paths.get(dirPath));
            for (FTPFile file : ftpClient.listFiles(fileDirectory)) {
                if (file.getName().contains(fileName)) {
                    retrieveFile(file, dirPath, ftpClient);
                }
            }

            return hash;
        } catch (Exception e){
            logger.error("Error loading files from ftp", e);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                logger.error("Error close connection to ftp ", ex);
            }
        }

        return null;
    }

    private void retrieveFile(FTPFile file, String dirPath, FTPClient ftpClient) throws Exception {
        try (OutputStream output = new FileOutputStream(dirPath + file.getName())) {
            if (ftpClient.retrieveFile(FILE_DIRECTORY_MSP
                            + "/" + file.getName(), output))
            {
                logger.info("File success download file " + file.getName());
            } else {
                logger.info("File error download file " + file.getName());
            }
        }
    }
}
