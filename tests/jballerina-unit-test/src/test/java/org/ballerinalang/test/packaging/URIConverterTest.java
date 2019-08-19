/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.packaging;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.URIConverter;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.wso2.ballerinalang.compiler.packaging.Patten.path;

/**
 * Testcase for URI converter.
 */
public class URIConverterTest {

    @Test
    public void testURIConverter() {
        URI baseURI = URI.create("http://staging.central.ballerina.io:9090/");
        Patten patten = new Patten(path("natasha", "foo.bar", "1.0.5"));
        URIConverter subject = new URIConverter(baseURI, new HashMap<>());

        List<URI> urls = patten.convert(subject, null)
                               .collect(Collectors.toList());

        URI expected = URI.create("http://staging.central.ballerina.io:9090/modules/natasha/foo.bar/1.0.5/");
        Assert.assertEquals(urls, Collections.singletonList(expected));
    }

}
