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

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

public class ReverseGeocodeStreamFunctionProcessorTest {

    private static final Logger LOGGER = Logger.getLogger(ReverseGeocodeStreamFunctionProcessorTest.class);

    @Test
    public void testReverseGeocode1() throws Exception {
        LOGGER.info("Test Reverse Geocode 1 - Coordinates in LK");

        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime("define stream LocationStream (deviceId string, timestamp long, latitude double, longitude double);"
                + "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude, longitude) " +
                " select streetNumber, neighborhood, route, administrativeAreaLevelTwo, administrativeAreaLevelOne, country, countryCode, postalCode, formattedAddress " +
                " insert into OutputStream");

        executionPlanRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                Assert.assertArrayEquals(new Object[]{"N/A", "N/A", "Palm Grove", "Colombo", "Western Province", "Sri Lanka", "LK", "00300", "Palm Grove, Colombo 00300, Sri Lanka"}, events[0].getData());
            }
        });
        executionPlanRuntime.start();

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LocationStream");
        inputHandler.send(new Object[]{"HTC-001", System.currentTimeMillis(), 6.909785, 79.852603});
        Thread.sleep(2000);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testReverseGeocode2() throws Exception {
        LOGGER.info("Test Reverse Geocode 2 - Coordinates in USA");

        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime("define stream LocationStream (deviceId string, timestamp long, latitude double, longitude double);"
                + "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude, longitude) " +
                " select streetNumber, neighborhood, route, administrativeAreaLevelTwo, administrativeAreaLevelOne, country, countryCode, postalCode, formattedAddress " +
                " insert into OutputStream");

        executionPlanRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                Assert.assertArrayEquals(new Object[]{"67-53", "Flushing", "Loubet Street", "Queens County", "New York", "United States", "US", "11375", "67-53 Loubet St, Flushing, NY 11375, USA"}, events[0].getData());
            }
        });
        executionPlanRuntime.start();

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LocationStream");
        inputHandler.send(new Object[]{"HTC-001", System.currentTimeMillis(), 40.715229, -73.8564082});
        Thread.sleep(2000);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testReverseGeocode3() throws Exception {
        LOGGER.info("Test Reverse Geocode 3 - Coordinates in UK");

        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime("define stream LocationStream (deviceId string, timestamp long, latitude double, longitude double);"
                + "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude, longitude) " +
                " select streetNumber, neighborhood, route, administrativeAreaLevelTwo, administrativeAreaLevelOne, country, countryCode, postalCode, formattedAddress " +
                " insert into OutputStream");

        executionPlanRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                Assert.assertArrayEquals(new Object[]{"N/A", "N/A", "Westminster Bridge Road", "Greater London", "N/A", "United Kingdom", "GB", "SW1A", "Westminster Bridge Rd, London SW1A, UK"}, events[0].getData());
            }
        });
        executionPlanRuntime.start();

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LocationStream");
        inputHandler.send(new Object[]{"HTC-001", System.currentTimeMillis(), 51.500861, -0.1228007});
        Thread.sleep(2000);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testReverseGeocode4() throws Exception {
        LOGGER.info("Test Reverse Geocode 4 - Less precised input");

        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime("define stream LocationStream (deviceId string, timestamp long, latitude double, longitude double);"
                + "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude, longitude) " +
                " select streetNumber, neighborhood, route, administrativeAreaLevelTwo, administrativeAreaLevelOne, country, countryCode, postalCode, formattedAddress " +
                " insert into OutputStream");

        executionPlanRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                Assert.assertArrayEquals(new Object[]{"N/A", "N/A", "Unnamed Road", "Colombo", "Western Province", "Sri Lanka", "LK", "N/A", "Unnamed Road, Colombo, Sri Lanka"}, events[0].getData());
            }
        });
        executionPlanRuntime.start();

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LocationStream");
        inputHandler.send(new Object[]{"HTC-001", System.currentTimeMillis(), 6.9, 79.8});
        Thread.sleep(2000);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testReverseGeocode5() throws Exception {
        LOGGER.info("Test Reverse Geocode 4 - Amazon Rain Forest");

        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime("define stream LocationStream (deviceId string, timestamp long, latitude double, longitude double);"
                + "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude, longitude) " +
                " select streetNumber, neighborhood, route, administrativeAreaLevelTwo, administrativeAreaLevelOne, country, countryCode, postalCode, formattedAddress " +
                " insert into OutputStream");

        executionPlanRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                EventPrinter.print(events);
                Assert.assertArrayEquals(new Object[]{"N/A", "N/A", "N/A", "Caapiranga", "State of Amazonas", "Brazil", "BR", "N/A", "Caapiranga - State of Amazonas, Brazil"}, events[0].getData());
            }
        });
        executionPlanRuntime.start();

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LocationStream");
        inputHandler.send(new Object[]{"HTC-001", System.currentTimeMillis(), -3.250617, -62.124425});
        Thread.sleep(2000);
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testReverseGeocode6() throws Exception {
        LOGGER.info("Test Reverse Geocode 5 - null inputs");

        SiddhiManager siddhiManager = new SiddhiManager();
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime("define stream LocationStream (deviceId string, timestamp long, latitude double, longitude double);"
                + "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude, longitude) " +
                " select streetNumber, neighborhood, route, administrativeAreaLevelTwo, administrativeAreaLevelOne, country, countryCode, postalCode, formattedAddress " +
                " insert into OutputStream");

        executionPlanRuntime.addCallback("OutputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {
                Assert.fail();
            }
        });

        executionPlanRuntime.start();

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("LocationStream");
        inputHandler.send(new Object[]{"HTC-001", System.currentTimeMillis(), null, null});
        Thread.sleep(2000);
        executionPlanRuntime.shutdown();
    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void testReverseGeocode7() throws Exception {
        LOGGER.info("Test Reverse Geocode 6 - Invalid number of input parameters");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.createExecutionPlanRuntime("define stream LocationStream (deviceId string, timestamp long, latitude double, longitude double);"
                + "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude) " +
                " select streetNumber, neighborhood, route, administrativeAreaLevelTwo, administrativeAreaLevelOne, country, countryCode, postalCode, formattedAddress " +
                " insert into OutputStream");
    }

    @Test(expected = ExecutionPlanValidationException.class)
    public void testReverseGeocode8() throws Exception {
        LOGGER.info("Test Reverse Geocode 7 - Invalid type of input parameters");

        SiddhiManager siddhiManager = new SiddhiManager();
        siddhiManager.createExecutionPlanRuntime("define stream LocationStream (deviceId string, timestamp long, latitude float, longitude float);"
                + "@info(name = 'query1') from LocationStream#geo:reversegeocode(latitude, longitude) " +
                " select streetNumber, neighborhood, route, administrativeAreaLevelTwo, administrativeAreaLevelOne, country, countryCode, postalCode, formattedAddress " +
                " insert into OutputStream");
    }
}