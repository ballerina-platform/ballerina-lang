/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.service.http.sample;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.test.service.http.HttpBaseTest;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.ballerinalang.jvm.XMLFactory.isEqual;
import static org.ballerinalang.jvm.XMLFactory.parse;

/**
 * Test case for XML Serialization.
 */
@Test(groups = "http-test")
public class SerializeComplexXmlTestCase extends HttpBaseTest {
    private static final String XML_DATA = "<?xml version=\"1.0\"?>\n" +
            "<?xml-stylesheet href=\"catalog.xsl\" type=\"text/xsl\"?>\n" +
            "<!--<!DOCTYPE catalog SYSTEM \"catalog.dtd\">-->\n" +
            "<!-- Single line comment.-->\n" +
            "<catalog>\n" +
            "    <product description=\"Cardigan Sweater\" product_image=\"cardigan.jpg\">\n" +
            "        <catalog_item gender=\"Men's\">\n" +
            "            <item_number>QWZ5671</item_number>\n" +
            "            <price>39.95</price>\n" +
            "            <size description=\"Medium\">\n" +
            "                <color_swatch image=\"red_cardigan.jpg\">Red</color_swatch>\n" +
            "                <color_swatch image=\"burgundy_cardigan.jpg\">Burgundy</color_swatch>\n" +
            "            </size>\n" +
            "\n" +
            "            <size description=\"Large\">\n" +
            "                <color_swatch image=\"red_cardigan.jpg\">Red</color_swatch>\n" +
            "                <color_swatch image=\"burgundy_cardigan.jpg\">Burgundy</color_swatch>\n" +
            "            </size>\n" +
            "        </catalog_item>\n" +
            "        <catalog_item gender=\"Women's\">\n" +
            "            <item_number>RRX9856</item_number>\n" +
            "            <price>42.50</price>\n" +
            "            <!-- Multi line comment.\n" +
            "               For each property, a random number is created.\n" +
            "               This number is made of two parts that each\n" +
            "               ranges from 100 to 999.\n" +
            "               Both parts are separated by a dash.\n" +
            "           -->\n" +
            "            <size description=\"Small\">\n" +
            "                <color_swatch image=\"red_cardigan.jpg\">Red</color_swatch>\n" +
            "                <color_swatch image=\"navy_cardigan.jpg\">Navy</color_swatch>\n" +
            "                <color_swatch image=\"burgundy_cardigan.jpg\">Burgundy</color_swatch>\n" +
            "            </size>\n" +
            "            <size description=\"Medium\">\n" +
            "                <color_swatch image=\"red_cardigan.jpg\">Red</color_swatch>\n" +
            "                <color_swatch image=\"navy_cardigan.jpg\">Navy</color_swatch>\n" +
            "                <color_swatch image=\"burgundy_cardigan.jpg\">Burgundy</color_swatch>\n" +
            "                <color_swatch image=\"black_cardigan.jpg\">Black</color_swatch>\n" +
            "            </size>\n" +
            "            <size description=\"Large\">\n" +
            "                <color_swatch image=\"navy_cardigan.jpg\">Navy</color_swatch>\n" +
            "                <color_swatch image=\"black_cardigan.jpg\">Black</color_swatch>\n" +
            "            </size>\n" +
            "            <size description=\"Extra Large\">\n" +
            "                <color_swatch image=\"burgundy_cardigan.jpg\">Burgundy</color_swatch>\n" +
            "                <color_swatch image=\"black_cardigan.jpg\">Black</color_swatch>\n" +
            "            </size>\n" +
            "        </catalog_item>\n" +
            "    </product>\n" +
            "</catalog>\n";

    @Test
    public void testXmlSerialization() throws IOException {
        int servicePort = 9250;
        HttpResponse response = HttpClientRequest.doGetAndPreserveNewlineInResponseData(
                serverInstance.getServiceURLHttp(servicePort, "serialize/xml"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(HttpHeaderNames.CONTENT_TYPE.toString()),
                            TestConstant.CONTENT_TYPE_XML, "Content-Type mismatched");
        Assert.assertTrue(isEqual(parse(response.getData()), parse(XML_DATA)), "Message content mismatched");
    }
}
