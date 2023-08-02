package com.example.apurimac

import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.apurimac.ui.SignupScreen

interface RallyDestination {


    interface RallyDestination {
       // val icon: ImageVector
      //  val route: String
      //  val screen: @Composable () -> Unit
    }

    /**
     * Rally app navigation destinations
     */
    object Signup: RallyDestination {
        //override val icon = Icons.Filled.PieChart
     //   override val route = "signup"
      //  override val screen: @Composable () -> Unit = { SignupScreen() }
    }
}