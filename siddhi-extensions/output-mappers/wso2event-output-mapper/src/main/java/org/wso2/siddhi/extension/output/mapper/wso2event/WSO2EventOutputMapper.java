package org.wso2.siddhi.extension.output.mapper.wso2event;

import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.stream.output.sink.OutputMapper;
import org.wso2.siddhi.core.stream.output.sink.OutputTransportCallback;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.core.util.transport.TemplateBuilder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Extension(
        name = "wso2event",
        namespace = "outputmapper",
        description = "Event to WSO2Event output mapper."
)
public class WSO2EventOutputMapper extends OutputMapper {
    private StreamDefinition streamDefinition;
    private OptionHolder optionHolder;
    private static final String PROPERTY_META_PREFIX = "meta_";
    private static final String PROPERTY_CORRELATION_PREFIX = "correlation_";
    private static final String STREAM_ID = "streamID";

    /**
     * Initialize the mapper and the mapping configurations.
     *
     * @param streamDefinition       The stream definition
     * @param optionHolder           Option holder containing static and dynamic options
     * @param payloadTemplateBuilder Unmapped payload for reference
     */
    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder optionHolder, TemplateBuilder payloadTemplateBuilder) {
        this.streamDefinition = streamDefinition;
        this.optionHolder = optionHolder;

        //streamID is expected to receive as an option
        if (!optionHolder.containsOption(STREAM_ID)) {
            throw new ExecutionPlanCreationException(String.format("{{%s}} configuration " +
                    "could not be found in provided configs.", STREAM_ID));
        }
    }

    /**
     * Map and publish the given {@link Event} array
     *
     * @param events                  Event object array
     * @param outputTransportCallback output transport callback
     * @param optionHolder            option holder containing static and dynamic options
     * @param payloadTemplateBuilder  Unmapped payload for reference
     */
    @Override
    public void mapAndSend(Event[] events, OutputTransportCallback outputTransportCallback, OptionHolder optionHolder, TemplateBuilder payloadTemplateBuilder) throws ConnectionUnavailableException {
        //TODO add support to publish multiple events
        for (Event event : events) {
            outputTransportCallback.publish(constructDefaultMapping(event), event);
        }
    }

    /**
     * Convert the given {@link Event} to WSO2Event Object
     *
     * @param event Event object
     * @return the constructed WSO2Event
     */
    private Object constructDefaultMapping(Event event) {
        org.wso2.carbon.databridge.commons.Event eventObject = new org.wso2.carbon.databridge.commons.Event();
        Object[] eventData = event.getData();
        List<Object> metaData = new ArrayList<Object>();
        List<Object> correlationData = new ArrayList<Object>();
        List<Object> payloadData = new ArrayList<Object>();

        // Construct WSO2Event
        eventObject.setStreamId(optionHolder.getOption(STREAM_ID, event));
        eventObject.setTimeStamp(event.getTimestamp());
        for (int i = 0; i < eventData.length; i++) {
            String attributeName = streamDefinition.getAttributeNameArray()[i];
            if (attributeName.startsWith(PROPERTY_META_PREFIX)) {
                metaData.add(eventData[i]);
            } else if (attributeName.startsWith(PROPERTY_CORRELATION_PREFIX)) {
                correlationData.add(eventData[i]);
            } else {
                payloadData.add(eventData[i]);
            }
        }
        if (!metaData.isEmpty()) {
            eventObject.setMetaData(metaData.toArray());
        }
        if (!correlationData.isEmpty()) {
            eventObject.setCorrelationData(correlationData.toArray());
        }
        if (!payloadData.isEmpty()) {
            eventObject.setPayloadData(payloadData.toArray());
        }

        // TODO remove if we are not going to support arbitraryDataMap
/*        // Get arbitrary data from event
        Map map = event.getArbitraryDataMap();
        if (map != null) {
            // Add arbitrary data map to the default template
            eventObject.setArbitraryDataMap(map);
        }*/

        return eventObject;
    }
}