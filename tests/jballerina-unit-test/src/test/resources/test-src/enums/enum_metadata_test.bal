// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

const annotation annot on source const;

enum A {
    # Doc A ONE
    ONE,
    # Doc A TWO
    TWO
}

enum B {
    # Doc B ONE
    ONE,
    # Doc B TWO
    TWO
}

enum C {
    # Doc C THREE
    THREE,
    # Doc C FOUR
    FOUR
}

enum D {
    # Doc D THREE
    THREE,
    FOUR
}

enum E {
    @annot
    FIVE,
    @annot
    SIX
}

enum F {
    @annot
    FIVE,
    @annot
    SIX
}

enum G {
    @annot
    SEVEN,
    @annot
    EIGHT
}

enum H {
    EIGHT,
    @annot
    SEVEN
}

enum I {
    # Doc I NINE
    @annot
    NINE
}

enum J {
    # Doc J NINE
    @annot
    NINE
}

enum K {
    # Doc K TEN
    TEN
}

enum L {
    @annot
    TEN,
    ELEVEN
}

enum M {
    # Docs
    @annot
    ELEVEN,
    # Docs M TWELVE
    TWELVE
}

enum N {
    # Docs N TWELVE
    @annot
    TWELVE,
    # Docs N THIRTEEN
    THIRTEEN
}

enum O {
    # Docs O TWELVE
    @annot
    TWELVE,
    THIRTEEN
}
