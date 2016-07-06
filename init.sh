#!/bin/bash

if [ -z "$PROPPR" ];
then
    echo "Please set up the PROPPR variable."
fi

export PYTHONPATH=$PYTHONPATH:`pwd`/src/python:`pwd`/packages
export GNAT=`pwd`
