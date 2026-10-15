package com.example.ui.screens.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.ui.components.GlassCard
import com.example.ui.theme.FugaCyan
import com.example.ui.theme.FugaError
import com.example.ui.theme.FugaPink
import com.example.ui.theme.FugaPurple
import com.example.ui.theme.FugaSuccess
import com.example.ui.theme.FugaViolet
import com.example.ui.theme.FugaWarning
import com.example.ui.theme.SpaceBlack
import com.example.ui.theme.SpaceCard
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onSpeak: (String) -> Unit,
    onExecuteAction: (ChatMessage) -> Unit,
    onBiometricVerifyPayment: (ChatMessage) -> Unit
) {
    val context = LocalContext.current
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(message.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.92f),
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!message.isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(listOf(FugaPurple, FugaCyan, FugaPink))
                        )
                        .padding(1.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CircleShape)
                            .background(SpaceBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "F",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = FugaCyan
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Message Bubble
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (message.isUser) 20.dp else 4.dp,
                            bottomEnd = if (message.isUser) 4.dp else 20.dp
                        )
                    )
                    .background(
                        if (message.isUser) {
                            Brush.horizontalGradient(listOf(FugaViolet, FugaPurple))
                        } else {
                            Brush.verticalGradient(listOf(SpaceCard, Color(0xFF131930)))
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (message.isUser) FugaPurple.copy(alpha = 0.5f) else Color(0x3326325B),
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (message.isUser) 20.dp else 4.dp,
                            bottomEnd = if (message.isUser) 4.dp else 20.dp
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag(if (message.isUser) "user_message_bubble" else "assistant_message_bubble")
            ) {
                Column {
                    Text(
                        text = message.text,
                        fontSize = 14.5.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = formattedTime,
                            fontSize = 10.sp,
                            color = if (message.isUser) Color.White.copy(alpha = 0.7f) else TextSecondary
                        )

                        if (!message.isUser) {
                            Spacer(modifier = Modifier.width(8.dp))
                            // Speak button
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Read aloud",
                                tint = TextSecondary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onSpeak(message.text) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Copy button
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy text",
                                tint = TextSecondary,
                                modifier = Modifier
                                    .size(13.dp)
                                    .clickable {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Fuga AI", message.text))
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                            )
                        }
                    }
                }
            }
        }

        // Action card if message contains an executable or sensitive intent
        if (message.actionType != null) {
            Spacer(modifier = Modifier.height(6.dp))
            ActionIntentCard(
                message = message,
                onExecuteAction = onExecuteAction,
                onBiometricVerifyPayment = onBiometricVerifyPayment
            )
        }
    }
}

@Composable
fun ActionIntentCard(
    message: ChatMessage,
    onExecuteAction: (ChatMessage) -> Unit,
    onBiometricVerifyPayment: (ChatMessage) -> Unit
) {
    val isPayment = message.actionType == "PAYMENT"
    val isSensitive = message.isSensitive || isPayment
    val isExecuted = message.actionStatus == "EXECUTED"

    GlassCard(
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .padding(top = 4.dp),
        shape = RoundedCornerShape(16.dp),
        borderColor = if (isExecuted) FugaSuccess.copy(alpha = 0.4f) else if (isSensitive) FugaWarning.copy(alpha = 0.6f) else FugaCyan.copy(alpha = 0.4f)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (isExecuted) FugaSuccess.copy(alpha = 0.2f)
                                else if (isSensitive) FugaWarning.copy(alpha = 0.2f)
                                else FugaCyan.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isExecuted) Icons.Default.CheckCircle else if (isSensitive) Icons.Default.Lock else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isExecuted) FugaSuccess else if (isSensitive) FugaWarning else FugaCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = if (isPayment) "Secure Payment Intent" else "Command: ${message.actionType}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = message.actionDetail ?: "Pending action",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                if (isExecuted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FugaSuccess.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "COMPLETED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = FugaSuccess
                        )
                    }
                }
            }

            if (!isExecuted) {
                Spacer(modifier = Modifier.height(10.dp))

                if (isSensitive) {
                    Button(
                        onClick = { onBiometricVerifyPayment(message) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("biometric_verify_action_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FugaPurple,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = FugaCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Biometric Verify & Pay",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = { onExecuteAction(message) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("execute_action_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FugaViolet,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Execute Command",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
