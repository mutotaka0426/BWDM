#!/bin/bash

current_branch=$(git symbolic-ref --short HEAD)

if [ $current_branch = "master" ] || [ $current_branch = "develop" ]; then
  ./gradlew dokka_$current_branch
  cd docs
  git add -A
  git commit -m "update"
  git push origin gh-pages
else
  echo "not master or develop"
fi


