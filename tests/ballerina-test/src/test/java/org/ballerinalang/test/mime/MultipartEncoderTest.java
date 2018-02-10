package org.ballerinalang.test.mime;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.mime.util.MimeUtil;
import org.ballerinalang.mime.util.MultipartDataSource;
import org.ballerinalang.mime.util.MultipartDecoder;
import org.ballerinalang.model.values.BStruct;
import org.jvnet.mimepull.MIMEPart;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.activation.MimeTypeParseException;

public class MultipartEncoderTest {
    private CompileResult result;
    //Used only to get an instance of CompileResult.
    private String sourceFilePath = "test-src/mime/dummy.bal";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile(sourceFilePath);
    }

    @Test(description = "Test that the body parts get correctly encoded for multipart/mixed")
    public void testMultipartWriterForMixed() {
        ArrayList<BStruct> bodyParts = new ArrayList<>();
        bodyParts.add(Util.getJsonBodyPart(result));
        bodyParts.add(Util.getXmlFilePart(result));
        bodyParts.add(Util.getTextBodyPart(result));
        bodyParts.add(Util.getBinaryFilePart(result));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String multipartDataBoundary = MimeUtil.getNewMultipartDelimiter();

        MultipartDataSource multipartDataSource = new MultipartDataSource(Util.getArrayOfBodyParts(bodyParts),
                multipartDataBoundary);
        multipartDataSource.serializeData(outputStream);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        try {
            List<MIMEPart> mimeParts= MultipartDecoder.decodeBodyParts("multipart/mixed; boundary=" +
                    multipartDataBoundary, inputStream);
            Assert.assertEquals(mimeParts.size(), 4);

        } catch (MimeTypeParseException e) {
            e.printStackTrace();
        }
    }
}
