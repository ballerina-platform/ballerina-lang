/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.observe.trace.extension.choreo.model;

/**
 * Represents a Choreo Trace Span Event.
 *
 * @since 2.0.0
 */
public class SpanEvent {
    private long time;
    private String moduleID;
    private String positionID;

    public SpanEvent(long time, String moduleID, String positionID) {
        this.time = time;
        this.moduleID = moduleID;
        this.positionID = positionID;
    }

    public long getTime() {
        return time;
    }

    public String getModuleID() {
        return moduleID;
    }

    public String getPositionID() {
        return positionID;
    }
}
