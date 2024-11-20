package io.ballerina.runtime.observability.metrics;

import io.ballerina.runtime.api.Environment;
import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.observability.BallerinaObserver;
import io.ballerina.runtime.observability.ObserveUtils;
import io.ballerina.runtime.observability.ObserverContext;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.runtime.observability.ObservabilityConstants.PROPERTY_KEY_HTTP_STATUS_CODE;
import static io.ballerina.runtime.observability.ObservabilityConstants.TAG_KEY_HTTP_STATUS_CODE_GROUP;
import static io.ballerina.runtime.observability.ObservabilityConstants.STATUS_CODE_GROUP_SUFFIX;

public class BallerinaMetricsLogsObserver implements BallerinaObserver {
    private static final String ORG_NAME = "ballerinax";
    private static final String PROPERTY_START_TIME = "_observation_start_time_";
    private static final PrintStream consoleError = System.err;

    private static Environment environment;

    public BallerinaMetricsLogsObserver(Environment environment) {
        BallerinaMetricsLogsObserver.environment = environment;
    }

    @Override
    public void startServerObservation(ObserverContext observerContext) {
    }

    @Override
    public void startClientObservation(ObserverContext observerContext) {
    }

    @Override
    public void stopServerObservation(ObserverContext observerContext) {
        if (!observerContext.isStarted()) {
            // Do not collect metrics if the observation hasn't started
            return;
        }
        stopObservation(observerContext);
    }

    @Override
    public void stopClientObservation(ObserverContext observerContext) {
        if (!observerContext.isStarted()) {
            // Do not collect metrics if the observation hasn't started
            return;
        }
        stopObservation(observerContext);
    }

    private void stopObservation(ObserverContext observerContext) {
        Set<Tag> tags = new HashSet<>();
        Map<String, Tag> customTags = observerContext.customMetricTags;
        if (customTags != null) {
            tags.addAll(customTags.values());
        }
        tags.addAll(observerContext.getAllTags());

        // Add status_code_group tag
        Integer statusCode = (Integer) observerContext.getProperty(PROPERTY_KEY_HTTP_STATUS_CODE);
        if (statusCode != null && statusCode > 0) {
            tags.add(Tag.of(TAG_KEY_HTTP_STATUS_CODE_GROUP, (statusCode / 100) + STATUS_CODE_GROUP_SUFFIX));
        }

        try {
            Long startTime = (Long) observerContext.getProperty(PROPERTY_START_TIME);
            long duration = System.nanoTime() - startTime;

            Optional<String> protocolValue = Optional.empty();
            if (tags.stream().anyMatch(tag -> tag.getKey().equals("protocol"))) {
                protocolValue = tags.stream().filter(tag -> tag.getKey().equals("protocol")).map(Tag::getValue)
                        .findFirst();
            }
            String protocol = protocolValue.orElse("http");

            BMap<BString, Object> logAttributes = ValueCreator.createMapValue();
            logAttributes.put(StringUtils.fromString("protocol"), StringUtils.fromString(protocol));
            tags.stream().filter(tag -> !tag.getKey().equals("protocol"))
                    .forEach(tag -> logAttributes.put(StringUtils.fromString(tag.getKey()),
                            StringUtils.fromString(tag.getValue())));
            logAttributes.put(StringUtils.fromString("response_time_seconds"),
                    StringUtils.fromString(String.valueOf(duration / 1E9)));

            printMetricLog(logAttributes);
        } catch (RuntimeException e) {
            handleError("multiple metrics", tags, e);
        }
    }

    private void handleError(String metricName, Set<Tag> tags, RuntimeException e) {
        // Metric Provider may throw exceptions if there is a mismatch in tags.
        consoleError.println("error: error collecting metrics for " + metricName + " with tags " + tags +
                ": " + e.getMessage());
    }

    private static void printMetricLog(BMap<BString, Object> logAttributes) {
        // TODO: Remove version when the API is finalized, and add the configured org name.
        Module metricsLogsModule = new Module(ORG_NAME, ObserveUtils.getMetricsLogsProvider().getValue(), "1");
        environment.getRuntime().callFunction(metricsLogsModule, "printMetricsLog", null, logAttributes);
    }
}
