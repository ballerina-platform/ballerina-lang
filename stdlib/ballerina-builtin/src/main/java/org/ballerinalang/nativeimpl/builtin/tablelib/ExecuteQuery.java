/*
*   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.ballerinalang.nativeimpl.builtin.tablelib;

import org.ballerinalang.bre.Context;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;

import java.util.Arrays;

/**
 * Native function wso2.ballerina.stream:execute.
 *
 * @since 0.90
 */
@BallerinaFunction(
        packageName = "ballerina.builtin",
        functionName = "executeQuery",
        args = {
                @Argument(name = "sqlQuery",
                        type = TypeKind.STRING)
        },
        isPublic = true
)

public class ExecuteQuery extends AbstractNativeFunction {

    private static final Logger log = LoggerFactory.getLogger(ExecuteQuery.class);

    public BValue[] execute(Context ctx) {

//        String inputQuery = getStringArgument(ctx, 0);
        SiddhiManager siddhiManager = new SiddhiManager();
        String inStreamDefinition = "define stream inputStream (text string); ";
        String query = ("@info(name = 'query1') " + "from inputStream "
                + "select text " + "insert into outputStream;");
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager
                .createSiddhiAppRuntime(inStreamDefinition + query);

        siddhiAppRuntime.addCallback("query1", new EventCallBack());
        InputHandler inputHandler = siddhiAppRuntime.getInputHandler("inputStream");
        siddhiAppRuntime.start();

        try {
            inputHandler.send(new Object[]{"Trump is a bad person."});
            inputHandler.send(new Object[]{"Trump is a good person."});
            inputHandler.send(new Object[]{"Trump is a good person. Trump is a bad person"});
            inputHandler.send(new Object[]{"What is wrong with these people"});

            log.info("Siddhi Manager Initiated");
            Thread.sleep(5000);
            siddhiAppRuntime.shutdown();

        } catch (Throwable t) {
            log.error("Error when processing events" + t);
        }

        return getBValues(new BInteger(1));
    }

    static class EventCallBack extends QueryCallback {

        @Override
        public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
            StringBuilder sb = new StringBuilder();
            sb.append("Events{ @timestamp = ").append(timeStamp).append(", inEvents = ").
                    append(Arrays.deepToString(inEvents)).append(", RemoveEvents = ").
                    append(Arrays.deepToString(removeEvents)).append(" }");
            System.out.printf(sb.toString());
        }
    }


}
