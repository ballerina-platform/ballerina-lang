// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# The type of stream events.
public type EventType "CURRENT"|"EXPIRED"|"ALL"|"RESET"|"TIMER";

# The types of joins between streams and tables.
public type JoinType "JOIN"|"LEFTOUTERJOIN"|"RIGHTOUTERJOIN"|"FULLOUTERJOIN";

public const OUTPUT = "OUTPUT";

# The reset event type
public const RESET = "RESET";

# The expired event type
public const EXPIRED = "EXPIRED";

# The current event type.
public const CURRENT = "CURRENT";

# The timer event type.
public const TIMER = "TIMER";

# The default key to group by if the group by clause is not used in query
public const DEFAULT = "DEFAULT";
public const DELIMITER = ".";
public const DELIMITER_REGEX = "\\.";
public const ASCENDING = "ascending";
public const DESCENDING = "descending";
