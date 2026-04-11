package com.katafract.exifarmor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlin.OptIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.katafract.exifarmor.models.PhotoMetadata
import com.katafract.exifarmor.models.StripOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    photos: List<PhotoMetadata>,
    currentOptions: StripOptions,
    isPro: Boolean = false,
    onOptionsChanged: (StripOptions) -> Unit,
    onStrip: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }

            Text(
                text = "Review Photos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${photos.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }

        // Strip Options
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                text = "Cleanup Preset",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = currentOptions == StripOptions.LOCATION_ONLY,
                    onClick = { onOptionsChanged(StripOptions.LOCATION_ONLY) },
                    label = { Text("Minimal", fontSize = 11.sp) },
                    modifier = Modifier.height(32.dp),
                )
                FilterChip(
                    selected = currentOptions == StripOptions.PRIVACY_FOCUSED,
                    onClick = { onOptionsChanged(StripOptions.PRIVACY_FOCUSED) },
                    label = { Text("Privacy", fontSize = 11.sp) },
                    modifier = Modifier.height(32.dp),
                )
                FilterChip(
                    selected = currentOptions == StripOptions.ALL,
                    onClick = { onOptionsChanged(StripOptions.ALL) },
                    label = { Text("Full Clean", fontSize = 11.sp) },
                    modifier = Modifier.height(32.dp),
                )
            }
        }

        // Free tier warning
        if (!isPro && photos.size > 5) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFECE51).copy(alpha = 0.15f))
                    .padding(12.dp),
            ) {
                Text(
                    text = "Free tier: only first 5 will be processed. Upgrade for unlimited.",
                    fontSize = 11.sp,
                    color = Color(0xFFB8860B),
                )
            }
        }

        // Photos List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        ) {
            items(photos) { photo ->
                PhotoPreviewCard(photo = photo)
            }
        }

        // Action Button
        Button(
            onClick = onStrip,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "Strip All",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun PhotoPreviewCard(
    photo: PhotoMetadata,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
    ) {
        // Thumbnail + Filename
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top,
        ) {
            AsyncImage(
                model = photo.uri,
                contentDescription = photo.filename,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = photo.filename,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Risk indicator
                val hasRisk = photo.hasGps || !photo.deviceMake.isNullOrBlank()
                val riskText = when {
                    photo.hasGps -> "High risk: GPS detected"
                    !photo.deviceMake.isNullOrBlank() -> "Medium risk: Device info"
                    else -> "Low risk: Clean"
                }
                val riskColor = when {
                    photo.hasGps -> Color(0xFFEF4444)
                    !photo.deviceMake.isNullOrBlank() -> Color(0xFFF59E0B)
                    else -> Color(0xFF10B981)
                }

                Text(
                    text = riskText,
                    fontSize = 10.sp,
                    color = riskColor,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        // Metadata details
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (photo.hasGps) {
                MetadataRow(label = "GPS", value = "Yes", color = Color(0xFFEF4444))
            }

            if (!photo.deviceMake.isNullOrBlank() || !photo.deviceModel.isNullOrBlank()) {
                MetadataRow(
                    label = "Device",
                    value = "${photo.deviceMake ?: "Unknown"} ${photo.deviceModel ?: ""}".trim(),
                )
            }

            if (!photo.dateTimeOriginal.isNullOrBlank()) {
                MetadataRow(label = "Timestamp", value = photo.dateTimeOriginal!!)
            }

            if (photo.pixelWidth != null && photo.pixelHeight != null) {
                MetadataRow(label = "Resolution", value = "${photo.pixelWidth}×${photo.pixelHeight}")
            }
        }
    }
}

@Composable
private fun MetadataRow(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            maxLines = 1,
        )
    }
}
