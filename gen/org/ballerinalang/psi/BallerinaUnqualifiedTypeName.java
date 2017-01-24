/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

// This is a generated file. Not intended for manual editing.
package org.ballerinalang.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface BallerinaUnqualifiedTypeName extends PsiElement {

  @Nullable
  BallerinaSimpleType getSimpleType();

  @Nullable
  BallerinaSimpleTypeArray getSimpleTypeArray();

  @Nullable
  BallerinaSimpleTypeIterate getSimpleTypeIterate();

  @Nullable
  BallerinaWithFullSchemaType getWithFullSchemaType();

  @Nullable
  BallerinaWithFullSchemaTypeArray getWithFullSchemaTypeArray();

  @Nullable
  BallerinaWithFullSchemaTypeIterate getWithFullSchemaTypeIterate();

  @Nullable
  BallerinaWithScheamIdTypeArray getWithScheamIdTypeArray();

  @Nullable
  BallerinaWithScheamIdTypeIterate getWithScheamIdTypeIterate();

  @Nullable
  BallerinaWithScheamURLType getWithScheamURLType();

  @Nullable
  BallerinaWithSchemaIdType getWithSchemaIdType();

  @Nullable
  BallerinaWithSchemaURLTypeArray getWithSchemaURLTypeArray();

  @Nullable
  BallerinaWithSchemaURLTypeIterate getWithSchemaURLTypeIterate();

}
