package io.horizontalsystems.bankwallet.modules.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.horizontalsystems.bankwallet.ui.compose.components.VSpacer
import io.horizontalsystems.bankwallet.ui.compose.components.body_jacob
import io.horizontalsystems.bankwallet.ui.compose.components.title3_jacob
import kotlinx.serialization.Serializable

@Serializable
object Profile
@Serializable
object FriendsList

@Composable
fun Xxx() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Profile) {
        composable<Profile> {
            Column {
                VSpacer(48.dp)
                title3_jacob("Profile")
                VSpacer(12.dp)
                body_jacob("Friends List", modifier = Modifier.clickable { navController.navigate(FriendsList) })
            }
        }
        composable<FriendsList> {
            Column {
                VSpacer(48.dp)
                title3_jacob("FriendsList")
            }
        }
    }
}