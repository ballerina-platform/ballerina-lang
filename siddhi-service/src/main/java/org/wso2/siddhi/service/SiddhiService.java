/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.siddhi.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.query.api.ExecutionPlan;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.service.util.ExecutionPlanConfiguration;
import org.wso2.siddhi.service.util.SiddhiServiceConstants;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Siddhi Service for Siddhi Runtime Management
 */

@Path("/siddhi")
public class SiddhiService {

    private Log log = LogFactory.getLog(SiddhiService.class);
    private SiddhiManager siddhiManager = new SiddhiManager();
    private Map<String, Map<String, InputHandler>> executionPlanSpecificInputHandlerMap = new ConcurrentHashMap<>();
    private List<ExecutionPlanConfiguration> executionPlanConfigurationList = new ArrayList<>();
    private Map<String, ExecutionPlanRuntime> executionPlanRunTimeMap = new ConcurrentHashMap<>();

    @GET
    @Path("/sendMsg")
    @Produces(MediaType.APPLICATION_JSON)
    public Response authenticate() {

        String refNum = "1111";
        String priority = "AAA";
        String fullMsg = "Hellllo";

        return Response.ok()
                .entity("{\"status\": \"ACK\",\"refNum\": \"" + refNum + "\",\"messageStatus\": \"" + priority + "\"," +
                        "" + "\"remarks\": \"" + fullMsg + "\"}")
                .build();
    }


    @POST
    @Path("/deploy")
    @Consumes(MediaType.TEXT_PLAIN)
    public Response dummy(String executionPlan) {

        log.info("ExecutionPlan = " + executionPlan);

        ExecutionPlan parsedExecutionPlan = SiddhiCompiler.parse(executionPlan);
        String executionPlanName = AnnotationHelper.getAnnotationElement(SiddhiServiceConstants.ANNOTATION_NAME_NAME,
                                                                         null, parsedExecutionPlan.getAnnotations()).getValue();
        if (!executionPlanRunTimeMap.containsKey(executionPlan)) {
            ExecutionPlanConfiguration executionPlanConfiguration = new ExecutionPlanConfiguration();
            executionPlanConfiguration.setName(executionPlanName);
            executionPlanConfigurationList.add(executionPlanConfiguration);

            ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(executionPlan);

            if (executionPlanRuntime != null) {
                Set<String> streamNames = executionPlanRuntime.getStreamDefinitionMap().keySet();
                Map<String, InputHandler> inputHandlerMap = new ConcurrentHashMap<>(streamNames.size());

                for (String streamName : streamNames) {
                    inputHandlerMap.put(streamName, executionPlanRuntime.getInputHandler(streamName));
                }

                executionPlanSpecificInputHandlerMap.put(executionPlanName, inputHandlerMap);

                executionPlanRunTimeMap.put(executionPlan, executionPlanRuntime);
                executionPlanRuntime.start();

                return Response.ok()
                        .entity("<Success>Execution Plan Runtime Created</Success>")
                        .build();
            }
        } else {
            return Response.ok()
                    .entity("<Error>There is a Execution plan already exists with same name</Error>")
                    .build();
        }

        return Response.ok()
                .entity("<Error>Not Sure</Error>")
                .build();

    }


}
