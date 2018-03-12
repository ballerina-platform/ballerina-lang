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

package org.ballerinalang.siddhi.query.api.util;

import org.ballerinalang.siddhi.query.api.exception.SiddhiAppContextException;

/**
 * Util for processing Siddhi Exceptions.
 */
public class ExceptionUtil {

    public static String getMessageWithContext(Throwable t, String siddhiAppName, String siddhiApp) {

        if (t instanceof SiddhiAppContextException) {
            ((SiddhiAppContextException) t).setQueryContextIndexIfAbsent(null,
                    null, siddhiAppName, siddhiApp);
            return t.getMessage();
        } else {
            return getMessageWithContext(siddhiAppName, null,
                    null, null, t.getMessage());
        }
    }

    public static String getMessageWithContext(String siddhiAppName, int[] queryContextStartIndex,
                                               int[] queryContextEndIndex, String siddhiAppPortion, String message) {
        if (siddhiAppName != null) {
            if (queryContextStartIndex != null && queryContextEndIndex != null) {
                if (siddhiAppPortion != null) {
                    if (message != null) {
                        return "Error on '" + siddhiAppName + "' @ Line: " + queryContextEndIndex[0] +
                                ". Position: " + queryContextEndIndex[1] + ", near '" + siddhiAppPortion + "'. " +
                                message;
                    } else {
                        return "Error on '" + siddhiAppName + "' @ Line: " + queryContextEndIndex[0] +
                                ". Position: " + queryContextEndIndex[1] + ", near '" + siddhiAppPortion + "'.";
                    }
                } else {
                    if (message != null) {
                        return "Error on '" + siddhiAppName + "' between @ Line: " + queryContextStartIndex[0] +
                                ". Position: " + queryContextStartIndex[1] + " and @ Line: " + queryContextEndIndex[0] +
                                ". Position: " + queryContextEndIndex[1] + ". " + message;
                    } else {
                        return "Error on '" + siddhiAppName + "' between @ Line: " + queryContextStartIndex[0] +
                                ". Position: " + queryContextStartIndex[1] + " and @ Line: " + queryContextEndIndex[0] +
                                ". Position: " + queryContextEndIndex[1] + ".";
                    }
                }
            } else {
                if (siddhiAppPortion != null) {
                    if (message != null) {
                        return "Error on '" + siddhiAppName + "' near '" + siddhiAppPortion + "'. " + message;
                    } else {
                        return "Error on '" + siddhiAppName + "' near '" + siddhiAppPortion + "'.";
                    }
                } else {
                    if (message != null) {
                        return "Error on '" + siddhiAppName + "'. " + message;
                    } else {
                        return "Error on '" + siddhiAppName + "'.";
                    }
                }
            }
        } else {
            if (queryContextStartIndex != null && queryContextEndIndex != null) {
                if (siddhiAppPortion != null) {
                    if (message != null) {
                        return "Error @ Line: " + queryContextEndIndex[0] +
                                ". Position: " + queryContextEndIndex[1] + ", near '" + siddhiAppPortion + "'. " +
                                message;
                    } else {
                        return "Error @ Line: " + queryContextEndIndex[0] +
                                ". Position: " + queryContextEndIndex[1] + ", near '" + siddhiAppPortion + "'.";
                    }
                } else {
                    if (message != null) {
                        return "Error between @ Line: " + queryContextStartIndex[0] +
                                ". Position: " + queryContextStartIndex[1] + " and @ Line: " + queryContextEndIndex[0] +
                                ". Position: " + queryContextEndIndex[1] + ". " + message;
                    } else {
                        return "Error between @ Line: " + queryContextStartIndex[0] +
                                ". Position: " + queryContextStartIndex[1] + " and @ Line: " + queryContextEndIndex[0] +
                                ". Position: " + queryContextEndIndex[1] + ".";
                    }
                }
            } else {
                if (siddhiAppPortion != null) {
                    if (message != null) {
                        return "Error near '" + siddhiAppPortion + "'. " + message;
                    } else {
                        return "Error near '" + siddhiAppPortion + "'.";
                    }
                } else {
                    return message;
                }
            }

        }
    }

    public static String getContext(int[] startIndex, int[] endIndex, String siddhiApp) {
        int startLinePosition = ordinalIndexOf(siddhiApp, "\n", startIndex[0]);
        int endLinePosition = ordinalIndexOf(siddhiApp, "\n", endIndex[0]);
        return siddhiApp.substring(startLinePosition + startIndex[1], endLinePosition +
                endIndex[1]);
    }

    private static int ordinalIndexOf(String str, String substr, int n) {
        int pos = 0;
        while (--n > 0) {
            pos = str.indexOf(substr, pos) + 1;
        }
        return pos;
    }


}
