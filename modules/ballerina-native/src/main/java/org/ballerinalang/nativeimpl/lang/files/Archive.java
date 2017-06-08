package org.ballerinalang.nativeimpl.lang.files;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.VFS;
import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.model.values.BFile;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Archive File
 */
@BallerinaFunction(
        packageName = "ballerina.lang.files",
        functionName = "archive",
        args = { @Argument(name = "source", type = TypeEnum.FILE),
                 @Argument(name = "destination", type = TypeEnum.FILE) },
        isPublic = true)
        @BallerinaAnnotation(annotationName = "Description", attributes = { @Attribute(name = "value",
        value = "This function archives a file to a given location") })
        @BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "source",
                value = "File/Directory that should be archived") })
        @BallerinaAnnotation(annotationName = "Param", attributes = { @Attribute(name = "destination",
                value = "The location where the Archive should be written") })
public class Archive extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(Archive.class);
    private static final byte[] bytes = new byte[4096];

    @Override public BValue[] execute(Context context) {

        BFile target = (BFile) getArgument(context, 0);
        BFile destination = (BFile) getArgument(context, 1);
        try {
            FileSystemManager fsManager = VFS.getManager();
            FileObject targetObj = fsManager.resolveFile(target.stringValue());
            if (targetObj.exists()) {
                FileObject destinationObj = fsManager.resolveFile(destination.stringValue());
                fileCompress(targetObj, destinationObj);
            }
        } catch (FileSystemException e) {
            throw new BallerinaException("Error while resolving file", e);
        }
        return VOID_RETURN;
    }

    private boolean fileCompress(FileObject fileObj, FileObject destObj) {
        boolean resultStatus;
        try {
            if (fileObj.exists()) {
                if (fileObj.getType() == FileType.FOLDER) {
                    List<FileObject> fileList = new ArrayList<FileObject>();
                    getAllFiles(fileObj, fileList);
                    writeZipFiles(fileObj, destObj, fileList);
                } else {
                    ZipOutputStream outputStream = null;
                    InputStream fileIn = null;
                    try {
                        outputStream = new ZipOutputStream(destObj.getContent().getOutputStream());
                        fileIn = fileObj.getContent().getInputStream();
                        ZipEntry zipEntry = new ZipEntry(fileObj.getName().getBaseName());
                        outputStream.putNextEntry(zipEntry);
                        int length;
                        while ((length = fileIn.read(bytes)) != -1) {
                            outputStream.write(bytes, 0, length);
                        }
                    } catch (Exception e) {
                        throw new BallerinaException("Unable to compress a file." + e.getMessage());
                    } finally {
                        try {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        } catch (IOException e) {
                            log.error("Error while closing ZipOutputStream: " + e.getMessage(), e);
                        }
                        try {
                            if (fileIn != null) {
                                fileIn.close();
                            }
                        } catch (IOException e) {
                            log.error("Error while closing InputStream: " + e.getMessage(), e);
                        }
                        IOUtils.closeQuietly(outputStream);
                    }
                }
                resultStatus = true;

                if (log.isDebugEnabled()) {
                    log.debug("File archiving completed." + destObj.getURL());
                }
            } else {
                log.error("The File location does not exist.");
                resultStatus = false;
            }
        } catch (IOException e) {
            throw new BallerinaException("Unable to process the zip file", e);
        }
        return resultStatus;
    }

    private void getAllFiles(FileObject dir, List<FileObject> fileList) {
        try {
            FileObject[] children = dir.getChildren();
            for (FileObject child : children) {
                fileList.add(child);
                if (child.getType() == FileType.FOLDER) {
                    getAllFiles(child, fileList);
                }
            }
        } catch (IOException e) {
            throw new BallerinaException("Unable to get all files", e);
        }
    }

    /**
     * @param fileObj        source fileObject
     * @param directoryToZip destination fileObject
     * @param fileList       list of files to be compressed
     * @throws IOException
     */
    private void writeZipFiles(FileObject fileObj, FileObject directoryToZip, List<FileObject> fileList) {
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(directoryToZip.getContent().getOutputStream());
            for (FileObject file : fileList) {
                if (file.getType() == FileType.FILE) {
                    addToZip(fileObj, file, zos);
                }
            }
        } catch (IOException e) {
            throw new BallerinaException("Error occur in writing files", e);
        } finally {
            if (zos != null) {
                IOUtils.closeQuietly(zos);
            }
        }
    }

    /**
     * @param fileObject   Source fileObject
     * @param file         The file inside source folder
     * @param outputStream ZipOutputStream
     */
    private void addToZip(FileObject fileObject, FileObject file, ZipOutputStream outputStream) {
        InputStream fin = null;
        try {
            fin = file.getContent().getInputStream();
            String name = file.getName().toString();
            String entry = name.substring(fileObject.getName().toString().length() + 1, name.length());
            ZipEntry zipEntry = new ZipEntry(entry);
            outputStream.putNextEntry(zipEntry);
            int length;
            while ((length = fin.read(bytes)) != -1) {
                outputStream.write(bytes, 0, length);
            }
        } catch (IOException e) {
            throw new BallerinaException ("Unable to add a file into the zip file directory." + e.getMessage());
        } finally {
            try {
                outputStream.closeEntry();
                if (fin != null) {
                    fin.close();
                }
            } catch (IOException e) {
                log.error("Error while closing InputStream: " + e.getMessage(), e);
            }
        }
    }
}
