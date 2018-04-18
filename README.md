# BWDM - Boundary Value/Vienna Development Method -
#### Test Cases Auto-Generation from a VDM Specification
[![GitHub tag](https://img.shields.io/github/tag/korosuke613/BWDM.svg)](https://github.com/korosuke613/BWDM/tags)
[![GitHub release](https://img.shields.io/github/release/korosuke613/BWDM/all.svg)](https://github.com/korosuke613/BWDM/releases)
[![GitHub closed pull requests](https://img.shields.io/github/issues-pr-closed-raw/korosuke613/BWDM.svg)](https://github.com/korosuke613/BWDM/pulls?q=is%3Apr+is%3Aclosed)

Master ::
[![Build Status](https://travis-ci.org/korosuke613/BWDM.svg?branch=master)](https://travis-ci.org/korosuke613/BWDM)
[![Coverage Status](https://coveralls.io/repos/github/korosuke613/BWDM/badge.svg?branch=master)](https://coveralls.io/github/korosuke613/BWDM?branch=master)
[![codebeat badge](https://codebeat.co/badges/2ee47212-b56d-4ef8-9412-645d71e98a94)](https://codebeat.co/projects/github-com-korosuke613-bwdm-master)

Develop ::
[![Build Status](https://travis-ci.org/korosuke613/BWDM.svg?branch=develop)](https://travis-ci.org/korosuke613/BWDM)
[![Coverage Status](https://coveralls.io/repos/github/korosuke613/BWDM/badge.svg?branch=develop)](https://coveralls.io/github/korosuke613/BWDM?branch=develop)
[![codebeat badge](https://codebeat.co/badges/dbeeed1d-2de8-4fff-b2e7-7fca0e77fb07)](https://codebeat.co/projects/github-com-korosuke613-bwdm-develop)







## Execution Environment, using Libraries,
* OS : Any OS (Please prepare same enviroment.)
* [![Java version](https://img.shields.io/badge/java-9+-4c7e9f.svg)](https://www.java.com/en/) : Execution Environment
* [![Gradle version](https://img.shields.io/badge/gradle-4.4+-007042.svg)](https://gradle.org/docs/) : Building
* [![JUnit version](https://img.shields.io/badge/junit-5+-dc524a.svg)](http://junit.org/junit5/) : Unit Testing
* [VDMJ Ver.4](https://github.com/nickbattle/vdmj.git) : Lexer and Parser
* [z3](https://github.com/Z3Prover/z3) : Theorem Prover


## install
1. Install Microsoft Z3 library.
2. Download [BWDM.jar](https://github.com/korosuke613/BWDM/releases).


## example

```bash
$ java -Djava.library.path=./libs -jar BWDM.jar ./vdm_files/probrem.vdmpp 
```

or

```bash
$ gradle run -Pargs="./vdm_files/problem.vdmpp" 
```

## History
* 20161110  create first repository.
* 20161111  implement FX.java. 
* 20170105  add 'test' package. rename some directories.
* 20171025  create new repository, change internal construction of BWDM.
* 20180115
  * Implementation for my master paper was almost done :)
  * BVA and Symbolic Exe. are available for auto-gen of testcase.
  * Command-line options are also available (below table).


| Option | Content |
| --- |:---|
| -n | Basically Info. |
| -a | Info. of BVA |
| -i | Info. of Symbolic Exe.  |
| -b | Output ONLY testcases of BVA |
| -s | Output OHLY testcases of Symbolic Exe. |
| -f | Output testcase into a file (default:display on console)|
| -v | Version |
| -h | Help |


## build

```bash
$ gradle jar
```
