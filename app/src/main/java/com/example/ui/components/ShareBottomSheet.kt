package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.PostItem
import com.example.ui.theme.*
import com.example.utils.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    post: PostItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = { BottomSheetDefaults.DragHandle(color = BorderGray) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Share",
                color = WhiteText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShareSheetOptionBtn(
                    label = "WhatsApp",
                    color = Color(0xFF25D366),
                    iconRes = R.drawable.ic_whatsapp
                ) {
                    ShareUtils.shareToWhatsApp(context, post.caption, post.id)
                    onDismiss()
                }

                ShareSheetOptionBtn(
                    label = "Facebook",
                    color = Color(0xFF1877F2),
                    iconRes = R.drawable.ic_facebook
                ) {
                    ShareUtils.shareToFacebook(context, post.id)
                    onDismiss()
                }

                ShareSheetOptionBtn(
                    label = "Copy Link",
                    color = GrayText,
                    iconRes = R.drawable.ic_link
                ) {
                    ShareUtils.copyLink(context, post.id)
                    onDismiss()
                }

                ShareSheetOptionBtn(
                    label = "More",
                    color = BlueFeed,
                    iconRes = R.drawable.ic_more
                ) {
                    ShareUtils.sharePost(context, post.id, post.caption)
                    onDismiss()
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun ShareSheetOptionBtn(
    label: String,
    color: Color,
    iconRes: Int,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(BorderGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = label,
            color = WhiteText,
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
