package org.ballerinalang.net.grpc.builder;

import com.google.protobuf.DescriptorProtos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**.
 * .
 */
public class Test {
    
    public static void main(String[] args) throws IOException {
        Path path = Paths.get("/", "home", "yasara", "Desktop", "demo2", "ballerina-server", "sample",
                "helloWorld.desc");
        byte[] descriptor = Files.readAllBytes(path);
        DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.parseFrom(descriptor);
//        DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.parseFrom(descriptor);
//        InputStream targetStream = new FileInputStream(initialFile);
//        DescriptorProtos.FileDescriptorSet set = DescriptorProtos.FileDescriptorSet
//                .parseFrom(targetStream);
//        byte[] bytes = set.getFile(0).toByteArray();
        new BalGenerate().generate(descriptor, new byte[][] {{}}, "/home/yasara/Desktop/ppp.bal");
    }
}
