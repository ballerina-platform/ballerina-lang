package org.wso2.ballerinalang.grpc.tool.cmd;

import com.google.protobuf.DescriptorProtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**.
 *.
 */
public class DescriptorsGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(DescriptorsGenerator.class);


    public static List<byte[]> getDependentByteArray(String exePath, String rootPath, String rootProto,
                                                     String dependencyDescLocation, ClassLoader classLoader) {
        List<byte[]> newList = new ArrayList<>();
        return dependentDescriptorGenerator(rootPath, rootProto, newList, dependencyDescLocation, exePath, classLoader);

    }
    private static List<byte[]> dependentDescriptorGenerator(String parentDescPath, String parentProtoPath,
                                                             List<byte[]> list, String dependencyDescLocation,
                                                             String exePath, ClassLoader classLoader) {
        try {
            File initialFile = new File(parentDescPath);
            InputStream targetStream = new FileInputStream(initialFile);
            DescriptorProtos.FileDescriptorSet descSet = DescriptorProtos.FileDescriptorSet.parseFrom(targetStream);
            for (int i = 0; i < descSet.getFile(0).getDependencyCount(); i++) {
                String depPath = descSet.getFile(0).getDependency(i);
                String descName = depPath.split("/")[depPath.split("/").length - 1]
                        .replace(".proto", "") + ".desc";
                String path = dependencyDescLocation + descName;
                File targetFile = new File(path);
                File parent = targetFile.getParentFile();
                if (!parent.exists() && !parent.mkdirs()) {
                    throw new IllegalStateException("Couldn't create dir: " + parent);
                }
                byte dataBytes[] = new byte[0];
                Path file = Paths.get(path);
                Files.write(file, dataBytes);
                String protoPath;
                if (!depPath.contains("google/protobuf")) {
                    protoPath = new File(new File(parentProtoPath.substring(0, parentProtoPath
                            .lastIndexOf('/'))).toURI().getPath() + descSet.getFile(0)
                            .getDependency(i)).toURI().getPath();
                } else  {
                    //Get file from resources folder
                    InputStream initialStream = classLoader.getResourceAsStream(depPath);
                    byte[] buffer = new byte[initialStream.available()];
                    initialStream.read(buffer);
                    File dd = new File("desc_gen/" + depPath);
                    File pa = dd.getParentFile();
                    if (!pa.exists() && !pa.mkdirs()) {
                        throw new IllegalStateException("Couldn't create dir: " + parent);
                    }
                    OutputStream outStream = new FileOutputStream(dd);
                    outStream.write(buffer);
                    outStream.close();
                    protoPath = dd.getAbsolutePath();
                    //pa.delete();
                }
                String command = exePath + " \\\n" +
                        "--proto_path=" + protoPath.substring(0, protoPath.lastIndexOf('/')) + " \\\n" +
                        protoPath + " \\\n" +
                        "--descriptor_set_out=" + path;
                boolean isWindows = System.getProperty("os.name")
                        .toLowerCase().startsWith("windows");
                ProcessBuilder builder = new ProcessBuilder();
                if (isWindows) {
                    builder.command("cmd.exe", "/c", "dir");
                } else {
                    builder.command("sh", "-c", command);
                }
                builder.directory(new File(System.getProperty("user.home")));
                Process process = null;
                try {
                    process = builder.start();
                    new BufferedReader(new InputStreamReader(process.getInputStream())).lines()
                            .forEach(System.out::println);
                    File childFile = new File(path);
                    InputStream childStream = new FileInputStream(childFile);
                    DescriptorProtos.FileDescriptorSet childDescSet = DescriptorProtos.FileDescriptorSet
                            .parseFrom(childStream);
                    if (childDescSet.getFile(0).getDependencyCount() != 0) {
                        List<byte[]> newList = new ArrayList<>();
                        dependentDescriptorGenerator(path, protoPath, newList, dependencyDescLocation,
                                 exePath, classLoader);
                    } else {
                        initialFile = new File(path);
                        targetStream = new FileInputStream(initialFile);
                        DescriptorProtos.FileDescriptorSet set = DescriptorProtos.FileDescriptorSet
                                .parseFrom(targetStream);
                        byte[] tt = set.getFile(0).toByteArray();
                        list.add(tt);
                    }
                } catch (IOException e) {
                    LOG.error("Error in file reading in dependencies:  ", e);
                }
            }

        } catch (IOException e) {

        }
        return  list;
    }
    public static byte[] getRootByteArray(String exePath, String protoPath, String descriptorPath) {
        String command = exePath + " \\\n" +
                "--proto_path=" + protoPath.substring(0, protoPath.lastIndexOf('/')) + " \\\n" +
                protoPath + " \\\n" +
                "--descriptor_set_out=" + descriptorPath;
        boolean isWindows = System.getProperty("os.name")
                .toLowerCase().startsWith("windows");
        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd.exe", "/c", "dir");
        } else {
            builder.command("sh", "-c", command);
        }
        builder.directory(new File(System.getProperty("user.home")));
        Process process = null;
        try {
            process = builder.start();
            new BufferedReader(new InputStreamReader(process.getInputStream())).lines()
                    .forEach(System.out::println);
            process.waitFor();
            File initialFile = new File(descriptorPath);
            InputStream targetStream = new FileInputStream(initialFile);
            DescriptorProtos.FileDescriptorSet set = DescriptorProtos.FileDescriptorSet.parseFrom(targetStream);
            return set.getFile(0).toByteArray();
        } catch (IOException e) {
            LOG.error("Error in file reading: ", e);
        } catch (InterruptedException e) {
            LOG.error("Error in file processing: ", e);
        }
        return null;
    }
}
