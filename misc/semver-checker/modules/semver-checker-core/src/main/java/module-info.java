module io.ballerina.semver.checker.core {
    requires io.ballerina.parser;
    requires io.ballerina.lang;
    requires io.ballerina.central.client;
    requires io.ballerina.tools.api;
    requires com.google.gson;
    requires slf4j.api;

    exports io.ballerina.semver.checker to io.ballerina.semver.checker.cli;
    exports io.ballerina.semver.checker.exception to io.ballerina.semver.checker.cli;
    exports io.ballerina.semver.checker.diff to io.ballerina.semver.checker.cli;
}
