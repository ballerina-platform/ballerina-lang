package org.wso2.siddhi.query.test;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class DefineTableTestCase {


    @Test
    public void Test1() throws SiddhiParserException {
        TableDefinition streamDefinition = SiddhiCompiler.parseTableDefinition("define table cseStream ( symbol string, price int, volume float )");
        Assert.assertEquals(TableDefinition.
                        id("cseStream").
                        attribute("symbol", Attribute.Type.STRING).
                        attribute("price", Attribute.Type.INT).
                        attribute("volume", Attribute.Type.FLOAT).toString(),
                streamDefinition.toString());
    }

    @Test
    public void Test2() throws SiddhiParserException {
        TableDefinition streamDefinition = SiddhiCompiler.parseTableDefinition("define table `define` ( `string` string, price int, volume float );");
        Assert.assertEquals(TableDefinition.
                        id("define").
                        attribute("string", Attribute.Type.STRING).
                        attribute("price", Attribute.Type.INT).
                        attribute("volume", Attribute.Type.FLOAT).toString(),
                streamDefinition.toString());
    }

    @Test
    public void Test3() throws SiddhiParserException {
        TableDefinition streamDefinition = SiddhiCompiler.parseTableDefinition("define table cseStream ( symbol string, price int, volume float )");
        Assert.assertEquals(TableDefinition.
                        id("cseStream").
                        attribute("symbol", Attribute.Type.STRING).
                        attribute("price", Attribute.Type.INT).
                        attribute("volume", Attribute.Type.FLOAT).toString(),
                streamDefinition.toString());
    }

    @Test
    public void Test4() throws SiddhiParserException {
        TableDefinition streamDefinition = SiddhiCompiler.parseTableDefinition("" +
                " @from(datasource='MyDatabase','CUSTOM')" +
                " define table cseStream ( symbol string, price int, volume float )");
        Assert.assertEquals(TableDefinition.
                        id("cseStream").
                        attribute("symbol", Attribute.Type.STRING).
                        attribute("price", Attribute.Type.INT).
                        attribute("volume", Attribute.Type.FLOAT).annotation(Annotation.annotation("from").element("datasource", "MyDatabase").element("CUSTOM")).toString(),
                streamDefinition.toString());
    }
}
