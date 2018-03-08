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
            [/[a-z_$][\w$]*/, {
                cases: {
                    '@controlKeywords': 'keyword.control.ballerina',
                    '@otherKeywords': 'keyword.other.ballerina',
                    '@typeKeywords': 'type.ballerina',
                    '(documentation|deprecated)': {token: 'keyword.other.ballerina', next: '@documentation'},
                    '@default': 'identifier',
                },
            }],
            [/[A-Z][\w\$]*/, 'type.identifier'],  // to show class names nicely

            // comments
            ['\s*((//).*$\n?)', 'comment'],

            // delimiters and operators
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
        ],

        string: [
            [/[^\\"]+/, 'string'],
            [/@escapes/, 'string.escape'],
            [/\\./, 'string.escape.invalid'],
            [/"/, { token: 'string.quote', bracket: '@close', next: '@pop' }],
        ],

        documentation: [
            ['(P|R|T|F|V)({{)(.+)(}})', 
                ['keyword.other.documentation', 'keyword.other.documentation',
                'variable.parameter.documentation', 'keyword.other.documentation' ]
            ],
            ['```(.+)```', 'comment.code.documentation'],
            ['``(.+)``', 'comment.code.documentation'],
            ['`(.+)`', 'comment.code.documentation'],
            ['\\\\{', 'comment.documentation'], // escaped bracket
            ['\\\\}', 'comment.documentation'], // escaped bracket
            ['{', '@brackets'],
            ['}', { token: '@brackets', next: '@pop'}],
            ['.', 'comment.documentation'],
        ]
    },
};
