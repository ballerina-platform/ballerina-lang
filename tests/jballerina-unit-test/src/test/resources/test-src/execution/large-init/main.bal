// Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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
import large_init.mod as _;

int count = 0;

listener Listener listener0 = new ("listener0");
listener Listener listener1 = new ("listener1");
listener Listener listener2 = new ("listener2");
listener Listener listener3 = new ("listener3");
listener Listener listener4 = new ("listener4");
listener Listener listener5 = new ("listener5");
listener Listener listener6 = new ("listener6");
listener Listener listener7 = new ("listener7");
listener Listener listener8 = new ("listener8");
listener Listener listener9 = new ("listener9");
listener Listener listener10 = new ("listener10");
listener Listener listener11 = new ("listener11");
listener Listener listener12 = new ("listener12");
listener Listener listener13 = new ("listener13");
listener Listener listener14 = new ("listener14");
listener Listener listener15 = new ("listener15");
listener Listener listener16 = new ("listener16");
listener Listener listener17 = new ("listener17");
listener Listener listener18 = new ("listener18");
listener Listener listener19 = new ("listener19");
listener Listener listener20 = new ("listener20");
listener Listener listener21 = new ("listener21");
listener Listener listener22 = new ("listener22");
listener Listener listener23 = new ("listener23");
listener Listener listener24 = new ("listener24");
listener Listener listener25 = new ("listener25");
listener Listener listener26 = new ("listener26");
listener Listener listener27 = new ("listener27");
listener Listener listener28 = new ("listener28");
listener Listener listener29 = new ("listener29");
listener Listener listener30 = new ("listener30");
listener Listener listener31 = new ("listener31");
listener Listener listener32 = new ("listener32");
listener Listener listener33 = new ("listener33");
listener Listener listener34 = new ("listener34");
listener Listener listener35 = new ("listener35");
listener Listener listener36 = new ("listener36");
listener Listener listener37 = new ("listener37");
listener Listener listener38 = new ("listener38");
listener Listener listener39 = new ("listener39");
listener Listener listener40 = new ("listener40");
listener Listener listener41 = new ("listener41");
listener Listener listener42 = new ("listener42");
listener Listener listener43 = new ("listener43");
listener Listener listener44 = new ("listener44");
listener Listener listener45 = new ("listener45");
listener Listener listener46 = new ("listener46");
listener Listener listener47 = new ("listener47");
listener Listener listener48 = new ("listener48");
listener Listener listener49 = new ("listener49");
listener Listener listener50 = new ("listener50");
listener Listener listener51 = new ("listener51");
listener Listener listener52 = new ("listener52");
listener Listener listener53 = new ("listener53");
listener Listener listener54 = new ("listener54");
listener Listener listener55 = new ("listener55");
listener Listener listener56 = new ("listener56");
listener Listener listener57 = new ("listener57");
listener Listener listener58 = new ("listener58");
listener Listener listener59 = new ("listener59");
listener Listener listener60 = new ("listener60");
listener Listener listener61 = new ("listener61");
listener Listener listener62 = new ("listener62");
listener Listener listener63 = new ("listener63");
listener Listener listener64 = new ("listener64");
listener Listener listener65 = new ("listener65");
listener Listener listener66 = new ("listener66");
listener Listener listener67 = new ("listener67");
listener Listener listener68 = new ("listener68");
listener Listener listener69 = new ("listener69");
listener Listener listener70 = new ("listener70");
listener Listener listener71 = new ("listener71");
listener Listener listener72 = new ("listener72");
listener Listener listener73 = new ("listener73");
listener Listener listener74 = new ("listener74");
listener Listener listener75 = new ("listener75");
listener Listener listener76 = new ("listener76");
listener Listener listener77 = new ("listener77");
listener Listener listener78 = new ("listener78");
listener Listener listener79 = new ("listener79");
listener Listener listener80 = new ("listener80");
listener Listener listener81 = new ("listener81");
listener Listener listener82 = new ("listener82");
listener Listener listener83 = new ("listener83");
listener Listener listener84 = new ("listener84");
listener Listener listener85 = new ("listener85");
listener Listener listener86 = new ("listener86");
listener Listener listener87 = new ("listener87");
listener Listener listener88 = new ("listener88");
listener Listener listener89 = new ("listener89");
listener Listener listener90 = new ("listener90");
listener Listener listener91 = new ("listener91");
listener Listener listener92 = new ("listener92");
listener Listener listener93 = new ("listener93");
listener Listener listener94 = new ("listener94");
listener Listener listener95 = new ("listener95");
listener Listener listener96 = new ("listener96");
listener Listener listener97 = new ("listener97");
listener Listener listener98 = new ("listener98");
listener Listener listener99 = new ("listener99");

TestEP globEP0 = new;
TestEP globEP1 = new;
TestEP globEP2 = new;
TestEP globEP3 = new;
TestEP globEP4 = new;
TestEP globEP5 = new;
TestEP globEP6 = new;
TestEP globEP7 = new;
TestEP globEP8 = new;
TestEP globEP9 = new;
TestEP globEP10 = new;
TestEP globEP11 = new;
TestEP globEP12 = new;
TestEP globEP13 = new;
TestEP globEP14 = new;
TestEP globEP15 = new;
TestEP globEP16 = new;
TestEP globEP17 = new;
TestEP globEP18 = new;
TestEP globEP19 = new;
TestEP globEP20 = new;
TestEP globEP21 = new;
TestEP globEP22 = new;
TestEP globEP23 = new;
TestEP globEP24 = new;
TestEP globEP25 = new;
TestEP globEP26 = new;
TestEP globEP27 = new;
TestEP globEP28 = new;
TestEP globEP29 = new;
TestEP globEP30 = new;
TestEP globEP31 = new;
TestEP globEP32 = new;
TestEP globEP33 = new;
TestEP globEP34 = new;
TestEP globEP35 = new;
TestEP globEP36 = new;
TestEP globEP37 = new;
TestEP globEP38 = new;
TestEP globEP39 = new;
TestEP globEP40 = new;
TestEP globEP41 = new;
TestEP globEP42 = new;
TestEP globEP43 = new;
TestEP globEP44 = new;
TestEP globEP45 = new;
TestEP globEP46 = new;
TestEP globEP47 = new;
TestEP globEP48 = new;
TestEP globEP49 = new;
TestEP globEP50 = new;
TestEP globEP51 = new;
TestEP globEP52 = new;
TestEP globEP53 = new;
TestEP globEP54 = new;
TestEP globEP55 = new;
TestEP globEP56 = new;
TestEP globEP57 = new;
TestEP globEP58 = new;
TestEP globEP59 = new;
TestEP globEP60 = new;
TestEP globEP61 = new;
TestEP globEP62 = new;
TestEP globEP63 = new;
TestEP globEP64 = new;
TestEP globEP65 = new;
TestEP globEP66 = new;
TestEP globEP67 = new;
TestEP globEP68 = new;
TestEP globEP69 = new;
TestEP globEP70 = new;
TestEP globEP71 = new;
TestEP globEP72 = new;
TestEP globEP73 = new;
TestEP globEP74 = new;
TestEP globEP75 = new;
TestEP globEP76 = new;
TestEP globEP77 = new;
TestEP globEP78 = new;
TestEP globEP79 = new;
TestEP globEP80 = new;
TestEP globEP81 = new;
TestEP globEP82 = new;
TestEP globEP83 = new;
TestEP globEP84 = new;
TestEP globEP85 = new;
TestEP globEP86 = new;
TestEP globEP87 = new;
TestEP globEP88 = new;
TestEP globEP89 = new;
TestEP globEP90 = new;
TestEP globEP91 = new;
TestEP globEP92 = new;
TestEP globEP93 = new;
TestEP globEP94 = new;
TestEP globEP95 = new;
TestEP globEP96 = new;
TestEP globEP97 = new;
TestEP globEP98 = new;
TestEP globEP99 = new;

public class Listener {

    private string name = "";

    public isolated function init(string name) returns error? {
        self.name = name;
    }

    public isolated function attach(service object {} s, string[]|string? name = ()) returns error? {
    }

    public isolated function detach(service object {} s) returns error? {
    }

    public isolated function 'start() returns error? {
    }

    public isolated function gracefulStop() returns error? {
    }

    public isolated function immediateStop() returns error? {
    }
}

client class TestEP {
    remote function action1(string s, int i) returns boolean {
        return false;
    }

    remote function action2(string s, boolean b) returns int {
        return 10;
    }

    function func() {
        count = count + 1;
    }

    function func1(string s) {
    }

    function func2(string s) returns int {
        return 5;
    }
}

function init() returns error? {
    check listener0.'start();
    globEP0.func();
    check listener1.'start();
    globEP1.func();
    check listener2.'start();
    globEP2.func();
    check listener3.'start();
    globEP3.func();
    check listener4.'start();
    globEP4.func();
    check listener5.'start();
    globEP5.func();
    check listener6.'start();
    globEP6.func();
    check listener7.'start();
    globEP7.func();
    check listener8.'start();
    globEP8.func();
    check listener9.'start();
    globEP9.func();
    check listener10.'start();
    globEP10.func();
    check listener11.'start();
    globEP11.func();
    check listener12.'start();
    globEP12.func();
    check listener13.'start();
    globEP13.func();
    check listener14.'start();
    globEP14.func();
    check listener15.'start();
    globEP15.func();
    check listener16.'start();
    globEP16.func();
    check listener17.'start();
    globEP17.func();
    check listener18.'start();
    globEP18.func();
    check listener19.'start();
    globEP19.func();
    check listener20.'start();
    globEP20.func();
    check listener21.'start();
    globEP21.func();
    check listener22.'start();
    globEP22.func();
    check listener23.'start();
    globEP23.func();
    check listener24.'start();
    globEP24.func();
    check listener25.'start();
    globEP25.func();
    check listener26.'start();
    globEP26.func();
    check listener27.'start();
    globEP27.func();
    check listener28.'start();
    globEP28.func();
    check listener29.'start();
    globEP29.func();
    check listener30.'start();
    globEP30.func();
    check listener31.'start();
    globEP31.func();
    check listener32.'start();
    globEP32.func();
    check listener33.'start();
    globEP33.func();
    check listener34.'start();
    globEP34.func();
    check listener35.'start();
    globEP35.func();
    check listener36.'start();
    globEP36.func();
    check listener37.'start();
    globEP37.func();
    check listener38.'start();
    globEP38.func();
    check listener39.'start();
    globEP39.func();
    check listener40.'start();
    globEP40.func();
    check listener41.'start();
    globEP41.func();
    check listener42.'start();
    globEP42.func();
    check listener43.'start();
    globEP43.func();
    check listener44.'start();
    globEP44.func();
    check listener45.'start();
    globEP45.func();
    check listener46.'start();
    globEP46.func();
    check listener47.'start();
    globEP47.func();
    check listener48.'start();
    globEP48.func();
    check listener49.'start();
    globEP49.func();
    check listener50.'start();
    globEP50.func();
    check listener51.'start();
    globEP51.func();
    check listener52.'start();
    globEP52.func();
    check listener53.'start();
    globEP53.func();
    check listener54.'start();
    globEP54.func();
    check listener55.'start();
    globEP55.func();
    check listener56.'start();
    globEP56.func();
    check listener57.'start();
    globEP57.func();
    check listener58.'start();
    globEP58.func();
    check listener59.'start();
    globEP59.func();
    check listener60.'start();
    globEP60.func();
    check listener61.'start();
    globEP61.func();
    check listener62.'start();
    globEP62.func();
    check listener63.'start();
    globEP63.func();
    check listener64.'start();
    globEP64.func();
    check listener65.'start();
    globEP65.func();
    check listener66.'start();
    globEP66.func();
    check listener67.'start();
    globEP67.func();
    check listener68.'start();
    globEP68.func();
    check listener69.'start();
    globEP69.func();
    check listener70.'start();
    globEP70.func();
    check listener71.'start();
    globEP71.func();
    check listener72.'start();
    globEP72.func();
    check listener73.'start();
    globEP73.func();
    check listener74.'start();
    globEP74.func();
    check listener75.'start();
    globEP75.func();
    check listener76.'start();
    globEP76.func();
    check listener77.'start();
    globEP77.func();
    check listener78.'start();
    globEP78.func();
    check listener79.'start();
    globEP79.func();
    check listener80.'start();
    globEP80.func();
    check listener81.'start();
    globEP81.func();
    check listener82.'start();
    globEP82.func();
    check listener83.'start();
    globEP83.func();
    check listener84.'start();
    globEP84.func();
    check listener85.'start();
    globEP85.func();
    check listener86.'start();
    globEP86.func();
    check listener87.'start();
    globEP87.func();
    check listener88.'start();
    globEP88.func();
    check listener89.'start();
    globEP89.func();
    check listener90.'start();
    globEP90.func();
    check listener91.'start();
    globEP91.func();
    check listener92.'start();
    globEP92.func();
    check listener93.'start();
    globEP93.func();
    check listener94.'start();
    globEP94.func();
    check listener95.'start();
    globEP95.func();
    check listener96.'start();
    globEP96.func();
    check listener97.'start();
    globEP97.func();
    check listener98.'start();
    globEP98.func();
    check listener99.'start();
    globEP99.func();
}

public function main() {
    test:assertEquals(count, 100);
}
