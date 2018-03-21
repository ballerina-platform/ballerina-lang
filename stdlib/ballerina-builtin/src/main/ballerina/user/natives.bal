// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package ballerina.user;

import ballerina/util;

@Description {value:"Returns the current user's country."}
@Return { value:"Current user's country if it can be determined, an empty string otherwise"}
public native function getCountry () returns (string);

@Description {value:"Returns the current user's home directory path."}
@Return { value:"Current user's home directory if it can be determined, an empty string otherwise"}
public native function getHome () returns (string);

@Description {value:"Returns the current user's language."}
@Return { value:"Current user's language if it can be determined, an empty string otherwise"}
public native function getLanguage () returns (string);

@Description {value:"Returns the current user's name."}
@Return { value:"Current user's name if it can be determined, an empty string otherwise"}
public native function getName () returns (string);

@Description {value:"Returns the current user's locale."}
@Return { value:"Current user's locale"}
public native function getLocale () returns (util:Locale);
