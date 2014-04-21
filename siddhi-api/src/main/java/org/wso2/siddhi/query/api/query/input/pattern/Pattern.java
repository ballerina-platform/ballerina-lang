/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.query.api.query.input.pattern;

import org.wso2.siddhi.query.api.query.input.TransformedStream;
import org.wso2.siddhi.query.api.query.input.pattern.element.CountElement;
import org.wso2.siddhi.query.api.query.input.pattern.element.FollowedByElement;
import org.wso2.siddhi.query.api.query.input.pattern.element.LogicalElement;
import org.wso2.siddhi.query.api.query.input.pattern.element.PatternElement;

public class Pattern {


    public static PatternStream every(PatternElement patternElement) {
        return new PatternStream(patternElement,null);
    }

    public static PatternElement logical(TransformedStream transformedStream1, LogicalElement.Type type,
                                         TransformedStream transformedStream2) {
        return new LogicalElement(transformedStream1, type, transformedStream2);
    }

    public static PatternElement followedBy(PatternElement patternElement,
                                            PatternElement followedByPatternElement) {
        return new FollowedByElement(patternElement, followedByPatternElement);
    }

    public static PatternElement count(TransformedStream transformedStream, int min, int max) {
        return new CountElement(transformedStream, min, max);
    }
}
