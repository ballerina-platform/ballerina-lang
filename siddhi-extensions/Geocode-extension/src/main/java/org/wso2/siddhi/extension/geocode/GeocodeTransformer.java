/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.geocode;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.event.in.InEvent;
import org.wso2.siddhi.core.event.in.InListEvent;
import org.wso2.siddhi.core.event.in.InStream;
import org.wso2.siddhi.core.executor.expression.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.transform.TransformProcessor;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.extension.annotation.SiddhiExtension;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;

/**
 * This extension transforms a location into its geo-coordinates and formatted
 * address
 */
@SiddhiExtension(namespace = "geo", function = "geocode")
public class GeocodeTransformer extends TransformProcessor {

	private static final Logger LOGGER = Logger.getLogger(GeocodeTransformer.class);
	private final Geocoder geocoder = new Geocoder();
	private boolean debugModeOn;
	private String location;
	private int paramPosition;
	private int inStreamNoOfAttributes;
	private String formattedAddress;
	private double latitude;
	private double longitude;

	public GeocodeTransformer() {
		this.outStreamDefinition =
		                           new StreamDefinition().name("geocodeStream")
		                                                 .attribute("formattedAddress",
		                                                            Attribute.Type.STRING)
		                                                 .attribute("latitude",
		                                                            Attribute.Type.DOUBLE)
		                                                 .attribute("longitude",
		                                                            Attribute.Type.DOUBLE)
		                                                 .attribute("location",
		                                                            Attribute.Type.STRING)
		                                                 .attribute("level", Attribute.Type.STRING)
		                                                 .attribute("time", Attribute.Type.STRING);
	}

	@Override
	protected InStream processEvent(InEvent inEvent) {
		location = inEvent.getData(paramPosition).toString();

		if (debugModeOn) {
			LOGGER.debug("Entered location: " + location);
		}

		// Make the geocode request to API library
		GeocoderRequest geocoderRequest =
		                                  new GeocoderRequestBuilder().setAddress(location)
		                                                              .setLanguage("en")
		                                                              .getGeocoderRequest();

		try {
			GeocodeResponse geocoderResponse = geocoder.geocode(geocoderRequest);

			if (!geocoderResponse.getResults().isEmpty()) {
				latitude =
				           geocoderResponse.getResults().get(0).getGeometry().getLocation()
				                           .getLat().doubleValue();
				longitude =
				            geocoderResponse.getResults().get(0).getGeometry().getLocation()
				                            .getLng().doubleValue();
				formattedAddress =
				                   geocoderResponse.getResults().get(0).getFormattedAddress()
				                                   .toString();
			} else {
				latitude = -1.0;
				longitude = -1.0;
				formattedAddress = "N/A";
			}

		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}

		if (debugModeOn) {
			LOGGER.debug("Formatted address: " + formattedAddress + ", Location coordinates: (" +
			             latitude + ", " + longitude + ")");
		}

		Object[] locationAttributes = new Object[] { formattedAddress, latitude, longitude };
		Object[] inStreamAttributes = new Object[inStreamNoOfAttributes];

		for (int i = 0; i < inStreamNoOfAttributes; i++) {
			inStreamAttributes[i] = inEvent.getData(i);
		}

		Object[] data = ArrayUtils.addAll(locationAttributes, inStreamAttributes);

		return new InEvent(inEvent.getStreamId(), System.currentTimeMillis(), data);
	}

	@Override
	protected InStream processEvent(InListEvent inListEvent) {
		InListEvent transformedListEvent = new InListEvent();
		for (Event event : inListEvent.getEvents()) {
			if (event instanceof InEvent) {
				transformedListEvent.addEvent((Event) processEvent((InEvent) event));
			}
		}
		return transformedListEvent;
	}

	@Override
	protected Object[] currentState() {
		return new Object[] { location, paramPosition, inStreamNoOfAttributes, formattedAddress,
		                     latitude, longitude };
	}

	@Override
	protected void restoreState(Object[] objects) {
		if ((objects.length == 6) && (objects[0] instanceof String) &&
		    (objects[1] instanceof Integer) && (objects[2] instanceof Integer) &&
		    (objects[3] instanceof String) && (objects[4] instanceof Double) &&
		    (objects[5] instanceof Double)) {

			this.location = (String) objects[0];
			this.paramPosition = (Integer) objects[1];
			this.inStreamNoOfAttributes = (Integer) objects[2];
			this.formattedAddress = (String) objects[3];
			this.latitude = (Double) objects[4];
			this.longitude = (Double) objects[5];
		} else {
			LOGGER.error("Restoring GeocodeTransformer object failed.");
		}
	}

	@Override
	protected void init(Expression[] parameters, List<ExpressionExecutor> expressionExecutors,
	                    StreamDefinition inStreamDefinition, StreamDefinition outStreamDefinition,
	                    String elementId, SiddhiContext siddhiContext) {

		debugModeOn = LOGGER.isDebugEnabled();

		if (parameters[0] instanceof Variable) {
			Variable var = (Variable) parameters[0];
			String attributeName = var.getAttributeName();
			paramPosition = inStreamDefinition.getAttributePosition(attributeName);
		}
		inStreamNoOfAttributes = inStreamDefinition.getAttributeList().size();
	}

	public void destroy() {
		// This method clears referenced variables
	}
}