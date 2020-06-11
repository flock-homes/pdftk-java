#!/usr/bin/env perl
use strict;
use warnings;

open(MANPAGE, "-|", "groff -mman -Tascii pdftk.1");
open(DEVNULL, ">", "/dev/null");
open(SYNOPSIS, ">", "java/com/gitlab/pdftk_java/resources/synopsis.txt");
open(DESCRIPTION, ">", "java/com/gitlab/pdftk_java/resources/description.txt");
my $fh = *DEVNULL;
my $newlines = 0;
while(<MANPAGE>) {
  s/(\x9B|\x1B\[)[0-?]*[ -\/]*[@-~]//g;
  if (m/SYNOPSIS/) { $fh = *SYNOPSIS; }
  if (m/DESCRIPTION/) { $fh = *DESCRIPTION; }
  if (m/^$/) {
    if ((++$newlines) > 1) { $fh = *DEVNULL; }
  } else {
    $newlines=0;
  }
  print $fh $_;
}
