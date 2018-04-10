const letter = '[a-zA-Z_]';
const letterOrDigit = '[a-zA-Z0-9_]';
const identifierLiteralChar = '([^|"\\\f\n\r\t]|\\\\[btnfr]|\\[|"\\/])';
const identifier = `(${letter}(${letterOrDigit})*)|(\^"${identifierLiteralChar}+")`;


export default {
    defaultToken: 'identifier',
    controlKeywords: [
        'if', 'else', 'iterator', 'try', 'catch', 'finally', 'fork', 'join', 'all', 'some',
        'while', 'throw', 'return', 'returns', 'break', 'timeout', 'transaction', 'aborted',
        'abort', 'committed', 'failed', 'retries', 'next', 'bind', 'with', 'lengthof', 'typeof',
        'foreach', 'in', 'match', 'but', 'check',
    ],

    otherKeywords: [
        'import', 'version', 'public', 'attach', 'as', 'native',
        'annotation', 'package', 'connector', 'function', 'resource', 'service', 'action',
        'worker', 'struct', 'transformer', 'endpoint',
        'const', 'true', 'false', 'reply', 'create', 'parameter', 'new' , 'async',
    ],

    typeKeywords: [
        'boolean', 'int', 'float', 'string', 'var', 'any', 'datatable', 'table', 'blob',
        'map', 'exception', 'json', 'xml', 'xmlns', 'error', 'type',
    ],

    operators: [
        '!', '%', '+', '-', '~=', '==', '===', '=', '!=', '<', '>', '&&', '\\', '?:',
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

    tokenizer: {
        root: [
            ['\\bfunction\\b', { token: 'keyword', next: '@function' }],
            ['\\btype\\b', { token: 'keyword', next: '@type' }],
            ['\\bobject\\b', { token: 'keyword', next: '@object' }],
            {include: '@code'}
        ],

        code: [
            [/[a-z_$][\w$]*/, {
                cases: {
                    '@controlKeywords': 'keyword.control',
                    '@otherKeywords': 'keyword',
                    '@typeKeywords': 'type',
                    '(documentation|deprecated)': { token: 'keyword', next: '@documentation' },
                    '@default': 'identifier',
                },
            }],

            {include: '@comment'},

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
        
        continuedType: [
            [`\\b${identifier}\\b`, {token:'type', next: '@pop'}],
            {include: 'comment'},
            {include: 'code'},
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
            ['<', {token:'delimiter.angle', next: 'functionReceiver'}],
            ['\\(', {token:'delimiter.parenthesis', next: 'functionParameters'}],
            ['\\breturns\\b', {token:'keyword', next:'functionReturns'}],
            ['{', {token: 'delimiter.curly', next: 'functionBody'}],
            ['}', {token: 'delimiter.curly', next: '@pop'}],
            [`\\b${identifier}\\b`, 'identifier'],
            {include: 'comment'},
        ],

        functionBody: [
            ['(?=\})',  { token: 'delimiter.curly', next: '@pop'}],
            ['\\bmatch\\b', { token: 'keyword', next: 'match' }],
            {include: 'code'},
        ],
        
        functionParameters: [
            [`\\b${identifier}\\b`, {token:'type', next: 'varDefStatement'}],
            ['\\)', {token:'delimiter.parenthesis', next: '@pop'}],
            ['\/\/.*', 'comment'],
            {include: 'code'},
        ],

        functionReceiver: [
            [`\\b${identifier}\\b`, {token:'type', next: 'varDefStatement'}],
            ['>', {token:'delimiter.angle', next: '@pop'}],
            {include: 'comment'},
        ],

        functionReturns: [
            [`\\b${identifier}\\b`, 'type'],
            ['\\(|\\)', 'operator'],
            ['[,:|]', 'operator'],
            ['{', {token: 'delimiter.curly', next: 'functionBody'}],
            ['\/\/.*', 'comment'],
            ['(?=\})', {token: 'delimiter.curly', next: '@pop'}],
        ],
        
        match: [
            ['{', {token:'delimiter.curly', next: 'matchBody'}],
            [`\\b${identifier}\\b`, 'identifier'],
            ['}', {token:'delimiter.curly', next: '@pop'}],
            ['\/\/.*', 'comment'],
        ],
        
        matchBody: [
            ['(?=\})',  { token: 'delimiter.curly', next: '@pop'}],
            ['=>', {token:'keyword', next: 'matchedStatement'}],
            [`\\b${identifier}\\b`, {token:'type', next: 'varDefStatement'}],
            {include: 'root'},
            ['\/\/.*', 'comment'],
        ],
        
        matchedStatement: [
            ['{', {token:'delimiter.curly', next: 'functionBody'}],
            ['[;}]', {token: 'delimiter.curly', next: '@pop'}],
            {include: 'root'},
        ],

        new: [
            ['\\(', {token:'delimiter.parenthesis', next: 'newParameters'}],
            ['{', {token:'delimiter.curly', next: 'newBody'}],
            ['}', {token:'delimiter.curly', next: '@pop'}],
            ['\/\/.*', 'comment'],
        ],

        newBody: [
            ['(?=\})', { token: 'delimiter.curly', next: '@pop'}],
        ],

        newParameters: [
            [`\\b${identifier}\\b`, {token:'variable.parameter'}],
            ['\\)', {token:'delimiter.parenthesis', next: '@pop'}],
            ['\/\/.*', 'comment'],
            {include: 'root'},
        ],

        object: [
            ['{', {token:'delimiter.curly', next: 'objectBody'}],
            ['}', {token:'delimiter.curly', next: '@pop'}],
            ['\/\/.*', 'comment'],
        ],

        objectBody: [
            ['\\b(private|public)\\b', {token: 'keyword', next: 'varDefBlock'} ],
            ['\\bfunction\\b', { token: 'keyword', next: 'function' }],
            ['\\bnew\\b', { token: 'keyword', next: 'new' }],
            ['(?=\})', { token: 'delimiter.curly', next: '@pop'}],
            ['\/\/.*', 'comment'],
        ],

        objectInit: [
            ['{', {token:'delimiter.curly', next: 'objectInitBody'}],
            ['}', {token:'delimiter.curly', next: '@pop'}],
            ['\/\/.*', 'comment'],
        ],
        
        objectInitBody: [
            {include: 'root'},
            ['(?=\})', { token: 'delimiter.curly', next: '@pop'}],
        ],

        string: [
            [/[^\\"]+/, 'string'],
            [/@escapes/, 'string.escape'],
            [/\\./, 'string.escape.invalid'],
            [/"/, { token: 'string.quote', bracket: '@close', next: '@pop' }],
        ],

        type: [
            [`\\b${identifier}\\b`, { token: 'identifier', next: '@pop'}],
            ['\/\/.*', 'comment'],
        ],
        
        varDefBlock: [
            ['{', {token:'delimiter.curly', next: 'varDefBlockBody'}],
            ['}', {token:'delimiter.curly', next: '@pop'}],
            ['\/\/.*', 'comment'],
        ],
        
        varDefBlockBody: [
            [';', 'semi'],
            [`\\b${identifier}\\b`, {token:'type', next: 'varDefStatement'}],
            ['(?=\})', { token: 'delimiter.curly', next: '@pop'}],
            ['\/\/.*', 'comment'],
        ],

        varDefStatement: [
            ['[|,:]', {token: 'operator', next: 'continuedType'}],
            [`\\b${identifier}\\b`, {token:'variable.parameter', next: '@pop'}],
            {include: 'root'},
        ],
    },
};
