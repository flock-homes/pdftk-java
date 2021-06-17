This is a port of [pdftk](https://www.pdflabs.com/tools/pdftk-server/)
into Java. 

The current goals are to keep functionality as compatible with the
original as it is reasonable, to fix any issues present in the
original (correctness takes precedence over compatibility, see the
[differences](#known-differences-with-pdftk)), and to clean up the
code. New functionality may be added, but it is not a priority.

So far all code has been manually translated and it passes the test
suite of [php-pdftk](https://github.com/mikehaertl/php-pdftk), but
[more testing is
needed](https://pdftk-java.gitlab.io/pdftk/coverage/). Due to the
differences between C++ and Java, it is likely that a few bugs have
sneaked in with respect to the original; any help in catching them
will be appreciated.

## Installation

### Using a package manager

There are pdftk-java packages available in a few
[repositories](https://repology.org/project/pdftk-java/versions),
including Arch, Debian / Ubuntu, Gentoo, Homebrew, MacPorts, and
SUSE. Please refer to the documentation of your package manager for
instructions.

:warning: Some distributions provide stable packages that
are older than 3.0.4, which fixed many crashes. Consider
updating to an unstable package with a more recent version.

:warning: As of May 2021 homebrew packages do not yet support M1
chips; a temporary alternative is to [install an Intel version of
homebrew](https://gitlab.com/pdftk-java/pdftk/-/issues/89#note_576347882)
alongside the M1 version.

A third-party [docker image](https://hub.docker.com/r/minidocks/pdftk)
based on a native image is also available.

### Pre-built binaries

The recommended way to install pdftk-java is through a package
manager, but if that is not an option there are pre-built binaries
available:

 - [Standalone jar](https://gitlab.com/pdftk-java/pdftk/-/jobs/1353200058/artifacts/raw/build/libs/pdftk-all.jar), including dependencies. Requires a JRE at runtime.
 - :warning: **Experimental** [Native Image](https://gitlab.com/pdftk-java/pdftk/-/jobs/1353200062/artifacts/raw/build/native-image/pdftk) for x86_64 GNU/Linux systems. Does not require any runtime dependencies.

## Dependencies

 - jdk >= 1.7
 - commons-lang3
 - bcprov
 - gradle >= 6.1 or ant (build time)
 - ivy (optionally for ant, for resolving dependencies at build time)

## Building and running with Gradle
If you have gradle installed, you can produce a standard jar, which
requires a Java Runtime Environment plus additional libraries, a
standalone jar, which only requires a Java Runtime Environment, or a
standalone native binary, which does not require any runtime
dependencies.

The build configuration is relatively simple so it should work with most
versions of gradle since 6.1 (tested 6.1 and 6.7.1) but if you have problems try
installing gradle wrapper at a particular version and then running the wrapper:
```
gradle wrapper --gradle-version 6.7.1
```

### Standard jar

To build a jar, simply run: 

```
gradle jar
```

and refer to the [ant instructions](#building-and-running-with-ant) for running it.

### Standalone jar

To build a standalone jar, simply run: 

```
gradle shadowJar
```

This can then be run with just java installed like:
```
java -jar build/libs/pdftk-all.jar
```

### Standalone binary (native image :warning: **Experimental**)

> :warning: Has issues with GraalVM 20.2 and 20.3, see issue #68.

Building a standalone binary requires
[GraalVM](https://www.graalvm.org), which replaces the standard JDK,
with the [Native Image
Plugin](https://www.graalvm.org/docs/reference-manual/native-image/)
installed. To build a standalone binary, simply run:

```
export JAVA_HOME=/path/to/graalvm
gradle nativeImage
```

This can then be run like:
```
./build/native-image/pdftk
```

## Building and running with ant

With ivy:
```
$ ant
```

Without ivy: install bcprov and commons-lang3, make a directory `lib`
and link `bcprov.jar` and `commons-lang3.jar` into it. Then:
```
$ ant jar
```

To run:
```
$ java -cp build/jar/pdftk.jar:lib/bcprov.jar:lib/commons-lang3.jar com.gitlab.pdftk_java.pdftk
```

## Troubleshooting

If reading the manual does not solve your problems, a good place to
find help is StackExchange (in particular the
[superuser](https://superuser.com/) and [Unix &
Linux](https://unix.stackexchange.com/) sites). Issues and feature
requests can be reported [over here](https://gitlab.com/pdftk-java/pdftk/-/issues).

## Known differences with pdftk

The following differences with respect to the original version of
pdftk are intended. Issue reports about other differences are welcome
(when in doubt, open an issue).

- Does not ask for owner password if not needed.
- Does not report some structure-only form fields.
- Reports some missing values in multi-valued form fields.
- Does not escape form fields if UTF-8 output is selected.
- Report entries may be in a different order.
- Reports annotations other than links.

## Source code organization

`java/com/gitlab/pdftk_java/` contains the translated Java sources. Currently these are
a few large files, but they should be split into one class per file.

`java/com/gitlab/pdftk_java/com/lowagie/text/` contains the sources for an old, yet-to-be-determined
version of the iText library. They were modified in the original C++
sources, hence it is not obvious whether they can be replaced by a
more recent vanilla version.

## Applications using pdftk-java

- [PDF Chain](https://pdfchain.sourceforge.io/) is a GUI interface for PDFtk. It's available on [flathub.org](https://flathub.org)
which means it's easily installable on any Linux distro:\
https://pdfchain.sourceforge.io/
https://flathub.org/apps/details/net.sourceforge.pdfchain
