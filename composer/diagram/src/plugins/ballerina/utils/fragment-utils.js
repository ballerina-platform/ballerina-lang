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

// using relative imports here, since gen-default-nodes runs without webpack
import { parseFragment } from 'api-client/api-client';

let customParseFragmentFn;

/**
 * Class for fragment utils.
 *
 * @class FragmentUtils
 * */
class FragmentUtils {

    /**
     * Create fragment for top level nodes.
     *
     * @param {string} sourceString - source fragment input.
     * @return {object} fragment details to be sent to fragment parser.
     * */
    static createTopLevelNodeFragment(sourceString) {
        return {
            expectedNodeType: 'top-level-node',
            source: sourceString,
        };
    }

    /**
     * Create fragment for service resource.
     *
     * @param {string} sourceString - source fragment input.
     * @return {object} fragment details to be sent to fragment parser.
     * */
    static createServiceResourceFragment(sourceString) {
        return {
            expectedNodeType: 'service-resource',
            source: sourceString,
        };
    }

     /**
     * Create fragment for connector action.
     *
     * @param {string} sourceString - source fragment input.
     * @return {object} fragment details to be sent to fragment parser.
     * */
    static createConnectorActionFragment(sourceString) {
        return {
            expectedNodeType: 'connector-action',
            source: sourceString,
        };
    }

     /**
     * Create fragment for worker.
     *
     * @param {string} sourceString - source fragment input.
     * @return {object} fragment details to be sent to fragment parser.
     * */
    static createWorkerFragment(sourceString) {
        return {
            expectedNodeType: 'worker',
            source: sourceString,
        };
    }

    /**
     * Create fragment for expression.
     *
     * @param {string} sourceString - source fragment input.
     * @return {object} fragment details to be sent to fragment parser.
     * */
    static createExpressionFragment(sourceString) {
        return {
            expectedNodeType: 'expression',
            source: sourceString,
        };
    }

    /**
     * Create fragment for variable reference list.
     *
     * @param {string} sourceString - source fragment input.
     * @return {object} fragment details to be sent to fragment parser.
     * */
    static createVariableReferenceListFragment(sourceString) {
        return {
            expectedNodeType: 'variable_reference_list',
            source: sourceString,
        };
    }

    /**
     * Create fragment for field definition list.
     *
     * @param {string} sourceString - source fragment input.
     * @return {object} fragment details to be sent to fragment parser.
     * */
    static createFieldDefinitionListFragment(sourceString) {
        return {
            expectedNodeType: 'field_definition_list',
            source: sourceString,
        }
    }

    /**
     * Create fragment for statement.
     *
     * @param {string} sourceString - source fragment input.
     * @return {object} fragment details to be sent to fragment parser.
     * */
    static createStatementFragment(sourceString) {
        return {
            expectedNodeType: 'statement',
            source: sourceString,
        };
    }

    /**
     * Create fragment for anon record.
     *
     * @param {string} sourceString - source fragment input.
     * @return {object} fragment details to be sent to fragment parser.
     * */
    static createAnonRecordFragment(sourceString) {
        return {
            expectedNodeType: 'anon_Record',
            source: sourceString,
        };
    }

    /**
     * Create fragment for endpoint variable definition.
     *
     * @param {string} sourceString - source fragment input.
     * @return {object} fragment details to be sent to fragment parser.
     * */
    static createEndpointVarDefFragment(sourceString) {
        return {
            expectedNodeType: 'endpoint_var_def',
            source: sourceString,
        };
    }

    /**
     * Create fragment for Join.
     *
     * @param {string} sourceString - source fragment input.
     * @return {object} fragment details to be sent to fragment parser.
     * */
    static createJoinCondition(sourceString) {
        return {
            expectedNodeType: 'join-condition',
            source: sourceString,
        };
    }

    /**
     * Create fragment for Argument parameter.
     *
     * @param {string} sourceString - source fragment input.
     * @return {object} fragment details to be sent to fragment parser.
     * */
    static createArgumentParameterFragment(sourceString) {
        return {
            expectedNodeType: 'argument_parameter_definitions',
            source: sourceString,
        };
    }

    /**
     * Create fragment for Return parameter.
     *
     * @param {string} sourceString - source fragment input.
     * @return {object} fragment details to be sent to fragment parser.
     * */
    static createReturnParameterFragment(sourceString) {
        return {
            expectedNodeType: 'return_parameter_definitions',
            source: sourceString,
        };
    }

    /**
     * Create fragment for transaction failed statement.
     *
     *  @param {string} sourceString - source fragment input.
     *  @return {object} fragment details to be sent to the fragment parser.
     * */
    static createTransactionFailedFragment(sourceString) {
        return {
            expectedNodeType: 'transaction_failed',
            source: sourceString,
        };
    }

    /**
     * parse fragment.
     *
     * @param {string} fragment - source fragment.
     * @return {object} fragment details to be sent to fragment parser.
     * */
    static parseFragment(fragment) {
        if (customParseFragmentFn) {
            return customParseFragmentFn(fragment);
        }
        return parseFragment(fragment);
    }

    /**
     * Lets developers override default fragment parsing logic.
     * This is used to use LS api when possible for fragment parsing.
     * This can be cleaned up once composer services are no longer used.
     *
     * @param {Function} parseFragmentFn call back function for parsing.
     */
    static setParseFragmentFn(parseFragmentFn) {
        customParseFragmentFn = parseFragmentFn;
    }
}

export default FragmentUtils;
