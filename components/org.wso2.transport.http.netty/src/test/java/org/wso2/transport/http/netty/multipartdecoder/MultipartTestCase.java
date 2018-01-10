import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.transport.http.netty.config.ChunkConfig;
import org.wso2.transport.http.netty.contractimpl.HttpWsServerConnectorFuture;
import org.wso2.transport.http.netty.listener.SourceHandler;
import org.wso2.transport.http.netty.message.HttpBodyPart;
import org.wso2.transport.http.netty.multipartdecoder.MultipartContentListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Test case for multipart handling.
 */
public class MultipartTestCase {

    private final HttpDataFactory dataFactory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private final String jsonContent = "{key:value, key2:value2}";
    private EmbeddedChannel channel;
    private HttpWsServerConnectorFuture httpWsServerConnectorFuture = new HttpWsServerConnectorFuture();
    MultipartContentListener listener;

    @BeforeClass
    public void setup() throws Exception {
        channel = new EmbeddedChannel();
        channel.pipeline().addLast(new HttpResponseDecoder());
        channel.pipeline().addLast(new HttpRequestEncoder());
        channel.pipeline().addLast(new SourceHandler(httpWsServerConnectorFuture, null, ChunkConfig.ALWAYS));
        listener = new MultipartContentListener();
        httpWsServerConnectorFuture.setHttpConnectorListener(listener);
    }

    @Test
    public void testMultipart() throws Exception {
        HttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");
        HttpPostRequestEncoder encoder = new HttpPostRequestEncoder(dataFactory, request, true);
        request.headers().add(HttpHeaders.Names.CONTENT_TYPE, HttpHeaders.Values.MULTIPART_FORM_DATA);
        encoder.addBodyHttpData(createJSONAttribute(request, dataFactory));
        encoder.addBodyHttpData(createFileUpload(request, dataFactory));
        request = encoder.finalizeRequest();
        sendMultipartRequest(request, encoder);
        boolean isMultipart = listener.isMultipart();
        Assert.assertEquals(isMultipart, true);
        List<HttpBodyPart> httpBodyParts = listener.getMultiparts();
        Assert.assertNotNull(httpBodyParts, "Received http body parts are null");
        String jsonPart = new String(httpBodyParts.get(0).getContent());
        Assert.assertEquals(jsonPart, jsonContent, "Received body Part value doesn't match with the sent value.");
        Assert.assertEquals(httpBodyParts.get(1).getContentType(), "plain/text", "Incorrect content type received");
        Assert.assertEquals(httpBodyParts.get(1).getPartName(), "file", "Incorrect part name.");
    }

    /**
     * Write multipart request to inbound channel.
     *
     * @param request HttpRequest
     * @param encoder HttpPostRequestEncoder
     * @throws Exception
     */
    private void sendMultipartRequest(HttpRequest request, HttpPostRequestEncoder encoder) throws Exception {
        channel.writeInbound(request);
        if (!channel.isOpen()) {
            encoder.cleanFiles();
            return;
        }
        HttpContent content;
        while (!encoder.isEndOfInput()) {
            content = encoder.readChunk(ByteBufAllocator.DEFAULT);
            channel.writeInbound(content);
        }
        channel.flush();
        encoder.cleanFiles();
    }

    /**
     * Create a json body part.
     *
     * @param request HttpRequest
     * @param factory HttpDataFactory
     * @return InterfaceHttpData
     * @throws IOException
     */
    private InterfaceHttpData createJSONAttribute(HttpRequest request, HttpDataFactory factory) throws IOException {
        ByteBuf content = Unpooled.buffer();
        ByteBufOutputStream byteBufOutputStream = new ByteBufOutputStream(content);
        byteBufOutputStream.writeBytes(jsonContent);
        return factory.createAttribute(request, "json", content.toString(CharsetUtil.UTF_8));
    }

    /**
     * Include a file as a body part.
     *
     * @param request HttpRequest
     * @param factory HttpDataFactory
     * @return InterfaceHttpData
     * @throws IOException
     */
    private InterfaceHttpData createFileUpload(HttpRequest request, HttpDataFactory factory) throws IOException {
        File file = File.createTempFile("upload", ".txt");
        file.deleteOnExit();
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
        bufferedWriter.write("Example file to be posted");
        bufferedWriter.close();
        FileUpload fileUpload = factory
                .createFileUpload(request, "file", file.getName(), "plain/text", "7bit", null, file.length());
        fileUpload.setContent(file);
        return fileUpload;
    }
}
