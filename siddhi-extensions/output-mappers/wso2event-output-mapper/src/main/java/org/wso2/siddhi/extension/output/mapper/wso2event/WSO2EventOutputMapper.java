package org.wso2.siddhi.extension.output.mapper.wso2event;

import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.output.sink.Sinkmapper;
import org.wso2.siddhi.core.stream.output.sink.SinkCallback;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.core.util.transport.TemplateBuilder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.List;

@Extension(
        name = "wso2event",
        namespace = "sinkMapper",
        description = "Event to WSO2Event output mapper."
)
public class WSO2EventSinkmapper extends Sinkmapper {
    private static final String PROPERTY_META_PREFIX = "meta_";
    private static final String PROPERTY_CORRELATION_PREFIX = "correlation_";
    private static final String STREAM_ID = "streamID";
    private StreamDefinition streamDefinition;
    private OptionHolder optionHolder;

    /**
     * Initialize the mapper and the mapping configurations.
     *
     * @param streamDefinition       The stream definition
     * @param optionHolder           Option holder containing static and dynamic options
     * @param payloadTemplateBuilder Unmapped payload for reference
     */
    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder optionHolder, TemplateBuilder
            payloadTemplateBuilder) {
        this.streamDefinition = streamDefinition;
        this.optionHolder = optionHolder;
    }

    /**
     * Map and publish the given {@link Event} array
     *
     * @param events                  Event object array
     * @param sinkCallback output transport callback
     * @param optionHolder            option holder containing static and dynamic options
     * @param payloadTemplateBuilder  Unmapped payload for reference
     */
    @Override
    public void mapAndSend(Event[] events, SinkCallback sinkCallback,
                           OptionHolder optionHolder, TemplateBuilder payloadTemplateBuilder)
            throws ConnectionUnavailableException {
        //TODO add support to publish multiple events
        for (Event event : events) {
            sinkCallback.publish(constructDefaultMapping(event), event);
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

        //streamID is expected to receive as an option
        eventObject.setStreamId(optionHolder.validateAndGetOption(STREAM_ID).getValue());
        eventObject.setTimestamp(event.getTimestamp());
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