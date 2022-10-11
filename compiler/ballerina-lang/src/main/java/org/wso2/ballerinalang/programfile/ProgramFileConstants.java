/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerinalang.programfile;

/**
 * @since 0.94
 */
@Deprecated
public class ProgramFileConstants {

    public static final int MAGIC_NUMBER = 0xBA1DA4CE;
    public static final short VERSION_NUMBER = 50;
    public static final int BIR_VERSION_NUMBER = 68;
    public static final short MIN_SUPPORTED_VERSION = 68;
    public static final short MAX_SUPPORTED_VERSION = 68;

    // todo move this to a proper place
    public static final String[] SUPPORTED_PLATFORMS = {"java11"};
    public static final String ANY_PLATFORM = "any";
}
