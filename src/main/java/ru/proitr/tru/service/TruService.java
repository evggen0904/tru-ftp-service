package ru.proitr.tru.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.proitr.tru.controllers.FtpController;
import ru.proitr.tru.enums.TruFileType;
import ru.proitr.tru.utils.FileUtils;

import java.io.File;
import java.io.IOException;

@Service
public class TruService {
    private static final Logger logger = LoggerFactory.getLogger(TruService.class);

    @Autowired
    private FtpController ftpController;
    @Autowired
    private MspService mspService;
    @Autowired
    private IpService ipService;

    @Value("${upload.path}")
    private String LOCAL_DIRECTORY;

    public void parseMspAndUpdate(String date) {
        String generatedDir = ftpController.downloadFromFtp(date, TruFileType.TRU_MSP);
        String currentDir = LOCAL_DIRECTORY + "/" + generatedDir;
        unzipFileAndUpdate(currentDir, TruFileType.TRU_MSP);
    }

    public void parseIpAndUpdate(String date) {
        String generatedDir = ftpController.downloadFromFtp(date, TruFileType.TRU_IP);
        String currentDir = LOCAL_DIRECTORY + "/" + generatedDir;
        unzipFileAndUpdate(currentDir, TruFileType.TRU_IP);
    }

    public void unzipFileAndUpdate(String currentDir, TruFileType truFileType) {
        try {
            FileUtils.unzipAll(currentDir);
            updateAll(currentDir, truFileType);
        } catch (IOException e) {
            logger.error("Error unzipping files", e);
        }
    }

    private void updateAll(String dirPath, TruFileType truFileType) {
        File dir = new File(dirPath);
        int count = 0;
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.getName().contains(".xml") && !file.getName().contains(".zip")
                        && file.getName().contains(truFileType.getCode())) {
                    if (truFileType.getId() == TruFileType.TRU_MSP.getId()) {
                        mspService.update(file);
                    } else {
                        ipService.update(file);
                    }
                    count++;
                    file.delete();
                }
            }
        }
    }


}
