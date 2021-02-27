module io.ballerina.mime {
    exports org.ballerinalang.mime.util;
    exports org.ballerinalang.mime.nativeimpl;
    requires io.ballerina.runtime;
    requires slf4j.api;
    requires io.ballerina.io;
    requires io.ballerina.lang;
    requires org.jvnet.mimepull;
    requires jakarta.activation;
}