package com.diecastcollector.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage

private val NoPhotoBackground = Color(0xFFBDBDBD)
private val NoPhotoInk = Color(0xFF616161)

/**
 * A Model's photo, cropped to fill [modifier]'s size. [photo] is anything Coil loads: the photo's
 * address on the API, or the bytes of a photo picked but not uploaded yet. With no photo, shows
 * [NoPhoto] instead, so a missing photo doesn't pass for one still loading.
 */
@Composable
internal fun ModelPhoto(photo: Any?, contentDescription: String?, modifier: Modifier) {
    Box(modifier = modifier.clip(RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
        if (photo == null) {
            NoPhoto(Modifier.fillMaxSize())
        } else {
            AsyncImage(
                model = photo,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

/** Grey card with a crossed-out camera and "Sem foto", scaled to fit a list thumbnail or the form. */
@Composable
private fun NoPhoto(modifier: Modifier) {
    BoxWithConstraints(modifier = modifier.background(NoPhotoBackground), contentAlignment = Alignment.Center) {
        val side = minOf(maxWidth, maxHeight)
        // A thumbnail (56dp) gets a small icon and text; the form's photo area a big one.
        val iconSize: Dp = side * 0.4f
        val fontSize = if (side < 100.dp) 10.sp else 18.sp
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CrossedOutCamera(Modifier.size(iconSize))
            Text(
                "Sem foto",
                color = NoPhotoInk,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CrossedOutCamera(modifier: Modifier) {
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.08f
        val bodyTop = size.height * 0.3f
        // Viewfinder bump, body, lens.
        drawRoundRect(
            color = NoPhotoInk,
            topLeft = Offset(size.width * 0.35f, size.height * 0.18f),
            size = Size(size.width * 0.3f, size.height * 0.14f),
            cornerRadius = CornerRadius(stroke),
            style = Stroke(stroke)
        )
        drawRoundRect(
            color = NoPhotoInk,
            topLeft = Offset(size.width * 0.08f, bodyTop),
            size = Size(size.width * 0.84f, size.height * 0.52f),
            cornerRadius = CornerRadius(stroke * 1.5f),
            style = Stroke(stroke)
        )
        drawCircle(
            color = NoPhotoInk,
            radius = size.minDimension * 0.15f,
            center = Offset(size.width / 2, bodyTop + size.height * 0.26f),
            style = Stroke(stroke)
        )
        // The slash, with a background-coloured edge so it reads as crossing the camera out.
        val start = Offset(size.width * 0.1f, size.height * 0.1f)
        val end = Offset(size.width * 0.9f, size.height * 0.9f)
        drawLine(NoPhotoBackground, start, end, strokeWidth = stroke * 3, cap = StrokeCap.Round)
        drawLine(NoPhotoInk, start, end, strokeWidth = stroke, cap = StrokeCap.Round)
    }
}
