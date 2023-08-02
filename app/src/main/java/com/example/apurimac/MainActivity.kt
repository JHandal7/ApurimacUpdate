package com.example.apurimac


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.apurimac.util.NotificationMessage
import com.example.apurimac.ui.ChatListScreen
import com.example.apurimac.ui.LoginScreen
import com.example.apurimac.ui.ProfileScreen
import com.example.apurimac.ui.SignupScreen
import com.example.apurimac.ui.SingleChatScreen
import com.example.apurimac.ui.SingleStatusScreen
import com.example.apurimac.ui.StatusListScreen
import com.example.apurimac.ui.theme.ApurimacTheme
import dagger.hilt.android.AndroidEntryPoint

sealed class DestinationScreen(val route: String) {
    object Signup : DestinationScreen("signup")
    object Login : DestinationScreen("login")
    object Profile : DestinationScreen("profile")

    object ChatList : DestinationScreen("chatList")
    object SingleChat : DestinationScreen("singleChat/{chatId}") {
        fun createRoute(id: String) = "singleChat/$id"
    }

    object StatusList : DestinationScreen("statusList")
    object SingleStatus : DestinationScreen("singleStatus/{userId}") {
        fun createRoute(userId: String?) = "singleStatus/$userId"
    }
}
  /*composable(
        route =
        "${SingleAccount.route}/{${SingleAccount.accountTypeArg}}",
        arguments = SingleAccount.arguments
    ) { na*/

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ApurimacTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatAppNavigation()
                }
            }
        }
    }
}

@Composable
fun ChatAppNavigation() {
    //var currentScreen: RallyDestination by remember { mutableStateOf(RallyDestination.Signup)}
    val navController = rememberNavController()
  //  val currentBackStackEntry by navController.currentBackStackEntryAsState()
    // val currentDestination=currentBackStackEntry?.destination
    // val currentScreen=currentBackStackEntry?.destination
  //  val currentBackStack by navController.currentBackStackEntryAsState()
  //  val currentDestination = currentBackStack?.destination
    val vm = hiltViewModel<CAViewModel>()
    NotificationMessage(vm = vm)
    NavHost(navController = navController, startDestination = DestinationScreen.Signup.route) {


        composable(DestinationScreen.Signup.route) {
            SignupScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Login.route) {
            LoginScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.Profile.route) {
            ProfileScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.StatusList.route) {
            StatusListScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.SingleStatus.route) {
            val userId = it.arguments?.getString("userId")
            userId?.let {
                SingleStatusScreen(
                    navController = navController,
                    vm = vm,
                    userId = it
                )
            }

        }
        composable(DestinationScreen.ChatList.route) {
            ChatListScreen(navController = navController, vm = vm)
        }
        composable(DestinationScreen.SingleChat.route) {


            val chatId = it.arguments?.getString("chatId")
            chatId?.let {
                SingleChatScreen(
                    navController = navController,
                    vm = vm,
                    chatId = chatId
                )
            }
        }
    }
}

    /*composable(
        route =
        "${SingleAccount.route}/{${SingleAccount.accountTypeArg}}",
        arguments = SingleAccount.arguments
    ) { na*/

   /* navBackStackEntry ->
    // Retrieve the passed argument
    val accountType =
        navBackStackEntry.arguments?.getString(SingleAccount.accountTypeArg)*/



