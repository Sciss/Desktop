# Desktop

[![Flattr this](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?user_id=sciss&url=https%3A%2F%2Fgithub.com%2FSciss%2FDesktop&title=Desktop%20Library&language=Scala&tags=github&category=software)
[![Build Status](https://travis-ci.org/Sciss/Desktop.svg?branch=master)](https://travis-ci.org/Sciss/Desktop)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/de.sciss/desktop_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/de.sciss/desktop_2.11)

## statement

Desktop is an application framework for Scala on the desktop, including support for Swing. It is (C)opyright 2013&ndash;2015 by Hanns Holger Rutz. All rights reserved. Desktop is released under the [GNU Lesser General Public License](https://raw.github.com/Sciss/Desktop/master/LICENSE) v2.1+ and comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`

## linking

To link to this library:

    "de.sciss" %% "desktop" % v

Furthermore, if you want to support the Mac (OS X) platform, also add the following:

    "de.sciss" %% "desktop-mac" % v

The current version `v` is `"0.7.1"`.

## building

Desktop currently builds against Scala 2.11, 2.10, using sbt 0.13. To build sub project `"desktop-mac"`, you need either OS X with Apple's EAWT extensions,
or otherwise the dummy [OrangeExtensions](http://ymasory.github.io/OrangeExtensions/) are used.

Some demos are available through `sbt desktop/test:run`. For Mac specific demos, `sbt desktop-mac/test:run`.
