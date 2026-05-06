package SudokuGame.ui.layout

import scalafx.scene.Node
import scalafx.scene.layout.{BorderPane, StackPane}

class MainApplicationLayout {

  private val baseLayout = new BorderPane {
    style = "-fx-background-color: #0B1120;"
  }

  val view: StackPane = new StackPane {
    children = Seq(baseLayout)
  }

  def setSidebar(node: Node): Unit = {
    baseLayout.left = node
  }

  def setMainContent(node: Node): Unit = {
    baseLayout.center = node
  }
}
