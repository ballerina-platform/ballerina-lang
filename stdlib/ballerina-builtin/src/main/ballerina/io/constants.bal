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

package ballerina.io;

public type Mode "r"|"w"|"rw"|"a";

public type Seperator ","|"\t"|":";

public type Format "default"|"csv"|"tdf";

@Description {value:"Describes default format to open CSV"}
@final public Seperator COMMA = ",";

@Description {value:"Describes RFC4180 format to open CSV"}
@final public Seperator TAB = "\t";

@Description {value:"Describes TDF format to open CSV"}
@final public Seperator COLON = ":";

@Description {value:"Describes access mode for reading"}
@final public Mode READ = "r";

@Description {value:"Describes access mode for writing"}
@final public Mode WRITE = "w";

@Description {value:"Describes acces mode for reading and writing"}
@final public Mode HYBRID = "rw";

@Description {value:"Describes access mode for append"}
@final public Mode APPEND = "a";
