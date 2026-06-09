package com.example.ui.game

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class Track(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: String,
    val skyColor: Color,
    val groundColor: Color,
    val roadColor: Color,
    val stripeColor: Color,
    val lapLengthMeters: Float = 3000f, // 3km per lap. average speed 180km/h (50m/s) -> 60s per lap. Perfect!
    val segments: List<TrackSegment>
)

data class TrackSegment(
    val name: String,
    val lengthMeters: Float,
    val curvature: Float, // 0.0 is straight, positive is curving right, negative curving left
    val elevationChange: Float = 0f, // vertical slope curves
    val type: SegmentType
)

enum class SegmentType {
    STRAIGHT,
    OVAL_LOOP,
    TECHNICAL_CORNER,
    ELEVATION_CURVE,
    SPIRAL_OUT,
    HYBRID_ZONE
}

object Tracks {
    val list = listOf(
        Track(
            id = "coastal_loop",
            name = "Coastal Loop Circuit",
            description = "Long oval ocean highway coupled with technical bayside S-curves. Perfect for balanced speeds.",
            difficulty = "Medium",
            skyColor = Color(0xFFF77F00), // Sunset Orange
            groundColor = Color(0xFFE9C46A), // Beach Sand
            roadColor = Color(0xFF264653), // Deep Bayside Slate
            stripeColor = Color(0xFFE76F51), // Coral Orange
            lapLengthMeters = 3000f,
            segments = listOf(
                TrackSegment("Ocean Highway", 1000f, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT),
                TrackSegment("Bay Technical Corners", 600f, curvature = -0.3f, elevationChange = 5f, SegmentType.TECHNICAL_CORNER),
                TrackSegment("S-Curve Bay Exit", 400f, curvature = 0.3f, elevationChange = -5f, SegmentType.TECHNICAL_CORNER),
                TrackSegment("Wide Oval Loop", 600f, curvature = 0.15f, elevationChange = 0f, SegmentType.OVAL_LOOP),
                TrackSegment("Beachside Finishing Straight", 400f, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT)
            )
        ),
        Track(
            id = "industrial_triangle",
            name = "Industrial Triangle Run",
            description = "Large triangle framework with heavy braking zones and rusty corner sequences. Precision-demanding.",
            difficulty = "Hard",
            skyColor = Color(0xFF343A40), // Dark Industrial Smokey Gray
            groundColor = Color(0xFF6C757D), // Concrete Slate
            roadColor = Color(0xFF212529), // Carbon Asphalt
            stripeColor = Color(0xFFFCA311), // Warning Yellow Stripes
            lapLengthMeters = 3500f,
            segments = listOf(
                TrackSegment("Connecting Freeway", 1200f, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT),
                TrackSegment("Apex Triangle Intersection", 500f, curvature = 0.5f, elevationChange = 0f, SegmentType.TECHNICAL_CORNER),
                TrackSegment("Ramp Decline", 600f, curvature = 0f, elevationChange = -15f, SegmentType.ELEVATION_CURVE),
                TrackSegment("Precision Braking Hairpins", 700f, curvature = -0.45f, elevationChange = 10f, SegmentType.TECHNICAL_CORNER),
                TrackSegment("Steel Bridge Straight", 500f, curvature = 0f, elevationChange = 5f, SegmentType.STRAIGHT)
            )
        ),
        Track(
            id = "spiral_city",
            name = "Spiral City Circuit",
            description = "Neon outer ring layout that gradually spirals inward. Cornering intensity scales up per lap.",
            difficulty = "Expert",
            skyColor = Color(0xFF10002B), // Stellar Deep Void
            groundColor = Color(0xFF240046), // Dark Cyber Purple
            roadColor = Color(0xFF03071E), // Obsidian Tech Road
            stripeColor = Color(0xFF7209B7), // Laser Magenta
            lapLengthMeters = 3200f,
            segments = listOf(
                TrackSegment("Outer Skyline Straight", 800f, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT),
                TrackSegment("Cyber Inward Loop 1", 700f, curvature = 0.2f, elevationChange = 8f, SegmentType.OVAL_LOOP),
                TrackSegment("Cyber Inward Loop 2", 700f, curvature = 0.3f, elevationChange = -8f, SegmentType.SPIRAL_OUT),
                TrackSegment("Technical Neon S-Bends", 600f, curvature = -0.4f, elevationChange = 0f, SegmentType.TECHNICAL_CORNER),
                TrackSegment("Grid Center Finish", 400f, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT)
            )
        ),
        Track(
            id = "metro_grid",
            name = "Metro Grid Circuit",
            description = "Square street tracks. Short straight segments with sharp 90-degree intersections. High technical ceiling.",
            difficulty = "Medium",
            skyColor = Color(0xFF1A1A24), // Evening Skylit Navy
            groundColor = Color(0xFF2B2B38), // Grey Metropolitan Roofs
            roadColor = Color(0xFF16161D), // Concrete Lanes
            stripeColor = Color(0xFF00D2FF), // Neon Azure Cyan
            lapLengthMeters = 2800f,
            segments = listOf(
                TrackSegment("Metro Avenue 1", 600f, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT),
                TrackSegment("First 90-Deg Junction", 400f, curvature = 0.6f, elevationChange = 0f, SegmentType.TECHNICAL_CORNER),
                TrackSegment("Subway Undercut", 600f, curvature = 0f, elevationChange = -20f, SegmentType.ELEVATION_CURVE),
                TrackSegment("Avenue Corner Chambers", 600f, curvature = -0.5f, elevationChange = 20f, SegmentType.HYBRID_ZONE),
                TrackSegment("Interstate Finish Line", 600f, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT)
            )
        )
    )

    fun generateRandomTrack(type: String): Track {
        val randomNum = Random.nextInt(100, 999)
        val randomId = "random_" + type + "_" + randomNum
        val name = when (type) {
            "oval" -> "Random Oval Circuit #$randomNum"
            "triangle" -> "Random Triangle GP #$randomNum"
            "serpentine" -> "Random Serpentine S-Trail #$randomNum"
            else -> "Randomized Complex Loop #$randomNum"
        }
        
        val desc = when (type) {
            "oval" -> "A cohesive 2-curve oval layout featuring high-speed straightaways with two sweeping wide corners."
            "triangle" -> "A precise three-apex technical triangle with extreme hairpin corners demanding immediate deceleration."
            "serpentine" -> "An intense, continuous winding snake path shifting dynamically between left and right S-curves."
            else -> "A fully hyper-randomized segmented roller coaster circuit testing all core reaction times."
        }
        
        val difficulty = when (type) {
            "oval" -> "Medium"
            "triangle" -> "Hard"
            "serpentine" -> "Expert"
            else -> "Hard"
        }

        val skyColors = listOf(Color(0xFF1B4965), Color(0xFF3A0CA3), Color(0xFF0F172A), Color(0xFF621B00), Color(0xFF141419))
        val groundColors = listOf(Color(0xFF5C677D), Color(0xFFE76F51), Color(0xFF0A0F1D), Color(0xFF2C1A4D))
        val roadColors = listOf(Color(0xFF1E293B), Color(0xFF4A4E69), Color(0xFF0F172A), Color(0xFF131516))
        val stripeColors = listOf(Color(0xFF06B6D4), Color(0xFFFBBF24), Color(0xFF10B981), Color(0xFFA855F7))

        val sky = skyColors.random()
        val ground = groundColors.random()
        val road = roadColors.random()
        val stripe = stripeColors.random()

        val segments = when (type) {
            "oval" -> {
                val isSweepingRight = Random.nextBoolean()
                val curvePower = if (isSweepingRight) 0.38f else -0.38f
                val straightLen = Random.nextFloat() * 400f + 850f
                val curveLen = Random.nextFloat() * 200f + 450f
                listOf(
                    TrackSegment("Main Superstraight", straightLen, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT),
                    TrackSegment("Sweeping Turn 1", curveLen, curvature = curvePower, elevationChange = 5f, SegmentType.OVAL_LOOP),
                    TrackSegment("Backstretch Straight", straightLen, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT),
                    TrackSegment("Sweeping Turn 2", curveLen, curvature = curvePower, elevationChange = -5f, SegmentType.OVAL_LOOP)
                )
            }
            "triangle" -> {
                val isTurningRight = Random.nextBoolean()
                val curvePower = if (isTurningRight) 0.65f else -0.65f
                val straight1 = Random.nextFloat() * 300f + 700f
                val straight2 = Random.nextFloat() * 300f + 700f
                val straight3 = Random.nextFloat() * 300f + 700f
                val turnLen = Random.nextFloat() * 150f + 250f
                listOf(
                    TrackSegment("Alpha Base Straight", straight1, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT),
                    TrackSegment("Apex Intersection 1", turnLen, curvature = curvePower, elevationChange = 8f, SegmentType.TECHNICAL_CORNER),
                    TrackSegment("Beta Climb Straight", straight2, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT),
                    TrackSegment("Apex Intersection 2", turnLen, curvature = curvePower, elevationChange = -15f, SegmentType.TECHNICAL_CORNER),
                    TrackSegment("Gamma Descent Straight", straight3, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT),
                    TrackSegment("Apex Intersection 3", turnLen, curvature = curvePower, elevationChange = 7f, SegmentType.TECHNICAL_CORNER)
                )
            }
            "serpentine" -> {
                val s1 = Random.nextFloat() * 150f + 200f
                val c1 = Random.nextFloat() * 0.15f + 0.35f
                listOf(
                    TrackSegment("Starting Straightaway", 400f, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT),
                    TrackSegment("Bayside Serpent Right", s1, curvature = c1, elevationChange = 4f, SegmentType.TECHNICAL_CORNER),
                    TrackSegment("Bayside Serpent Left", s1, curvature = -c1, elevationChange = -4f, SegmentType.TECHNICAL_CORNER),
                    TrackSegment("Bayside Serpent Right II", s1, curvature = c1, elevationChange = 4f, SegmentType.TECHNICAL_CORNER),
                    TrackSegment("Bayside Serpent Left II", s1, curvature = -c1, elevationChange = -4f, SegmentType.TECHNICAL_CORNER),
                    TrackSegment("Serpent Apex Loop", s1 + 100f, curvature = c1 * 1.3f, elevationChange = 0f, SegmentType.OVAL_LOOP),
                    TrackSegment("Home stretch", 300f, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT)
                )
            }
            else -> {
                val straightLen = Random.nextFloat() * 300f + 500f
                listOf(
                    TrackSegment("Custom Straightaway", straightLen, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT),
                    TrackSegment("Custom Tight Grip Left", 350f, curvature = -0.45f, elevationChange = -5f, SegmentType.TECHNICAL_CORNER),
                    TrackSegment("Custom Winding S-Bend", 400f, curvature = 0.4f, elevationChange = 5f, SegmentType.TECHNICAL_CORNER),
                    TrackSegment("Custom Fast Lane", 600f, curvature = 0f, elevationChange = 0f, SegmentType.STRAIGHT)
                )
            }
        }

        val lapLength = segments.sumOf { it.lengthMeters.toDouble() }.toFloat()

        return Track(
            id = randomId,
            name = name,
            description = desc,
            difficulty = difficulty,
            skyColor = sky,
            groundColor = ground,
            roadColor = road,
            stripeColor = stripe,
            lapLengthMeters = lapLength,
            segments = segments
        )
    }
}
