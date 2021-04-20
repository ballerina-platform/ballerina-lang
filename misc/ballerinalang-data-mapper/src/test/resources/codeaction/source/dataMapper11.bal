// Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

type Mark record {|
   int maths;
   int physics;
   int chemistry;
   int...;
|};

type Grades record {|
   int maths;
   int physics;
   int history;
   int...;
|};


public function main(){
   Mark marks = {
       maths: 70,
       physics: 80,
       chemistry: 60,
       "history" : 80
   };

   Grades grades = marks;
}
