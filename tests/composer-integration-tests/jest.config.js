module.exports = {
    "testEnvironment": "jsdom",
    'roots': [
        '<rootDir>/tests'
    ],
    'transform': {
        '.*\.tsx?$': 'ts-jest',
        "^.+\\.ts$": "ts-jest",
        ".+\\.(css|styl|less|sass|scss)$": "<rootDir>/node_modules/jest-css-modules-transform"
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
        "!**/build/**",
        "**/src/**",
        "!**/node_modules/**",
    ],
    "moduleNameMapper": {
        "\\.(jpg|jpeg|png|gif|eot|otf|webp|svg|ttf|woff|woff2|mp4|webm|wav|mp3|m4a|aac|oga)$": "<rootDir>/__mocks__/fileMock.js"
    }
}