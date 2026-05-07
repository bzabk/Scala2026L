package SudokuGame.ui.layout

import scalafx.geometry.Pos
import scalafx.scene.Node
import scalafx.scene.layout.{BorderPane, StackPane}

class MainApplicationLayout {

  private val baseLayout = new BorderPane {
    style = "-fx-background-color: #0B1120;"
  }

  val view: StackPane = new StackPane {
    children = Seq(baseLayout)
  }

  private var overlayPane: StackPane = _

  def setSidebar(node: Node): Unit =
    baseLayout.left = node

  def setMainContent(node: Node): Unit =
    baseLayout.center = node

  def showOverlay(content: Node): Unit = {
    hideOverlay()
    overlayPane = new StackPane {
      style = "-fx-background-color: rgba(0, 0, 0, 0.7);"
      alignment = Pos.Center
      children = content
    }
    view.children.add(overlayPane)
  }

  def hideOverlay(): Unit = {
    if (overlayPane != null) {
      view.children.remove(overlayPane)
      overlayPane = null
    }
  }
}
