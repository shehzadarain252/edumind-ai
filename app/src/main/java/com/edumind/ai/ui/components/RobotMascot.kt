package com.edumind.ai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Original EduMind robot mascot, drawn with Canvas (no copyrighted assets).
 * A friendly violet robot face with an antenna, used on the splash screen,
 * home screen and AI chat.
 */
@Composable
fun RobotMascot(size: Dp = 120.dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val headTop = h * 0.22f
        val headH = h * 0.62f
        val headW = w * 0.72f
        val headLeft = cx - headW / 2f

        val violet = Color(0xFF7C3AED)
        val violetLight = Color(0xFFA78BFA)
        val violetDark = Color(0xFF5B21B6)
        val dark = Color(0xFF17132B)
        val white = Color.White
        val glow = Color(0xFFEDE9FE)

        // Glow behind the head.
        drawCircle(
            color = glow,
            radius = w * 0.46f,
            center = Offset(cx, h * 0.52f),
        )
        // Antenna.
        drawLine(
            color = violet,
            start = Offset(cx, headTop - h * 0.10f),
            end = Offset(cx, headTop),
            strokeWidth = w * 0.03f,
        )
        drawCircle(
            color = violetLight,
            radius = w * 0.055f,
            center = Offset(cx, headTop - h * 0.12f),
        )
        // Ears.
        drawCircle(
            color = violetDark,
            radius = w * 0.05f,
            center = Offset(headLeft - w * 0.02f, headTop + headH * 0.5f),
        )
        drawCircle(
            color = violetDark,
            radius = w * 0.05f,
            center = Offset(headLeft + headW + w * 0.02f, headTop + headH * 0.5f),
        )
        // Head.
        drawRoundRect(
            color = violet,
            topLeft = Offset(headLeft, headTop),
            size = Size(headW, headH),
            cornerRadius = CornerRadius(w * 0.16f, w * 0.16f),
        )
        // Face screen.
        drawRoundRect(
            color = Color(0xFF2E1065),
            topLeft = Offset(headLeft + w * 0.08f, headTop + h * 0.08f),
            size = Size(headW - w * 0.16f, headH - h * 0.16f),
            cornerRadius = CornerRadius(w * 0.12f, w * 0.12f),
        )
        // Eyes.
        val eyeY = headTop + headH * 0.38f
        val eyeR = w * 0.075f
        drawCircle(color = Color(0xFF67E8F9), radius = eyeR, center = Offset(cx - w * 0.16f, eyeY))
        drawCircle(color = Color(0xFF67E8F9), radius = eyeR, center = Offset(cx + w * 0.16f, eyeY))
        drawCircle(color = white, radius = eyeR * 0.35f, center = Offset(cx - w * 0.16f - eyeR * 0.3f, eyeY - eyeR * 0.3f))
        drawCircle(color = white, radius = eyeR * 0.35f, center = Offset(cx + w * 0.16f - eyeR * 0.3f, eyeY - eyeR * 0.3f))
        // Smile.
        drawArc(
            color = Color(0xFF67E8F9),
            startAngle = 15f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(cx - w * 0.12f, headTop + headH * 0.52f),
            size = Size(w * 0.24f, h * 0.16f),
            style = Stroke(width = w * 0.035f),
        )
    }
}
