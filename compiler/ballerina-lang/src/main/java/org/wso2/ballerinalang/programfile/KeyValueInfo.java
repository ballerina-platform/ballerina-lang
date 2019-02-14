///*
// *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// *  WSO2 Inc. licenses this file to you under the Apache License,
// *  Version 2.0 (the "License"); you may not use this file except
// *  in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing,
// *  software distributed under the License is distributed on an
// *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// *  KIND, either express or implied.  See the License for the
// *  specific language governing permissions and limitations
// *  under the License.
// */
//
//package org.wso2.ballerinalang.programfile;
//
//import java.util.LinkedList;
//import java.util.List;
//
///**
// *
// */
//public class KeyValueInfo {
//
//    public int keyCPIndex;
//    public int originalKeyCPIndex;
//    public int keyTypeDescCPIndex;
//    public int keyTypeDescTag;
//
//    public int valueCPIndex;
//    public int originalValueCPIndex;
//    public int valueTypeDescCPIndex;
//    public int valueTypeDescTag;
//
//    public boolean isTerminal;
//
//    public List<KeyValueInfo> children;
//
//    public KeyValueInfo(int keyCPIndex, int originalKeyCPIndex, int keyTypeDescCPIndex, int keyTypeDescTag,
//                        int valueCPIndex, int originalValueCPIndex, int valueTypeDescCPIndex, int valueTypeDescTag) {
//        this.keyCPIndex = keyCPIndex;
//        this.originalKeyCPIndex = originalKeyCPIndex;
//        this.keyTypeDescCPIndex = keyTypeDescCPIndex;
//        this.keyTypeDescTag = keyTypeDescTag;
//
//        this.valueCPIndex = valueCPIndex;
//        this.originalValueCPIndex = originalValueCPIndex;
//        this.valueTypeDescCPIndex = valueTypeDescCPIndex;
//        this.valueTypeDescTag = valueTypeDescTag;
//
//        this.children = new LinkedList<>();
//    }
//}
