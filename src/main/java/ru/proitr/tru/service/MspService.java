package ru.proitr.tru.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.gov.zakupki._223fz.purchase._1.GwsPositionType;
import ru.gov.zakupki._223fz.purchase._1.ListGWS;
import ru.gov.zakupki._223fz.purchase._1.ListGWSData;
import ru.proitr.tru.repository.TruMspRepository;
import ru.proitr.tru.tables.daos.NFtp_223TruMspDao;
import ru.proitr.tru.tables.daos.NFtp_223TruMspPositionsDao;
import ru.proitr.tru.tables.pojos.NFtp_223TruMsp;
import ru.proitr.tru.tables.pojos.NFtp_223TruMspPositions;
import ru.proitr.tru.utils.DateUtils;
import ru.proitr.tru.utils.JaxbFactory;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MspService {
    private static final Logger logger = LoggerFactory.getLogger(MspService.class);
    @Value("${validation.path}")
    private String VALIDATION_XSD_PATH;
    @Autowired
    private NFtp_223TruMspDao truMspDao;
    @Autowired
    private NFtp_223TruMspPositionsDao truMspPositionsDao;
    @Autowired
    private TruMspRepository truMspRepository;

    public void update(File file) {
        if (file == null) {
            return;
        }
        try {
            Unmarshaller unmarshaller = JaxbFactory.getMspUnmarshaller();
            if (unmarshaller == null) {
                return;
            }
            InputStream is = new ByteArrayInputStream(org.apache.commons.io.FileUtils.readFileToByteArray(file));
            ListGWS listGWS = (ListGWS) unmarshaller.unmarshal(is);
            update(listGWS);
        } catch (FileNotFoundException e) {
            logger.error("File not found error", e);
        } catch (IOException e) {
            logger.error("File reading error", e);
        } catch (JAXBException e) {
            logger.error("Unmarshalling error", e);
        }
    }

    private void update(ListGWS listGWS) {
        if (listGWS == null) {
            return;
        }

        ListGWSData listGWSData = listGWS.getBody().getItem().getListGWSData();
        String customerInn = listGWSData.getCustomer().getMainInfo().getInn();
        String customerKpp = listGWSData.getCustomer().getMainInfo().getKpp();
        boolean isCustomerExists = truMspRepository.isEaistCustomer(customerInn, customerKpp);
        if (!isCustomerExists) {
            return;
        }

        NFtp_223TruMsp actualMsp = truMspRepository.getActualMsp(customerInn, customerKpp);
        if (actualMsp != null) {
            actualMsp.setDeletedDate(LocalDate.now());
            truMspDao.update(actualMsp);
        }

        NFtp_223TruMsp truMsp = new NFtp_223TruMsp();
        truMsp.setGuid(listGWSData.getGuid());
        truMsp.setParentGuid(listGWSData.getParentId());
        truMsp.setUrlEis(listGWSData.getUrlEIS());
        truMsp.setCreatedDate(DateUtils.getLocalDateFromCalendar(listGWSData.getCreateDateTime()));
        truMsp.setCustomerInn(customerInn);
        truMsp.setCustomerKpp(customerKpp);
        truMsp.setPlacerInn(listGWSData.getPlacer().getMainInfo().getInn());
        truMsp.setPlacerKpp(listGWSData.getPlacer().getMainInfo().getKpp());
        truMsp.setPublicationDate(DateUtils.getLocalDateFromCalendar(listGWSData.getPublicationDate()));
        if (listGWSData.getStatus() != null) {
            truMsp.setStatus(listGWSData.getStatus().value());
        }
        truMsp.setVersion(listGWSData.getVersion());
        truMsp.setModificationDescription(listGWSData.getModificationDescription());
        truMsp.setXmlContent(getXmlContent(listGWS));

        truMspDao.insert(truMsp);

        updateMspPositions(truMsp, listGWSData.getGwsPositions().getGwsPosition());
    }

    private byte[] getXmlContent(ListGWS listGWS) {
        Marshaller marshaller = JaxbFactory.getMarshallerMsp();
        if (marshaller == null) {
            return null;
        }

        return JaxbFactory.marchallObjectToByteArray(listGWS, marshaller);
    }

    private void updateMspPositions(NFtp_223TruMsp truMsp, List<GwsPositionType> gwsPositions) {
        List<NFtp_223TruMspPositions> mspPositions = new ArrayList<>();
        for (GwsPositionType gwsPosition : gwsPositions) {
            NFtp_223TruMspPositions mspPosition = new NFtp_223TruMspPositions();
            mspPosition.setOosFtp_223TruMspId(truMsp.getId());
            mspPosition.setPositionNumber(Long.valueOf(gwsPosition.getOrdinalNumber()));
            if (gwsPosition.getOkpd2() != null) {
                mspPosition.setOkpd2Code(gwsPosition.getOkpd2().getCode());
                mspPosition.setOkpd2Name(gwsPosition.getOkpd2().getName());
            }
            if (gwsPosition.getOkdp() != null) {
                mspPosition.setOkpdCode(gwsPosition.getOkdp().getCode());
                mspPosition.setOkpdName(gwsPosition.getOkdp().getName());
            }
            mspPositions.add(mspPosition);
        }
        truMspPositionsDao.insert(mspPositions);
    }
}
