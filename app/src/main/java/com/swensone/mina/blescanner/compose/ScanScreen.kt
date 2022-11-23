package com.swensone.mina.blescanner.compose

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.swensone.mina.blescanner.BleViewModel
import com.swensone.mina.blescanner.R
import com.swensone.mina.blescanner.compose.theme.BleScannerTheme
import com.swensone.mina.blescanner.data.AdvertisementWrapper


@Composable
fun ScanningScreen(vm: BleViewModel = hiltViewModel()) {

    var scanningBtnText by rememberSaveable { mutableStateOf("startScanning") }

    BleScannerTheme() {
        Scaffold(topBar =
        { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }) {

            Box(modifier = Modifier
                .padding(it),contentAlignment = Alignment.Center) {
                Column() {
                    Button(
                        modifier = Modifier.padding(vertical = 24.dp),
                        onClick = { vm.toggleScan()}
                    ) {
                        Text(scanningBtnText)
                    }
                    val advs = vm.advertisements.collectAsState().value
                    LazyColumn(modifier = Modifier.padding(vertical = 4.dp)) {
                        items(items = advs) { adv ->
                            AdvItem(adv = adv)
                        }
                    }
                }

                val state = vm.isScanRunning.collectAsState().value
                if (state == true) {
                    scanningBtnText = "startScaning"
                    LoadingScreen()
                } else {
                    scanningBtnText = "stopScanning"
                    Unit
                }
            }


        }

    }
}

@Composable
fun AdvItem(adv: AdvertisementWrapper) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            Text(text = adv.nameOrAddress)
            Text(text = adv.rssiDisplay)
        }
    }
}


@Composable
fun LoadingScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        CircularProgressIndicator()
    }
}
