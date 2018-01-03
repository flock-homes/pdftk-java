#!/usr/bin/env bash

shopt -s globstar

javac -cp /usr/share/java/bcprov.jar:/usr/share/bcprov/lib/bcprov.jar:. -Xmaxerrs 10 -Xlint:unchecked -Xmaxwarns 1 attachments.java report.java passwords.java pdftk.java TK_Session.java pdftk/**/*.java
