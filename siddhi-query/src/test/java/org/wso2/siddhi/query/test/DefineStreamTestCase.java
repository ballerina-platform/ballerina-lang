package org.wso2.siddhi.query.test;

import junit.framework.Assert;
import org.antlr.runtime.RecognitionException;
import org.junit.Test;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class DefineStreamTestCase {


    @Test
    public void Test1() throws RecognitionException, SiddhiParserException {
        StreamDefinition streamDefinition = SiddhiCompiler.parseStreamDefinition("define stream cseStream ( symbol string, price int, volume float )");
        Assert.assertEquals(new StreamDefinition().
                name("cseStream").
                attribute("symbol", Attribute.Type.STRING).
                attribute("price", Attribute.Type.INT).
                attribute("volume", Attribute.Type.FLOAT).toString(),
                            streamDefinition.toString());
    }

    @Test
    public void Test2() throws RecognitionException, SiddhiParserException {
        StreamDefinition streamDefinition = SiddhiCompiler.parseStreamDefinition("define stream `define` ( `string` string, price int, volume float );");
        Assert.assertEquals(new StreamDefinition().
                name("define").
                attribute("string", Attribute.Type.STRING).
                attribute("price", Attribute.Type.INT).
                attribute("volume", Attribute.Type.FLOAT).toString(),
                            streamDefinition.toString());
    }


}
