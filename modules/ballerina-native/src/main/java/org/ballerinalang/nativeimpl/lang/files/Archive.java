package org.ballerinalang.nativeimpl.lang.files;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Archive Function
 */
@BallerinaFunction(
        packageName = "ballerina.lang.files",
        functionName = "archive",
        args = {@Argument(name = "source", type = TypeEnum.STRUCT, structType = "File",
                structPackage = "ballerina.lang.files"),
                @Argument(name = "destination", type = TypeEnum.STRUCT, structType = "File",
                        structPackage = "ballerina.lang.files")},
        isPublic = true
)
@BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function archives a file to a given location") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "source",
        value = "File/Directory that should be archived") })
@BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "destination",
        value = "The location where the Archived file should be written") })
public class Archive extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(Archive.class);

    private List<String> fileList;
    private String source;

    @Override public BValue[] execute(Context context) {

        BStruct source = (BStruct) getArgument(context, 0);
        BStruct destination = (BStruct) getArgument(context, 1);
        File sourceFile = new File(source.getValue(0).stringValue());
        this.source = sourceFile.getAbsolutePath();
        File destinationFile = new File(destination.getValue(0).stringValue());
        File parent = destinationFile.getParentFile();
        if (parent != null) {
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    throw new BallerinaException("Error in writing file");
                }
            }
        }
        fileList = new ArrayList<>();

        generateFileList(sourceFile);
        compress(destinationFile);
        return VOID_RETURN;
    }

    /**
     * Compress File
     *
     * @param zipFile output ZIP file location
     */
    public void compress(File zipFile) {

        byte[] buffer = new byte[1024];
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        FileInputStream in = null;
        try {

            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);

            log.debug("Output to Zip : " + zipFile);

            for (String file : this.fileList) {

                log.debug("File Added : " + file);
                ZipEntry ze = new ZipEntry(file);
                zos.putNextEntry(ze);

                in = new FileInputStream(source + File.separator + file);

                int len;
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }

                in.close();
            }

        } catch (IOException e) {
            throw new BallerinaException("Error while writing file", e);
        } finally {
            closeQuietly(in);
            closeQuietly(zos);
            closeQuietly(fos);
        }
    }

    /**
     * Traverse a directory and get all files,
     * and add the file into fileList
     *
     * @param node file or directory
     */
    public void generateFileList(File node) {

        if (node.isFile()) {
            fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            if (subNote != null) {
                for (String filename : subNote) {
                    generateFileList(new File(node, filename));
                }
            } else {
                throw new BallerinaException("Error while reading file");
            }
        }

    }

    /**
     * Format the file path for zip
     *
     * @param file file path
     * @return Formatted file path
     */
    private String generateZipEntry(String file) {
        return file.substring(source.length() + 1, file.length());
    }

    private void closeQuietly(Closeable resource) {
        try {
            if (resource != null) {
                resource.close();
            }
        } catch (Exception e) {
            throw new BallerinaException("Exception during Resource.close()", e);
        }
    }
}
