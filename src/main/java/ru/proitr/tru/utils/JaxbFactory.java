package ru.proitr.tru.utils;

import ru.gov.zakupki._223fz.purchase._1.ListGWS;
import ru.gov.zakupki._223fz.purchase._1.ListInnov;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayOutputStream;

public class JaxbFactory {
    private static Unmarshaller unmarshallerMsp;
    private static Unmarshaller unmarshallerIp;
    private static Marshaller marshallerMsp;
    private static Marshaller marshallerIp;

    public static Unmarshaller getMspUnmarshaller() {
        if (unmarshallerMsp == null) {
            try {
                unmarshallerMsp = JAXBContext.newInstance(ListGWS.class).createUnmarshaller();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        return unmarshallerMsp;
    }

    public static Unmarshaller getIpUnmarshaller() {
        if (unmarshallerIp == null) {
            try {
                unmarshallerIp = JAXBContext.newInstance(ListInnov.class).createUnmarshaller();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        return unmarshallerIp;
    }

    public static Marshaller getMarshallerMsp() {
        if (marshallerMsp == null) {
            try {
                marshallerMsp = JAXBContext.newInstance(ListGWS.class).createMarshaller();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        return marshallerMsp;
    }

    public static Marshaller getMarshallerIp() {
        if (marshallerIp == null) {
            try {
                marshallerIp = JAXBContext.newInstance(ListInnov.class).createMarshaller();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        return marshallerIp;
    }

    public static byte[] marchallObjectToByteArray(Object object, Marshaller marshaller) {
        if (marshaller == null || object == null) {
            return null;
        }
        try {
            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            marshaller.marshal(object, bs);
            return bs.toByteArray();
        } catch (JAXBException e) {
            return null;
        }
    }
}
