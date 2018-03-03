package org.ballerinalang.net.grpc.builder;
//
//import com.google.protobuf.DescriptorProtos;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//
///**.
// * .
// */
//public class Test {
//
//    public static void main(String[] args) throws IOException {
//       // Path path = Paths.get("/", "home", "yasara", "Desktop",
//  //              "mymessages.desc");
//        //byte[] descriptor = Files.readAllBytes(path);
//       // DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.parseFrom(descriptor);
////        DescriptorProtos.FileDescriptorProto proto = DescriptorProtos.FileDescriptorProto.parseFrom(descriptor);
//      //  File initialFile = new File("mymessages.desc");
//        File initialFile = new File("/home/yasara/Desktop/tt"+"/t3.desc");
//        InputStream targetStream = new FileInputStream(initialFile);
//        DescriptorProtos.FileDescriptorSet set = DescriptorProtos.FileDescriptorSet
//                .parseFrom(targetStream);
//        byte[] bytes = set.getFile(0).toByteArray();
//        Path path = Paths.get("/home/yasara/Desktop");
//        List<byte[]> list = new ArrayList<>();
//        list.add(com.google.protobuf.WrappersProto.getDescriptor().toProto().toByteArray());
//        new BallerinaFile(bytes,list, path).build();
//    }
//}
