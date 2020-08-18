# Desktop

[![Build Status](https://travis-ci.org/Sciss/Desktop.svg?branch=main)](https://travis-ci.org/Sciss/Desktop)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.sciss/desktop_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.sciss/desktop_2.12)
<a href="https://liberapay.com/sciss/donate"><img alt="Donate using Liberapay" src="https://liberapay.com/assets/widgets/donate.svg" height="24"></a>

## statement

Desktop is an application framework for Scala on the desktop, including support for Swing.
It is (C)opyright 2013&ndash;2020 by Hanns Holger Rutz. All rights reserved. Desktop is released under
the [GNU Lesser General Public License](https://git.iem.at/sciss/Desktop/raw/main/LICENSE) v2.1+ and comes
with absolutely no warranties. To contact the author, send an e-mail to `contact at sciss.de`

Please consider supporting this project through Liberapay (see badge above) – thank you!

## linking

To link to this library:

    "de.sciss" %% "desktop" % v

Furthermore, if you want to support a particular platform, also add some of the following:

    "de.sciss" %% "desktop-mac" % v
    "de.sciss" %% "desktop-linux" % v

The current version `v` is `"0.10.7"`.

## building

Desktop currently builds against Scala 2.13, 2.12, using sbt. The last version to support Scala 2.11 was 0.10.4.
To build sub project `"desktop-mac"`, you need either OS X with Apple's EAWT extensions, or otherwise the
dummy [OrangeExtensions](http://ymasory.github.io/OrangeExtensions/) are used.

Note that as of 0.10.5, you need Java 9 or newer to compile; however the library can still be used on
Java 8. Java 9 is needed to compile support for Java 9 `java.awt.Desktop` API.

Some demos are available through `sbt desktop/test:run`. For Mac specific demos, `sbt desktop-mac/test:run`.
Note that when you run on JDK 11 on macOS, it will use the Java 9 API instead of EAWT.

## contributing

Please see the file [CONTRIBUTING.md](CONTRIBUTING.md)

