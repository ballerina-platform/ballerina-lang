package org.ballerinalang.test.mime;


import io.netty.util.internal.StringUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.net.http.Constants;
import org.ballerinalang.net.http.HttpUtil;
import org.ballerinalang.test.services.testutils.HTTPTestRequest;
import org.ballerinalang.test.services.testutils.MessageUtils;
import org.ballerinalang.test.services.testutils.Services;
import org.ballerinalang.test.utils.ResponseReader;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.Header;
import org.wso2.transport.http.netty.message.HTTPCarbonMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.mime.util.Constants.CONTENT_TYPE;
import static org.ballerinalang.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.mime.util.Constants.MULTIPART_MIXED;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_FILE;
import static org.ballerinalang.mime.util.Constants.PROTOCOL_PACKAGE_MIME;

public class GenericMultipartTest {
    private CompileResult result, serviceResult;
    private final String requestStruct = Constants.IN_REQUEST;
    private final String protocolPackageHttp = Constants.PROTOCOL_PACKAGE_HTTP;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;
    private final String protocolPackageFile = PROTOCOL_PACKAGE_FILE;
    private final String entityStruct = Constants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;
    private String sourceFilePath = "test-src/mime/multipart-request.bal";
    private final String carbonMessage = "CarbonMessage";
    private final String ballerinaRequest = "BallerinaRequest";
    private final String multipartEntity = "MultipartEntity";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(sourceFilePath);
        serviceResult = BServiceUtil.setupProgramFile(this, sourceFilePath);
    }

    @Test(description = "Test sending a multipart request as multipart/mixed with multiple body parts")
    public void testMultiplePartsForMixed() {
        String path = "/test/multipleparts";
        Map<String, Object> messageMap = createPrerequisiteMessages(path, MULTIPART_MIXED);
       /* ArrayList<BStruct> bodyParts = new ArrayList<>();
        bodyParts.add(getJsonBodyPart());
        bodyParts.add(getXmlFilePart());
        bodyParts.add(getTextBodyPart());
        bodyParts.add(getBinaryFilePart());
        HTTPTestRequest cMsg = getCarbonMessageWithBodyParts(messageMap, getArrayOfBodyParts(bodyParts));*/
        List<Header> headers = new ArrayList<>();
        String multipartDataBoundary = MimeUtil.getNewMultipartDelimiter();
        headers.add(new Header(CONTENT_TYPE, "multipart/mixed; boundary=" + multipartDataBoundary));
       /* String multipartBody = "--" + multipartDataBoundary + "\r\n" +
                                "Content-Disposition: form-data; name=\"foo\"" + "\r\n" +
                                "Content-Type: text/plain; charset=UTF-8" + "\r\n" +
                                "\r\n" +
                                "bar" +
                                "\r\n" +
                                "--" + multipartDataBoundary + "\r\n" +
                                "Content-Disposition: form-data; name=\"quux\"; filename=\"file-01.txt\"" + "\r\n" +
                                "Content-Type: text/plain" + "\r\n" +
                               "Content-Transfer-Encoding: binary" + "\r\n" +
                                "\r\n" +
                                "File 01" + StringUtil.NEWLINE +
                                "\r\n" +
                                "--" + multipartDataBoundary + "--" + "\r\n";*/
        String multipartBody = "--" + multipartDataBoundary + "\r\n" +
                "Content-Type: text/plain; charset=UTF-8" + "\r\n" +
                "\r\n" +
                "bar" +
                "\r\n" +
                "--" + multipartDataBoundary + "\r\n" +
                "Content-Type: text/plain" + "\r\n" +
                "Content-Transfer-Encoding: binary" + "\r\n" +
                "\r\n" +
                "File 01" + StringUtil.NEWLINE +
                "\r\n" +
                "--" + multipartDataBoundary + "--" + "\r\n";

        HTTPTestRequest inRequestMsg = MessageUtils.generateHTTPMessage(path, Constants.HTTP_METHOD_POST, headers,
                multipartBody);
        HTTPCarbonMessage response = Services.invokeNew(serviceResult, inRequestMsg);
        Assert.assertNotNull(response, "Response message not found");
        Assert.assertEquals(ResponseReader.getReturnValue(response), " -- bar -- File 01" + StringUtil.NEWLINE);
    }

    /**
     * Create prerequisite messages that are needed to proceed with the test cases.
     *
     * @param path Represent path to the resource
     * @return A map of relevant messages
     */
    private Map<String, Object> createPrerequisiteMessages(String path, String topLevelContentType) {
        Map<String, Object> messageMap = new HashMap<>();
        BStruct request = getRequestStruct();
        HTTPTestRequest cMsg = MessageUtils.generateHTTPMessageForMultiparts(path, Constants.HTTP_METHOD_POST);
        HttpUtil.addCarbonMsg(request, cMsg);
        BStruct entity = getEntityStruct();
        MimeUtil.setContentType(getMediaTypeStruct(), entity, topLevelContentType);
        messageMap.put(carbonMessage, cMsg);
        messageMap.put(ballerinaRequest, request);
        messageMap.put(multipartEntity, entity);
        return messageMap;
    }

    private BStruct getRequestStruct() {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageHttp, requestStruct);
    }

    private BStruct getEntityStruct() {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime, entityStruct);
    }

    private BStruct getMediaTypeStruct() {
        return BCompileUtil.createAndGetStruct(result.getProgFile(), protocolPackageMime,
                mediaTypeStruct);
    }

}
