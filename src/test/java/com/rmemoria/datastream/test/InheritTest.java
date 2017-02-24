package com.rmemoria.datastream.test;

import com.rmemoria.datastream.DataMarshaller;
import com.rmemoria.datastream.StreamContext;
import com.rmemoria.datastream.StreamFileTypeXML;
import com.rmemoria.datastream.test.model.Address;
import com.rmemoria.datastream.test.model.CustomCustomer;
import com.rmemoria.datastream.test.model.Person;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

/**
 * Test if extended classes are mapped by super class in XML
 * Created by rmemoria on 10/11/15.
 */
public class InheritTest {

    private StreamContext context;

    @Test
    public void test() throws Exception {
        File file = new File("target/inherit-test-result.xml");

        writeXml(file, getContext());
    }



    protected void writeXml(File file, StreamContext context) throws Exception {
        CustomCustomer c = new CustomCustomer();
        c.setName("Ricardo");
        c.setAge(40);
        c.setBirthDate(new Date());

        FileOutputStream f = new FileOutputStream(file);
        DataMarshaller m = getContext().createMarshaller(StreamFileTypeXML.class);
        m.marshall(c, f);

        f.close();
    }

    /**
     * @return the contextSingleObject
     */
    public StreamContext getContext() {
        if (context == null) {
            context = ContextUtil.createContext("src/test/resources/inherit-test.xml");
        }
        return context;
    }

}
