#!/bin/bash
if [ -z "$1" ]
    then echo "You need to specify the version to deploy.";
    exit 1;
fi
VERSION=$1
echo "Deploying version: $VERSION"
scp ./target/universal/lastobot-$VERSION.tgz lastobot:~/deploy

ssh lastobot /bin/bash << EOF
    cd ~/deploy
    if [ -d "lastobot-$VERSION" ]
        then echo "Version $VERSION has already been deployed";
        exit 1;
    fi

    tar xvfz lastobot-$VERSION.tgz
    rm current
    ln -s lastobot-$VERSION current
    ls -rtla
    # for usability
    ln -s ~/deploy/lastobot-$VERSION ~/lastobot
EOF
