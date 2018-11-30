module.exports = {
    'roots': [
        '<rootDir>/src',
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
    ],
    "moduleNameMapper": {
        "\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$": "<rootDir>/tests/mocks/fileMock.js",
        "\\.(css|less|scss)$": "<rootDir>/tests/mocks/styleMock.js"
    },
}