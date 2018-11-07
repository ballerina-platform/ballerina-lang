module.exports = {
    'roots': [
        '<rootDir>/src-ts',
        '<rootDir>/tests'
    ],
    'transform': {
        '.*\.tsx?$': 'ts-jest'
    },
    'testRegex': '(/__tests__/.*|(\\.|/)(test|spec))\\.(jsx?|tsx?)$',
    'moduleFileExtensions': [
        'ts',
        'tsx',
        'js',
        'jsx',
        'json',
        'node'
    ],
    "collectCoverageFrom": [
        "!**/lib/**",
        "**/src/**",
        "!**/node_modules/**",
        "!**/stories/**"
    ]
}