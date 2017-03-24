/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
define(['lodash', './ast-node-add-operation', './ast-node-remove-operation', './ast-node-modify-operation',
        './custom-undoable-operation', './source-modify-operation'],
    function (_, ASTNodeAddOperation, ASTNodeRemoveOperation, ASTNodeModifyOperation, CustomUndoableOperation,
                SourceModifyOperation) {

        var UndoableOperationFactory = {};
        /**
         * A Factory method to create undoable operations
         * @param args
         */
        UndoableOperationFactory.getOperation = function(args){
            switch (args.type){
                case "child-added": return new ASTNodeAddOperation(args);
                case "child-removed": return new ASTNodeRemoveOperation(args);
                case "node-modified": return new ASTNodeModifyOperation(args);
                case "source-modified": return new SourceModifyOperation(args);
                case "custom": return new CustomUndoableOperation(args);
            }
        };

        UndoableOperationFactory.isSourceModifiedOperation = function(undoableOperation){
            return undoableOperation instanceof SourceModifyOperation;
        };

        return UndoableOperationFactory;
    });

