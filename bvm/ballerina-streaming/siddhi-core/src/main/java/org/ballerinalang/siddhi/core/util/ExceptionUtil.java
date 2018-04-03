/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.siddhi.core.util;

import org.ballerinalang.siddhi.core.config.SiddhiAppContext;
import org.ballerinalang.siddhi.core.exception.SiddhiAppCreationException;
import org.ballerinalang.siddhi.query.api.SiddhiElement;
import org.ballerinalang.siddhi.query.api.exception.SiddhiAppContextException;

/**
 * Util class to handling Siddhi exceptions.
 */
public class ExceptionUtil {

    public static void populateQueryContext(Throwable t, SiddhiElement siddhiElement,
                                            SiddhiAppContext siddhiAppContext) {
        if (siddhiElement != null) {
            if (siddhiAppContext != null) {
                if (t instanceof SiddhiAppContextException) {
                    ((SiddhiAppContextException) t).setQueryContextIndexIfAbsent(
                            siddhiElement.getQueryContextStartIndex(),
                            siddhiElement.getQueryContextEndIndex(), siddhiAppContext.getName(),
                            siddhiAppContext.getSiddhiAppString());
                } else {
                    throw new SiddhiAppCreationException(t.getMessage(), t, siddhiElement.getQueryContextStartIndex(),
                            siddhiElement.getQueryContextEndIndex(), siddhiAppContext.getName(),
                            siddhiAppContext.getSiddhiAppString());
                }
            } else {
                if (t instanceof SiddhiAppContextException) {
                    ((SiddhiAppContextException) t).setQueryContextIndexIfAbsent(
                            siddhiElement.getQueryContextStartIndex(),
                            siddhiElement.getQueryContextEndIndex(), null, null);
                } else {
                    throw new SiddhiAppCreationException(t.getMessage(), t, siddhiElement.getQueryContextStartIndex(),
                            siddhiElement.getQueryContextEndIndex(), null, null);
                }
            }
        } else {
            if (siddhiAppContext != null) {
                if (t instanceof SiddhiAppContextException) {
                    ((SiddhiAppContextException) t).setQueryContextIndexIfAbsent(
                            null, null, siddhiAppContext.getName(),
                            siddhiAppContext.getSiddhiAppString());
                } else {
                    throw new SiddhiAppCreationException(t.getMessage(), t, null, null, siddhiAppContext.getName(),
                            siddhiAppContext.getSiddhiAppString());
                }
            } else {
                if (!(t instanceof SiddhiAppContextException)) {
                    throw new SiddhiAppCreationException(t.getMessage(), t, null, null, null, null);
                }
            }
        }
    }

    public static String getMessageWithContext(Throwable throwable, SiddhiAppContext siddhiAppContext) {

        return org.ballerinalang.siddhi.query.api.util.ExceptionUtil.getMessageWithContext(throwable,
                siddhiAppContext.getName(), siddhiAppContext.getSiddhiAppString());
    }


}
