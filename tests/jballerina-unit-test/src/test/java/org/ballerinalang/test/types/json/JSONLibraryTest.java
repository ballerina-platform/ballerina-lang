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

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.json.JsonGenerator;
import io.ballerina.runtime.internal.json.JsonParser;
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
        Object node = JsonParser.parse(json);
        Assert.assertFalse(node instanceof BArray);
        Assert.assertFalse(node instanceof Boolean);
        Assert.assertFalse(node instanceof Double);
        Assert.assertFalse(node instanceof Long);
        Assert.assertFalse(node instanceof BString);
        Assert.assertTrue(node instanceof BMap);
        Assert.assertNotNull(node);

        BMap<String, Object> jsonObj = (BMap<String, Object>) node;
        Assert.assertTrue(jsonObj.get(StringUtils.fromString("a")) instanceof BString);
        Assert.assertTrue(jsonObj.get(StringUtils.fromString("b")) instanceof Long);
        Assert.assertTrue(jsonObj.get(StringUtils.fromString("c")) instanceof BDecimal);
        Assert.assertFalse(jsonObj.get(StringUtils.fromString("c")) instanceof Long);
        Assert.assertTrue(jsonObj.get(StringUtils.fromString("d")) instanceof Boolean);
        Assert.assertTrue(jsonObj.get(StringUtils.fromString("e")) instanceof Boolean);
        Assert.assertNull(jsonObj.get(StringUtils.fromString("f")));
        Assert.assertTrue(jsonObj.get(StringUtils.fromString("g")) instanceof BMap);
        Assert.assertFalse(jsonObj.get(StringUtils.fromString("h")) instanceof BMap);
        Assert.assertTrue(jsonObj.get(StringUtils.fromString("h")) instanceof BArray);
    }

    @Test
    public void testBasicJsonObjectParseValues() {
        String json = "{\"a\":\"abc\",\"b\":1,\"c\":3.14,\"d\":true,\"e\":false,\"f\":null,"
                + "\"g\":{\"1\":\"a\",\"2\":\"b\"},\"h\":[\"A\",20,30,\"D\"]}";
        BMap<String, Object> node = (BMap<String, Object>) JsonParser.parse(json);
        Assert.assertEquals(node.get(StringUtils.fromString("a")).toString(), "abc");
        Assert.assertEquals(node.get(StringUtils.fromString("b")), 1L);
        Assert.assertEquals(node.get(StringUtils.fromString("c")), ValueCreator.createDecimalValue("3.14"));
        Assert.assertTrue((Boolean) node.get(StringUtils.fromString("d")));
        Assert.assertFalse((Boolean) node.get(StringUtils.fromString("e")));
        Assert.assertNull(node.get(StringUtils.fromString("f")));

        Assert.assertTrue(node.get(StringUtils.fromString("g")) instanceof BMap);
        BMap<String, Object> objNode = (BMap<String, Object>) node.get(StringUtils.fromString("g"));
        Assert.assertEquals(objNode.get(StringUtils.fromString("1")).toString(), "a");
        Assert.assertEquals(objNode.get(StringUtils.fromString("2")).toString(), "b");

        Assert.assertTrue(node.get(StringUtils.fromString("h")) instanceof BArray);
        BArray arrayNode = (BArray) node.get(StringUtils.fromString("h"));
        Assert.assertEquals(arrayNode.size(), 4);
        Assert.assertEquals(arrayNode.getRefValue(0).toString(), "A");
        Assert.assertEquals((arrayNode.getRefValue(1)), 20L);
        Assert.assertEquals((arrayNode.getRefValue(2)), 30L);
        Assert.assertEquals(arrayNode.getRefValue(3).toString(), "D");
    }

    @Test
    public void testBasicJsonObjectGenValues() {
        String json = "{\"a\":\"abc\", \"b\":1, \"c\":3.14, \"d\":true, \"e\":false, \"f\":null, "
                + "\"g\":{\"1\":\"a\", \"2\":\"b\"}, \"h\":[\"A\", 20, 30, \"D\"]}";
        Object node = JsonParser.parse(json);
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             JsonGenerator gen = new JsonGenerator(byteOut)) {
            gen.serialize(node);
            gen.flush();
            Assert.assertEquals(byteOut.toString(), json);
        } catch (IOException e) {
            Assert.fail("Error while generating json");
        }
    }

    @Test
    public void testJsonEscapeChars() {
        String json = "[{\"a\":\"abc\\\"\", \"x\":\"1\\b\\f\", \"c\":3.14, \"d\":true, \"e\":false, \"f\":null, \"g\":"
                + "{\"1\\n2\":\"a\\r\", \"2\":\"b\"}, \"h\":[\"A\\tB\", 20, 30, \"D\\\\\"]}]";
        String expected =
                "[{\"a\":\"abc\"\",\"x\":\"1\b\f\",\"c\":3.14,\"d\":true,\"e\":false,\"f\":null,\"g\":{\"1\n" +
                        "2\":\"a\r\",\"2\":\"b\"},\"h\":[\"A\tB\",20,30,\"D\\\"]}]";
        Object node = JsonParser.parse(json);
        String result = node.toString();
        Assert.assertEquals(result, expected);
    }

    @Test
    public void testJsonUnicodeChars() {
        String json = "{\"firstName\":\"Will\", \"lastName\":\"Smith\", \"info\":\"\\u2600\", "
                + "\"info2\":\"A\\u2655\\u2665\\u266A\\u266aB\"}";
        String json2 = "{\"firstName\":\"Will\",\"lastName\":\"Smith\",\"info\":\"\u2600\","
                + "\"info2\":\"A\u2655\u2665\u266A\u266aB\"}";
        BMap<String, Object> node = (BMap<String, Object>) JsonParser.parse(json);
        Assert.assertEquals(node.get(StringUtils.fromString("info")).toString(), "☀");
        Assert.assertEquals(node.get(StringUtils.fromString("info2")).toString(), "A♕♥♪♪B");
        Assert.assertEquals(node.stringValue(null), json2);
    }

    @Test
    public void testJsonDeepLevels() {
        String json = "{\"A\":{\"B\":{\"C\":{\"D\":{\"E\":{\"F\":{\"G\":{\"H\":{\"I\":{\"J\":"
                + "{\"K\":{\"L\":{\"M\":{\"N\":5}}}}}}}}}}}}}}";
        Object node = JsonParser.parse(json);
        Assert.assertEquals(node.toString(), json);
    }

    @Test
    public void testComplexJSONParseGen() {
        String json = """
                [
                  {
                 "_id": "5a2e62bcc952044058853b92",
                 "index": 0,
                    "guid": "a8d6d338-5fb4-4775-a14f-30aa631c539f",
                    "isActive": false,
                    "balance": "$3,656.67",
                 "picture": "http://placehold.it/32x32",
                    "age": 32,
                 "eyeColor": "green",
                 "name": "Knapp Price",
                    "gender": "male",
                 "company": "BALAOBA",
                    "email": "knappprice@balaoba.com",
                 "phone": "+1 (994) 584-2832",
                    "address": "806 Grand Street, Bedias, Virgin Islands, 274",
                    "about": "Consequat ex veniam voluptate eu commodo minim nulla aliqua\
                 enim magna dolore cillum velit. Voluptate eiusmod proident cillum qui enim\
                 velit fugiat aliqua sunt nulla duis pariatur. Tempor minim magna consequat\
                 cillum id sunt pariatur duis aliquip consequat. Exercitation id eiusmod occaecat\
                 qui commodo ex excepteur aliquip nostrud consectetur ullamco magna sunt mollit.\
                 Excepteur sint esse aliquip ea adipisicing nostrud sunt reprehenderit nulla dolore\
                 officia reprehenderit.\\r\\n",
                 "registered": "2015-09-03T11:14:04 -06:-30",
                    "latitude": -42.496948,
                 "longitude": 21.642371,
                    "tags": [
                 "fugiat",
                 "fugiat",
                 "labore",
                 "consequat",
                "adipisicing",
                      "esse",
                 "enim"
                 ],
                    "friends": [
                      {
                 "id": 0,
                 "name": "Matilda Hutchinson"
                },
                      {
                 "id": 1,
                 "name": "Christensen Chase"
                },
                      {
                 "id": 2,
                 "name": "Adrian Castillo"
                }
                    ],
                    "greeting": "Hello, Knapp Price! You have 2 unread messages.",
                    "favoriteFruit": "banana"
                  },
                  {
                    "_id": "5a2e62bcb795ed1030bf2729",
                 "index": 1,
                    "guid": "9593b0fd-b7a6-4269-8598-791c1d446072",
                 "isActive": true,
                    "balance": "$3,876.09",
                 "picture": "http://placehold.it/32x32",
                    "age": 23,
                 "eyeColor": "blue",
                 "name": "Carmela Taylor",
                    "gender": "female",
                 "company": "SPLINX",
                    "email": "carmelataylor@splinx.com",
                 "phone": "+1 (946) 580-3736",
                    "address": "984 Legion Street, Neahkahnie, Louisiana, 3996",
                    "about": "Amet incididunt reprehenderit id deserunt aute exercitation\
                 deserunt consequat irure veniam deserunt. Proident labore nostrud nulla\
                 irure nulla elit aliquip. Anim officia laboris magna velit fugiat pariatur\
                 culpa tempor enim consequat aute nulla mollit consequat. Minim pariatur ex\
                 ut ut labore nisi. Do excepteur in consequat exercitation ipsum incididunt.\
                 Culpa deserunt sit magna sit adipisicing id.\\r\\n",
                    "registered": "2017-10-02T10:56:41 -06:-30",
                    "latitude": -58.108242,
                    "longitude": 13.373699,
                    "tags": [
                 "eiusmod",
                 "minim",
                 "veniam",
                 "laboris",
                 "in",
                      "id",
                 "incididunt"
                 ],
                    "friends": [
                      {
                 "id": 0,
                 "name": "Flores Pacheco"
                },
                      {
                 "id": 1,
                 "name": "Duran Melton"
                 },
                      {
                 "id": 2,
                 "name": "Tammie Mcgowan"
                }
                    ],
                    "greeting": "Hello, Carmela Taylor! You have 3 unread messages.",
                    "favoriteFruit": "apple"
                  },
                  {
                 "_id": "5a2e62bc61cf3baedbf94387",
                 "index": 2,
                    "guid": "8ef39ecc-dc8e-4ffd-b495-d62850e97887",
                 "isActive": true,
                    "balance": "$3,337.73",
                 "picture": "http://placehold.it/32x32",
                    "age": 29,
                 "eyeColor": "green",
                 "name": "Laverne Soto",
                    "gender": "female",
                 "company": "TRASOLA",
                 "email": "lavernesoto@trasola.com",
                    "phone": "+1 (973) 431-3053",
                    "address": "973 Minna Street, Fairacres, California, 1962",
                    "about": "Cupidatat et sunt sunt in anim et ad proident anim ex qui.\
                 Amet commodo ullamco ea tempor non tempor sint fugiat commodo eiusmod\
                 officia. Nostrud et officia esse duis commodo non do excepteur sunt\
                 voluptate et. Cillum ex aliquip laboris fugiat laborum elit veniam cupidatat\
                 cillum commodo nulla ut ut.\\r\\n",
                    "registered": "2016-06-09T10:19:50 -06:-30",
                    "latitude": 74.986738,
                    "longitude": 16.096849,
                    "tags": [
                 "deserunt",
                 "reprehenderit",
                 "ex",
                 "cillum",
                 "in",
                      "aute",
                 "elit"
                 ],
                    "friends": [
                      {
                 "id": 0,
                 "name": "Vicki Le"
                },
                      {
                 "id": 1,
                 "name": "Elvia Hull"
                },
                      {
                 "id": 2,
                 "name": "Sophie Blankenship"
                }
                    ],
                    "greeting": "Hello, Laverne Soto! You have 1 unread messages.",
                    "favoriteFruit": "strawberry"
                  },
                  {
                    "_id": "5a2e62bc638ea321dad78f67",
                 "index": 3,
                    "guid": "00dd92e2-c71d-44f7-b5dc-42e9fd4e7f37",
                    "isActive": false,
                 "balance": "$1,939.48",
                    "picture": "http://placehold.it/32x32",
                 "age": 26,
                    "eyeColor": "blue",
                 "name": "Rosalyn Walker",
                 "gender": "female",
                    "company": "ELPRO",
                 "email": "rosalynwalker@elpro.com",
                    "phone": "+1 (805) 405-3108",
                    "address": "504 Highland Avenue, Faxon, Indiana, 2611",
                    "about": "Magna enim aute esse pariatur non dolore est nulla dolore cupidatat.\
                 Aliquip qui aliquip nisi ut do fugiat esse occaecat quis veniam. Pariatur anim \
                minim commodo dolore. Irure excepteur elit officia irure veniam deserunt excepteur\
                 mollit. Incididunt non eiusmod nulla tempor dolore duis duis et elit deserunt ad\
                 sunt adipisicing veniam.\\r\\n",
                    "registered": "2014-11-26T09:31:46 -06:-30",
                    "latitude": -76.595194,
                    "longitude": 82.539045,
                    "tags": [
                 "adipisicing",
                 "voluptate",
                 "ex",
                 "nisi",
                 "enim",
                      "laboris",
                 "pariatur"
                ],
                    "friends": [
                      {
                 "id": 0,
                 "name": "Deleon Calhoun"
                },
                      {
                 "id": 1,
                 "name": "Delia Porter"
                },
                      {
                 "id": 2,
                 "name": "Toni Mercer"
                }
                    ],
                    "greeting": "Hello, Rosalyn Walker! You have 7 unread messages.",
                    "favoriteFruit": "strawberry"
                  },
                  {
                    "_id": "5a2e62bc0ee2fa109d65e5f8",
                 "index": 4,
                    "guid": "21fba439-7ccd-4552-b047-29de1770ffdd",
                 "isActive": true,
                    "balance": "$3,032.06",
                 "picture": "http://placehold.it/32x32",
                    "age": 23,
                 "eyeColor": "blue",
                    "name": "Earline Cortez",
                 "gender": "female",
                 "company": "VENOFLEX",
                    "email": "earlinecortez@venoflex.com",
                 "phone": "+1 (936) 582-2977",
                    "address": "919 Fairview Place, Manitou, Alabama, 6067",
                    "about": "Ad ad anim sint esse consequat eu id. In aute Lorem ipsum \
                labore do laboris aute et culpa. Ex consectetur exercitation occaecat non \
                consequat anim nulla dolore nulla excepteur ut amet labore. Elit irure proident\
                 ipsum consectetur. Eiusmod proident magna qui aliquip non.\\r\\n",
                    "registered": "2016-11-03T10:46:27 -06:-30",
                    "latitude": -51.928376,
                    "longitude": 99.427013,
                    "tags": [
                 "exercitation",
                 "sunt",
                 "non",
                 "nisi",
                 "est",
                 "Lorem",
                      "nostrud"
                 ],
                    "friends": [
                      {
                 "id": 0,
                 "name": "Jeri Noel"
                },
                      {
                 "id": 1,
                 "name": "Webster Kirby"
                },
                      {
                 "id": 2,
                 "name": "Beatriz Flores"
                }
                    ],
                    "greeting": "Hello, Earline Cortez! You have 5 unread messages.",
                    "favoriteFruit": "apple"
                  },
                  {
                    "_id": "5a2e62bc8a1e885b9edad867",
                 "index": 5,
                    "guid": "ecf7f3ef-17e5-45b2-9d13-9a487dd4221d",
                 "isActive": true,
                    "balance": "$1,374.20",
                 "picture": "http://placehold.it/32x32",
                    "age": 33,
                 "eyeColor": "blue",
                 "name": "Virginia Jackson",
                    "gender": "female",
                 "company": "GREEKER",
                    "email": "virginiajackson@greeker.com",
                 "phone": "+1 (810) 461-2989",
                    "address": "249 Ivan Court, Tecolotito, Maryland, 3734",
                    "about": "Labore incididunt nisi id est elit laborum officia amet ex \
                enim eiusmod. Ea nostrud occaecat qui deserunt velit sit Lorem aliqua ea magna. \
                Aliquip irure ex fugiat ex irure ex excepteur. Laborum duis nisi est nulla \
                incididunt velit elit culpa mollit mollit reprehenderit velit. Mollit id occaecat\
                 eu proident sit sit est consequat nostrud aliquip id.\\r\\n",
                    "registered": "2014-08-06T02:13:23 -06:-30",
                    "latitude": 6.170452,
                    "longitude": 67.163572,
                    "tags": [
                 "non",
                 "minim",
                 "ipsum",
                 "duis",
                 "velit",
                      "fugiat",
                 "anim"
                 ],
                    "friends": [
                      {
                 "id": 0,
                 "name": "Melton Walton"
                },
                      {
                 "id": 1,
                 "name": "Ellison Marks"
                },
                      {
                 "id": 2,
                 "name": "Aurelia Wong"
                }
                    ],
                    "greeting": "Hello, Virginia Jackson! You have 2 unread messages.",
                    "favoriteFruit": "strawberry"
                  },
                  {
                    "_id": "5a2e62bced0680efe15377df",
                 "index": 6,
                    "guid": "1d620f55-7627-4c6f-ba44-83e0e80cad86",
                 "isActive": true,
                    "balance": "$1,228.11",
                 "picture": "http://placehold.it/32x32",
                    "age": 32,
                 "eyeColor": "green",
                 "name": "Valencia Bishop",
                    "gender": "male",
                 "company": "GOLOGY",
                    "email": "valenciabishop@gology.com",
                 "phone": "+1 (877) 449-3372",
                    "address": "382 Grant Avenue, Dundee, New Mexico, 1007",
                    "about": "Consectetur sint ex veniam velit. Et excepteur adipisicing \
                pariatur sit ex. In adipisicing occaecat qui est est magna. Excepteur \
                aliqua mollit pariatur in. Anim voluptate deserunt proident excepteur \
                deserunt duis nisi ipsum culpa aliquip est.\\r\\n",
                    "registered": "2016-08-10T08:37:17 -06:-30",
                    "latitude": 49.170631,
                    "longitude": 129.413828,
                    "tags": [
                 "incididunt",
                 "adipisicing",
                 "sit",
                 "nisi",
                 "mollit",
                      "eu",
                 "tempor"
                    ],
                    "friends": [
                      {
                 "id": 0,
                 "name": "Imelda Obrien"
                },
                      {
                 "id": 1,
                 "name": "Jefferson Webster"
                },
                      {
                 "id": 2,
                 "name": "Bonnie Vincent"
                }
                    ],
                    "greeting": "Hello, Valencia Bishop! You have 4 unread messages.",
                    "favoriteFruit": "banana"
                  }
                ]""";
        Object node = JsonParser.parse(json);
        Assert.assertEquals(node.toString().length(), 7585);
    }

    @Test
    public void testQuoteInArrayElements() {
        String json = "{\"fruits\":[\"apple\", \"orange\", \"grapes\"]}";
        Object node = JsonParser.parse(json);
        Assert.assertEquals(node.toString(), "{\"fruits\":[\"apple\",\"orange\",\"grapes\"]}");
    }

    @Test(expectedExceptions = {BError.class},
            expectedExceptionsMessageRegExp = "expected '\"' or '}' at line: 1 column: 2")
    public void testMismatchQuotes() {
        String json = "{'fruits':[\"apple', 'orange', \"grapes\"]}";
        Object node = JsonParser.parse(json);
    }
}
