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

type City record {|
    string name;
    string city;
|};

type PetByAge record {
    int age;
    string nickname?;
};

type PetByType record {
    "Cat"|"Dog" pet_type;
    boolean hunts?;
};

type Pet PetByAge|PetByType;

type Movie record {
    string title;
    int[]|float[] year;
};

configurable decimal|string decimalVar = ?;
configurable float|int floatVar = ?;
configurable float|decimal|string unionVar1 = ?;

configurable xml:Element|int xmlVar = ?;
configurable string|boolean stringVar = ?;
configurable xml:Comment|string|boolean unionVar2 = ?;

configurable map<anydata>[]|int mapArrayVar = ?;
configurable table<map<anydata>>|int tableVar = ?;
configurable table<map<anydata>>|map<anydata>[]|int unionVar3 = ?;

configurable map<string>|City city = ?;
configurable Pet pet = ?;
configurable Pet dog = {"age": 4, "nickname": "Fido", "hunts": true};
configurable string[]|int[]|decimal[] arrayUnionVal = ?;
configurable int[]|[float, int] arrTupleUnionVal = ?;
configurable int[]|[decimal, int] arrTupleUnionVal2 = [1.2d, 0];
configurable map<int[]|decimal[]> mapUnionVal = {a: [1.1, 2], b: [2.1, 3, 4]};
configurable float|int|[string, int] unionVal1 = ?;

configurable Movie[]|table<map<anydata>> movies = ?;
configurable Movie[]|table<map<anydata>> films = table [
    {
        title: "Inception",
        year: [2010.0d, 2011.0d]
    },
    {
        title: "Alice in wonderland",
        year: [2010.0d, 2016.0d]
    },
    {
        title: "Coco",
        year: [2017.0d, 2018.0d]
    }
];
configurable Movie[]|table<map<anydata>> stories = [
    {
        title: "Inception",
        year: [2010.0, 2011.0]
    },
    {
        title: "Alice in wonderland",
        year: [2010.0, 2016.0]
    },
    {
        title: "Coco",
        year: [2017.0, 2018.0]
    }
];
configurable table<map<anydata>>|Movie[] dramas = table [
    {
        title: "Inception",
        year: [2010.0d, 2011.0d]
    },
    {
        title: "Alice in wonderland",
        year: [2010.0d, 2016.0d]
    },
    {
        title: "Coco",
        year: [2017.0d, 2018.0d]
    }
];
configurable table<map<anydata>>|Movie[] scenarios = ?;
configurable table<map<anydata>>|Movie[] events = ?;

configurable map<string|int>[]|Movie[] cartoons = [
    {
        title: "Inception",
        year: [2010.0, 2011.0]
    },
    {
        title: "Alice in wonderland",
        year: [2010.0, 2016.0]
    },
    {
        title: "Coco",
        year: [2017.0, 2018.0]
    }
];
configurable map<string|int[]>[]|Movie[] videos = ?;
configurable table<map<anydata>>|map<json>[] anime = table [
    {
        title: "Inception",
        year: [2010.0d, 2011.0d]
    },
    {
        title: "Alice in wonderland",
        year: [2010.0d, 2016.0d]
    },
    {
        title: "Coco",
        year: [2017.0d, 2018.0d]
    }
];
configurable table<map<anydata>>|map<json>[] blogs = ?;

public function test_ambiguous_union_type() {
    test:assertEquals(decimalVar, <decimal>34.56);
    test:assertEquals(floatVar, 78.91);
    test:assertEquals(unionVar1, "This is a union variable");

    test:assertEquals(xmlVar, xml `<name>Jane Doe</name>`);
    test:assertEquals(stringVar, "This is a string");
    test:assertEquals(unionVar2, true);

    test:assertEquals(mapArrayVar.toString(), "[{\"a\":\"test string\",\"b\":12,\"c\":false}]");
    test:assertEquals(tableVar.toString(), "[{\"a\":\"Hello World!\",\"b\":34,\"c\":true}]");
    test:assertEquals(unionVar3, 24);

    test:assertEquals(city, {name: "Jack", city: "Colombo"});
    test:assertEquals(city is map<string>, true);
    test:assertEquals(city is City & readonly, true);
    test:assertEquals(pet, {"age": 4, "nickname": "Fido", "pet_type": "Dog"});
    test:assertEquals(pet is Pet, true);
    test:assertEquals(pet is PetByAge, true);
    test:assertEquals(pet is PetByType, true);
    test:assertEquals(dog, {"age": 4, "nickname": "Fido", "hunts": true});
    test:assertEquals(dog is Pet, true);
    test:assertEquals(dog is PetByAge, true);
    test:assertEquals(dog is PetByType, false);
    test:assertEquals(arrayUnionVal, [1, 2, 3]);
    test:assertEquals(arrayUnionVal is string[], false);
    test:assertEquals(arrayUnionVal is int[], true);
    test:assertEquals(arrayUnionVal is decimal[], false);
    test:assertEquals(arrTupleUnionVal, [23.1, 24]);
    test:assertEquals(arrTupleUnionVal is int[], false);
    test:assertEquals(arrTupleUnionVal is [float, int], true);
    test:assertEquals(arrTupleUnionVal2, [1.2d, 0]);
    test:assertEquals(arrTupleUnionVal2 is int[], false);
    test:assertEquals(arrTupleUnionVal2 is [decimal, int], true);
    test:assertEquals(mapUnionVal, {a: <decimal[] & readonly>[1.1d, 2], b: <decimal[] & readonly>[2.1d, 3, 4]});
    test:assertEquals(mapUnionVal is map<int[]|decimal[]>, true);
    test:assertEquals(mapUnionVal is map<int[]>, false);
    test:assertEquals(mapUnionVal is map<decimal[]>, true);
    test:assertEquals(unionVal1, 23.0);
    test:assertEquals(unionVal1 is float, true);
    test:assertEquals(unionVal1 is int, false);
    test:assertEquals(unionVal1 is [string, int], false);

    var ArrayValWithInt = [
        {
            title: "Inception",
            year: [2010, 2011]
        },
        {
            title: "Alice in wonderland",
            year: [2010, 2016]
        },
        {
            title: "Coco",
            year: [2017, 2018]
        }
    ];

    var arrayValWithFloat = [
        {
            title: "Inception",
            year: [2010.0, 2011.0]
        },
        {
            title: "Alice in wonderland",
            year: [2010.0, 2016.0]
        },
        {
            title: "Coco",
            year: [2017.0, 2018.0]
        }
    ];

    var tableValWithDecimal = table [
        {
            title: "Inception",
            year: [2010.0d, 2011.0d]
        },
        {
            title: "Alice in wonderland",
            year: [2010.0d, 2016.0d]
        },
        {
            title: "Coco",
            year: [2017.0d, 2018.0d]
        }
    ];

    var tableValWithFloat = table [
        {
            title: "Inception",
            year: [2010.0, 2011.0]
        },
        {
            title: "Alice in wonderland",
            year: [2010.0, 2016.0]
        },
        {
            title: "Coco",
            year: [2017.0, 2018.0]
        }
    ];

    var tableValWithInt = table [
        {
            title: "Inception",
            year: [2010, 2011]
        },
        {
            title: "Alice in wonderland",
            year: [2010, 2016]
        },
        {
            title: "Coco",
            year: [2017, 2018]
        }
    ];

    test:assertEquals(movies, ArrayValWithInt);
    test:assertEquals(movies is Movie[], true);
    test:assertEquals(movies is table<map<anydata>>, false);
    test:assertEquals(films, tableValWithDecimal);
    test:assertEquals(films is Movie[], false);
    test:assertEquals(films is table<map<anydata>>, true);
    test:assertEquals(stories, arrayValWithFloat);
    test:assertEquals(stories is Movie[], true);
    test:assertEquals(stories is table<map<anydata>>, false);
    test:assertEquals(dramas, tableValWithDecimal);
    test:assertEquals(dramas is Movie[], false);
    test:assertEquals(dramas is table<map<anydata>>, true);
    test:assertEquals(scenarios, tableValWithFloat);
    test:assertEquals(scenarios is Movie[], false);
    test:assertEquals(scenarios is table<map<anydata>>, true);
    test:assertEquals(events, tableValWithFloat);
    test:assertEquals(events is Movie[], false);
    test:assertEquals(events is table<map<anydata>>, true);

    test:assertEquals(cartoons, arrayValWithFloat);
    test:assertEquals(cartoons is map<string|int>[], false);
    test:assertEquals(cartoons is Movie[], true);
    test:assertEquals(videos, ArrayValWithInt);
    test:assertEquals(videos is map<string|int[]>[], true);
    test:assertEquals(videos is Movie[], true);
    test:assertEquals(anime, tableValWithDecimal);
    test:assertEquals(anime is table<map<anydata>>, true);
    test:assertEquals(anime is map<json>[], false);
    test:assertEquals(blogs, tableValWithInt);
    test:assertEquals(blogs is table<map<anydata>>, true);
    test:assertEquals(blogs is map<json>[], false);
}
