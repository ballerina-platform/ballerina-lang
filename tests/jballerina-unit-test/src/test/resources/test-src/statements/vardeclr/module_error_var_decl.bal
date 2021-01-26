//  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
//  WSO2 Inc. licenses this file to you under the Apache License,
//  Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

type userDefinedError error <basicErrorDetail>;
type basicErrorDetail record {
    int basicErrorNo?;
};

type userDefinedError2 error<userDefinedErrorDetail2>;
type userDefinedErrorDetail2 record {
    userDefinedError errorVar?;
};

userDefinedError2 error (message5, errorVar = error (message6, basicErrorNo = detail6)) =
                            error userDefinedError2("error message five", errorVar = error userDefinedError("error message six", basicErrorNo = 7));

public function testBasic() {

}