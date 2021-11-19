module io.ballerina.identifierutil {

    requires org.apache.commons.text;

    exports io.ballerina.identifierutil to io.ballerina.lang, io.ballerina.runtime, io.ballerina.shell,
            io.ballerina.testerina.runtime, io.ballerina.lang.runtime, io.ballerina.lang.error, io.ballerina.cli,
            ballerina.debug.adapter.core;
}
