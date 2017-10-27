package org.ballerinalang.test.nativeimpl.functions.io.util;


import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.ByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains the util functions related to I/O test cases.
 */
public class TestUtil {

    /**
     * Opens a channel from a specified file.
     *
     * @param filePath the path to the file.
     * @return the file channel.
     * @throws IOException during I/O error.
     * @throws URISyntaxException during failure to validate uri syntax.
     */
    public static ByteChannel openForReading(String filePath) throws IOException, URISyntaxException {
        Set<OpenOption> opts = new HashSet<>();
        opts.add(StandardOpenOption.READ);
        URL fileResource = TestUtil.class.getClassLoader().getResource(filePath);
        ByteChannel channel = null;
        if (null != fileResource) {
            Path path = Paths.get(fileResource.toURI());
            channel = Files.newByteChannel(path, opts);
        }
        return channel;
    }

    /**
     * Opens a file for writing.
     *
     * @param filePath the path the file should be opened for writing.
     * @return the writable channel.
     * @throws IOException during I/O error.
     */
    public static ByteChannel openForWriting(String filePath) throws IOException {
        Set<OpenOption> opts = new HashSet<>();
        opts.add(StandardOpenOption.CREATE);
        opts.add(StandardOpenOption.WRITE);
        Path path = Paths.get(filePath);
        return Files.newByteChannel(path, opts);
    }
}
