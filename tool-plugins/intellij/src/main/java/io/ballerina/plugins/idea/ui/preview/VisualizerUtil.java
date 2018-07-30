package io.ballerina.plugins.idea.ui.preview;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class VisualizerUtil {
    private static final Logger LOG = Logger.getInstance(VisualizerUtil.class);

    public static String md5(String buffer, @NonNls String key) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Cannot find 'md5' algorithm; ", e);
        }

        Objects.requireNonNull(md5).update(buffer.getBytes(StandardCharsets.UTF_8));
        byte[] code = md5.digest(key.getBytes(StandardCharsets.UTF_8));
        BigInteger bi = new BigInteger(code).abs();
        return bi.abs().toString(16);
    }

    @NotNull
    public static String generateMarkdownHtml(@NotNull VirtualFile file, @NotNull String text) {
        final VirtualFile parent = file.getParent();
        final URI baseUri = parent != null ? new File(parent.getPath()).toURI() : null;

        String html = "<html> <header><title>This is title</title></header> <body> Hello world </body></html>";

        // MarkdownCodeFencePluginCache.getInstance().registerCacheProvider(cacheCollector);

        return html;
    }
}