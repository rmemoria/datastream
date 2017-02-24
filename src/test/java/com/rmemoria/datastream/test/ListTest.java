package com.rmemoria.datastream.test;

import static org.junit.Assert.*;


import com.rmemoria.datastream.DataStreamException;
import com.rmemoria.datastream.DataUnmarshaller;
import com.rmemoria.datastream.StreamContext;
import com.rmemoria.datastream.StreamFileTypeXML;
import com.rmemoria.datastream.test.model.Order;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by rmemoria on 5/11/15.
 */
public class ListTest {


    private StreamContext context;

    /**
     * Test the required field
     * @throws FileNotFoundException
     */
    @Test
    public void testRequired() throws FileNotFoundException {
        //
        StreamContext context = getContext();

        InputStream in = new FileInputStream(new File("src/test/resources/list-data.xml"));
        DataUnmarshaller um = context.createUnmarshaller(StreamFileTypeXML.class);
        Order order = (Order)um.unmarshall(in);

        assertNotNull(order);
        assertNotNull(order.getItems());
        assertEquals(order.getItems().size(), 2);
    }

    /**
     * @return the contextSingleObject
     */
    public StreamContext getContext() {
        if (context == null) {
            context = ContextUtil.createContext("src/test/resources/list-test.xml");
        }
        return context;
    }
}
