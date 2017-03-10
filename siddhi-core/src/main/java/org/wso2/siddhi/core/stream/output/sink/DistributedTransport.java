package org.wso2.siddhi.core.stream.output.sink;

import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.List;

/**
 *
 */
public abstract class DistributedTransport extends OutputTransport {

    public static final String DISTRIBUTION_STRATEGY_KEY = "strategy";
    public static final String DISTRIBUTION_CHANNELS_KEY = "channels";
    public static final String PARTITION_KEY_FIELD_KEY = "partition-key";

    public static final String DISTRIBUTION_STRATEGY_ROUND_ROBIN = "round-robin";
    public static final String DISTRIBUTION_STRATEGY_DUPLICATE = "duplicate";
    public static final String DISTRIBUTION_STRATEGY_PARTITIONED = "partitioned";

    private String distributionStrategy;
    private int channelCount = -1;
    private int partitionFiledIndex = -1;
    private DistributedPublishingAlgorithm publisher;
    protected StreamDefinition streamDefinition;
    protected OptionHolder sinkOptionHolder;

    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder optionHolder){
        this.streamDefinition = streamDefinition;
        this.sinkOptionHolder = optionHolder;
    }

    @Override
    public void publish(Object payload, Event event, OptionHolder optionHolder) throws ConnectionUnavailableException {
        publisher.publish(payload, event, optionHolder);
    }

    public void initDistribution(OptionHolder distributedOptionHolder, List<OptionHolder> nodeOptionHolders){
        distributionStrategy = distributedOptionHolder.validateAndGetStaticValue(DISTRIBUTION_STRATEGY_KEY);

        if (distributionStrategy == null || distributionStrategy.isEmpty()){
            throw new ExecutionPlanValidationException("Distribution strategy is not specified.");
        }

        if (distributedOptionHolder.isOptionExists(DISTRIBUTION_CHANNELS_KEY)) {
            channelCount = Integer.parseInt(distributedOptionHolder.validateAndGetStaticValue(DISTRIBUTION_CHANNELS_KEY));
            if (channelCount <= 0) {
                throw new ExecutionPlanValidationException("There must be at least one channel.");
            }
        }

        if (distributionStrategy.equals(DISTRIBUTION_STRATEGY_PARTITIONED)){
            partitionFiledIndex = streamDefinition.getAttributePosition(PARTITION_KEY_FIELD_KEY);
        }

        if (distributionStrategy.equals(DISTRIBUTION_STRATEGY_ROUND_ROBIN)){
            publisher = getRoundRobinPublisher();
        } else if (distributionStrategy.equals(DISTRIBUTION_STRATEGY_DUPLICATE)){
            publisher = getAllEndpointsPublisher();
        } else if (distributionStrategy.equals(DISTRIBUTION_STRATEGY_PARTITIONED)){
            publisher = getPartitionedPublisher();
        } else {
            throw new ExecutionPlanValidationException("Unknown distribution strategy '" + distributionStrategy + "'.");
        }

        initTransport(streamDefinition, sinkOptionHolder, nodeOptionHolders);
    }

    public String getDistributionStrategy(){
        return  distributionStrategy;
    }

    public int getChannelCount(){
        if (channelCount == -1){
            throw new ExecutionPlanValidationException("Channel count not specified.");
        }

        return channelCount;
    }

    public int getPartitionFiledIndex(){
        return partitionFiledIndex;
    }

    public abstract DistributedPublishingAlgorithm getRoundRobinPublisher();

    public abstract DistributedPublishingAlgorithm getAllEndpointsPublisher();

    public abstract DistributedPublishingAlgorithm getPartitionedPublisher();

    public abstract void initTransport(StreamDefinition streamDefinition, OptionHolder sinkOptionHolder, List<OptionHolder> nodeOptionHolders);

}
