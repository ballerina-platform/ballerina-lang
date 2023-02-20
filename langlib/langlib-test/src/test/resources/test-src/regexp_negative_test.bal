// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public function testNegativeIndexFind() returns error? {
    string:RegExp r = re `e`;
    _ = check (trap r.find("Hello", -3));
}

public function testInvalidIndexFind() returns error? {
    string:RegExp r = re `r`;
    _ = check (trap r.find("World", 12));
}

public function testLongIndexFind() returns error? {
    string:RegExp r = re `r`;
    _ = check (trap r.find("World", 68719476704));
}

public function testNegativeIndexFindAll() returns error? {
    string:RegExp r = re `/(g|ng)/gim`;
    string phrase = "There once was a king who liked to sing in the rain";
    _ = check (trap r.findAll(phrase, -8));
}

public function testInvalidIndexFindAll() returns error? {
    string:RegExp r = re `/(op|p)/gim`;
    string phrase = "Don't stoP going up till you get to the tOp if you want to shop";
    _ = check (trap r.findAll(phrase, 112));
}

public function testLongIndexFindAll() returns error? {
    string:RegExp r = re `/(ab|c)/gim`;
    string phrase = "Don't stoP going up till you get to the tOp if you want to shop";
    _ = check (trap r.findAll(phrase, 137438953408));
}

public function testNegativeIndexFindGroups() returns error? {
    string:RegExp r = re `([bB].tt[a-z]*)`;
    _ = check (trap r.findGroups("Butter was bought by Betty but the butter was bitter", -3));
}

public function testInvalidIndexFindGroups() returns error? {
    string:RegExp r = re `([bB].tt[a-z]*)`;
    _ = check (trap r.findGroups("Butter was bought by Betty but the butter was bitter", 97));
}

public function testLongIndexFindGroups() returns error? {
    string:RegExp r = re `([aA].ee[A-Z]*)`;
    _ = check (trap r.findGroups("Butter was bought by Betty but the butter was bitter", 274877906816));
}

public function testNegativeIndexFindAllGroups() returns error? {
    string:RegExp r = re `(([a-z]u)(bble))`;
    _ = check (trap r.findAllGroups("rubble, trouble, bubble, hubble", -4));
}

public function testInvalidIndexFindAllGroups() returns error? {
    string:RegExp r = re `(([a-z]u)(bble))`;
    _ = check (trap r.findAllGroups("rubble, trouble, bubble, hubble", 123));
}

public function testLongIndexFindAllGroups() returns error? {
    string:RegExp r = re `(([0-9A-Z]u)+(ful))`;
    _ = check (trap r.findAllGroups("rubble, trouble, bubble, hubble", 549755813632));
}
