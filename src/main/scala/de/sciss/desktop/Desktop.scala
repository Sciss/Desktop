package de.sciss.desktop

object Desktop {
  final val isMac = sys.props("os.name").contains("Mac")
}