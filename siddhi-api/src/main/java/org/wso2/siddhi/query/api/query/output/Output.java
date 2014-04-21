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
package org.wso2.siddhi.query.api.query.output;

import org.wso2.siddhi.query.api.exception.MalformedAttributeException;
import org.wso2.siddhi.query.api.expression.constant.Constant;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;
import org.wso2.siddhi.query.api.expression.constant.LongConstant;
import org.wso2.siddhi.query.api.expression.constant.TimeConstant;

public abstract class Output {

    public enum Type {
        ALL, FIRST, LAST, SNAPSHOT
    }

    public static EventOutputRate perEvents(Constant events) {
        if (events instanceof LongConstant) {
            return new EventOutputRate(((LongConstant) events).getValue().intValue());
        } else if (events instanceof IntConstant) {
            return new EventOutputRate(((IntConstant) events).getValue());
        }
        throw new MalformedAttributeException("Unsupported output event rate type, output event rate only supports int");
    }

    public static TimeOutputRate perTimePeriod(TimeConstant timeConstant) {
        return new TimeOutputRate(timeConstant.value());
    }

    public static TimeOutputRate perTimePeriod(LongConstant longConstant) {
        return new TimeOutputRate(longConstant.getValue());
    }

    public static SnapshotOutputRate perSnapshot(TimeConstant timeConstant) {
        return new SnapshotOutputRate(timeConstant.value());
    }

    public static SnapshotOutputRate perSnapshot(LongConstant longConstant) {
        return new SnapshotOutputRate(longConstant.getValue());
    }


}
