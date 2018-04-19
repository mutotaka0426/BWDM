#!/bin/bash

if [$TRAVIS_PULL_REQUEST=="true"]; then
  return
fi

if [ $BRANCH = "master" ] || [ $$BRANCH = "develop" ]; then
  ./gradlew dokka_$current_branch
  cd docs
  git add -A
  git commit -m "update"
  git push origin gh-pages
else
  echo "not master or develop"
fi


