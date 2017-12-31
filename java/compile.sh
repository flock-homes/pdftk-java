#!/usr/bin/env bash

javac -Xmaxerrs 10 -Xlint:unchecked -Xmaxwarns 1 report.java passwords.java pdftk.java TK_Session.java pdftk/com/lowagie/text/*.java pdftk/com/lowagie/text/*/*.java
