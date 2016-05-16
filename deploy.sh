#!/bin/bash
if [ -z "$1" ]
 then echo "You need to specify the version to deploy."; exit 1;
fi
VERSION=$1
echo "Deploying version: $VERSION"
scp ./target/universal/lastobot-$VERSION.tgz lastobot:/deploy