#!/usr/bin/env bash

shopt -s globstar

UBUNTUCP=/usr/share/java/bcprov.jar:/usr/share/java/commons-lang3.jar
GENTOOCP=/usr/share/bcprov/lib/bcprov.jar

javac -cp $UBUNTUCP:$GENTOOCP:. -Xmaxerrs 10 -Xlint:unchecked -Xmaxwarns 1 attachments.java report.java passwords.java pdftk.java TK_Session.java pdftk/**/*.java
