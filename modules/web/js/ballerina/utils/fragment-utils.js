import $ from 'jquery';

class FragmentUtils {
    static createExpressionFragment(sourceString) {
        return {
            expectedNodeType: 'expression',
            source: sourceString
        };
    }

    static createStatementFragment(sourceString) {
        return {
            expectedNodeType: 'statement',
            source: sourceString
        };
    }

    static parseFragment(fragment) {
        var data = {};
        $.ajax({
            type: 'POST',
            context: this,
            url: 'http://localhost:8289/ballerina/model/parse-fragment',
            data: JSON.stringify(fragment),
            contentType: 'application/json; charset=utf-8',
            async: false,
            dataType: 'json',
            success: function (response) {
                data = response;
            },
            error: function(){
                data = {'error': 'Unable to call fragment parser Backend.'};
            }
        });
        return data;
    }
}
export default FragmentUtils;
