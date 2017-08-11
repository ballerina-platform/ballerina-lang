/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.model;

/**
 * Independently Executable Unit in Linked Execution.
 *
 * @since 0.8.0
 */
public interface LinkedNode extends Node {

    /**
     * Get next LinkedNode to current LinkedNode.
     *
     * @return next LinkedNode.
     */
    LinkedNode next();

    /**
     * Set next LinkedNode to current LinkedNode.
     *
     * @param linkedNode next.
     */
    void setNext(LinkedNode linkedNode);

    /**
     * Get Sibling LinkedNode next to current LinkedNode.
     *
     * @return next sibling LinkedNode.
     */
    LinkedNode getNextSibling();

    /**
     * Set Sibling LinkedNode next to current LinkedNode.
     *
     * @param linkedNode next sibling LinkedNode.
     */
    void setNextSibling(LinkedNode linkedNode);

    /**
     * Get Parent LinkedNode of current LinkedNode.
     *
     * @return parent LinkedNode.
     */
    LinkedNode getParent();

    /**
     * Set Parent LinkedNode of current LinkedNode.
     *
     * @param linkedNode parent LinkedNode.
     */
    void setParent(LinkedNode linkedNode);
}
