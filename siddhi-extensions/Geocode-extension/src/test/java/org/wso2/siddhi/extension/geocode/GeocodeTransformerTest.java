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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.config.SiddhiConfiguration;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;

public class GeocodeTransformerTest {

	private static final Logger LOGGER = Logger.getLogger(GeocodeTransformerTest.class);
	protected static SiddhiManager siddhiManager;
	private static List<Object[]> data;
	protected long start;
	protected long end;

	@AfterClass
	public static void tearDown() throws Exception {
		Thread.sleep(1000);
		LOGGER.info("Shutting down Siddhi");
		siddhiManager.shutdown();
	}

	@Test
	public void testProcess() throws Exception {
		LOGGER.info("TestProcess");

		start = System.currentTimeMillis();

		String eventFuseExecutionPlan =
		                                "from geocodeStream#transform.geo:geocode(location) \n"
		                                        + "select * \n" + "insert into geocodeOutStream;";

		String eventFuseQueryReference = siddhiManager.addQuery(eventFuseExecutionPlan);

		end = System.currentTimeMillis();
		LOGGER.info(String.format("Time to add query: [%f sec]", ((end - start) / 1000f)));
		siddhiManager.addCallback(eventFuseQueryReference, new QueryCallback() {
			@Override
			public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {

				for (Event event : inEvents) {
					System.out.println("Formatted address: " + event.getData(0) +
					                   ", Coordinates: (" + event.getData(1) + ", " +
					                   event.getData(2) + "), Location: " + event.getData(3) +
					                   ", Traffic level: " + event.getData(4) + ", Time: " +
					                   event.getData(5));
				}
			}
		});
		generateEvents();
	}

	private void generateEvents() throws Exception {
		InputHandler inputHandler = siddhiManager.getInputHandler("geocodeStream");
		for (Object[] dataLine : data) {
			inputHandler.send(new Object[] { dataLine[0], dataLine[1], dataLine[2] });
		}
	}

	@Before
	public void setUp() throws Exception {
		LOGGER.info("Init Siddhi setUp");

		SiddhiConfiguration siddhiConfiguration = new SiddhiConfiguration();

		List<Class> extensions = new ArrayList<Class>();
		extensions.add(GeocodeTransformer.class);

		siddhiConfiguration.setSiddhiExtensions(extensions);

		siddhiManager = new SiddhiManager(siddhiConfiguration);

		LOGGER.info("calling setUpChild");
		siddhiManager.defineStream("define stream geocodeStream (location string, level string, time string)");

		data = new ArrayList<Object[]>();

		data.add(new Object[] { "ingurukade junction", "Regular", "Sun Nov 03 13:36:05 +0000 2014" });
		data.add(new Object[] { "gunasekara mawatha", "Regular", "Sun Nov 02 13:36:05 +0000 2014" });
		data.add(new Object[] { "hendala road", "Regular", "Sun Nov 12 13:36:05 +0000 2014" });
		data.add(new Object[] { "mt lavinia", "Regular", "Sun Nov 10 13:36:05 +0000 2014" });
		data.add(new Object[] { "duplication rd", "Regular", "Sun Nov 02 13:36:05 +0000 2014" });
	}
}