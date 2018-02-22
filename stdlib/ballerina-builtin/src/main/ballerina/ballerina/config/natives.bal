// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package ballerina.config;

@Description { value:"Retrieve the specified global configuration value" }
@Param { value:"property: The configuration to be retrieved" }
@Return { value:"Configuration value of property" }
public native function getGlobalValue(string property)(string);

@Description { value:"Retrieve the specified configuration value for the named instance" }
@Param { value:"instanceId: The ID of the instance" }
@Param { value:"property: The configuration to be retrieved" }
@Return { value:"Configuration value of the property of the instance denoted by instanceId" }
public native function getInstanceValue(string instanceId, string property)(string);
