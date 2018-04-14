# BWDM - Boundary Value/Vienna Development Method -
#### Test Cases Auto-Generation from a VDM Specification
Master 
[![Build Status](https://travis-ci.org/korosuke613/BWDM.svg?branch=master)](https://travis-ci.org/korosuke613/BWDM)
[![Coverage Status](https://coveralls.io/repos/github/korosuke613/BWDM/badge.svg?branch=master)](https://coveralls.io/github/korosuke613/BWDM?branch=master)

Build
[![Build Status](https://travis-ci.org/korosuke613/BWDM.svg?branch=develop)](https://travis-ci.org/korosuke613/BWDM)
[![Coverage Status](https://coveralls.io/repos/github/korosuke613/BWDM/badge.svg?branch=develop)](https://coveralls.io/github/korosuke613/BWDM?branch=develop)


## Execution Environment, using Libraries,
* OS : Any OS (Please prepare same enviroment.)
* [Java9](https://www.oracle.com/java/java9.html) : Execution Environment
* [VDMJ Ver.4](https://github.com/nickbattle/vdmj.git) : Lexer and Parser
* [JUnit5](https://github.com/junit-team/junit5) : Unit Testing
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