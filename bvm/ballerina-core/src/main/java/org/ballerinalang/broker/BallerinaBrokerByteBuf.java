package org.ballerinalang.broker;

import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.ballerinalang.model.values.BValue;

/**
 * Implementation of {@link io.netty.buffer.ByteBuf}, to hold a {@link BValue}, to use with the internal broker core
 * used in Ballerina.
 *
 * @since 0.973.0
 */
public class BallerinaBrokerByteBuf extends UnpooledHeapByteBuf {

    private final BValue value;

    public BallerinaBrokerByteBuf(BValue value) {
        super(UnpooledByteBufAllocator.DEFAULT, 0, 0);
        this.value = value;
    }

    public BValue getValue() {
        return value;
    }
}
