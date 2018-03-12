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
package org.ballerinalang.test.utils.debug;

import org.ballerinalang.util.debugger.dto.BreakPointDTO;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test Util class to hold debug point details.
 *
 * @since 0.966.0
 */
public class DebugPoint {

    private BreakPointDTO expBreakPoint;

    private Step nextStep;

    private AtomicInteger noOfHits;

    DebugPoint(BreakPointDTO expBreakPoint, Step nextStep, int noOfHits) {
        this.expBreakPoint = expBreakPoint;
        this.nextStep = nextStep;
        this.noOfHits = new AtomicInteger(noOfHits);
    }

    boolean match(BreakPointDTO breakPoint) {
        return breakPoint.getFileName().equals(expBreakPoint.getFileName())
                && breakPoint.getLineNumber() == expBreakPoint.getLineNumber() && noOfHits.get() > 0;
    }

    BreakPointDTO getExpBreakPoint() {
        return expBreakPoint;
    }

    Step getNextStep() {
        return nextStep;
    }

    int decrementAndGetHits() {
        return noOfHits.decrementAndGet();
    }

    @Override
    public String toString() {
        return "\t" + expBreakPoint + "\t remaining hits - " + noOfHits.get();
    }

}
