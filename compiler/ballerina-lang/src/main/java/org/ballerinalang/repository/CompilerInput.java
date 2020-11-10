/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.repository;

import io.ballerina.compiler.syntax.tree.SyntaxTree;

/**
 * This represents a Ballerina package source entry.
 * 
 * @since 0.94
 */
public interface CompilerInput {

    /**
     * Returns the entry name, e.g. file name. 
     * 
     * @return the entry name
     */
    String getEntryName();
    
    /**
     * Returns the source code this source entry represents.
     * 
     * @return the source code binary encoded
     */
    byte[] getCode();

    /**
     * Returns the syntax tree this source entry represents.
     *
     * @return syntax tree
     */
    SyntaxTree getTree();
}
