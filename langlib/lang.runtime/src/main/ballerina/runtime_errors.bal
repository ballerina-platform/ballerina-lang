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


// TypeCastError
public type TypeCastError distinct error;

// IndexOutOfRangeError
public type IndexOutOfRange distinct error;
public type IndexNumberTooLarge distinct IndexOutOfRange;
public type ArrayIndexOutOfRange distinct IndexOutOfRange;
public type TupleIndexOutOfRange distinct IndexOutOfRange;
public type StringIndexOutOfRange distinct IndexOutOfRange;

// InherentTypeViolationError
public type InherentTypeViolation distinct error;
public type IncompatibleType distinct InherentTypeViolation;
public type InherentTableTypeViolation distinct InherentTypeViolation;
public type IncompatibleTypeForJsonCasting distinct InherentTypeViolation;

// StringOperationError
public type StringOperationError distinct error;
public type InvalidSubstringRange distinct StringOperationError;

public type ArithmeticError distinct error;

// ConversionError
public type ConversionError distinct error;
public type NumberConversionError distinct ConversionError;
public type CannotConvertNil distinct ConversionError;

// KeyConstraintViolationError
public type KeyConstraintViolation distinct error;

// IllegalListInsertionError
public type IllegalListInsertion distinct error;

// KeyNotFoundError
public type KeyNotFound distinct error;

// InvalidUpdateError
public type InvalidUpdate distinct error;
public type InvalidReadonlyValueUpdate distinct InvalidUpdate;
public type InvalidFinalFieldUpdate distinct InvalidUpdate;

// OperationNotSupportedError
public type OperationNotSupported distinct error;
public type UnsupportedComparisonOperation distinct OperationNotSupported;

// UnorderedTypesError
public type UnorderedTypes distinct error;

// IteratorMutabilityError
public type IteratorMutabilityError distinct error;

// JsonOperationError
public type JSONOperationError distinct error;
public type JSONConversionError distinct JSONOperationError;

// XmlOperationError
public type XmlOperationError distinct error;
