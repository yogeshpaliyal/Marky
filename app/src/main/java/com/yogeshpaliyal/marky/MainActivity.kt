package com.yogeshpaliyal.marky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import com.yogeshpaliyal.marky.databinding.ActivityMainBinding
import org.commonmark.node.Document
import org.commonmark.parser.Parser
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate


class MainActivity : ComponentActivity() {

    private val mViewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            MarkyTheme {

                val model = mViewModel.editorMode.observeAsState()
                val content = mViewModel.content.observeAsState()

                val root = remember(content.value ?: ""){
                    val parser = Parser.builder().build()
                    val root = parser.parse(content.value ?: "") as Document
                    root
                }

                Scaffold(topBar = {
                    Row() {
                        TextButton(onClick = { mViewModel.changeToEditor() }) {
                            Text(text = "Editor")
                        }
                        TextButton(onClick = { mViewModel.changeToPreview() }) {
                            Text(text = "Preview")
                        }
                    }
                }) {
                    if (model.value is Editor) {
                        TextField(
                            value = content.value ?: "",
                            onValueChange = { mViewModel.content.value = it }, modifier = Modifier.fillMaxSize())
                    }else{
                        MDDocument(root)
                    }
                }
            }
        }

    }

    @Composable
    fun MarkyTheme(
        darkTheme: Boolean = isSystemInDarkTheme(),
        content: @Composable() () -> Unit
    ) {


        MaterialTheme(
            content = content
        )
    }
}