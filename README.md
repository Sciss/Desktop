# Desktop

## statement

Desktop is an application framework for Scala on the desktop, including support for Swing. It is (C)opyright 2013&ndash;2014 by Hanns Holger Rutz. All rights reserved. Desktop is released under the [GNU Lesser General Public License](https://raw.github.com/Sciss/Desktop/master/LICENSE) v3+ and comes with absolutely no warranties. To contact the author, send an email to `contact at sciss.de`

## linking

To link to this library:

    "de.sciss" %% "desktop" % v

Furthermore, if you want to support the Mac (OS X) platform, also add the following:

    "de.sciss" %% "desktop-mac" % v

The current version `v` is `"0.4.1+"`.

## building

Desktop currently builds against Scala 2.10, using sbt 0.13. To build sub project `"desktop-mac"`, you need either OS X, or otherwise copy the [OrangeExtensions](http://ymasory.github.io/OrangeExtensions/) jar into the unmanaged library folder `lib`.

Some demos are available through `sbt desktop/test:run`. For Mac specific demos, `sbt desktop-mac/test:run`.