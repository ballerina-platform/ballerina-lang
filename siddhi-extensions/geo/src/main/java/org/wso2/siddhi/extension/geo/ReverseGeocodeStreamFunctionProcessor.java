/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import com.google.code.geocoder.model.GeocoderAddressComponent;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.LatLng;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.exception.ExecutionPlanRuntimeException;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * This extension transforms a latitude and longitude coordinates into precise address information.
 * The output contains string properties streetNumber, neighborhood, route, administrativeAreaLevelTwo,
 * administrativeAreaLevelOne, country, countryCode, postalCode and formattedAddress in order.
 * However, these information are not available for all the geo coordinates. For example, if the latitude
 * and longitude represent a place in a forest, only the high level information like country will be returned.
 * For those which are not available, this extension will return "N/A" as the value.
 */
public class ReverseGeocodeStreamFunctionProcessor extends StreamFunctionProcessor {

    private static final Logger LOGGER = Logger.getLogger(ReverseGeocodeStreamFunctionProcessor.class);
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
        if (data[0] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to geo:reversegeocode() function. The first argument cannot be null");
        }
        if (data[1] == null) {
            throw new ExecutionPlanRuntimeException("Invalid input given to geo:reversegeocode() function. The second argument cannot be null");
        }

        BigDecimal latitude = new BigDecimal((Double) data[0]);
        BigDecimal longitude = new BigDecimal((Double) data[1]);

        LatLng coordinate = new LatLng(latitude, longitude);

        // Make the geocode request to API library
        GeocoderRequest geocoderRequest = new GeocoderRequestBuilder()
                .setLocation(coordinate)
                .setLanguage("en")
                .getGeocoderRequest();

        String streetNumber = "N/A";
        String neighborhood = "N/A";
        String route = "N/A";
        String administrativeAreaLevelTwo = "N/A";
        String administrativeAreaLevelOne = "N/A";
        String country = "N/A";
        String countryCode = "N/A";
        String postalCode = "N/A";
        String formattedAddress = "N/A";

        try {
            GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);

            if (!geocoderResponse.getResults().isEmpty()) {
                formattedAddress = geocoderResponse.getResults().get(0).getFormattedAddress();
                List<GeocoderAddressComponent> addressComponents = geocoderResponse.getResults().get(0).getAddressComponents();
                for (GeocoderAddressComponent component : addressComponents) {
                    List<String> types = component.getTypes();
                    if (types.contains("street_number")) {
                        streetNumber = component.getLongName();
                    } else if (types.contains("neighborhood")) {
                        neighborhood = component.getLongName();
                    } else if (types.contains("route")) {
                        route = component.getLongName();
                    } else if (types.contains("administrative_area_level_2")) {
                        administrativeAreaLevelTwo = component.getLongName();
                    } else if (types.contains("administrative_area_level_1")) {
                        administrativeAreaLevelOne = component.getLongName();
                    } else if (types.contains("country")) {
                        country = component.getLongName();
                        countryCode = component.getShortName();
                    } else if (types.contains("postal_code")) {
                        postalCode = component.getLongName();
                    }
                }
            }
        } catch (IOException e) {
            throw new ExecutionPlanRuntimeException("Error in connection to Google Maps API.", e);
        }

        if (debugModeOn) {
            String message = String.format("Street Number: %s, Neighborhood: %s, Route: %s, Administrative Area Level 2: %s, Administrative Area Level 1: %s, Country: %s, ISO Country code: %s, Postal code: %s, Formatted address: %s", streetNumber, neighborhood,
                    route, administrativeAreaLevelTwo, administrativeAreaLevelOne, country, countryCode, postalCode, formattedAddress);
            LOGGER.debug(message);
        }
        return new Object[]{streetNumber, neighborhood, route, administrativeAreaLevelTwo, administrativeAreaLevelOne, country, countryCode, postalCode, formattedAddress};
    }

    /**
     * The process method of the StreamFunction, used when zero or one function parameter is provided
     *
     * @param data null if the function parameter count is zero or runtime data value of the function parameter
     * @return the data for additional output attribute introduced by the function
     */
    @Override
    protected Object[] process(Object data) {
        throw new RuntimeException("");
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
        if (attributeExpressionExecutors.length != 2) {
            throw new ExecutionPlanValidationException("Invalid no of arguments passed to geo:reversegeocode() function, required 1, " +
                    "but found " + attributeExpressionExecutors.length);
        }
        if (attributeExpressionExecutors[0].getReturnType() != Attribute.Type.DOUBLE || attributeExpressionExecutors[1].getReturnType() != Attribute.Type.DOUBLE) {
            throw new ExecutionPlanCreationException("Both input parameters should be of type double");
        }
        ArrayList<Attribute> attributes = new ArrayList<Attribute>(9);
        attributes.add(new Attribute("streetNumber", Attribute.Type.STRING));
        attributes.add(new Attribute("neighborhood", Attribute.Type.STRING));
        attributes.add(new Attribute("route", Attribute.Type.STRING));
        attributes.add(new Attribute("administrativeAreaLevelTwo", Attribute.Type.STRING));
        attributes.add(new Attribute("administrativeAreaLevelOne", Attribute.Type.STRING));
        attributes.add(new Attribute("country", Attribute.Type.STRING));
        attributes.add(new Attribute("countryCode", Attribute.Type.STRING));
        attributes.add(new Attribute("postalCode", Attribute.Type.STRING));
        attributes.add(new Attribute("formattedAddress", Attribute.Type.STRING));
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