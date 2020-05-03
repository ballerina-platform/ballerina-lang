module ballerina.lang {
    requires java.compiler;
    requires org.apache.commons.lang3;
    requires toml4j;
    requires gson;
    requires java.xml;
    requires org.objectweb.asm;
    requires ballerina.runtime;
    requires io.netty.buffer;
    requires antlr4.runtime;
    requires ballerina.cli.module;
    exports org.wso2.ballerinalang.compiler.util;
    exports org.ballerinalang.toml.model;
    exports org.wso2.ballerinalang.util;
    exports org.ballerinalang.model.types;
    exports org.wso2.ballerinalang.compiler.tree;
    exports org.ballerinalang.compiler;
}