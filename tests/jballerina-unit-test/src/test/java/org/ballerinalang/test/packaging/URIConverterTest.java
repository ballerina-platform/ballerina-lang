package org.ballerinalang.test.packaging;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.converters.URIConverter;

import java.net.URI;
import java.util.Collections;
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
        URIConverter subject = new URIConverter(baseURI);

        List<URI> urls = patten.convert(subject, null)
                               .collect(Collectors.toList());

        URI expected = URI.create("http://staging.central.ballerina.io:9090/modules/natasha/foo.bar/1.0.5/");
        Assert.assertEquals(urls, Collections.singletonList(expected));
    }

}
