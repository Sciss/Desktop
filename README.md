# Desktop

## statement

Desktop is an application framework for Scala on the desktop, including support for Swing. It is (C)opyright 2013&ndash;2014 by Hanns Holger Rutz. All rights reserved. Desktop is released under the [GNU Lesser General Public License](https://raw.github.com/Sciss/Desktop/master/LICENSE) v3+ and comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`

## linking

To link to this library:

    libraryDependencies += "de.sciss" %% "desktop" % v

The current version `v` is `"0.4.+"`.

## building

ScalaDesktop currently builds against Scala 2.10, using sbt 0.13. It uses uses [ScalaSwingContrib](https://github.com/benhutchison/ScalaSwingContrib) for the popup menu wrapper.

Some demos are available through `sbt desktop-core/test:run`.