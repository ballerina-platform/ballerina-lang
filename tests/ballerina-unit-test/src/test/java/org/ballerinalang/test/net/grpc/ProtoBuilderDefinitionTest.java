/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.test.net.grpc;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.net.grpc.exception.GrpcServerException;
import org.ballerinalang.net.grpc.proto.ServiceProtoConstants;
import org.ballerinalang.net.grpc.proto.definition.EmptyMessage;
import org.ballerinalang.net.grpc.proto.definition.EnumField;
import org.ballerinalang.net.grpc.proto.definition.Field;
import org.ballerinalang.net.grpc.proto.definition.File;
import org.ballerinalang.net.grpc.proto.definition.Message;
import org.ballerinalang.net.grpc.proto.definition.MessageKind;
import org.ballerinalang.net.grpc.proto.definition.Method;
import org.ballerinalang.net.grpc.proto.definition.Service;
import org.ballerinalang.net.grpc.proto.definition.StandardDescriptorBuilder;
import org.ballerinalang.net.grpc.proto.definition.StructMessage;
import org.ballerinalang.net.grpc.proto.definition.UserDefinedEnumMessage;
import org.ballerinalang.net.grpc.proto.definition.UserDefinedMessage;
import org.ballerinalang.net.grpc.proto.definition.WrapperMessage;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import static org.ballerinalang.net.grpc.GrpcConstants.WRAPPER_STRING_MESSAGE;

/**
 * Test class for Proto builder definitions.
 *
 * @since 0.982.0
 */
public class ProtoBuilderDefinitionTest {

    @Test(description = "Test case for empty message type definition")
    public void testEmptyMessageType() throws GrpcServerException {
        EmptyMessage emptyMessage = (EmptyMessage) EmptyMessage.newBuilder().build();
        Assert.assertEquals(emptyMessage.getDependency(), "google/protobuf/empty.proto");
        Assert.assertEquals(emptyMessage.getMessageKind(), MessageKind.EMPTY);
        DescriptorProtos.DescriptorProto messageDescriptor = emptyMessage.getDescriptorProto();
        Assert.assertEquals(messageDescriptor.getName(), "Empty");
    }

    @Test(description = "Test case for enum message type definition")
    public void testEnumMessageType() throws GrpcServerException {
        // Normal usecase
        UserDefinedEnumMessage.Builder builder = UserDefinedEnumMessage.newBuilder("Gender");
        EnumField field1 = EnumField.newBuilder().setName("Male").setIndex(1).build();
        EnumField field2 = EnumField.newBuilder().setName("Female").setIndex(2).build();
        builder.addFieldDefinition(field1);
        builder.addFieldDefinition(field2);
        UserDefinedEnumMessage message = builder.build();
        Assert.assertEquals(message.getMessageKind(), MessageKind.USER_DEFINED);
        Assert.assertEquals(message.getMessageDefinition(),
                "enum Gender {" + ServiceProtoConstants.NEW_LINE_CHARACTER
                        + "\tMale = 1;" + ServiceProtoConstants.NEW_LINE_CHARACTER
                        + "\tFemale = 2;" + ServiceProtoConstants.NEW_LINE_CHARACTER
                        + "}" + ServiceProtoConstants.NEW_LINE_CHARACTER);
        Assert.assertEquals(message.getDescriptorProto().getName(), "Gender");

        // when we pass null value for the message.
        try {
            UserDefinedEnumMessage.newBuilder(null);
            Assert.fail("Cannot create enum message without message type name");
        } catch (GrpcServerException e) {
            Assert.assertEquals(e.getMessage(), "Error while initializing the builder, message type cannot be null");
        }

        UserDefinedEnumMessage pkgMessage = UserDefinedEnumMessage.newBuilder("service:gender").build();
        Assert.assertEquals(pkgMessage.getDescriptorProto().getName(), "service.gender");
    }

    @Test(description = "Test case for message field definition")
    public void testMessageField() throws GrpcServerException {
        BTypeSymbol intSymbol = new BTypeSymbol(SymTag.TYPE, Flags.PUBLIC, new Name("int"),
                new PackageID("ballerina/builtin"), null, new BPackageSymbol(new PackageID("ballerina/builtin"),
                null));
        BType intType = new BType(TypeTags.INT, intSymbol);
        Field fieldDef = Field.newBuilder("age").setLabel("required").setIndex(1).setType(intType).setDefaultValue
                ("30").build();
        Assert.assertNotNull(fieldDef.getFieldDescriptorProto());
        Assert.assertEquals(fieldDef.getFieldDescriptorProto().toString(),
                "name: \"age\"\nnumber: 1\nlabel: LABEL_REQUIRED\ntype: TYPE_INT64\ndefault_value: \"30\"\n");
        Assert.assertEquals(fieldDef.getFieldDefinition(), "required int64 age = 1;" + ServiceProtoConstants
                .NEW_LINE_CHARACTER);

        //with null value to field label
        fieldDef = Field.newBuilder("age").setIndex(1).setType(intType).setLabel(null).setDefaultValue
                ("30").build();
        Assert.assertNotNull(fieldDef.getFieldDescriptorProto());
        Assert.assertEquals(fieldDef.getFieldDescriptorProto().toString(),
                "name: \"age\"\nnumber: 1\ntype: TYPE_INT64\ndefault_value: \"30\"\n");
        Assert.assertEquals(fieldDef.getFieldDefinition(), "int64 age = 1;" + ServiceProtoConstants
                .NEW_LINE_CHARACTER);

        //with invalid field label
        try {
            Field.newBuilder("age").setIndex(1).setType(intType).setLabel("dump").setDefaultValue
                    ("30").build();
            Assert.fail("Should not create message field with invalid field.");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals(e.getMessage(), "Illegal label: dump");
        }

        //record field type.
        BTypeSymbol recordSymbol = new BTypeSymbol(SymTag.RECORD, Flags.PUBLIC, new Name("A"),
                null, null, null);
        BType recordType = new BRecordType(recordSymbol);
        fieldDef = Field.newBuilder("age").setIndex(1).setType(recordType).setLabel(null).build();
        Assert.assertNotNull(fieldDef.getFieldDescriptorProto());
        Assert.assertEquals(fieldDef.getFieldDescriptorProto().toString(),
                "name: \"age\"\nnumber: 1\ntype_name: \"A\"\n");
        Assert.assertEquals(fieldDef.getFieldDefinition(), "A age = 1;" + ServiceProtoConstants.NEW_LINE_CHARACTER);

        //pass different type.
        BNilType nilType = new BNilType();
        try {
            Field.newBuilder("age").setIndex(1).setType(nilType).setLabel(null).build();
            Assert.fail("Should not create message field with unsupported field types");
        } catch (GrpcServerException e) {
            Assert.assertEquals(e.getMessage(), "Unsupported field type, field type without symbol is not supported.");
        }
    }

    @Test(description = "Test case for service method definition")
    public void testServiceMethod() {
        //unary method definition.
        Method unaryMethod = Method.newBuilder("hello").setInputType("google.protobuf.StringValue")
                .setOutputType("google.protobuf.StringValue").build();
        Assert.assertEquals(unaryMethod.getMethodDefinition(), "\trpc hello(google.protobuf.StringValue) returns " +
                "(google.protobuf.StringValue);" + ServiceProtoConstants.NEW_LINE_CHARACTER);
        Assert.assertNotNull(unaryMethod.getMethodDescriptor());
        Assert.assertEquals(unaryMethod.getMethodDescriptor().toString(),
                "name: \"hello\"\ninput_type: \"google.protobuf.StringValue\"\noutput_type: " +
                        "\"google.protobuf.StringValue\"\n");

        //server streaming definition.
        Method serverStreamingMethod = Method.newBuilder("hello").setInputType("google.protobuf.StringValue")
                .setServerStreaming(true).setOutputType("google.protobuf.StringValue").build();
        Assert.assertEquals(serverStreamingMethod.getMethodDefinition(),
                "\trpc hello(google.protobuf.StringValue) returns (stream google.protobuf.StringValue);"
                        + ServiceProtoConstants.NEW_LINE_CHARACTER);
        Assert.assertNotNull(serverStreamingMethod.getMethodDescriptor());
        Assert.assertEquals(serverStreamingMethod.getMethodDescriptor().toString(),
                "name: \"hello\"\ninput_type: \"google.protobuf.StringValue\"\noutput_type: " +
                        "\"google.protobuf.StringValue\"\nserver_streaming: true\n");

        //client streaming definition.
        Method clientStreamingMethod = Method.newBuilder("hello").setInputType("google.protobuf.StringValue")
                .setClientStreaming(true).setOutputType("google.protobuf.StringValue").build();
        Assert.assertEquals(clientStreamingMethod.getMethodDefinition(),
                "\trpc hello(stream google.protobuf.StringValue) returns (google.protobuf.StringValue);"
                        + ServiceProtoConstants.NEW_LINE_CHARACTER);
        Assert.assertNotNull(clientStreamingMethod.getMethodDescriptor());
        Assert.assertEquals(clientStreamingMethod.getMethodDescriptor().toString(),
                "name: \"hello\"\ninput_type: \"google.protobuf.StringValue\"\noutput_type: " +
                        "\"google.protobuf.StringValue\"\nclient_streaming: true\n");

        //bidirectional streaming definition.
        Method bidiStreamingMethod = Method.newBuilder("hello").setInputType("google.protobuf.StringValue")
                .setClientStreaming(true).setServerStreaming(true).setOutputType("google.protobuf.StringValue").build();
        Assert.assertEquals(bidiStreamingMethod.getMethodDefinition(), "\trpc hello(stream google.protobuf" +
                ".StringValue) returns (stream google.protobuf.StringValue);" + ServiceProtoConstants
                .NEW_LINE_CHARACTER);
        Assert.assertNotNull(bidiStreamingMethod.getMethodDescriptor());
        Assert.assertEquals(bidiStreamingMethod.getMethodDescriptor().toString(),
                "name: \"hello\"\ninput_type: \"google.protobuf.StringValue\"\noutput_type: " +
                        "\"google.protobuf.StringValue\"\nclient_streaming: true\nserver_streaming: true\n");
    }

    @Test(description = "Test case for service definition")
    public void testServiceDefinition() {
        Method method1 = Method.newBuilder("method1").setInputType("google.protobuf.StringValue")
                .setOutputType("google.protobuf.StringValue").build();
        Method method2 = Method.newBuilder("method2").setInputType("google.protobuf.StringValue")
                .setClientStreaming(true).setOutputType("google.protobuf.StringValue").build();
        Service service = Service.newBuilder("HelloService").addMethod(method1).addMethod(method2).build();
        Assert.assertEquals(service.getServiceDefinition(),
                "service HelloService {" + ServiceProtoConstants.NEW_LINE_CHARACTER
                        + "\t\trpc method1(google.protobuf.StringValue) returns (google.protobuf.StringValue);"
                        + ServiceProtoConstants.NEW_LINE_CHARACTER
                        + "\t\trpc method2(stream google.protobuf.StringValue) returns (google.protobuf.StringValue);"
                        + ServiceProtoConstants.NEW_LINE_CHARACTER
                        + "}" + ServiceProtoConstants.NEW_LINE_CHARACTER);
        Assert.assertNotNull(service.getServiceDescriptor());
    }

    @Test(description = "Test case for standard descriptor provider")
    public void testStandardDescriptor() {
        String[] descriptorArray = {"google/protobuf/empty.proto", "google/protobuf/wrappers.proto"};
        Descriptors.FileDescriptor[] fileDescriptors = StandardDescriptorBuilder.getFileDescriptors(descriptorArray);
        Assert.assertEquals(fileDescriptors.length, 2);
        Assert.assertEquals(fileDescriptors[0].getFullName(), "google/protobuf/empty.proto");
        Assert.assertEquals(fileDescriptors[1].getFullName(), "google/protobuf/wrappers.proto");
    }

    @Test(description = "Test case for wrapper message definition")
    public void testWrapperMessageDefinition() throws GrpcServerException {
        //with null message name.
        try {
            WrapperMessage.newBuilder(null).build();
            Assert.fail("Should not allow to create new builder with null message name.");
        } catch (GrpcServerException ex) {
            Assert.assertEquals(ex.getMessage(), "Error while initializing the builder, message name cannot be null");
        }
        //with correct message name.
        Message message = WrapperMessage.newBuilder(WRAPPER_STRING_MESSAGE).build();
        Assert.assertNull(message.getNestedMessageList());
        Assert.assertNull(message.getNestedEnumList());
        Assert.assertEquals(message.getDependency(), "google/protobuf/wrappers.proto");
        Assert.assertNull(message.getMessageDefinition());
        Assert.assertEquals(message.getSimpleName(), "StringValue");
        Assert.assertEquals(message.getCanonicalName(), "google.protobuf.StringValue");
        Assert.assertNotNull(message.getDescriptorProto());
        Assert.assertEquals(message.getMessageKind(), MessageKind.WRAPPER);
        //with incorrect message name.
        try {
            WrapperMessage.newBuilder("WrongValue").build();
            Assert.fail("Should not allow to create new builder with null message name.");
        } catch (GrpcServerException ex) {
            Assert.assertEquals(ex.getMessage(), "Error while deriving message definition. Cannot find descriptor " +
                    "for the message name: WrongValue");
        }
    }

    @Test(description = "Test case for struct message definition")
    public void testStructMessageDefinition() throws GrpcServerException {
        //with null message name.
        try {
            StructMessage.newBuilder(null).build();
            Assert.fail("Should not allow to create new builder with null message name.");
        } catch (GrpcServerException ex) {
            Assert.assertEquals(ex.getMessage(), "Error while initializing the builder, message name cannot be null");
        }
        //with correct message name.
        Message message = StructMessage.newBuilder("ListValue").build();
        Assert.assertNull(message.getNestedMessageList());
        Assert.assertNull(message.getNestedEnumList());
        Assert.assertEquals(message.getDependency(), "google/protobuf/struct.proto");
        Assert.assertNull(message.getMessageDefinition());
        Assert.assertEquals(message.getSimpleName(), "ListValue");
        Assert.assertEquals(message.getCanonicalName(), "google.protobuf.ListValue");
        Assert.assertNotNull(message.getDescriptorProto());
        Assert.assertEquals(message.getMessageKind(), MessageKind.STRUCT);
        //with incorrect message name.
        try {
            StructMessage.newBuilder("WrongValue").build();
            Assert.fail("Should not allow to create new builder with null message name.");
        } catch (GrpcServerException ex) {
            Assert.assertEquals(ex.getMessage(), "Error while deriving message definition. Cannot find descriptor " +
                    "for the message name: WrongValue");
        }
    }

    @Test(description = "Test case for user defined message definition")
    public void testUserDefinedMessageDefinition() throws GrpcServerException {
        // when we pass null value for the message.
        try {
            UserDefinedMessage.newBuilder(null);
            Assert.fail("Cannot create message without message type name");
        } catch (GrpcServerException e) {
            Assert.assertEquals(e.getMessage(), "Error while initializing the builder, message type cannot be null");
        }

        UserDefinedMessage pkgMessage = (UserDefinedMessage) UserDefinedMessage.newBuilder("service:person").build();
        Assert.assertEquals(pkgMessage.getDescriptorProto().getName(), "service.person");

        BTypeSymbol intSymbol = new BTypeSymbol(SymTag.TYPE, Flags.PUBLIC, new Name("int"),
                new PackageID("ballerina/builtin"), null, new BPackageSymbol(new PackageID("ballerina/builtin"),
                null));
        BType intType = new BType(TypeTags.INT, intSymbol);
        Field field1 = Field.newBuilder("age").setLabel("required").setIndex(1).setType(intType).setDefaultValue
                ("30").build();
        BTypeSymbol stringSymbol = new BTypeSymbol(SymTag.TYPE, Flags.PUBLIC, new Name("string"),
                new PackageID("ballerina/builtin"), null, new BPackageSymbol(new PackageID("ballerina/builtin"),
                null));
        BType stringType = new BType(TypeTags.STRING, stringSymbol);
        Field field2 = Field.newBuilder("name").setLabel("required").setIndex(2).setType(stringType).setDefaultValue
                ("Sam").build();

        UserDefinedEnumMessage enumMessage = UserDefinedEnumMessage.newBuilder("Gender").build();

        Message message = UserDefinedMessage.newBuilder("Person").addMessageDefinition(pkgMessage)
                .addMessageDefinition
                (enumMessage).addFieldDefinition(field1).addFieldDefinition(field2).build();
        Assert.assertEquals(message.getMessageKind(), MessageKind.USER_DEFINED);
        Assert.assertEquals(message.getMessageDefinition(),
                "message Person {" + ServiceProtoConstants.NEW_LINE_CHARACTER +
                        "\trequired int64 age = 1;" + ServiceProtoConstants.NEW_LINE_CHARACTER +
                        "\trequired string name = 2;" + ServiceProtoConstants.NEW_LINE_CHARACTER +
                        "}" + ServiceProtoConstants.NEW_LINE_CHARACTER);
        Assert.assertNotNull(message.getDescriptorProto());
        Assert.assertEquals(message.getNestedMessageList().size(), 1);
        Assert.assertEquals(message.getNestedEnumList().size(), 1);
        Assert.assertEquals(message.getSimpleName(), "Person");
        Assert.assertNull(message.getDependency());
    }

    @Test(description = "Test case for file definition")
    public void testFileDefinition() throws GrpcServerException {
        EnumField field1 = EnumField.newBuilder().setName("Male").setIndex(1).build();
        UserDefinedEnumMessage enumMessage = UserDefinedEnumMessage.newBuilder("Gender")
                .addFieldDefinition(field1).build();
        Message customMessage = UserDefinedMessage.newBuilder("Person").build();
        Service service = Service.newBuilder("HelloService").build();


        File.Builder fileBuilder = File.newBuilder("HelloWorld").setEnum(enumMessage)
                .setDependency("google/protobuf/wrappers.proto").setMessage(customMessage).setSyntax("proto3")
                .setPackage("service").setService(service);
        Assert.assertTrue(fileBuilder.isEnumExists("Gender"));
        Assert.assertFalse(fileBuilder.isEnumExists("Invalid"));
        Assert.assertEquals(fileBuilder.getRegisteredDependencies().size(), 1);
        Assert.assertEquals(fileBuilder.getRegisteredMessages().size(), 1);
        Assert.assertEquals(fileBuilder.getRegisteredEnums().size(), 1);

        File file = fileBuilder.build();
        Assert.assertEquals(file.getFileDefinition(),
                "syntax = \"proto3\";" + ServiceProtoConstants.NEW_LINE_CHARACTER +
                        "package service;" + ServiceProtoConstants.NEW_LINE_CHARACTER +
                        "import \"google/protobuf/wrappers.proto\";" + ServiceProtoConstants.NEW_LINE_CHARACTER +
                        "service HelloService {" + ServiceProtoConstants.NEW_LINE_CHARACTER +
                        "}" + ServiceProtoConstants.NEW_LINE_CHARACTER +
                        "message Person {" + ServiceProtoConstants.NEW_LINE_CHARACTER +
                        "}" + ServiceProtoConstants.NEW_LINE_CHARACTER +
                        "enum Gender {" + ServiceProtoConstants.NEW_LINE_CHARACTER +
                        "\tMale = 1;" + ServiceProtoConstants.NEW_LINE_CHARACTER +
                        "}" + ServiceProtoConstants.NEW_LINE_CHARACTER);
        Assert.assertNotNull(file.getFileDescriptorProto());
        Assert.assertNotNull(file.getFileDescriptor());
    }

    @Test(description = "Test case for message kind enum")
    public void testMessageKindEnum() {
        Assert.assertEquals(MessageKind.valueFrom(0), MessageKind.USER_DEFINED);
        Assert.assertNull(MessageKind.valueFrom(-1));
        Assert.assertEquals(MessageKind.STRUCT.value(), 3);
        Assert.assertEquals(MessageKind.STRUCT.toString(), "3");
    }
}
