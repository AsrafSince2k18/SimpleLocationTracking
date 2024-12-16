package com.example.simplelocationtraking.presentance

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.simplelocationtraking.data.service.LocationService
import com.example.simplelocationtraking.data.service.LocationService.Companion.START
import com.example.simplelocationtraking.data.service.LocationService.Companion.STOP
import com.example.simplelocationtraking.presentance.ui.theme.SimpleLocationTrakingTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SimpleLocationTrakingTheme {
                HandleMultiplePermission(permission = listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
                )) {
                    HomeScreen(context = this)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    context: Context
) {

    Scaffold(
        modifier=Modifier
            .fillMaxSize(),
        topBar ={
            TopAppBar(
                title = {
                    Text(text = "Tracking",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                }
            )
        }
    ) { paddingValues ->
        Row (
            modifier=Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ){

            OutlinedButton(
                onClick = {
                    val intent = Intent(context,LocationService::class.java).also {
                        it.action=STOP
                    }
                    context.stopService(intent)
                }
            ) {
                Text(text = "Stop")
            }

            Button(
                onClick = {

                    val intent = Intent(context,LocationService::class.java).also {
                        it.action=START
                    }
                    context.startService(intent)

                }
            ) {
                Text(text = "Start")
            }
        }
    }

}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HandleMultiplePermission(
    permission :List<String>,
    onHome : @Composable () -> Unit
) {

    val snackBar = remember { SnackbarHostState() }
    val permissionHandle  = rememberMultiplePermissionsState(permissions  =permission)
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackBar)
        }
    ) {
        when{
            permissionHandle.permissions.all { it.status.isGranted } -> {
                onHome()
            }
            permissionHandle.permissions.any { it.status.shouldShowRationale } -> {
                LaunchedEffect(key1 = Unit) {

                    val action = snackBar.showSnackbar(
                        message = "Permission required",
                        actionLabel = "Go to setting"
                    )

                    if(SnackbarResult.ActionPerformed == action){
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package",context.packageName,null))
                        context.startActivity(intent)
                    }

                }
            }
            else -> permissionHandle.launchMultiplePermissionRequest()
        }
    }


}

