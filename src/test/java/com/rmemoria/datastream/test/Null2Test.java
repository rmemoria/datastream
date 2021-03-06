package com.rmemoria.datastream.test;

import com.rmemoria.datastream.DataMarshaller;
import com.rmemoria.datastream.DataUnmarshaller;
import com.rmemoria.datastream.StreamContext;
import com.rmemoria.datastream.StreamFileTypeXML;
import com.rmemoria.datastream.test.model.Address;
import com.rmemoria.datastream.test.model.Person;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static org.junit.Assert.assertEquals;

/**
 * Test the objectGraph property 'includeNullValues' = false
 * Created by rmemoria on 9/10/15.
 */
public class Null2Test {

    private static final String PERSON_NAME = "Абрам";

    private StreamContext context;

    @Test
    public void testNullValue() throws Exception {
        File file = new File("target/null-test2-result.xml");

        // write object to xml
        writeXml(file, getContext());

        // Check if XML is correct
        checkXML(file);

        // read object from xml
        readObject(file, getContext());
    }

    protected void writeXml(File file, StreamContext context) throws Exception {
        Person p = new Person();
        p.setName(PERSON_NAME);
        Address addr = new Address();
        FileOutputStream f = new FileOutputStream(file);
        DataMarshaller m = getContext().createMarshaller(StreamFileTypeXML.class);
        m.marshall(p, f);

        f.close();
    }


    // validate XML
    protected void checkXML(File file) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);

        assertEquals(doc.getChildNodes().getLength(), 1);

        Node node = doc.getFirstChild();
        assertEquals(node.getNodeName(), "person");
        assertEquals(node.getChildNodes().getLength(), 1);

        Node cname = ((Element)node).getElementsByTagName("name").item(0);

        assertEquals(cname.getTextContent(), PERSON_NAME);
    }

    protected void readObject(File file, StreamContext context) throws Exception {
        // read content
        FileInputStream fin = new FileInputStream(file);
        DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
        Object obj = um.unmarshall(fin);
        assertEquals(obj.getClass(), Person.class);

        Person p = (Person)obj;
        assertEquals(p.getName(), PERSON_NAME);
        assertEquals(p.getAddress(), null);
        assertEquals(p.getBirthDate(), null);
    }

    /**
     * @return the contextSingleObject
     */
    public StreamContext getContext() {
        if (context == null) {
            context = ContextUtil.createContext("src/test/resources/null-test2.xml");
        }
        return context;
    }

}
