This is an attempt to port pfdtk into java. The current goal is to make a translation as faithful as it is reasonable, and to leave possible improvements and refactoring for later. So far operations not related to reports have been translated (manually), but a lot more testing is needed. Due to the differences between C++ and Java, it is likely that a few bugs have sneaked in with respect to the original; any help in catching them will be appreciated.

##Dependencies

- jdk
 - ant
 - ivy (optional, for resolving dependencies at build time only)
 - bcprov

##Building instructions

With ivy:
```
$ ant
```

Without ivy: install bcprov, make a directory `lib` and link `bcprov.jar` into it. Then:
```
$ ant jar
```
