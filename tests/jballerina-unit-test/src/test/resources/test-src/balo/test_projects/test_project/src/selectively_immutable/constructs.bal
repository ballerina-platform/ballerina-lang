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

import ballerina/lang.'xml;

public function getXmlComments() returns ['xml:Comment, 'xml:Comment] {
    'xml:Comment & readonly a = xml `<!--I'm a comment-->`;
    'xml:Comment b = xml `<!--I'm another comment-->`;
    return [a, b];
}

public function getXmlPIs() returns ['xml:ProcessingInstruction, 'xml:ProcessingInstruction] {
    'xml:ProcessingInstruction & readonly a = xml `<?pi a="1234" bc="def"?>`;
    'xml:ProcessingInstruction b = xml `<?pi a="4567" bc="def"?>`;
    return [a, b];
}

public function getXmlElements() returns ['xml:Element, 'xml:Element] {
    'xml:Element & readonly a = xml `<Student><name>Emma</name><id>6040</id></Student>`;
    'xml:Element b = xml `<Student><name>Jo</name><id>6040</id></Student>`;
    return [a, b];
}

public type Employee record {|
    Details details;
    string department;
|};

public type Details record {|
    string name;
    int id;
|};

public function getSelectivelyImmutableListTypes() returns [any, any, any, any] {
    int[] & readonly a = [1, 2];
    int[] b = [1, 2];

    Employee & readonly emp = {
        details: {
            name: "Emma",
            id: 1234
        },
        department: "finance"
    };

    [Employee, Employee] & readonly c = [emp, {details: {name: "Jo", id: 5678}, department: "IT"}];
    [Employee, Employee] d = [{details: {name: "Amy", id: 1234}, department: "IT"},
                              {details: {name: "Jo", id: 5678}, department: "IT"}];

    return [a, b, c, d];
}

public function getSelectivelyImmutableMappingTypes() returns [any, any, any, any, any] {
    boolean bool = false;

    map<boolean> & readonly a = {
        a: true,
        bool,
        c: false
    };

    map<int> b = {
        a: 1,
        b: 2
    };

    Employee & readonly c = {
        details: {
            name: "Emma",
            id: 1234
        },
        department: "finance"
    };

    Employee d = {
        details: {
            name: "Mary",
            id: 4567
        },
        department: "IT"
    };

    Student & readonly e = {
        details: {
            name: "Jo",
            id: 4567
        },
        "math": ["P", 75],
        "science": ["P", 65]
    };
    return [a, b, c, d, e];
}

public type Identifier record {|
    readonly string name;
    int id;
|};

public type RESULT "P"|"F";

public type Student record {|
    Details details;
    [RESULT, int] math?;
    [RESULT, int]...;
|};

public type Owner object {
    public function getId() returns int;
};

public class OwnerA {
    final int id;

    public function init(int id) {
        self.id = id;
    }

    public function getId() returns int {
        return self.id;
    }
}

public class OwnerB {
    final int id;
    string name;

    public function init(int id, string name) {
        self.id = id;
        self.name = name;
    }

    public function getId() returns int {
        return 2 * self.id;
    }
}

public type MixedRecord record {|
    'xml:Comment & readonly a;
    'xml:Comment b;
    'xml:ProcessingInstruction & readonly c;
    'xml:ProcessingInstruction d;
    'xml:Element & readonly e;
    'xml:Element f;
    'xml:Text g;
    xml h;

    json[] & readonly i;
    int[] j;
    [Identifier, int] & readonly k;
    [string, float] l;

    Student & readonly m;
    Details n;
    map<string> & readonly o;
    map<json> p;

    table<Identifier> key(name) & readonly q;
    table<map<string>> r;

    Owner & readonly s;
    Owner t;
|};

public function getMixedRecord() returns MixedRecord {
    table<Identifier> key(name) & readonly q = table [
                                                  {name: "Jo", id: 4567},
                                                  {name: "Emma", id: 1234},
                                                  {name: "Amy", id: 678}
                                               ];
    table<map<string>> rVal = table [
                                 {x: "x", y: "y"},
                                 {z: "z"}
                              ];

    MixedRecord mr = {
        a: xml `<!--Comment 1-->`,
        b: xml `<!--Comment 2-->`,
        c: xml `<?pi a="1234" bc="def"?>`,
        d: xml `<?pi b="342" a="qwe"?>`,
        e: xml `<Student><name>Emma</name><id>6040</id></Student>`,
        f: xml `<Student><name>Jo</name><id>1234</id></Student>`,
        g: xml `Text value`,
        h: xml `<name>Jo</name>`,
        i: [{a: 1, b: "str"}, true],
        j: [1, 2, 3],
        k: [{name: "Maryam", id: 1234}, 20],
        l: ["Brad", 20],
        m: {details: {name: "Amy", id: 1234}, "math": ["P", 80]},
        n: {name: "Kim", id: 234789},
        o: {a: "123", b: "234", "c": "name"},
        p: {a: 1, b: true, c: "name"},
        q,
        r: rVal,
        s: new OwnerA(222),
        t: new OwnerB(1234, "Rob")
    };
    return mr;
}

public type ReadOnlyStudent readonly & Student;

public type AB "A"|"B";

public type Config object {
    public string name;

    public function getName() returns string;
};

public class MyConfig {
    public final string name;

    public function init(string name) {
        self.name = name;
    }

    public function getName() returns string {
        return self.name;
    }
}

public function getImmutableConfig() returns Config & readonly {
    return new MyConfig("client config");
}

public type Versioning record {|
    string pattern = "v{major}.{minor}";
    boolean allow = true;
    boolean matchMajor = false;
|};
