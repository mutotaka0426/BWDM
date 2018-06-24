#!/bin/bash

if [ $TRAVIS_PULL_REQUEST = "true" ]; then
  return
fi

if [ $TRAVIS_BRANCH = "master" ] || [ $TRAVIS_BRANCH = "develop" ]; then
  cd docs
  git checkout -b gh-pages
  git pull --depth=1 origin gh-pages
  cd ..
  ./gradlew dokka_$TRAVIS_BRANCH
  cd docs
  git add -A
  git commit -m "update"
  git push origin gh-pages
else
  echo "not master or develop"
fi


