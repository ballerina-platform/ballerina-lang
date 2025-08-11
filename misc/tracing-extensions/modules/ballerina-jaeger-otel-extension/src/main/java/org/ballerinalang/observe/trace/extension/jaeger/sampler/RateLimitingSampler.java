/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.ballerinalang.observe.trace.extension.jaeger.sampler;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.trace.data.LinkData;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.sdk.trace.samplers.SamplingDecision;
import io.opentelemetry.sdk.trace.samplers.SamplingResult;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

/**
 * This class is copied from https://github.com/open-telemetry/opentelemetry-java/blob/v1.32.0/sdk-extensions/
 * jaeger-remote-sampler/src/main/java/io/opentelemetry/sdk/extension/trace/jaeger/sampler/RateLimitingSampler.java.
 * This sampler uses a leaky bucket rate limiter to ensure that traces are sampled with a certain constant rate.
 */
public class RateLimitingSampler implements Sampler {
    public static final String TYPE = "ratelimiting";
    private static final AttributeKey<String> SAMPLER_TYPE = stringKey("sampler.type");
    private static final AttributeKey<Double> SAMPLER_PARAM = doubleKey("sampler.param");

    private final RateLimiter rateLimiter;
    private final SamplingResult onSamplingResult;
    private final SamplingResult offSamplingResult;
    private final String description;

    /**
     * Creates rate limiting sampler.
     *
     * @param maxTracesPerSecond the maximum number of sampled traces per second.
     */
    public RateLimitingSampler(int maxTracesPerSecond) {
        double maxBalance = maxTracesPerSecond < 1.0 ? 1.0 : maxTracesPerSecond;
        this.rateLimiter = new RateLimiter(maxTracesPerSecond, maxBalance, Clock.getDefault());
        Attributes attributes =
                Attributes.of(SAMPLER_TYPE, TYPE, SAMPLER_PARAM, (double) maxTracesPerSecond);
        this.onSamplingResult = SamplingResult.create(SamplingDecision.RECORD_AND_SAMPLE, attributes);
        this.offSamplingResult = SamplingResult.create(SamplingDecision.DROP, attributes);
        description = "RateLimitingSampler{" + decimalFormat(maxTracesPerSecond) + "}";
    }

    @Override
    public SamplingResult shouldSample(
            Context parentContext,
            String traceId,
            String name,
            SpanKind spanKind,
            Attributes attributes,
            List<LinkData> parentLinks) {
        return this.rateLimiter.checkCredit(1.0) ? onSamplingResult : offSamplingResult;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getDescription();
    }

    private static String decimalFormat(double value) {
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance();
        decimalFormatSymbols.setDecimalSeparator('.');

        DecimalFormat decimalFormat = new DecimalFormat("0.00", decimalFormatSymbols);
        return decimalFormat.format(value);
    }
}
