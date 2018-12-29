# Desktop

[![Build Status](https://travis-ci.org/Sciss/Desktop.svg?branch=master)](https://travis-ci.org/Sciss/Desktop)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.sciss/desktop_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.sciss/desktop_2.11)

## statement

Desktop is an application framework for Scala on the desktop, including support for Swing.
It is (C)opyright 2013&ndash;2018 by Hanns Holger Rutz. All rights reserved. Desktop is released under
the [GNU Lesser General Public License](https://git.iem.at/sciss/Desktop/raw/master/LICENSE) v2.1+ and comes
with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`

## linking

To link to this library:

    "de.sciss" %% "desktop" % v

Furthermore, if you want to support a particular platform, also add some of the following:

    "de.sciss" %% "desktop-mac" % v
    "de.sciss" %% "desktop-linux" % v

The current version `v` is `"0.10.0"`.

## building

Desktop currently builds against Scala 2.12, 2.11, using sbt. To build sub project `"desktop-mac"`, you need either
OS X with Apple's EAWT extensions, or otherwise the
dummy [OrangeExtensions](http://ymasory.github.io/OrangeExtensions/) are used.

Some demos are available through `sbt desktop/test:run`. For Mac specific demos, `sbt desktop-mac/test:run`.

## contributing

Please see the file [CONTRIBUTING.md](CONTRIBUTING.md)

