package ru.proitr.tru.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import ru.proitr.tru.enums.TruFileType;
import ru.proitr.tru.service.TruService;
import ru.proitr.tru.utils.FileUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@Controller
public class FileUploadController {
    @Value("${upload.path}")
    private String LOCAL_DIRECTORY;

    @Autowired
    private TruService truService;


    @GetMapping("eaist-tru-ftp/uploadMsp")
    public String uploadMsp() {
        return "fileUploadMsp";
    }

    @GetMapping("eaist-tru-ftp/uploadIp")
    public String uploadIp() {
        return "fileUploadIp";
    }

    @RequestMapping(value="eaist-tru-ftp/uploadMsp", method=RequestMethod.POST)
    public @ResponseBody
    String fileUploadMsp(@RequestParam("file") MultipartFile file){
        return uploadFile(file, TruFileType.TRU_MSP);
    }

    @RequestMapping(value="eaist-tru-ftp/uploadIp", method=RequestMethod.POST)
    public @ResponseBody
    String fileUploadIp(@RequestParam("file") MultipartFile file){
        return uploadFile(file, TruFileType.TRU_IP);
    }

    private String uploadFile(MultipartFile file, TruFileType fileType) {
        if (file.isEmpty()) {
            return "Файл пустой";
        }
        if (!file.getOriginalFilename().endsWith(".zip")) {
            return "Загружаемый файл должен быть архивом zip";
        }

        try {
            String hash = FileUtils.getHash();
            String dirPath = LOCAL_DIRECTORY + "/" + hash + "/";
            Files.createDirectories(Paths.get(dirPath));
            byte[] bytes = file.getBytes();
            org.apache.commons.io.FileUtils.writeByteArrayToFile(new File(dirPath + "/" + file.getOriginalFilename()), bytes);
            truService.unzipFileAndUpdate(dirPath, fileType);

            return "Файл обработан";
        } catch (Exception e) {
            return "Ошибка загрузки => " + e.getMessage();
        }
    }
}
