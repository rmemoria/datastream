package com.rmemoria.datastream.test;

import com.rmemoria.datastream.DataMarshaller;
import com.rmemoria.datastream.StreamContext;
import com.rmemoria.datastream.StreamFileTypeXML;
import com.rmemoria.datastream.test.model.Address;
import com.rmemoria.datastream.test.model.Person;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

/**
 * Created by rmemoria on 22/10/15.
 */
public class Null4Test {

    private StreamContext context;

    @Test
    public void test() throws Exception {
        File file = new File("target/null4-test-result.xml");

        writeXml(file, getContext());
    }

    protected void writeXml(File file, StreamContext context) throws Exception {
        Person p = new Person();
        p.setName("Bob Brown");
        p.setBirthDate(new Date());
        Address adr = new Address();
        adr.setStreet("My Street");
        adr.setArea(Address.AddressArea.URBAN);
        adr.setZip("22030-100");
        p.setAddress(adr);

        FileOutputStream f = new FileOutputStream(file);
        DataMarshaller m = getContext().createMarshaller(StreamFileTypeXML.class);
        m.marshall(p, f);

        f.close();
    }

    /**
     * @return the contextSingleObject
     */
    public StreamContext getContext() {
        if (context == null) {
            context = ContextUtil.createContext("src/test/resources/null4-test.xml");
        }
        return context;
    }

}
