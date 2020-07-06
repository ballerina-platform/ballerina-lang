module io.ballerina.crypto {
    requires io.ballerina.jvm;
    requires io.ballerina.time;
    requires org.apache.commons.codec;
    exports org.ballerinalang.stdlib.crypto;
    exports org.ballerinalang.stdlib.crypto.nativeimpl;
}