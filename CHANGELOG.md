## [3.1.1] - 2020-05-01

### Fixed
 - Do not escape form fields if UTF-8 output selected

## [3.1.0] - 2020-04-26

### Added
 - update_info handles page metrics (media, rotation and labels)

### Fixed
 - Report page label indices correctly

## [3.0.10] - 2020-04-18

### Fixed
 - Crash in dump_data_annots
 - Load commons-lang-3 only if needed
 - Crash when copying malformed inputs

## [3.0.9] - 2020-01-13

### Added
 - Native image build option

### Fixed
 - Print an informative error if missing dependencies
 - Crash with newlines in arguments

## [3.0.8] - 2019-10-14

### Changed
 - Build for JRE version 1.7

## [3.0.7] - 2019-09-09

### Fixed
 - Crash involving passwords and file handles (java.lang.NullPointerException).

## [3.0.6] - 2019-06-04

### Fixed
 - Crash in burst with invalid file patterns.
 - Do not report some spurious form fields.
 - Escape more characters on XML-encoded reports.

## [3.0.5] - 2019-05-20

### Fixed
 - Do not print error if PdfID is missing.
 - Crash in burst with certain inputs (java.lang.NullPointerException).

## [3.0.4] - 2019-05-02

### Fixed
 - Crashes with type casting (java.lang.ClassCastException)

## [3.0.3] - 2019-01-15

### Fixed
 - Bug that corrupts images in PDF files with (de)compress option
 - Crash with incomplete records

## [3.0.2] - 2018-12-05

### Fixed
 - Issue with rotation not being applied.
 - Do not require owner password when user password is given.

## [3.0.1] - 2018-09-30

### Fixed
 - Issue reading one file three or more times.

## [3.0.0] - 2018-09-04

### Added
 - Translation of pdftk into Java.
