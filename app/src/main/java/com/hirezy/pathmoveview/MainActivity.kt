package com.hirezy.pathmoveview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.hirezy.pathmoveview.ui.theme.PathMoveViewTheme
import com.hirezy.pathmoveview.view.FontPathToPointsView
import com.hirezy.pathmoveview.view.PathMoveView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PathMoveViewTheme {
                Scaffold(
                    topBar = {
                        Text(
                            text = "Compose Example",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(it)
                            .fillMaxSize()
                            .verticalScroll(
                                rememberScrollState()
                            ),
                    ) {
                        LoadAndroidViewByPathMoveView()
                        LoadAndroidViewByFontPathToPointsView()
                    }
                }
            }
        }
    }
}


@Composable
fun LoadAndroidViewByPathMoveView() {
    AndroidView(
        factory = {
            PathMoveView(it)
        },
        modifier = Modifier.wrapContentSize().padding(top = 80.dp)
    ) {
        it.start()
    }
}

@Composable
fun LoadAndroidViewByFontPathToPointsView() {
    AndroidView(
        factory = {
            FontPathToPointsView(it)
        },
        modifier = Modifier.wrapContentSize()
    ) {
        it.text = "端午节快乐"
    }
}