package ru.proitr.tru.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.proitr.tru.utils.DateUtils;

import java.util.Date;

@Service
public class ScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    @Autowired
    private TruService truService;

    @Scheduled(cron = "* * 10 * * *")
    public void startSchedule() {
        String date = DateUtils.getStringFromDate(new Date());

        logger.info("begin auto update");
        truService.parseMspAndUpdate(date);
        truService.parseIpAndUpdate(date);
        logger.info("finish auto update");
    }
}
