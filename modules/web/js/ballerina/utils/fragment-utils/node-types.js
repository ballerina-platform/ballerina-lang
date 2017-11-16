
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

/**
 * Create fragment for top level nodes.
 *
 * @param {string} sourceString - source fragment input.
 * @return {object} fragment details to be sent to fragment parser.
 * */
export function createTopLevelNodeFragment(sourceString) {
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
export function createServiceResourceFragment(sourceString) {
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
export function createConnectorActionFragment(sourceString) {
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
export function createWorkerFragment(sourceString) {
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
export function createExpressionFragment(sourceString) {
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
export function createVariableReferenceListFragment(sourceString) {
    return {
        expectedNodeType: 'variable_reference_list',
        source: sourceString,
    };
}

/**
 * Create fragment for statement.
 *
 * @param {string} sourceString - source fragment input.
 * @return {object} fragment details to be sent to fragment parser.
 * */
export function createStatementFragment(sourceString) {
    return {
        expectedNodeType: 'statement',
        source: sourceString,
    };
}

/**
 * Create fragment for endpoint variable definition.
 *
 * @param {string} sourceString - source fragment input.
 * @return {object} fragment details to be sent to fragment parser.
 * */
export function createEndpointVarDefFragment(sourceString) {
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
export function createJoinCondition(sourceString) {
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
export function createArgumentParameterFragment(sourceString) {
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
export function createReturnParameterFragment(sourceString) {
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
export function createTransactionFailedFragment(sourceString) {
    return {
        expectedNodeType: 'transaction_failed',
        source: sourceString,
    };
}
