module io.ballerina.observe.metrics.extension.defaultimpl {
    requires io.ballerina.runtime;
    requires HdrHistogram;
    requires static org.jetbrains.annotations;

    exports org.ballerinalang.observe.metrics.extension.defaultimpl;
}
