package com.rmemoria.datastream.test;

import com.rmemoria.datastream.*;
import com.rmemoria.datastream.test.model.Address;
import com.rmemoria.datastream.test.model.Person;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by rmemoria on 9/10/15.
 */
public class NullTest3 {

    private static final String PERSON_NAME = "Абрам";

    private StreamContext context;

    @Test
    public void test() throws Exception {
        File file = new File("target/null-test3-result.xml");

        writeXml(file, getContext());

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


    protected void readObject(File file, StreamContext context) throws Exception {
        // read content
        FileInputStream fin = new FileInputStream(file);
        DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
        context.addInterceptor(new DataInterceptor() {
            @Override
            public Object newObject(Class objectType, Map<String, Object> params) {
                Person p = new Person();
                p.setBirthDate(new Date());
                return p;
            }

            @Override
            public Class getObjectClass(Object obj) {
                return obj.getClass();
            }
        });
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
            context = ContextUtil.createContext("src/test/resources/null-test3.xml");
        }
        return context;
    }

}
