// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

class OperatorMapper {
    map<OperatorKind> operatorTypeMap = {}

    public function init() {
        self.fillOperatorMapper();
    }

    function fillOperatorMapper() {
        self.operatorTypeMap = {
            ADD: PLUS_OP,
            SUB: MINUS_OP,
            DIV: DIVISION_OP,
            MUL: MULTIPLICATION_OP,
            COLON: COLON_OP,
            MOD: MOD_OP,
            LT_EQUAL: LT_EQUAL_OP,
            GT_EQUAL: GT_EQUAL_OP,
            GT: GT_OP,
            LT: LT_OP,
            EQUAL: EQUAL_OP,
            NOT_EQUAL: NOT_EQUAL_OP,
            REF_EQUAL: REF_EQUAL_OP,
            REF_NOT_EQUAL: REF_NOT_EQUAL_OP,
            UNARY_MINUS: MINUS_OP,
            UNARY_PLUS: PLUS_OP,
            NOT: NOT_OP,
            BIT_COMPLEMENT: BIT_COMPLEMENT_OP,
            UNTAINT: UNTAINT_TYPE
        };
    }
};
