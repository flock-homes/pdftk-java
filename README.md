This is an attempt to port pfdtk into Java. The current goal is to
make a translation as faithful as it is reasonable, and to leave
possible improvements and refactoring for later. So far all code has
been manually translated, but a lot more testing is needed. Due to the
differences between C++ and Java, it is likely that a few bugs have
sneaked in with respect to the original; any help in catching them
will be appreciated.

## Dependencies

 - jdk >= 1.7
 - commons-lang3
 - bcprov
 - ant (build time)
 - ivy (optional, for resolving dependencies at build time)

## Building instructions

With ivy:
```
$ ant
```

Without ivy: install bcprov and commons-lang3, make a directory `lib`
and link `bcprov.jar` and `commons-lang3.jar` into it. Then:
```
$ ant jar
```

## Running instructions

```
$ java -cp build/jar/pdftk.jar:lib/*.jar pdftk
```

## Source organization

`pdftk/` contains the original C++ sources for reference.

`java/` contains the translated Java sources. Currently these are a
few large files, but they should be split into one class per file and
grouped into a package.

`java/pdftk/` contains the sources for an old, yet-to-be-determined
version of the iText library. They were modified in the original C++
sources, hence it is not obvious whether they can be replaced by a
more recent vanilla version.
