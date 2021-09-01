// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/test;
import configOpenRecord.type_defs;
import testOrg/configLib.util;

configurable type_defs:IntRest intRest = ?;
configurable type_defs:FloatRest floatRest = ?;
configurable type_defs:StringRest stringRest = ?;
configurable type_defs:BooleanRest booleanRest = ?;
configurable type_defs:AnydataRest anyRest = ?;

configurable type_defs:Player player = ?;
configurable type_defs:PlayerArray playerArr = ?;
configurable type_defs:PlayerTable playerTable = ?;
configurable type_defs:PlayerMap playerMap = ?;
configurable type_defs:PlayerMapTable playerMapTable = ?;


public function testRestFields() {
    test:assertEquals(intRest.toString(), "{\"id\":11,\"age\":26}");
    test:assertEquals(floatRest.toString(), "{\"weight\":62.3,\"height\":161.5}");
    test:assertEquals(stringRest.toString(), "{\"city\":\"Colombo\",\"name\":\"Hinduja\"}");
    test:assertEquals(booleanRest.toString(), "{\"isBoolean\":true,\"isByte\":false}");
    test:assertEquals(anyRest.toString(), "{\"arrVal\":[100,200],\"intVal\":100,\"stringVal\":\"string\"," + 
    "\"floatVal\":103.507,\"mapVal\":{\"m\":\"value\",\"n\":608}}");

    test:assertEquals(player.toString(), "{\"id\":101,\"teamMate\":{\"id\":102,\"teamMate\":{\"id\":103}}}");
    test:assertEquals(playerArr.toString(), "[{\"id\":104,\"teamMate\":{\"id\":105,\"teamMate\":{\"id\":106}}}," +
    "{\"id\":107,\"teamMate\":{\"id\":108,\"teamMate\":{\"id\":109}}}]");
    test:assertEquals(playerTable.toString(), "[{\"id\":110,\"teamMate\":{\"id\":111,\"teamMate\":{\"id\":112}}}," + 
    "{\"id\":113,\"teamMate\":{\"id\":114,\"teamMate\":{\"id\":115}}}]");
    test:assertEquals(playerMap.toString(), "{\"entry1\":{\"id\":116,\"teamMate\":{\"id\":117," + 
    "\"teamMate\":{\"id\":118}}},\"entry2\":{\"id\":119}}");
    test:assertEquals(playerMapTable.toString(), "{\"entry1\":[{\"id\":120},{\"id\":121," + 
    "\"teamMate\":{\"id\":122,\"teamMate\":{\"id\":123}}}]}");

    util:testRecordIterator(intRest, 2);
    util:testRecordIterator(floatRest, 2);
    util:testRecordIterator(stringRest, 2);
    util:testRecordIterator(booleanRest, 2);
    util:testRecordIterator(anyRest, 5);
    util:testRecordIterator(player, 2);

    util:testArrayIterator(playerArr, 2);
    util:testTableIterator(playerTable);
    util:testMapIterator(playerMap, 2);
    util:testMapIterator(playerMapTable, 1);
}
