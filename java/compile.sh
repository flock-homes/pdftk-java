#!/usr/bin/env bash

javac -Xmaxerrs 100 -Xlint:unchecked -Xmaxwarns 1 report.java passwords.java pdftk.java TK_Session.java
