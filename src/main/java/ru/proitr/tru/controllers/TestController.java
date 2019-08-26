package ru.proitr.tru.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.proitr.tru.enums.TruFileType;
import ru.proitr.tru.service.TruService;
import ru.proitr.tru.utils.DateUtils;

import java.util.Date;

@RestController
public class TestController {
    @Autowired
    private FtpController ftpController;
    @Autowired
    private TruService truService;

    @GetMapping("file")
    public String getFileFromFtp() {
        ftpController.downloadFromFtp("20190819", TruFileType.TRU_MSP);

        return "ok";
    }

    @GetMapping("msp")
    public String getMsp(@RequestParam(required = false) String date) {
        if (date == null) {
            date = DateUtils.getStringFromDate(new Date());
        }
        truService.parseMspAndUpdate(date);
        return "msp";
    }

    @GetMapping("ip")
    public String getIp(@RequestParam(required = false) String date) {
        if (date == null) {
            date = DateUtils.getStringFromDate(new Date());
        }
        truService.parseIpAndUpdate(date);
        return "ip";
    }
}
