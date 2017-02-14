/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.completion;

public class BallerinaCompletionUtil {

    public static final int KEYWORD_PRIORITY = 20;
    public static final int CONTEXT_KEYWORD_PRIORITY = 25;
    public static final int VALUE_TYPE_PRIORITY = 30;
    public static final int REFERENCE_TYPE_PRIORITY = 30;
    public static final int PACKAGE_PRIORITY = 35;
    public static final int CONNECTOR_PRIORITY = 35;
    public static final int FUNCTION_PRIORITY = 35;
    public static final int STRUCT_PRIORITY = 35;
    public static final int VARIABLE_PRIORITY = 35;

    private BallerinaCompletionUtil() {

    }
}
