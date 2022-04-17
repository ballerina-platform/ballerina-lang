module io.ballerina.semver.checker.core {
    requires io.ballerina.parser;
    requires io.ballerina.lang;
    requires io.ballerina.central.client;
    requires slf4j.api;

    exports org.ballerinalang.semver.checker to io.ballerina.semver.checker.cli;
    exports org.ballerinalang.semver.checker.exception to io.ballerina.semver.checker.cli;
    exports org.ballerinalang.semver.checker.diff to io.ballerina.semver.checker.cli;
}
