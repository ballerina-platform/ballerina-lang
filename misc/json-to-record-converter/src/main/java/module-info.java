module io.ballerina.Misc.jsonToRecordConverter {
    requires com.google.gson;
    requires io.ballerina.parser;
    requires io.ballerina.formatter.core;
    requires io.ballerina.tools.api;
    requires org.apache.commons.lang3;
    requires javatuples;

    exports io.ballerina.converters;
}