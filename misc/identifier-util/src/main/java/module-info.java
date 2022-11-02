module io.ballerina.identifier {

    requires org.apache.commons.text;

    exports io.ballerina.identifier to io.ballerina.lang, io.ballerina.runtime, io.ballerina.shell,
            io.ballerina.testerina.runtime, io.ballerina.lang.runtime, io.ballerina.lang.error,
            ballerina.debug.adapter.core, io.ballerina.jsonmapper, io.ballerina.cli;
}
