/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.json;

import org.ballerinalang.core.model.util.JsonGenerator;
import org.ballerinalang.core.model.util.JsonParser;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BallerinaException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * The following tests will verify Ballerina's custom JSON parser and generator
 * functionality.
 */
public class JSONLibraryTest {

    @Test
    public void testBasicJsonObjectParseCheckTypes() {
        String json = "{\"a\":\"abc\",\"b\":1,\"c\":3.14,\"d\":true,\"e\":false,\"f\":null,\"g\":"
                + "{\"1\":\"a\",\"2\":\"b\"},\"h\":[\"A\",\"B\",\"C\",\"D\"]}";
        BValue node = JsonParser.parse(json);
        Assert.assertFalse(node instanceof BValueArray);
        Assert.assertFalse(node instanceof BBoolean);
        Assert.assertFalse(node instanceof BFloat);
        Assert.assertFalse(node instanceof BInteger);
        Assert.assertFalse(node instanceof BString);
        Assert.assertTrue(node instanceof BMap);
        Assert.assertNotNull(node);

        BMap<String, BValue> jsonObj = (BMap<String, BValue>) node;
        Assert.assertTrue(jsonObj.get("a") instanceof BString);
        Assert.assertTrue(jsonObj.get("b") instanceof BInteger);
        Assert.assertTrue(jsonObj.get("c") instanceof BFloat);
        Assert.assertFalse(jsonObj.get("c") instanceof BInteger);
        Assert.assertTrue(jsonObj.get("d") instanceof BBoolean);
        Assert.assertTrue(jsonObj.get("e") instanceof BBoolean);
        Assert.assertNull(jsonObj.get("f"));
        Assert.assertTrue(jsonObj.get("g") instanceof BMap);
        Assert.assertFalse(jsonObj.get("h") instanceof BMap);
        Assert.assertTrue(jsonObj.get("h") instanceof BValueArray);
    }

    @Test
    public void testBasicJsonObjectParseValues() {
        String json = "{\"a\":\"abc\",\"b\":1,\"c\":3.14,\"d\":true,\"e\":false,\"f\":null,"
                + "\"g\":{\"1\":\"a\",\"2\":\"b\"},\"h\":[\"A\",20,30,\"D\"]}";
        BMap<String, BValue> node = (BMap<String, BValue>) JsonParser.parse(json);
        Assert.assertEquals(node.get("a").stringValue(), "abc");
        Assert.assertEquals(((BInteger) node.get("b")).intValue(), 1);
        Assert.assertEquals(((BFloat) node.get("c")).floatValue(), 3.14);
        Assert.assertTrue(((BBoolean) node.get("d")).booleanValue());
        Assert.assertFalse(((BBoolean) node.get("e")).booleanValue());
        Assert.assertNull(node.get("f"));

        Assert.assertTrue(node.get("g") instanceof BMap);
        BMap<String, BValue> objNode = (BMap<String, BValue>) node.get("g");
        Assert.assertEquals(objNode.get("1").stringValue(), "a");
        Assert.assertEquals(objNode.get("2").stringValue(), "b");

        Assert.assertTrue(node.get("h") instanceof BValueArray);
        BValueArray arrayNode = (BValueArray) node.get("h");
        Assert.assertEquals(arrayNode.size(), 4);
        Assert.assertEquals(arrayNode.getRefValue(0).stringValue(), "A");
        Assert.assertEquals(((BInteger) arrayNode.getRefValue(1)).intValue(), 20);
        Assert.assertEquals(((BInteger) arrayNode.getRefValue(2)).intValue(), 30);
        Assert.assertEquals(arrayNode.getRefValue(3).stringValue(), "D");
    }

    @Test
    public void testBasicJsonObjectGenValues() throws IOException {
        String json = "{\"a\":\"abc\", \"b\":1, \"c\":3.14, \"d\":true, \"e\":false, \"f\":null, "
                + "\"g\":{\"1\":\"a\", \"2\":\"b\"}, \"h\":[\"A\", 20, 30, \"D\"]}";
        BValue node = JsonParser.parse(json);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        JsonGenerator gen = new JsonGenerator(byteOut);
        gen.serialize(node);
        gen.flush();
        Assert.assertEquals(new String(byteOut.toByteArray()), json);
    }

    @Test
    public void testJsonEscapeChars() throws IOException {
        String json = "[{\"a\":\"abc\\\"\", \"x\":\"1\\b\\f\", \"c\":3.14, \"d\":true, \"e\":false, \"f\":null, \"g\":"
                + "{\"1\\n2\":\"a\\r\", \"2\":\"b\"}, \"h\":[\"A\\tB\", 20, 30, \"D\\\\\"]}]";
        BValue node = JsonParser.parse(json);
        String result = node.toString();
        Assert.assertEquals(result, json);
    }

    @Test
    public void testJsonUnicodeChars() {
        String json = "{\"firstName\":\"Will\", \"lastName\":\"Smith\", \"info\":\"\\u2600\", "
                + "\"info2\":\"A\\u2655\\u2665\\u266A\\u266aB\"}";
        String json2 = "{\"firstName\":\"Will\", \"lastName\":\"Smith\", \"info\":\"\u2600\", "
                + "\"info2\":\"A\u2655\u2665\u266A\u266aB\"}";
        BMap<String, BValue> node = (BMap<String, BValue>) JsonParser.parse(json);
        Assert.assertEquals(node.get("info").stringValue(), "☀");
        Assert.assertEquals(node.get("info2").stringValue(), "A♕♥♪♪B");
        Assert.assertEquals(node.toString(), json2);
    }

    @Test
    public void testJsonDeepLevels() {
        String json = "{\"A\":{\"B\":{\"C\":{\"D\":{\"E\":{\"F\":{\"G\":{\"H\":{\"I\":{\"J\":"
                + "{\"K\":{\"L\":{\"M\":{\"N\":5}}}}}}}}}}}}}}";
        BValue node = JsonParser.parse(json);
        Assert.assertEquals(node.stringValue(), json);
    }

    @Test
    public void testComplexJSONParseGen() {
        String json = "[\n" +
                "  {\n \"_id\": \"5a2e62bcc952044058853b92\",\n \"index\": 0,\n" +
                "    \"guid\": \"a8d6d338-5fb4-4775-a14f-30aa631c539f\",\n    \"isActive\": false,\n" +
                "    \"balance\": \"$3,656.67\",\n \"picture\": \"http://placehold.it/32x32\",\n" +
                "    \"age\": 32,\n \"eyeColor\": \"green\",\n \"name\": \"Knapp Price\",\n" +
                "    \"gender\": \"male\",\n \"company\": \"BALOOBA\",\n" +
                "    \"email\": \"knappprice@balooba.com\",\n \"phone\": \"+1 (994) 584-2832\",\n" +
                "    \"address\": \"806 Grand Street, Bedias, Virgin Islands, 274\",\n" +
                "    \"about\": \"Consequat ex veniam voluptate eu commodo minim nulla aliqua"
                + " enim magna dolore cillum velit. Voluptate eiusmod proident cillum qui enim"
                + " velit fugiat aliqua sunt nulla duis pariatur. Tempor minim magna consequat"
                + " cillum id sunt pariatur duis aliquip consequat. Exercitation id eiusmod occaecat"
                + " qui commodo ex excepteur aliquip nostrud consectetur ullamco magna sunt mollit."
                + " Excepteur sint esse aliquip ea adipisicing nostrud sunt reprehenderit nulla dolore"
                + " officia reprehenderit.\\r\\n\",\n \"registered\": \"2015-09-03T11:14:04 -06:-30\",\n" +
                "    \"latitude\": -42.496948,\n \"longitude\": 21.642371,\n" +
                "    \"tags\": [\n \"fugiat\",\n \"fugiat\",\n \"labore\",\n \"consequat\",\n\"adipisicing\",\n" +
                "      \"esse\",\n \"enim\"\n ],\n" +
                "    \"friends\": [\n" +
                "      {\n \"id\": 0,\n \"name\": \"Matilda Hutchinson\"\n},\n" +
                "      {\n \"id\": 1,\n \"name\": \"Christensen Chase\"\n},\n" +
                "      {\n \"id\": 2,\n \"name\": \"Adrian Castillo\"\n}\n" +
                "    ],\n" +
                "    \"greeting\": \"Hello, Knapp Price! You have 2 unread messages.\",\n" +
                "    \"favoriteFruit\": \"banana\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"_id\": \"5a2e62bcb795ed1030bf2729\",\n \"index\": 1,\n" +
                "    \"guid\": \"9593b0fd-b7a6-4269-8598-791c1d446072\",\n \"isActive\": true,\n" +
                "    \"balance\": \"$3,876.09\",\n \"picture\": \"http://placehold.it/32x32\",\n" +
                "    \"age\": 23,\n \"eyeColor\": \"blue\",\n \"name\": \"Carmela Taylor\",\n" +
                "    \"gender\": \"female\",\n \"company\": \"SPLINX\",\n" +
                "    \"email\": \"carmelataylor@splinx.com\",\n \"phone\": \"+1 (946) 580-3736\",\n" +
                "    \"address\": \"984 Legion Street, Neahkahnie, Louisiana, 3996\",\n" +
                "    \"about\": \"Amet incididunt reprehenderit id deserunt aute exercitation"
                + " deserunt consequat irure veniam deserunt. Proident labore nostrud nulla"
                + " irure nulla elit aliquip. Anim officia laboris magna velit fugiat pariatur"
                + " culpa tempor enim consequat aute nulla mollit consequat. Minim pariatur ex"
                + " ut ut labore nisi. Do excepteur in consequat exercitation ipsum incididunt."
                + " Culpa deserunt sit magna sit adipisicing id.\\r\\n\",\n" +
                "    \"registered\": \"2017-10-02T10:56:41 -06:-30\",\n" +
                "    \"latitude\": -58.108242,\n" +
                "    \"longitude\": 13.373699,\n" +
                "    \"tags\": [\n \"eiusmod\",\n \"minim\",\n \"veniam\",\n \"laboris\",\n \"in\",\n" +
                "      \"id\",\n \"incididunt\"\n ],\n" +
                "    \"friends\": [\n" +
                "      {\n \"id\": 0,\n \"name\": \"Flores Pacheco\"\n},\n" +
                "      {\n \"id\": 1,\n \"name\": \"Duran Melton\"\n },\n" +
                "      {\n \"id\": 2,\n \"name\": \"Tammie Mcgowan\"\n}\n" +
                "    ],\n" +
                "    \"greeting\": \"Hello, Carmela Taylor! You have 3 unread messages.\",\n" +
                "    \"favoriteFruit\": \"apple\"\n" +
                "  },\n" +
                "  {\n \"_id\": \"5a2e62bc61cf3baedbf94387\",\n \"index\": 2,\n" +
                "    \"guid\": \"8ef39ecc-dc8e-4ffd-b495-d62850e97887\",\n \"isActive\": true,\n" +
                "    \"balance\": \"$3,337.73\",\n \"picture\": \"http://placehold.it/32x32\",\n" +
                "    \"age\": 29,\n \"eyeColor\": \"green\",\n \"name\": \"Laverne Soto\",\n" +
                "    \"gender\": \"female\",\n \"company\": \"TRASOLA\",\n \"email\": \"lavernesoto@trasola.com\",\n" +
                "    \"phone\": \"+1 (973) 431-3053\",\n" +
                "    \"address\": \"973 Minna Street, Fairacres, California, 1962\",\n" +
                "    \"about\": \"Cupidatat et sunt sunt in anim et ad proident anim ex qui."
                + " Amet commodo ullamco ea tempor non tempor sint fugiat commodo eiusmod"
                + " officia. Nostrud et officia esse duis commodo non do excepteur sunt"
                + " voluptate et. Cillum ex aliquip laboris fugiat laborum elit veniam cupidatat"
                + " cillum commodo nulla ut ut.\\r\\n\",\n" +
                "    \"registered\": \"2016-06-09T10:19:50 -06:-30\",\n" +
                "    \"latitude\": 74.986738,\n" +
                "    \"longitude\": 16.096849,\n" +
                "    \"tags\": [\n \"deserunt\",\n \"reprehenderit\",\n \"ex\",\n \"cillum\",\n \"in\",\n" +
                "      \"aute\",\n \"elit\"\n ],\n" +
                "    \"friends\": [\n" +
                "      {\n \"id\": 0,\n \"name\": \"Vicki Le\"\n},\n" +
                "      {\n \"id\": 1,\n \"name\": \"Elvia Hull\"\n},\n" +
                "      {\n \"id\": 2,\n \"name\": \"Sophie Blankenship\"\n}\n" +
                "    ],\n" +
                "    \"greeting\": \"Hello, Laverne Soto! You have 1 unread messages.\",\n" +
                "    \"favoriteFruit\": \"strawberry\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"_id\": \"5a2e62bc638ea321dad78f67\",\n \"index\": 3,\n" +
                "    \"guid\": \"00dd92e2-c71d-44f7-b5dc-42e9fd4e7f37\",\n" +
                "    \"isActive\": false,\n \"balance\": \"$1,939.48\",\n" +
                "    \"picture\": \"http://placehold.it/32x32\",\n \"age\": 26,\n" +
                "    \"eyeColor\": \"blue\",\n \"name\": \"Rosalyn Walker\",\n \"gender\": \"female\",\n" +
                "    \"company\": \"ELPRO\",\n \"email\": \"rosalynwalker@elpro.com\",\n" +
                "    \"phone\": \"+1 (805) 405-3108\",\n" +
                "    \"address\": \"504 Highland Avenue, Faxon, Indiana, 2611\",\n" +
                "    \"about\": \"Magna enim aute esse pariatur non dolore est nulla dolore cupidatat."
                + " Aliquip qui aliquip nisi ut do fugiat esse occaecat quis veniam. Pariatur anim "
                + "minim commodo dolore. Irure excepteur elit officia irure veniam deserunt excepteur"
                + " mollit. Incididunt non eiusmod nulla tempor dolore duis duis et elit deserunt ad"
                + " sunt adipisicing veniam.\\r\\n\",\n" +
                "    \"registered\": \"2014-11-26T09:31:46 -06:-30\",\n" +
                "    \"latitude\": -76.595194,\n" +
                "    \"longitude\": 82.539045,\n" +
                "    \"tags\": [\n \"adipisicing\",\n \"voluptate\",\n \"ex\",\n \"nisi\",\n \"enim\",\n" +
                "      \"laboris\",\n \"pariatur\"\n],\n" +
                "    \"friends\": [\n" +
                "      {\n \"id\": 0,\n \"name\": \"Deleon Calhoun\"\n},\n" +
                "      {\n \"id\": 1,\n \"name\": \"Delia Porter\"\n},\n" +
                "      {\n \"id\": 2,\n \"name\": \"Toni Mercer\"\n}\n" +
                "    ],\n" +
                "    \"greeting\": \"Hello, Rosalyn Walker! You have 7 unread messages.\",\n" +
                "    \"favoriteFruit\": \"strawberry\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"_id\": \"5a2e62bc0ee2fa109d65e5f8\",\n \"index\": 4,\n" +
                "    \"guid\": \"21fba439-7ccd-4552-b047-29de1770ffdd\",\n \"isActive\": true,\n" +
                "    \"balance\": \"$3,032.06\",\n \"picture\": \"http://placehold.it/32x32\",\n" +
                "    \"age\": 23,\n \"eyeColor\": \"blue\",\n" +
                "    \"name\": \"Earline Cortez\",\n \"gender\": \"female\",\n \"company\": \"VENOFLEX\",\n" +
                "    \"email\": \"earlinecortez@venoflex.com\",\n \"phone\": \"+1 (936) 582-2977\",\n" +
                "    \"address\": \"919 Fairview Place, Manitou, Alabama, 6067\",\n" +
                "    \"about\": \"Ad ad anim sint esse consequat eu id. In aute Lorem ipsum "
                + "labore do laboris aute et culpa. Ex consectetur exercitation occaecat non "
                + "consequat anim nulla dolore nulla excepteur ut amet labore. Elit irure proident"
                + " ipsum consectetur. Eiusmod proident magna qui aliquip non.\\r\\n\",\n" +
                "    \"registered\": \"2016-11-03T10:46:27 -06:-30\",\n" +
                "    \"latitude\": -51.928376,\n" +
                "    \"longitude\": 99.427013,\n" +
                "    \"tags\": [\n \"exercitation\",\n \"sunt\",\n \"non\",\n \"nisi\",\n \"est\",\n \"Lorem\",\n" +
                "      \"nostrud\"\n ],\n" +
                "    \"friends\": [\n" +
                "      {\n \"id\": 0,\n \"name\": \"Jeri Noel\"\n},\n" +
                "      {\n \"id\": 1,\n \"name\": \"Webster Kirby\"\n},\n" +
                "      {\n \"id\": 2,\n \"name\": \"Beatriz Flores\"\n}\n" +
                "    ],\n" +
                "    \"greeting\": \"Hello, Earline Cortez! You have 5 unread messages.\",\n" +
                "    \"favoriteFruit\": \"apple\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"_id\": \"5a2e62bc8a1e885b9edad867\",\n \"index\": 5,\n" +
                "    \"guid\": \"ecf7f3ef-17e5-45b2-9d13-9a487dd4221d\",\n \"isActive\": true,\n" +
                "    \"balance\": \"$1,374.20\",\n \"picture\": \"http://placehold.it/32x32\",\n" +
                "    \"age\": 33,\n \"eyeColor\": \"blue\",\n \"name\": \"Virginia Jackson\",\n" +
                "    \"gender\": \"female\",\n \"company\": \"GREEKER\",\n" +
                "    \"email\": \"virginiajackson@greeker.com\",\n \"phone\": \"+1 (810) 461-2989\",\n" +
                "    \"address\": \"249 Ivan Court, Tecolotito, Maryland, 3734\",\n" +
                "    \"about\": \"Labore incididunt nisi id est elit laborum officia amet ex "
                + "enim eiusmod. Ea nostrud occaecat qui deserunt velit sit Lorem aliqua ea magna. "
                + "Aliquip irure ex fugiat ex irure ex excepteur. Laborum duis nisi est nulla "
                + "incididunt velit elit culpa mollit mollit reprehenderit velit. Mollit id occaecat"
                + " eu proident sit sit est consequat nostrud aliquip id.\\r\\n\",\n" +
                "    \"registered\": \"2014-08-06T02:13:23 -06:-30\",\n" +
                "    \"latitude\": 6.170452,\n" +
                "    \"longitude\": 67.163572,\n" +
                "    \"tags\": [\n \"non\",\n \"minim\",\n \"ipsum\",\n \"duis\",\n \"velit\",\n" +
                "      \"fugiat\",\n \"anim\"\n ],\n" +
                "    \"friends\": [\n" +
                "      {\n \"id\": 0,\n \"name\": \"Melton Walton\"\n},\n" +
                "      {\n \"id\": 1,\n \"name\": \"Ellison Marks\"\n},\n" +
                "      {\n \"id\": 2,\n \"name\": \"Aurelia Wong\"\n}\n" +
                "    ],\n" +
                "    \"greeting\": \"Hello, Virginia Jackson! You have 2 unread messages.\",\n" +
                "    \"favoriteFruit\": \"strawberry\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"_id\": \"5a2e62bced0680efe15377df\",\n \"index\": 6,\n" +
                "    \"guid\": \"1d620f55-7627-4c6f-ba44-83e0e80cad86\",\n \"isActive\": true,\n" +
                "    \"balance\": \"$1,228.11\",\n \"picture\": \"http://placehold.it/32x32\",\n" +
                "    \"age\": 32,\n \"eyeColor\": \"green\",\n \"name\": \"Valencia Bishop\",\n" +
                "    \"gender\": \"male\",\n \"company\": \"GOLOGY\",\n" +
                "    \"email\": \"valenciabishop@gology.com\",\n \"phone\": \"+1 (877) 449-3372\",\n" +
                "    \"address\": \"382 Grant Avenue, Dundee, New Mexico, 1007\",\n" +
                "    \"about\": \"Consectetur sint ex veniam velit. Et excepteur adipisicing "
                + "pariatur sit ex. In adipisicing occaecat qui est est magna. Excepteur "
                + "aliqua mollit pariatur in. Anim voluptate deserunt proident excepteur "
                + "deserunt duis nisi ipsum culpa aliquip est.\\r\\n\",\n" +
                "    \"registered\": \"2016-08-10T08:37:17 -06:-30\",\n" +
                "    \"latitude\": 49.170631,\n" +
                "    \"longitude\": 129.413828,\n" +
                "    \"tags\": [\n \"incididunt\",\n \"adipisicing\",\n \"sit\",\n \"nisi\",\n \"mollit\",\n" +
                "      \"eu\",\n \"tempor\"\n" +
                "    ],\n" +
                "    \"friends\": [\n" +
                "      {\n \"id\": 0,\n \"name\": \"Imelda Obrien\"\n},\n" +
                "      {\n \"id\": 1,\n \"name\": \"Jefferson Webster\"\n},\n" +
                "      {\n \"id\": 2,\n \"name\": \"Bonnie Vincent\"\n}\n" +
                "    ],\n" +
                "    \"greeting\": \"Hello, Valencia Bishop! You have 4 unread messages.\",\n" +
                "    \"favoriteFruit\": \"banana\"\n" +
                "  }\n" +
                "]";
        BValue node = JsonParser.parse(json);
        Assert.assertEquals(node.toString().length(), 7829);
    }

    @Test
    public void testSingleQuoteInArrayElements() {
        String json = "{'fruits':['apple', 'orange', \"grapes\"]}";
        BValue node = JsonParser.parse(json);
        Assert.assertEquals(node.toString(), "{\"fruits\":[\"apple\", \"orange\", \"grapes\"]}");
    }

    @Test(expectedExceptions = {BallerinaException.class},
          expectedExceptionsMessageRegExp = "expected , or ] at line: 1 column: 32")
    public void testMismatchQuotes() {
        String json = "{'fruits':[\"apple', 'orange', \"grapes\"]}";
        BValue node = JsonParser.parse(json);
        Assert.assertEquals(node.toString(), "{\"fruits\":[\"apple\", \"orange\", \"grapes\"]}");
    }
}
