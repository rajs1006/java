package de.funkedigital.autotagging.utils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Util class to perform {@link javax.xml.bind.JAXB} operations
 *
 * @author sraj
 */
public class JaxbUtils {

    public static <T> T getXmlEntityFromUrl(String url, Class<T> cls) throws MalformedURLException, JAXBException {
        URL u = new URL(url);
        JAXBContext jaxbContext = JAXBContext.newInstance(cls);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        T t = (T) jaxbUnmarshaller.unmarshal(u);
        return t;
    }

    public static <T> T getXmlEntityFromStream(InputStreamReader stream, Class<T> cls) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(cls);

        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        T t = (T) jaxbUnmarshaller.unmarshal(stream);
        return t;
    }
}
