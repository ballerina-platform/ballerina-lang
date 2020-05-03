module ballerina.config {
    requires antlr4.runtime;
    requires ballerina.toml.parser;
    requires org.apache.commons.lang3;
    exports org.ballerinalang.config;
    exports org.ballerinalang.config.cipher;
}