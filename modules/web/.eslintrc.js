module.exports = {
    "parserOptions": {
        "ecmaVersion": 6,
        "sourceType": "module",
        "ecmaFeatures": {
            "jsx": true,
        }
    },
    "env": {
        "browser": true,
        "commonjs": true,
        "es6": true
    },
    "extends": "airbnb",
    "rules": {
        "linebreak-style": [
            "error",
            "unix"
        ],
        "no-unused-vars": "error",
        "no-undef": "error",
        "no-mixed-spaces-and-tabs": "error",
        "no-redeclare": "error",
        "indent": [
            "error",
            4
        ],
        "react/jsx-uses-vars": 1,
        "quotes": [
            "error",
            "single"
        ],
        "semi": [
            "error",
            "always"
        ],
        "eqeqeq": [
            "error",
            "always"
        ]
    },
    "plugins": [
        "react"
    ]
};
