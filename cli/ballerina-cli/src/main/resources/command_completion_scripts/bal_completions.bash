#!/bin/bash 

commands="-v --help --version add bindgen build clean dist doc encrypt format grpc help init new openapi pull push
run search shell test update version"

_bal_completions() 
{
    if [ "${#COMP_WORDS[@]}" == "2" ]; then 
        COMPREPLY=($(compgen -W "$commands" "${COMP_WORDS[1]/-/\\-}")) 
    fi
} 
 
complete -F _bal_completions bal
