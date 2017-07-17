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
        "max-len": ["error", 120],
        "require-jsdoc": ["warn", {
            "require": {
                "FunctionDeclaration": true,
                "MethodDefinition": true,
                "ClassDeclaration": true
            }
        }],
        "valid-jsdoc": ["warn", {
            "requireReturn": false
        }],
        "indent": ["error", 4],
        "import/no-extraneous-dependencies": ["off", { 
            "devDependencies": false, 
            "optionalDependencies": false, 
            "peerDependencies": false 
        }],
        "import/no-unresolved": ["off"],
        "import/extensions": ["off"],
        "import/no-named-as-default": ["off"],
        "import/no-named-as-default-member": ["off"],
        "no-underscore-dangle": ["error", { 
            "allowAfterThis": true
        }],
        "no-param-reassign": ["off"],
        "no-restricted-syntax": ["off"],
        "no-plusplus": ["off"],
        "func-names": ["off"],
        "class-methods-use-this": ["off"],
        "arrow-body-style": "off",
        "prefer-template": "off",
        "jsx-a11y/no-static-element-interactions": "off",
        "jsx-a11y/no-noninteractive-element-interactions": "off",
        "react/jsx-indent": ["error", 4],
        "react/jsx-indent-props": ["error", 4],
        "react/prefer-stateless-function": ["off"],
        "no-mixed-operators": ["error", {"allowSamePrecedence": true}],
        "jsx-quotes": ["off"],
        "no-else-return": "off",
    },
    "plugins": [
        "react"
    ]
};
