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


@Description {value : "Supported record formats of CSV"}
public type Format "default"|"csv"|"tdf";

@Description {value : "Delimiters which will be used to seperate between records"}
public type Seperator ","|"\t"|":";

@Description {value:"Describes default format to open CSV"}
@final public Seperator COMMA = ",";

@Description {value:"Describes RFC4180 format to open CSV"}
@final public Seperator TAB = "\t";

@Description {value:"Describes TDF format to open CSV"}
@final public Seperator COLON = ":";
