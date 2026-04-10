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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
    onOptionsChanged: (StripOptions) -> Unit,
    onStrip: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Preview (${photos.size} photos)",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        // Strip Options
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = "Strip Options",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = currentOptions == StripOptions.ALL,
                    onClick = { onOptionsChanged(StripOptions.ALL) },
                    label = { Text("All") },
                )
                FilterChip(
                    selected = currentOptions == StripOptions.LOCATION_ONLY,
                    onClick = { onOptionsChanged(StripOptions.LOCATION_ONLY) },
                    label = { Text("Location") },
                )
                FilterChip(
                    selected = currentOptions == StripOptions.PRIVACY_FOCUSED,
                    onClick = { onOptionsChanged(StripOptions.PRIVACY_FOCUSED) },
                    label = { Text("Privacy") },
                )
            }
        }

        // Photos List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp,
            ),
        ) {
            items(photos) { photo ->
                PhotoPreviewCard(photo = photo)
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
            ) {
                Text("Back")
            }
            Button(
                onClick = onStrip,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
            ) {
                Text("Strip Metadata")
            }
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
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        // Thumbnail
        AsyncImage(
            model = photo.uri,
            contentDescription = photo.filename,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop,
        )

        // Info
        Column(
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = photo.filename,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )

            if (photo.hasGps) {
                Row(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .background(
                            color = Color.Red.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.small,
                        )
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = "GPS found",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Red,
                    )
                    Text(
                        text = " GPS location detected",
                        fontSize = 12.sp,
                        color = Color.Red,
                    )
                }
            }

            if (!photo.deviceMake.isNullOrBlank() || !photo.deviceModel.isNullOrBlank()) {
                Text(
                    text = "Device: ${photo.deviceMake ?: "?"} ${photo.deviceModel ?: "?"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (!photo.dateTimeOriginal.isNullOrBlank()) {
                Text(
                    text = "Taken: ${photo.dateTimeOriginal}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            if (photo.pixelWidth != null && photo.pixelHeight != null) {
                Text(
                    text = "${photo.pixelWidth}×${photo.pixelHeight}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
