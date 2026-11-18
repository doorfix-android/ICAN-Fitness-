package com.ican.fitness.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ican.fitness.domain.AnimationPattern
import com.ican.fitness.ui.theme.GraphiteElevated
import com.ican.fitness.ui.theme.OliveAccent
import com.ican.fitness.ui.theme.WarmCream
import com.ican.fitness.ui.theme.WarmCreamMuted
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ExerciseAnimationCanvas(
    pattern: AnimationPattern?,
    exerciseName: String,
    modifier: Modifier = Modifier
) {
    if (pattern == null) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(190.dp)
                .background(GraphiteElevated, RoundedCornerShape(16.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Illustrated Guide: $exerciseName\n(Animation unavailable — follow step-by-step form cues below)",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmCreamMuted
            )
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "exercise_cycle")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rep_phase"
    )

    val phaseLabel = when {
        phase < 0.25f -> "Start Position"
        phase > 0.75f -> "Peak Contraction"
        else -> "Controlled Movement"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(GraphiteElevated, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LIVE FORM CYCLE",
                style = MaterialTheme.typography.labelLarge,
                color = OliveAccent,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = phaseLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = WarmCreamMuted
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(165.dp)
        ) {
            val w = size.width
            val h = size.height
            val floorY = h * 0.85f

            // Ground line
            drawLine(
                color = WarmCreamMuted.copy(alpha = 0.25f),
                start = Offset(w * 0.1f, floorY),
                end = Offset(w * 0.9f, floorY),
                strokeWidth = 3f
            )

            val bodyColor = WarmCream
            val activeMuscleColor = OliveAccent
            val jointColor = Color(0xFFD6A85A)

            fun drawLimb(a: Offset, b: Offset, highlight: Boolean = false, width: Float = 10f) {
                drawLine(
                    color = if (highlight) activeMuscleColor else bodyColor,
                    start = a,
                    end = b,
                    strokeWidth = width,
                    cap = StrokeCap.Round
                )
                drawCircle(color = jointColor, radius = 5f, center = a)
                drawCircle(color = jointColor, radius = 5f, center = b)
            }

            when (pattern) {
                AnimationPattern.PUSH_HORIZONTAL -> {
                    val foot = Offset(w * 0.24f, floorY - 6f)
                    val hand = Offset(w * 0.72f, floorY - 6f)
                    val lift = 58f * (1f - phase * 0.65f)
                    val shoulder = Offset(w * 0.70f, floorY - lift)
                    val hip = Offset(w * 0.47f, floorY - lift * 0.55f)
                    val elbow = Offset(
                        (shoulder.x + hand.x) / 2f - 14f * phase,
                        (shoulder.y + hand.y) / 2f
                    )
                    val head = Offset(shoulder.x + 22f, shoulder.y - 10f)

                    drawLimb(foot, hip, width = 11f)
                    drawLimb(hip, shoulder, highlight = true, width = 13f)
                    drawLimb(shoulder, elbow, highlight = true, width = 9f)
                    drawLimb(elbow, hand, highlight = true, width = 9f)
                    drawCircle(color = bodyColor, radius = 14f, center = head)
                }

                AnimationPattern.SQUAT_PATTERN -> {
                    val ankle = Offset(w * 0.50f, floorY - 6f)
                    val drop = 52f * phase
                    val knee = Offset(w * 0.50f + 28f * phase, floorY - 52f + drop * 0.35f)
                    val hip = Offset(w * 0.50f - 26f * phase, floorY - 98f + drop)
                    val shoulder = Offset(hip.x + 14f * phase, hip.y - 54f)
                    val head = Offset(shoulder.x + 4f, shoulder.y - 20f)
                    val hand = Offset(shoulder.x + 36f * phase + 12f, shoulder.y + 18f * (1f - phase))

                    drawLimb(ankle, knee, highlight = true, width = 11f)
                    drawLimb(knee, hip, highlight = true, width = 12f)
                    drawLimb(hip, shoulder, width = 13f)
                    drawLimb(shoulder, hand, width = 8f)
                    drawCircle(color = bodyColor, radius = 14f, center = head)
                }

                AnimationPattern.PRESS_OVERHEAD -> {
                    val foot = Offset(w * 0.50f, floorY - 6f)
                    val hip = Offset(w * 0.50f, floorY - 76f)
                    val shoulder = Offset(w * 0.50f, floorY - 128f)
                    val head = Offset(w * 0.50f, shoulder.y - 20f)
                    val handY = shoulder.y - 44f * phase
                    val elbow = Offset(w * 0.50f + 26f * (1f - phase * 0.4f), (shoulder.y + handY) / 2f)
                    val hand = Offset(w * 0.50f + 16f, handY)

                    drawLimb(foot, hip, width = 11f)
                    drawLimb(hip, shoulder, width = 13f)
                    drawLimb(shoulder, elbow, highlight = true, width = 9f)
                    drawLimb(elbow, hand, highlight = true, width = 9f)
                    drawCircle(color = activeMuscleColor, radius = 8f, center = hand)
                    drawCircle(color = bodyColor, radius = 14f, center = head)
                }

                AnimationPattern.CURL_ARM -> {
                    val foot = Offset(w * 0.48f, floorY - 6f)
                    val hip = Offset(w * 0.48f, floorY - 76f)
                    val shoulder = Offset(w * 0.48f, floorY - 128f)
                    val head = Offset(w * 0.48f, shoulder.y - 20f)
                    val elbow = Offset(w * 0.50f, shoulder.y + 34f)
                    val angle = -0.3f + phase * 2.1f
                    val hand = Offset(
                        elbow.x + 34f * sin(angle),
                        elbow.y + 34f * cos(angle)
                    )

                    drawLimb(foot, hip, width = 11f)
                    drawLimb(hip, shoulder, width = 13f)
                    drawLimb(shoulder, elbow, highlight = true, width = 9f)
                    drawLimb(elbow, hand, highlight = true, width = 9f)
                    drawCircle(color = activeMuscleColor, radius = 7f, center = hand)
                    drawCircle(color = bodyColor, radius = 14f, center = head)
                }

                AnimationPattern.ROW_HORIZONTAL, AnimationPattern.PULL_VERTICAL -> {
                    val foot = Offset(w * 0.42f, floorY - 6f)
                    val hip = Offset(w * 0.42f, floorY - 74f)
                    val shoulder = Offset(w * 0.58f, floorY - 108f)
                    val head = Offset(shoulder.x + 18f, shoulder.y - 12f)
                    val elbow = Offset(shoulder.x - 18f * phase, shoulder.y + 18f - 22f * phase)
                    val hand = Offset(shoulder.x + 6f, shoulder.y + 44f - 28f * phase)

                    drawLimb(foot, hip, width = 11f)
                    drawLimb(hip, shoulder, highlight = true, width = 13f)
                    drawLimb(shoulder, elbow, highlight = true, width = 9f)
                    drawLimb(elbow, hand, width = 9f)
                    drawCircle(color = bodyColor, radius = 14f, center = head)
                }

                AnimationPattern.HINGE_GLUTE -> {
                    val foot = Offset(w * 0.72f, floorY - 6f)
                    val knee = Offset(w * 0.62f, floorY - 46f)
                    val shoulder = Offset(w * 0.30f, floorY - 12f)
                    val hip = Offset(w * 0.47f, floorY - 12f - 34f * phase)
                    val head = Offset(shoulder.x - 20f, shoulder.y - 4f)

                    drawLimb(foot, knee, width = 10f)
                    drawLimb(knee, hip, highlight = true, width = 12f)
                    drawLimb(hip, shoulder, highlight = true, width = 12f)
                    drawCircle(color = bodyColor, radius = 13f, center = head)
                }

                AnimationPattern.PLANK_CORE -> {
                    val foot = Offset(w * 0.24f, floorY - 6f)
                    val elbow = Offset(w * 0.72f, floorY - 6f)
                    val shoulder = Offset(w * 0.72f, floorY - 34f)
                    val hip = Offset(w * 0.48f, floorY - 32f - 4f * phase)
                    val kneeDrive = Offset(w * 0.36f + 22f * phase, floorY - 16f - 10f * phase)
                    val head = Offset(shoulder.x + 22f, shoulder.y - 6f)

                    drawLimb(foot, kneeDrive, width = 10f)
                    drawLimb(kneeDrive, hip, highlight = true, width = 11f)
                    drawLimb(hip, shoulder, highlight = true, width = 13f)
                    drawLimb(shoulder, elbow, width = 9f)
                    drawCircle(color = bodyColor, radius = 13f, center = head)
                }

                AnimationPattern.JUMPING_CARDIO -> {
                    val jumpLift = 18f * phase
                    val hip = Offset(w * 0.50f, floorY - 74f - jumpLift)
                    val shoulder = Offset(w * 0.50f, hip.y - 48f)
                    val head = Offset(w * 0.50f, shoulder.y - 20f)
                    val spread = 28f * phase
                    val leftFoot = Offset(w * 0.50f - 8f - spread, floorY - 6f - jumpLift)
                    val rightFoot = Offset(w * 0.50f + 8f + spread, floorY - 6f - jumpLift)
                    val leftHand = Offset(w * 0.50f - 18f - spread, shoulder.y + 30f - 66f * phase)
                    val rightHand = Offset(w * 0.50f + 18f + spread, shoulder.y + 30f - 66f * phase)

                    drawLimb(hip, leftFoot, highlight = true, width = 10f)
                    drawLimb(hip, rightFoot, highlight = true, width = 10f)
                    drawLimb(hip, shoulder, width = 12f)
                    drawLimb(shoulder, leftHand, highlight = true, width = 8f)
                    drawLimb(shoulder, rightHand, highlight = true, width = 8f)
                    drawCircle(color = bodyColor, radius = 13f, center = head)
                }

                AnimationPattern.EXTEND_TRICEP -> {
                    val foot = Offset(w * 0.68f, floorY - 6f)
                    val hip = Offset(w * 0.48f, floorY - 38f + 16f * phase)
                    val shoulder = Offset(w * 0.44f, hip.y - 42f)
                    val hand = Offset(w * 0.34f, floorY - 46f)
                    val elbow = Offset(w * 0.33f - 10f * phase, (shoulder.y + hand.y) / 2f)
                    val head = Offset(shoulder.x + 4f, shoulder.y - 18f)

                    drawLimb(foot, hip, width = 10f)
                    drawLimb(hip, shoulder, width = 12f)
                    drawLimb(shoulder, elbow, highlight = true, width = 9f)
                    drawLimb(elbow, hand, highlight = true, width = 9f)
                    drawCircle(color = bodyColor, radius = 13f, center = head)
                }

                AnimationPattern.STRETCH_HOLD, AnimationPattern.MOBILITY_FLOW -> {
                    val knee = Offset(w * 0.35f, floorY - 6f)
                    val hand = Offset(w * 0.65f, floorY - 6f)
                    val hip = Offset(w * 0.35f, floorY - 46f)
                    val shoulder = Offset(w * 0.65f, floorY - 46f)
                    val spineMid = Offset(w * 0.50f, floorY - 46f + (if (phase > 0.5f) -18f else 16f))
                    val head = Offset(shoulder.x + 20f, shoulder.y - 8f)

                    drawLimb(knee, hip, width = 10f)
                    drawLimb(hip, spineMid, highlight = true, width = 12f)
                    drawLimb(spineMid, shoulder, highlight = true, width = 12f)
                    drawLimb(shoulder, hand, width = 9f)
                    drawCircle(color = bodyColor, radius = 13f, center = head)
                }
            }
        }
    }
}
