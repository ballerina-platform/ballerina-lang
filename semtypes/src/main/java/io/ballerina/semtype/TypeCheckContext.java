/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.semtype;

/**
 * TypeCheckContext node.
 *
 * @since 2.0.0
 */
public class TypeCheckContext {
    private final Env env;
    // todo: Normal hash tables should do here
    //    BddMemoTable listMemo = table [];
    //    BddMemoTable mappingMemo = table [];
    //    BddMemoTable functionMemo = table [];

    public TypeCheckContext(Env env) {
        this.env = env;
    }


//    function listAtomType(Atom atom) returns ListAtomicType {
//        if atom is RecAtom {
//            return self.env.getRecListAtomType(atom);
//        }
//        else {
//            return <ListAtomicType>atom.atomicType;
//        }
//    }
//
//    function mappingAtomType(Atom atom) returns MappingAtomicType {
//        if atom is RecAtom {
//            return self.env.getRecMappingAtomType(atom);
//        }
//        else {
//            return <MappingAtomicType>atom.atomicType;
//        }
//    }
//
//    function functionAtomType(Atom atom) returns FunctionAtomicType {
//        return self.env.getRecFunctionAtomType(<RecAtom>atom);
//    }
}
