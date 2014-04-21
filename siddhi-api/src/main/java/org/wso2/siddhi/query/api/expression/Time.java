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
package org.wso2.siddhi.query.api.expression;

import org.wso2.siddhi.query.api.expression.constant.TimeConstant;

public abstract class Time {


    public static TimeConstant milliSec(int i) {

        return new TimeConstant((long) i);
    }


    public static TimeConstant sec(int i) {

        return new TimeConstant(((long) i) * 1000);
    }


    public static TimeConstant minute(int i) {

        return new TimeConstant(((long) i) * 60 * 1000);
    }


    public static TimeConstant hour(int i) {

        return new TimeConstant(((long) i) * 60 * 60 * 1000);
    }


    public static TimeConstant day(int i) {

        return new TimeConstant(((long) i) * 24 * 60 * 60 * 1000);
    }

    public static TimeConstant week(int i) {

        return new TimeConstant(((long) i) * 7 * 24 * 60 * 60 * 1000);
    }

    public static TimeConstant month(int i) {

        return new TimeConstant(((long) i) * 30 * 24 * 60 * 60 * 1000);
    }

    public static TimeConstant year(int i) {

        return new TimeConstant(((long) i) * 365 * 24 * 60 * 60 * 1000);
    }

}
