import globals from "globals";
import tsParser from "@typescript-eslint/parser";
import tsPlugin from "@typescript-eslint/eslint-plugin";

export default [
    {
        files: ["**/*.ts"],
        languageOptions: {
            globals: globals.browser,
            parser: tsParser,
            parserOptions: {
                ecmaVersion: 2020,
                sourceType: "module",
            },
        },
        plugins: {
            "@typescript-eslint": tsPlugin,
        },
        // https://typescript-eslint.io/rules/
        rules: {
            "@typescript-eslint/array-type": "error", // Segnala se non viene usata la dichiarazione [] per gli array
            "@typescript-eslint/consistent-generic-constructors": "error", // Segnala se non viene usata l'annotazione "constructor" per le dichiarazioni di classe
            "@typescript-eslint/adjacent-overload-signatures": "error", // Segnala se le dichiarazioni "overload" di una funzione non sono vicine tra loro
            "@typescript-eslint/no-this-alias": "error", // Segnala se viene dato un alias a this
            "no-unused-vars": "off",                      // Disabilita la regola di default (consigliato per non dare falsi errori con la regola successiva)
            "@typescript-eslint/no-unused-vars": "error", // Segnala se ci sono variabili non utilizzate
            "@typescript-eslint/no-unnecessary-type-constraint": "error", // Segnala se la dichiarazione di un estende "any" o "unknown"
            "@typescript-eslint/no-unnecessary-type-assertion": "error", // Segnala se se viene usato "as" per dire che un espressione è di tipo diverso da quello che ci si aspetta
            "@typescript-eslint/no-unsafe-declaration-merging": "error", // Segnala se si tenta di unire due dichiarazioni con lo stesso nome
            "@typescript-eslint/no-unsafe-function-type": "error", // Segnala se non viene specificato il tipo di ritorno di una funzione
            "@typescript-eslint/no-unsafe-return": "error", // Segnala se una funzione ritorna un tipo non sicuro, come "any"
            "@typescript-eslint/prefer-readonly": "error", // Segnala se una variabile privata, che non viene modificata al di fuori del costruttore, non è dichiarata come readonly
            "@typescript-eslint/restrict-plus-operands": "error", // Segnala se si tenta di sommare due variabili di tipo diverso
            "@typescript-eslint/triple-slash-reference": "error", // Segnala se si utilizza "/// <reference path='...' />" invece di "import"
            "@typescript-eslint/no-extra-non-null-assertion": "error", // Segnala se si usa "!!" per forzare una variabile a non essere null (è inutile più di un !)
            "@typescript-eslint/no-inferrable-types": "error", // Segnala se si cerca di esplicitare il tipo di una variabile quando è già implicito (con int, string e boolean)
            "@typescript-eslint/no-explicit-any": "error", // Segnala se si usa "any" come tipo di variabile im modo esplicito
        }
    },
];
