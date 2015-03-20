package com.rmemoria.datastream.test;

import com.rmemoria.datastream.*;
import com.rmemoria.datastream.test.model.Order;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by rmemoria on 20/3/15.
 */
public class UnkownPropertyTest {

    private StreamContext context;

    public static void main(String[] args) throws Exception {
        UnkownPropertyTest test = new UnkownPropertyTest();
        test.testUnknownProperty();
    }

    /**
     * Test generation and reading of a single object
     * @throws java.io.IOException
     */
    @Test(expected = DataStreamException.class)
    public void testUnknownProperty() throws Exception {
        ContextUtil.createContext("src/test/resources/order-schema-unknownproperty.xml");
    }


}
