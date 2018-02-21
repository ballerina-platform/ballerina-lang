package org.ballerinalang.net.grpc.builder;

import com.google.protobuf.DescriptorProtos;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Test {

    public static void main(String[] args) throws IOException {

        File initialFile = new File("/home/yasara/Desktop/helloworld-new-descriptor.desc");
        File initialFile2 = new File("/home/yasara/Desktop/dependencies/wrappers.desc");
        InputStream targetStream = null;
//        try {
        targetStream = new FileInputStream(initialFile);
        DescriptorProtos.FileDescriptorSet set = DescriptorProtos.FileDescriptorSet.parseFrom(targetStream);
        targetStream = new FileInputStream(initialFile2);
        DescriptorProtos.FileDescriptorSet set2 = DescriptorProtos.FileDescriptorSet.parseFrom(targetStream);
//            return Descriptors.FileDescriptor.buildFrom((DescriptorProtos.FileDescriptorProto)
//                    set.getFileOrBuilder(0), Objects.requireNonNull(getFileDescriptors()));
//           Descriptors.FileDescriptor fileDescriptor=  Descriptors.FileDescriptor.buildFrom((DescriptorProtos.FileDescriptorProto)
//                    set.getFileOrBuilder(0), new Descriptors.FileDescriptor[]{});
//           fileDescriptor.getFile().getServices().get(0).getMethods().get(0).getInputType().getName();
//        } catch (IOException | Descriptors.DescriptorValidationException e) {
//            throw new RuntimeException("Error : ", e);
//        }
//        InputStream targetStream = null;
//            targetStream = new FileInputStream(initialFile);
//        DescriptorProtos.FileDescriptorSet set = DescriptorProtos.FileDescriptorSet.parseFrom(targetStream);
//        Descriptors.FileDescriptor.buildFrom(set.getFileOrBuilder(0), new Descriptors.FileDescriptor[]{});
//        ServiceProto.getDescriptor().getFile().getServices().get(0).getMethods().get(methodId)
//                .getInputType().getName();

        byte[] bytes = set.getFile(0).toByteArray();
        byte[] bytes2 = set2.getFile(0).toByteArray();
        new BalGenerate().generate(bytes, new byte[][]{bytes2}, "/home/yasara/Desktop/yasara2.bal");
//        DescriptorProtos.FileDescriptorSet fileDescriptorSet;
//        byte[] bytes = WrappersProto.getDescriptor().getFile().toProto().toByteArray();
//        String tt = bytesToHex(bytes);
//        byte [] nn = hexStringToByteArray(tt);
//
//        InputStream targetStream = new ByteArrayInputStream(nn);
//        DescriptorProtos.FileDescriptorProto set = DescriptorProtos.FileDescriptorProto.parseFrom(targetStream);
//        DescriptorProtos.FileDescriptorProto proto;
//        try {
//            proto = DescriptorProtos.FileDescriptorProto.parseFrom(bytes, ExtensionRegistry.newInstance());
//            proto.getDependencyCount();
//        } catch (InvalidProtocolBufferException e) {
//            throw new IllegalArgumentException(
//                    "Failed to parse protocol buffer descriptor for generated code.", e);
//        }
    }
//    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
//    private static String bytesToHex(byte[] data) {
//        char[] hexChars = new char[data.length * 2];
//        for (int j = 0; j < data.length; j++) {
//            int v = data[j] & 0xFF;
//            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
//            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
//        }
//        return new String(hexChars);
//    }
//    private static byte[] hexStringToByteArray(String s) {
//        int len = s.length();
//        byte[] data = new byte[len / 2];
//        for (int i = 0; i < len; i += 2) {
//            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//                    + Character.digit(s.charAt(i+1), 16));
//        }
//        return data;
//    }
}
