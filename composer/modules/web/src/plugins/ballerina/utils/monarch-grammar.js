const letter = '[a-zA-Z_]';
const letterOrDigit = '[a-zA-Z0-9_]';
const identifierLiteralChar = '([^|"\\\f\n\r\t]|\\\\[btnfr]|\\[|"\\/])';
const identifier = `(${letter}(${letterOrDigit})*)|(\^"${identifierLiteralChar}+")`;


export default {
    defaultToken: 'invalid',
    controlKeywords: [
        'if', 'else', 'iterator', 'try', 'catch', 'finally', 'fork', 'join', 'all', 'some',
        'while', 'throw', 'return', 'returns', 'break', 'timeout', 'transaction', 'aborted',
        'abort', 'committed', 'failed', 'retries', 'next', 'bind', 'with', 'lengthof', 'typeof', 'enum',
        'foreach', 'in',
    ],

    otherKeywords: [
        'import', 'version', 'public', 'attach', 'as', 'native',
        'annotation', 'package', 'connector', 'function', 'resource', 'service', 'action',
        'worker', 'struct', 'transformer', 'endpoint',
        'const', 'true', 'false', 'reply', 'create', 'parameter',
    ],

    typeKeywords: [
        'boolean', 'int', 'float', 'string', 'var', 'any', 'datatable', 'table', 'blob',
        'map', 'exception', 'json', 'xml', 'xmlns', 'error', 'type',
    ],

    operators: [
        '!', '%', '+', '-', '~=', '==', '===', '=', '!=', '<', '>', '&&', '\\',
    ],

    brackets: [
      ['{', '}', 'delimiter.curly'],
      ['[', ']', 'delimiter.square'],
      ['(', ')', 'delimiter.parenthesis'],
      ['<', '>', 'delimiter.angle'],
    ],

    // we include these common regular expressions
    symbols: /[=><!~?:&|+\-*\/\^%]+/,

    // C# style strings
    escapes: /\\(?:[abfnrtv\\"']|x[0-9A-Fa-f]{1,4}|u[0-9A-Fa-f]{4}|U[0-9A-Fa-f]{8})/,

    // The main tokenizer for our languages
    tokenizer: {
        root: [
            // identifiers and keywords
            ['function', { token: 'keyword', next: '@function' }],

            [/[a-z_$][\w$]*/, {
                cases: {
                    '@controlKeywords': 'keyword.control.ballerina',
                    '@otherKeywords': 'keyword.ballerina',
                    '@typeKeywords': 'type.ballerina',
                    '(documentation|deprecated)': { token: 'keyword.ballerina', next: '@documentation' },
                    '@default': 'identifier',
                },
            }],

            // comments
            ['\s*((//).*$\n?)', 'comment'],

            // delimiters and operators nmnm
            [/[{}()\[\]]/, '@brackets'],
            [/[<>](?!@symbols)/, '@brackets'],
            [/@symbols/, {
                cases: {
                    '@operators': 'operator',
                    '@default': '',
                },
            }],

            // @ annotations.
            // As an example, we emit a debugging log message on these tokens.
            // Note: message are supressed during the first load -- change some lines to see them.
            [/@\s*[a-zA-Z_\$][\w\$]*/, { token: 'annotation', log: 'annotation token: $0' }],

            // numbers
            [/\d*\.\d+([eE][\-+]?\d+)?/, 'number.float'],
            [/0[xX][0-9a-fA-F]+/, 'number.hex'],
            [/\d+/, 'number'],

            // delimiter: after number because of .\d floats
            [/[;,.]/, 'delimiter'],

            // strings
            [/"/, { token: 'string.quote', bracket: '@open', next: '@string' }],

            [identifier, 'identifier'],
        ],

        comment: [['\/\/.*', 'comment']],

        string: [
            [/[^\\"]+/, 'string'],
            [/@escapes/, 'string.escape'],
            [/\\./, 'string.escape.invalid'],
            [/"/, { token: 'string.quote', bracket: '@close', next: '@pop' }],
        ],

        documentation: [
            ['(P|R|T|F|V)({{)(.+)(}})',
                ['keyword.other.documentation', 'keyword.other.documentation',
                    'variable.parameter.documentation', 'keyword.other.documentation'],
            ],
            ['```(.+)```', 'comment.code.documentation'],
            ['``(.+)``', 'comment.code.documentation'],
            ['`(.+)`', 'comment.code.documentation'],
            ['\\\\{', 'comment.documentation'], // escaped bracket
            ['\\\\}', 'comment.documentation'], // escaped bracket
            ['{', '@brackets'],
            ['}', { token: '@brackets', next: '@pop' }],
            ['.', 'comment.documentation'],
        ],

        function: [
            ['<', {token:'tag', next: 'functionReceiver'}],
            ['\\(', {token:'delimiter.parenthesis', next: 'functionParameters'}],
            ['\\breturns\\b', {token:'keyword', next:'@functionReturns'}],
            ['{', {token: 'delimiter.curly', next: 'functionBody'}],
            [`\\b${identifier}\\b`, 'identifier'],
        ],

        functionReceiver: [
            [`\\b${identifier}\\b`, {token:'type', next: 'varDefStatement'}],
            ['>', {token:'tag', next: '@pop'}],
        ],

        functionParameters: [
            [`\\b${identifier}\\b`, {token:'type', next: 'varDefStatement'}],
            ['\\)', {token:'delimiter.parenthesis', next: '@pop'}],
            ['\/\/.*', 'comment'],
            {include: 'root'},
        ],

        functionReturns: [
            [`\\b${identifier}\\b`, 'type'],
            ['\\(|\\)', 'operator'],
            ['[,:|]', 'operator'],
            ['{', {token: 'delimiter.curly', next: 'functionBody'}],
        ],

        functionBody: [
            {include: 'root'},
            ['}', '@pop']
        ],
        varDefStatement: [
            ['[|,:]', {token: 'operator', next: 'continuedType'}],
            [`\\b${identifier}\\b`, {token:'variable.parameter', next: '@pop'}],
            {include: 'root'},
        ],

        continuedType: [
            [`\\b${identifier}\\b`, {token:'type', next: '@pop'}],
            ['\/\/.*', 'comment'],
            {include: 'root'},
        ],
    },
};
