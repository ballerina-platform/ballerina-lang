/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.geo;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This extension transforms a location into its geo-coordinates and formatted
 * address
 */
public class GeocodeStreamFunctionProcessor extends StreamFunctionProcessor {

    private static final Logger LOGGER = Logger.getLogger(GeocodeStreamFunctionProcessor.class);
    private final Geocoder geocoder = new Geocoder();
    private boolean debugModeOn;

    /**
     * The process method of the StreamFunction, used when more then one function parameters are provided
     *
     * @param data the data values for the function parameters
     * @return the data for additional output attributes introduced by the function
     */
    @Override
    protected Object[] process(Object[] data) {
        return process(data[0]);
    }

    /**
     * The process method of the StreamFunction, used when zero or one function parameter is provided
     *
     * @param data null if the function parameter count is zero or runtime data value of the function parameter
     * @return the data for additional output attribute introduced by the function
     */
    @Override
    protected Object[] process(Object data) {
        String location = data.toString();

        // Make the geocode request to API library
        GeocoderRequest geocoderRequest = new GeocoderRequestBuilder()
                .setAddress(location)
                .setLanguage("en")
                .getGeocoderRequest();

        double latitude, longitude;
        String formattedAddress;
        try {
            GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);

            if (!geocoderResponse.getResults().isEmpty()) {
                latitude = geocoderResponse.getResults().get(0).getGeometry().getLocation()
                        .getLat().doubleValue();
                longitude = geocoderResponse.getResults().get(0).getGeometry().getLocation()
                        .getLng().doubleValue();
                formattedAddress = geocoderResponse.getResults().get(0).getFormattedAddress();
            } else {
                latitude = -1.0;
                longitude = -1.0;
                formattedAddress = "N/A";
            }

        } catch (IOException e) {
            throw new ExecutionPlanRuntimeException("Error in connection to Google Maps API.", e);
        }

        if (debugModeOn) {
            LOGGER.debug("Formatted address: " + formattedAddress + ", Location coordinates: (" +
                    latitude + ", " + longitude + ")");
        }
        return new Object[]{formattedAddress, latitude, longitude};
    }

    /**
     * The init method of the StreamProcessor, this method will be called before other methods
     *
     * @param inputDefinition              the incoming stream definition
     * @param attributeExpressionExecutors the executors of each function parameters
     * @param executionPlanContext         the context of the execution plan
     * @return the additional output attributes introduced by the function
     */
    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        debugModeOn = LOGGER.isDebugEnabled();
        if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.STRING) {
            throw new ExecutionPlanCreationException("First parameter should be of type string");
        }
        ArrayList<Attribute> attributes = new ArrayList<Attribute>(6);
        attributes.add(new Attribute("formattedAddress", Attribute.Type.STRING));
        attributes.add(new Attribute("latitude", Attribute.Type.DOUBLE));
        attributes.add(new Attribute("longitude", Attribute.Type.DOUBLE));
        return attributes;
    }

    /**
     * This will be called only once and this can be used to acquire
     * required resources for the processing element.
     * This will be called after initializing the system and before
     * starting to process the events.
     */
    @Override
    public void start() {

    }

    /**
     * This will be called only once and this can be used to release
     * the acquired resources for processing.
     * This will be called before shutting down the system.
     */
    @Override
    public void stop() {

    }

    /**
     * Used to collect the serializable state of the processing element, that need to be
     * persisted for the reconstructing the element to the same state on a different point of time
     *
     * @return stateful objects of the processing element as an array
     */
    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    /**
     * Used to restore serialized state of the processing element, for reconstructing
     * the element to the same state as if was on a previous point of time.
     *
     * @param state the stateful objects of the element as an array on
     *              the same order provided by currentState().
     */
    @Override
    public void restoreState(Object[] state) {

    }
}