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

 - [Standalone jar](https://gitlab.com/pdftk-java/pdftk/-/packages/generic/v3.3.1/pdftk-all.jar), including dependencies. Requires a JRE at runtime.
 - :warning: **Experimental** [Native Image](https://gitlab.com/pdftk-java/pdftk/-/packages/generic/v3.3.1/pdftk) for x86_64 GNU/Linux systems. Does not require any runtime dependencies.

## Dependencies

 - jdk >= 1.8
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
versions of gradle since 6.1 (tested 6.1 and 7.1) but if you have problems try
installing gradle wrapper at a particular version and then running the wrapper:
```
gradle wrapper --gradle-version 7.1
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
it will probably be convenient to create an alias or launcher script.

## FAQ

**Q: Should I use pdftk-java?**

**A:** If you are already using pdftk, then pdftk-java should be a
seamless replacement. If you are looking for a command line tool to
use occasionally and in scripts, pdftk-java should do the job. If you
want a robust library to call from a Java program (or a similar
language), then it it probably better to use a proper library, for
instance the [iText](https://itextpdf.com) library that pdftk-java
uses internally or its more permissively licensed fork
[OpenPDF](https://github.com/LibrePDF/OpenPDF).

**Q: How do I get help?**

**A:** If reading the manual does not solve your problem, a good place to
find help is StackExchange, and in particular the
[superuser](https://superuser.com/) and [Unix &
Linux](https://unix.stackexchange.com/) sites. Issues and feature
requests can be reported [over here](https://gitlab.com/pdftk-java/pdftk/-/issues).

**Q: I got a ClassCastException / NullPointerException. What can I
do?**

**A:** These errors were relatively frequent in early versions of
pdftk-java, please double-check that you are running the latest
version. If that is indeed the case, an issue report will be very
welcome.

**Q: I got a ClassNotFoundException / NoClassDefFoundError. What can I
do?**

**A:** If you installed the package manually, then you probably forgot
to specify the classpath. See the [running
instructions](#building-and-running-with-ant).

**Q: I am trying to fill a form with non-ASCII characters but they do
not show up. How can I fix it?**

**A:** Often the problem with disappearing characters is that the PDF
does not contain the appropriate fonts. As of release 3.3.0 a
workaround for this issue is to replace the embedded fonts with a
local font:
```
pdftk form.pdf fill_form data.fdf output filled.pdf replacement_font "DejaVu Sans"
```
See also issues #84, #96, #97 for more details. CJK languages are a
different story, see issue #37.

**Q: Is there a GUI?**

**A:** There are a few GUIs that were designed with pdftk as a backend
that should still work. One of them is [PDF
Chain](https://pdfchain.sourceforge.io/), also available on
[Flathub](https://flathub.org/apps/details/net.sourceforge.pdfchain),
which means it's easily installable on any Linux distro.

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
