// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

string 'ƮέŞŢ_String_\ \/\:\@\[\`\{\~\u{2324} = "value";
any 'ƮέŞŢ_Any_\ \/\:\@\[\`\{\~\u{2324} = 88343;
Person 'ƮέŞŢ_Person_\ \/\:\@\[\`\{\~\u{2324} =
{'1st_name: "Harry", '\ \/\:\@\[\`\{\~\u{2324}_last_name:"potter", 'Ȧɢέ: 25};


public function 'get_String_\ \/\:\@\[\`\{\~\u{2324}_ƮέŞŢ() returns string {
    return 'ƮέŞŢ_String_\ \/\:\@\[\`\{\~\u{2324};
}

public function 'get_Variable_\ \/\:\@\[\`\{\~\u{2324}_ƮέŞŢ() returns any {
    return 'ƮέŞŢ_Any_\ \/\:\@\[\`\{\~\u{2324};
}

public function 'get_Person_\ \/\:\@\[\`\{\~\u{2324}_ƮέŞŢ() returns Person {
    return 'ƮέŞŢ_Person_\ \/\:\@\[\`\{\~\u{2324};
}

public type Person record {
    string '1st_name;
    string '\ \/\:\@\[\`\{\~\u{2324}_last_name;
    int 'Ȧɢέ;
};
