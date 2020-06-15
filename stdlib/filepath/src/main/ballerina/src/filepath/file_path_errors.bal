// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Represents error occur when the file/directory does not exist at the given filepath.
public type FileNotFoundError distinct error;

# Represents error occur when the file at the given filepath is not a symbolic link.
public type NotLinkError distinct error;

# Represents IO error occur when trying to access the file at the given filepath.
public type IOError distinct error;

# Represents security error occur when trying to access the file at the given filepath.
public type SecurityError distinct error;

# Represents error occur when the given file path is invalid.
public type InvalidPathError distinct error;

# Represent error occur when the given pattern is not a valid filepath pattern.
public type InvalidPatternError distinct error;

# Represents an error that occurs when the given target filepath cannot be derived relative to the base filepath.
public type RelativePathError distinct error;

# Represents error occur in the UNC path.
public type UNCPathError distinct error;

# Represents generic error for filepath
public type GenericError distinct error;

# Represents filepath related errors.
public type Error FileNotFoundError | NotLinkError | IOError | SecurityError | InvalidPathError |
InvalidPatternError | RelativePathError | UNCPathError | GenericError;
