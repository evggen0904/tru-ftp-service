package ru.proitr.tru.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gov.zakupki._223fz.purchase._1.InnovPositionType;
import ru.gov.zakupki._223fz.purchase._1.ListInnov;
import ru.gov.zakupki._223fz.purchase._1.ListInnovData;
import ru.proitr.tru.repository.TruMspRepository;
import ru.proitr.tru.tables.daos.NFtp_223TruIpDao;
import ru.proitr.tru.tables.daos.NFtp_223TruIpPositionsDao;
import ru.proitr.tru.tables.pojos.NFtp_223TruIp;
import ru.proitr.tru.tables.pojos.NFtp_223TruIpPositions;
import ru.proitr.tru.utils.DateUtils;
import ru.proitr.tru.utils.JaxbFactory;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class IpService {
    private static final Logger logger = LoggerFactory.getLogger(IpService.class);

    @Autowired
    private NFtp_223TruIpDao truIpDao;
    @Autowired
    private NFtp_223TruIpPositionsDao truIpPositionsDao;
    @Autowired
    private TruMspRepository truMspRepository;

    public void update(File file) {
        if (file == null) {
            return;
        }
        try {
            Unmarshaller unmarshaller = JaxbFactory.getIpUnmarshaller();
            if (unmarshaller == null) {
                return;
            }
            ListInnov listInnov = (ListInnov) unmarshaller.unmarshal(new ByteArrayInputStream(org.apache.commons.io.FileUtils.readFileToByteArray(file)));
            update(listInnov);
        } catch (FileNotFoundException e) {
            logger.error("File not found error", e);
        } catch (IOException e) {
            logger.error("File reading error", e);
        } catch (JAXBException e) {
            logger.error("Unmarshalling error", e);
        }
    }

    private void update(ListInnov listInnov) {
        if (listInnov == null) {
            return;
        }

        ListInnovData listInnovData = listInnov.getBody().getItem().getListInnovData();
        String customerInn = listInnovData.getCustomer().getMainInfo().getInn();
        String customerKpp = listInnovData.getCustomer().getMainInfo().getKpp();
        boolean isCustomerExists = truMspRepository.isEaistCustomer(customerInn, customerKpp);
        if (!isCustomerExists) {
            return;
        }

        NFtp_223TruIp actualIp = truMspRepository.getActualIp(customerInn, customerKpp);
        if (actualIp != null) {
            actualIp.setDeletedDate(LocalDate.now());
            truIpDao.update(actualIp);
        }

        NFtp_223TruIp truIp = new NFtp_223TruIp();
        truIp.setGuid(listInnovData.getGuid());
        truIp.setParentGuid(listInnovData.getParentId());
        truIp.setUrlEis(listInnovData.getUrlEIS());
        truIp.setCreatedDate(DateUtils.getLocalDateFromCalendar(listInnovData.getCreateDateTime()));
        truIp.setCustomerInn(customerInn);
        truIp.setCustomerKpp(customerKpp);
        truIp.setPlacerInn(listInnovData.getPlacer().getMainInfo().getInn());
        truIp.setPlacerKpp(listInnovData.getPlacer().getMainInfo().getKpp());
        truIp.setPublicationDate(DateUtils.getLocalDateFromCalendar(listInnovData.getPublicationDate()));
        if (listInnovData.getStatus() != null) {
            truIp.setStatus(listInnovData.getStatus().value());
        }
        truIp.setVersion(listInnovData.getVersion());
        truIp.setModificationDescription(listInnovData.getModificationDescription());
        truIp.setXmlContent(getXmlContent(listInnov));

        truIpDao.insert(truIp);

        updateMspPositions(truIp, listInnovData.getInnovPositions().getInnovPosition());
    }

    private byte[] getXmlContent(ListInnov listInnov) {
        Marshaller marshaller = JaxbFactory.getMarshallerIp();
        if (marshaller == null) {
            return null;
        }
        return JaxbFactory.marchallObjectToByteArray(listInnov, marshaller);
    }

    private void updateMspPositions(NFtp_223TruIp truIp, List<InnovPositionType> innovPositions) {
        List<NFtp_223TruIpPositions> ipPositions = new ArrayList<>();
        for (InnovPositionType innovPosition : innovPositions) {
            NFtp_223TruIpPositions ipPosition = new NFtp_223TruIpPositions();
            ipPosition.setOosFtp_223TruIpId(truIp.getId());
            ipPosition.setPositionNumber(Long.valueOf(innovPosition.getOrdinalNumber()));
            if (innovPosition.getOkpd2() != null) {
                ipPosition.setOkpd2Code(innovPosition.getOkpd2().getCode());
                ipPosition.setOkpd2Name(innovPosition.getOkpd2().getName());
            }
            if (innovPosition.getOkdp() != null) {
                ipPosition.setOkpdCode(innovPosition.getOkdp().getCode());
                ipPosition.setOkpdName(innovPosition.getOkdp().getName());
            }
            ipPositions.add(ipPosition);
        }
        truIpPositionsDao.insert(ipPositions);
    }
}
