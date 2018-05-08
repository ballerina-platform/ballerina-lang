// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

documentation {
    Representation of readonly annotation.
}
public annotation<type> readonly;

documentation {
    Representation of final annotation.
}
public annotation<type> final;

documentation {
    Denote that the parameter is security sensitive hence tainted data should not be accepted.
}
public annotation<type, parameter> sensitive;

documentation {
    Denote that the return value is tainted.
}
public annotation<type, parameter> tainted;

documentation {
    Denote that the return value is untainted.
}
public annotation<type, parameter> untainted;
