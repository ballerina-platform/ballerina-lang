module.exports = {
    "env": {
        "browser": true,
        "commonjs": true,
        "amd": true
    },
    "extends": "eslint:recommended",
    "rules": {
        "linebreak-style": [
            "error",
            "unix"
        ],

        // TODO: Fix all breaks of following rules and turn them on
        "no-unused-vars": "off",
        "no-undef": "off",
        "no-mixed-spaces-and-tabs": "off",
        "no-redeclare": "off",

        // TODO: Fix all breaks of following rules and uncomment to turn them on
        // "indent": [
        //     "error",
        //     4
        // ],
        // "quotes": [
        //     "error",
        //     "single"
        // ],
        // "semi": [
        //     "error",
        //     "always"
        // ]
        // "eqeqeq": [
        //     "error",
        //     "always"
        // ]
    }
};
