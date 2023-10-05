
import classes.CButton
import classes.CSlider
import demos.classes.Animation
import kotlinx.coroutines.DelicateCoroutinesApi
import org.openrndr.WindowMultisample
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.draw.font.loadFace
import org.openrndr.extra.noise.random
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.shapes.grid
import org.openrndr.extra.shapes.rectify.RectifiedContour
import org.openrndr.extra.shapes.rectify.rectified
import org.openrndr.math.IntVector2
import org.openrndr.math.Matrix44
import org.openrndr.math.Vector2
import org.openrndr.math.transforms.scale
import org.openrndr.math.transforms.translate
import org.openrndr.shape.*
import org.openrndr.svg.loadSVG
import java.io.File


@OptIn(DelicateCoroutinesApi::class)
fun main() = application {
    configure {
        width = 608
        height = 342
        hideWindowDecorations = true
        windowAlwaysOnTop = true
        position = IntVector2(1285,110)
        windowTransparent = true
        multisample = WindowMultisample.SampleCount(4)
    }
    oliveProgram {
// MOUSE STUFF //////
        var mouseClick = false
        var mouseState = "up"
        mouse.dragged.listen { mouseState = "drag" }
        mouse.exited.listen { mouseState = "up" }
        mouse.buttonUp.listen { mouseState = "up"; mouseClick = true }
        mouse.buttonDown.listen { mouseState = "down" }
        mouse.moved.listen { mouseState = "move" }
// END //////////////
        val columnCount = 30
        val rowCount = 4
        val marginX = -20.0
        val marginY = -20.0
        val gutterX = 0.0
        val gutterY = 0.0
        var grid = drawer.bounds.grid(columnCount, rowCount, marginX, marginY, gutterX, gutterY)
        val flatGrid = grid.flatten()

        val incremCheck = onceObj()
        var palette = listOf(ColorRGBa.fromHex(0xF1934B), ColorRGBa.fromHex(0x0E8847), ColorRGBa.fromHex(0xD73E1C), ColorRGBa.fromHex(0xF4ECDF), ColorRGBa.fromHex(0x552F20))
        val white = ColorRGBa.WHITE
        val black = ColorRGBa.BLACK
        val animation = Animation()
        val loopDelay = 5.0
        val message = "hello"
        animation.loadFromJson(File("data/keyframes/keyframes-0.json"))
        val svgA = loadSVG(File("data/fonts/a.svg"))
        val firstShape = svgA.root.findShapes()[0].shape
        val firstContour = firstShape.contours[0]

        val image = loadImage("data/images/cheeta.jpg")
        val scale: DoubleArray = typeScale(3, 100.0, 3)
        val typeFace: Pair<List<FontMap>, List<FontImageMap>> = defaultTypeSetup(scale, listOf("reg", "reg", "bold"))
        val animArr = mutableListOf<Animation>()
        val randNums = mutableListOf<Double>()
        val charArr = message.toCharArray()

        var listOfIntersections: MutableList<ContourIntersection> = mutableListOf<ContourIntersection>()
        var listInsideContour = mutableListOf<Vector2>()

        for (gridItem in flatGrid) {
            for (contour in firstShape.contours) {
                val intersections = contour.intersections(gridItem.contour)
                listOfIntersections.addAll(intersections)
            }
        }

        listOfIntersections.forEach { e ->
            animArr.add(Animation())
            randNums.add(random(0.0, 1.0))
        }

        animArr.forEach { a ->
            a.loadFromJson(File("data/keyframes/keyframes-0.json"))
        }
        val globalSpeed = 0.01



        extend {
            animArr.forEachIndexed { i, a ->
                a((randNums[i] * 0.5 + frameCount * globalSpeed) % loopDelay)
            }
            drawer.clear(ColorRGBa.TRANSPARENT)
//            drawer.circle(drawer.bounds.center, 10.0)
            drawer.stroke = null
            drawer.fill = white
//            drawer.contours(firstShape.contours)
//            firstShape.contours.forEach { n->
//                n.in
//            }

//            var newIntersections: ListOf<Shape>


            drawer.stroke = black
            drawer.fill = white


            for (i in 2 until listOfIntersections.size step 2) {
                val current = listOfIntersections[i].position
                val next = listOfIntersections[i + 1].position
//                drawer.lineSegment(current.x, current.y, next.x, next.y)

                drawer.pushTransforms()
                val xOffset1 = animArr[i].circleSlider * 100.0
                val yOffset1 = animArr[i].circleSlider * 1.0

                val xOffset2 = animArr[i].circleSlider * 120.0
                val yOffset2 = animArr[i].circleSlider * 1.0

                drawer.contour(contour {
                    moveTo(Vector2(current.x + xOffset1, current.y + yOffset1))
                    lineTo(Vector2(next.x + xOffset2, next.y + yOffset2))
                    lineTo(Vector2(next.x + xOffset2 + 10, next.y + yOffset2 + 10))
                    lineTo(Vector2(current.x + xOffset1 + 10, current.y + yOffset1 + 10))
                    close()
                })

                drawer.popTransforms()
            }
            listOfIntersections.forEach { n->
//                drawer.circle(n.position, 1.0)
            }

            // THIS NEEDS TO STAY AT THE END //
            if (mouseClick) mouseClick = false
            // END END ////////////////////////
            drawer.fill = null
            drawer.stroke = null
            flatGrid.forEach{ n ->
                drawer.rectangle(n)
            }
        }
    }
}