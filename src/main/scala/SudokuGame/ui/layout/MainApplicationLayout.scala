package SudokuGame.ui.layout

import javafx.animation.Animation
import scala.compiletime.uninitialized
import scalafx.animation.{KeyFrame, Timeline}
import scalafx.geometry.Pos
import scalafx.scene.Group
import scalafx.scene.Node
import scalafx.scene.layout.{BorderPane, StackPane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle
import scalafx.util.Duration

class MainApplicationLayout {

  private val baseLayout = new BorderPane {
    style = "-fx-background-color: #0B1120;"
  }

  private var overlayPane: StackPane = _
  

  private val dotCount    = 10
  private val orbitRadius = 26.0
  private val dotRadius   = 4.5

  private val dots: Seq[Circle] = (0 until dotCount).map { i =>
    val angle = Math.toRadians(i * 360.0 / dotCount - 90)
    new Circle {
      radius  = dotRadius
      centerX = orbitRadius * Math.cos(angle)
      centerY = orbitRadius * Math.sin(angle)
      fill    = Color.web("#3b82f6")
      opacity = 0.12
    }
  }

  private var activeIndex = 0

  private val spinnerTimeline = new Timeline {
    cycleCount = Animation.INDEFINITE
    keyFrames = Seq(
      KeyFrame(Duration(80), onFinished = _ => {
        dots.zipWithIndex.foreach { case (dot, i) =>
          dot.opacity = ((i - activeIndex + dotCount) % dotCount) match {
            case 0 => 1.0
            case 1 => 0.65
            case 2 => 0.38
            case 3 => 0.20
            case _ => 0.08
          }
        }
        activeIndex = (activeIndex + 1) % dotCount
      })
    )
  }

  private val spinnerGroup = new Group {
    children = dots
  }

  private val loadingOverlay = new StackPane {
    alignment = Pos.Center
    style     = "-fx-background-color: rgba(0, 0, 0, 0.55);"
    mouseTransparent = false
    children  = Seq(spinnerGroup)
  }


  val view: StackPane = new StackPane {
    children = Seq(baseLayout)
  }

  

  def setSidebar(node: Node): Unit =
    baseLayout.left = node

  def setMainContent(node: Node): Unit =
    baseLayout.center = node

  def showOverlay(content: Node): Unit = {
    hideOverlay()
    overlayPane = new StackPane {
      style     = "-fx-background-color: rgba(0, 0, 0, 0.7);"
      alignment = Pos.Center
      children  = content
    }
    view.children.add(overlayPane)
  }

  def hideOverlay(): Unit = {
    if (overlayPane != null) {
      view.children.remove(overlayPane)
      overlayPane = null
    }
  }

  def showLoading(): Unit = {
    view.children.remove(loadingOverlay.delegate)
    view.children.add(loadingOverlay)
    spinnerTimeline.play()
  }

  def hideLoading(): Unit = {
    spinnerTimeline.stop()
    activeIndex = 0
    view.children.remove(loadingOverlay.delegate)
  }
}