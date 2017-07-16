import $ from 'jquery';

class FragmentUtils {
    static createExpressionFragment(sourceString) {
        return {
            expectedNodeType: 'expression',
            source: sourceString,
        };
    }

    static createStatementFragment(sourceString) {
        return {
            expectedNodeType: 'statement',
            source: sourceString,
        };
    }

    static createJoinCondition(sourceString) {
        return {
            expectedNodeType: 'join-condition',
            source: sourceString,
        };
    }

    static createArgumentParameterFragment(sourceString) {
        return {
            expectedNodeType: 'argument_parameter_definitions',
            source: sourceString
        }
    }

    static createReturnParameterFragment(sourceString) {
        return {
            expectedNodeType: 'return_parameter_definitions',
            source: sourceString
        }
    }

    static parseFragment(fragment) {
        let data = {};
        $.ajax({
            type: 'POST',
            context: this,
            url: 'http://localhost:8289/ballerina/model/parse-fragment',
            data: JSON.stringify(fragment),
            contentType: 'application/json; charset=utf-8',
            async: false,
            dataType: 'json',
            success(response) {
                data = response;
            },
            error() {
                data = {error: 'Unable to call fragment parser Backend.'};
            },
        });
        return data;
    }
}
export default FragmentUtils;
