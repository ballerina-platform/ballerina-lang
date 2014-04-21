/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.extension.holder;

import org.wso2.siddhi.core.config.SiddhiContext;
import org.wso2.siddhi.core.query.processor.window.WindowProcessor;

public class WindowExtensionHolder extends AbstractExtensionHolder {
    private static WindowExtensionHolder instance;

    private WindowExtensionHolder(SiddhiContext siddhiContext) {
        super(WindowProcessor.class, siddhiContext);
    }

    public static WindowExtensionHolder getInstance(SiddhiContext siddhiContext) {
        if (instance == null) {
            instance = new WindowExtensionHolder(siddhiContext);
        }
        return instance;
    }
}
