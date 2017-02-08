package org.wso2.siddhi.extension.output.mapper.wso2event;

import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.publisher.OutputMapper;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Extension(
        name = "wso2event",
        namespace = "outputmapper",
        description = ""
)
public class WSO2EventOutputMapper extends OutputMapper {
    private StreamDefinition streamDefinition;
    Map<String, String> options;
    private static final String PROPERTY_META_PREFIX = "meta_";
    private static final String PROPERTY_CORRELATION_PREFIX = "correlation_";
    private static final String STREAM_ID = "streamID";
    private boolean isDynamicStreamID;

    /**
     * Initialize the mapper and the mapping configurations.
     *
     * @param streamDefinition       The stream definition
     * @param options                Additional mapping options
     * @param unmappedDynamicOptions Unmapped dynamic options
     */
    @Override
    public void init(StreamDefinition streamDefinition, Map<String, String> options, Map<String, String> unmappedDynamicOptions) {
        this.streamDefinition = streamDefinition;
        this.options = options;

        //streamID is expected to receive as an option/dynamic option
        List<String> availableOptions = new ArrayList<>();
        availableOptions.addAll(options.keySet());
        availableOptions.addAll(unmappedDynamicOptions.keySet());
        //validate for mandatory field streamID
        if (availableOptions.contains(STREAM_ID)) {
            isDynamicStreamID = unmappedDynamicOptions.containsKey(STREAM_ID);
        } else {
            throw new ExecutionPlanCreationException(String.format("{{%s}} configuration " +
                    "could not be found in provided configs.", STREAM_ID));
        }
    }

    /**
     * Convert the given {@link Event} to WSO2Event Object
     *
     * @param event          Event object
     * @param dynamicOptions Dynamic options
     * @return the constructed Event Object in WSO2Event format
     */
    @Override
    public Object convertToTypedInputEvent(Event event, Map<String, String> dynamicOptions) {
        org.wso2.carbon.databridge.commons.Event eventObject = new org.wso2.carbon.databridge.commons.Event();
        Object[] eventData = event.getData();
        List<Object> metaData = new ArrayList<Object>();
        List<Object> correlationData = new ArrayList<Object>();
        List<Object> payloadData = new ArrayList<Object>();

        // Construct WSO2Event
        if (!isDynamicStreamID) {
            eventObject.setStreamId(options.get(STREAM_ID));
        } else {
            eventObject.setStreamId(dynamicOptions.get(STREAM_ID));
        }
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

        // Get arbitrary data from event
        Map map = event.getArbitraryDataMap();
        if (map != null) {
            // Add arbitrary data map to the default template
            eventObject.setArbitraryDataMap(map);
        }

        return eventObject;
    }

    /**
     * @param event            Event object
     * @param mappedAttributes Event mapping string array
     * @param dynamicOptions   Dynamic options
     * @return null
     */
    // Note: Currently, we do not support custom mapping for "wso2event" type since the expected usecases can be
    // handled by writing siddhi queries
    @Override
    public Object convertToMappedInputEvent(Event event, String[] mappedAttributes, Map<String, String> dynamicOptions) {
        return null;
    }
}