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

package org.ballerinalang.test.service.http2;

import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * A test case for http2 client upgrade with a large payload.
 */
@Test(groups = "http2-test")
public class ClientUpgradeWithLargePayload extends Http2BaseTest {

    private final int servicePort = 9101;

    @Test(description = "Test http2 client upgrade scenario with a large payload.")
    public void testClientUpgradewithLargePayload() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(serverInstance.getServiceURLHttp(servicePort, "echo/initial"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getData(),
                "{\"web-app\":{\"servlet\":[{\"servlet-name\":\"cofaxCDS\", \"servlet-class\":"
                        + "\"org.cofax.cds.CDSServlet\", \"init-param\":{\"configGlossary:installationAt\":"
                        + "\"Philadelphia, PA\", \"configGlossary:adminEmail\":\"ksm@pobox.com\", "
                        + "\"configGlossary:poweredBy\":\"Cofax\", \"configGlossary:poweredByIcon\":"
                        + "\"/images/cofax.gif\", \"configGlossary:staticPath\":\"/content/static\", "
                        + "\"templateProcessorClass\":\"org.cofax.WysiwygTemplate\", \"templateLoaderClass\":"
                        + "\"org.cofax.FilesTemplateLoader\", \"templatePath\":\"templates\", \"templateOverridePath\":"
                        + "\"\", \"defaultListTemplate\":\"listTemplate.htm\", \"defaultFileTemplate\":"
                        + "\"articleTemplate.htm\", \"useJSP\":false, \"jspListTemplate\":\"listTemplate.jsp\", "
                        + "\"jspFileTemplate\":\"articleTemplate.jsp\", \"cachePackageTagsTrack\":200, "
                        + "\"cachePackageTagsStore\":200, \"cachePackageTagsRefresh\":60, \"cacheTemplatesTrack\":100, "
                        + "\"cacheTemplatesStore\":50, \"cacheTemplatesRefresh\":15, \"cachePagesTrack\":200, "
                        + "\"cachePagesStore\":100, \"cachePagesRefresh\":10, \"cachePagesDirtyRead\":10, "
                        + "\"searchEngineListTemplate\":\"forSearchEnginesList.htm\", \"searchEngineFileTemplate\":"
                        + "\"forSearchEngines.htm\", \"searchEngineRobotsDb\":\"WEB-INF/robots.db\", "
                        + "\"useDataStore\":true, \"dataStoreClass\":\"org.cofax.SqlDataStore\", \"redirectionClass\":"
                        + "\"org.cofax.SqlRedirection\", \"dataStoreName\":\"cofax\", \"dataStoreDriver\":"
                        + "\"com.microsoft.jdbc.sqlserver.SQLServerDriver\", \"dataStoreUrl\":"
                        + "\"jdbc:microsoft:sqlserver://LOCALHOST:1433;DatabaseName=goon\", \"dataStoreUser\":"
                        + "\"sa\", \"dataStorePassword\":\"dataStoreTestQuery\", \"dataStoreTestQuery\":"
                        + "\"SET NOCOUNT ON;select test='test';\", \"dataStoreLogFile\":"
                        + "\"/usr/local/tomcat/logs/datastore.log\", \"dataStoreInitConns\":10, "
                        + "\"dataStoreMaxConns\":100, \"dataStoreConnUsageLimit\":100, \"dataStoreLogLevel\":\"debug\","
                        + " \"maxUrlLength\":500}}, {\"servlet-name\":\"cofaxEmail\", \"servlet-class\":"
                        + "\"org.cofax.cds.EmailServlet\", \"init-param\":{\"mailHost\":\"mail1\", "
                        + "\"mailHostOverride\":\"mail2\"}}, {\"servlet-name\":\"cofaxAdmin\", \"servlet-class\":"
                        + "\"org.cofax.cds.AdminServlet\"}, {\"servlet-name\":\"fileServlet\", \"servlet-class\":"
                        + "\"org.cofax.cds.FileServlet\"}, {\"servlet-name\":\"cofaxTools\", \"servlet-class\":"
                        + "\"org.cofax.cms.CofaxToolsServlet\", \"init-param\":{\"templatePath\":\"toolstemplates/\", "
                        + "\"log\":1, \"logLocation\":\"/usr/local/tomcat/logs/CofaxTools.log\", \"logMaxSize\":\"\", "
                        + "\"dataLog\":1, \"dataLogLocation\":\"/usr/local/tomcat/logs/dataLog.log\", "
                        + "\"dataLogMaxSize\":\"\", \"removePageCache\":\"/content/admin/remove?cache=pages&id=\", "
                        + "\"removeTemplateCache\":\"/content/admin/remove?cache=templates&id=\", "
                        + "\"fileTransferFolder\":\"/usr/local/tomcat/webapps/content/fileTransferFolder\", "
                        + "\"lookInContext\":1, \"adminGroupID\":4, \"betaServer\":true}}], \"servlet-mapping\":{"
                        + "\"cofaxCDS\":\"/\", \"cofaxEmail\":\"/cofaxutil/aemail/*\", \"cofaxAdmin\":\"/admin/*\", "
                        + "\"fileServlet\":\"/static/*\", \"cofaxTools\":\"/tools/*\"}, \"taglib\":{\"taglib-uri\":"
                        + "\"cofax.tld\", \"taglib-location\":\"/WEB-INF/tlds/cofax.tld\"}}}",
                "Incorrect data received");
    }
}
