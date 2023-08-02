package com.example.apurimac.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.apurimac.CAViewModel
import com.example.apurimac.data.Message
import com.example.apurimac.util.CommonDivider
import com.example.apurimac.util.CommonImage

@Composable
fun SingleChatScreen(navController: NavController, vm: CAViewModel, chatId: String) {
    LaunchedEffect(key1 = Unit) {
        vm.populateChat(chatId)
    }
    BackHandler {
        vm.depopulateChat()
    }


    val currentChat = vm.chats.value.first { it.chatId == chatId }

    val myUser = vm.userData.value

    val chatUser = if (myUser?.userId == currentChat.user1.userId)
        currentChat.user2
    else currentChat.user1
    var reply by rememberSaveable { mutableStateOf("") }

    val onSendReplay = {
        vm.onSendReplay(chatId,reply)
        reply=""
    }
    val chatMessages = vm.chatMessages

    Column(modifier = Modifier.fillMaxSize()) {
        ChatHeader(
            name = chatUser.name ?: "",
            imageUrl = chatUser.imageUrl ?: "",
            )
         {
       navController.popBackStack()
        vm.depopulateChat()
            // navController.popBackStack()
    }
        Messages(
            modifier = Modifier.weight(1f),
           chatMessages= chatMessages.value,
            currentUserId = myUser?.userId ?: ""

        )


        ReplyBox(reply = reply, onReplyChange = { reply = it }, onSendReplay = onSendReplay)


    }
}

@Composable
fun ChatHeader(name: String, imageUrl: String, onBackClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.ArrowBack, contentDescription = null,
            modifier = Modifier
                .clickable { onBackClicked.invoke() }
                .padding(8.dp)
        )
        CommonImage(
            data = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape)
        )
        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(4.dp)
        )
    }
    CommonDivider()
}

@Composable
fun Messages(
    modifier: Modifier,
    chatMessages: List<Message>,
    currentUserId: String
) {
    LazyColumn(modifier = modifier) {
        items(chatMessages) { msg ->
    val alignment = if (msg.sentBy == currentUserId) Alignment.End
    else Alignment.Start
    val color = if (msg.sentBy == currentUserId) Color(0xFF68C400)
    else Color(0xFFC0C0C0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = alignment
    ) {
        Text(
           // text = msg.message ?: "",
            text = msg.message ?:"" ,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .padding(12.dp),
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
//let
        }//items

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReplyBox(reply: String, onReplyChange: (String) -> Unit, onSendReplay: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TextField(value = reply, onValueChange = onReplyChange, maxLines = 3)
            Button(onClick = onSendReplay) {
                Text(text = "Send")
            }
        }
    }

}