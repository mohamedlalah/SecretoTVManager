// SPDX-License-Identifier: GPL-3.0-only
package app.tellyscope.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import kotlin.math.ceil

/** The supplied zellige lattice, reconstructed as centerlines over one periodic cell.
 * Coordinates can cross the cell edge so neighboring copies meet without a border.
 */
private val lattice = """
    M8,21 L10,-2 L32,-2 M8,21 L10,28 M8,21 L4,20 L-7,8 L-10,7 L-25,21 L-28,22 M8,137 L8,132 L4,122 L12,113 L13,110 M8,137 L9,140
    M8,137 L3,137 L-7,132 L-12,132 L-22,137 L-28,137 M8,160 L10,154 M8,160 L9,161 L9,182 L31,182 L32,183 M8,160 L4,161 L-7,173 L-12,173 L-24,161 L-28,160 M9,43 L10,41
    M9,43 L8,50 L4,59 L12,68 L13,72 M9,43 L1,45 L-6,49 L-12,49 L-28,43 M9,140 L19,144 M9,140 L10,154 M10,28 L14,29 L19,34 L19,36
    M10,28 L9,28 L10,41 M10,41 L19,38 L19,36 M10,154 L13,153 L19,147 L19,144 M13,72 L20,73 M13,72 L-2,73 L-6,84 L-10,87
    M13,110 L19,109 M13,110 L-1,109 L-7,96 L-10,94 M19,36 L25,36 L31,34 M19,109 L20,105 L32,93 L32,88 L21,78 L20,73 M19,109 L41,109 L41,132 L42,133
    M19,144 L27,147 L31,147 M20,73 L21,72 L41,72 L41,50 L42,49 M31,34 L32,32 L41,30 M31,34 L35,40 L41,43 M31,147 L37,140 L41,139
    M31,147 L32,149 L41,151 M32,183 L39,182 M32,183 L31,187 L18,200 L18,203 L31,216 L32,220 M32,220 L39,221 M39,182 L40,177 L46,172 L46,167 L42,158 L42,154
    M39,182 L52,183 L58,197 L60,197 M39,221 L52,220 L58,206 L60,206 M39,221 L40,226 L46,232 L46,236 L43,241 L41,249 M41,27 L46,27 L58,21 L62,21 L73,27 L78,27 M41,27 L41,30
    M41,30 L41,43 M41,43 L42,49 M41,139 L42,133 M41,139 L41,151 M41,151 L42,154
    M42,49 L46,50 L58,63 L61,63 L74,50 L77,49 M42,133 L45,132 L60,118 L75,132 L77,132 M42,154 L48,155 L57,160 L63,160 L67,157 L78,154 M60,197 L63,195 L64,190 L69,182 L81,182 M60,197 L60,206
    M60,206 L63,208 L64,213 L68,220 L82,221 M77,49 L79,43 M77,49 L78,50 L78,72 L100,72 L101,73 M77,132 L79,109 L101,109 M77,132 L79,139
    M78,27 L78,23 L73,11 L81,3 L82,-1 M78,27 L79,30 M78,154 L79,151 M78,154 L77,161 L73,170 L81,178 L81,182 M79,30 L88,32 L88,34
    M79,30 L78,30 L79,43 M79,43 L82,42 L88,36 L88,34 M79,139 L83,140 L88,145 L88,147 M79,139 L78,139 L78,151 L79,151 M79,151 L83,151 L88,147
    M81,182 L88,183 M82,221 L88,220 M88,34 L101,36 M88,147 L101,145 M88,183 L89,182 L110,182 L110,161 L111,160
    M88,183 L89,187 L102,200 L102,203 L89,216 L88,220 M88,220 L110,220 L111,221 L110,226 L111,244 M101,36 L106,29 L110,28 M101,36 L102,39 L110,40 M101,73 L107,72
    M101,73 L100,76 L87,90 L88,93 L101,106 L101,109 M101,109 L108,110 M101,145 L102,143 L110,140 M101,145 L106,152 L110,153 M107,72 L108,68 L115,61 L115,56 L111,47 L111,43
    M107,72 L121,72 L126,81 L127,86 L129,87 M108,110 L121,109 L126,100 L127,95 L129,94 M108,110 L109,114 L115,120 L115,126 L113,128 L111,137 M110,28 L111,22 M110,28 L111,29 L110,40
    M110,40 L111,43 M110,140 L111,137 M110,140 L111,141 L110,153 M110,153 L111,160 M129,87 L129,94
""".trimIndent()

private const val TileWidth = 139f
private const val TileHeight = 222f

/** Decorative layer behind a screen's content. The path is rebuilt only when its draw cache changes. */
@Composable
internal fun Modifier.zelligeBackground(background: Color = MaterialTheme.colorScheme.background): Modifier {
    val colors = MaterialTheme.colorScheme
    if (background.luminance() < 0.5f) return drawWithCache {
        onDrawBehind { drawRect(background) }
    }
    val outline = colors.primary.copy(alpha = 0.048f)

    return drawWithCache {
        val unit = 112.dp.toPx() / TileWidth
        val tile = PathParser().parsePathString(lattice).toPath()
        val columns = ceil(size.width / (TileWidth * unit)).toInt()
        val rows = ceil(size.height / (TileHeight * unit)).toInt()
        val field = Path().apply {
            for (row in -1..rows) {
                for (column in -1..columns) {
                    addPath(tile, Offset(column * TileWidth, row * TileHeight))
                }
            }
        }
        val stroke = Stroke(width = 0.75.dp.toPx() / unit, cap = StrokeCap.Round, join = StrokeJoin.Round)
        onDrawBehind {
            drawRect(background)
            scale(unit, unit, pivot = Offset.Zero) {
                drawPath(field, outline, style = stroke)
            }
        }
    }
}
