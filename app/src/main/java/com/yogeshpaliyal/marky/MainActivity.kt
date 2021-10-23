package com.yogeshpaliyal.marky

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.commonmark.node.Document
import org.commonmark.parser.Parser


class MainActivity : ComponentActivity() {

    private val mViewModel by viewModels<MainActivityViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            MarkyTheme {

                val model by mViewModel.editorMode.observeAsState()
                val content = mViewModel.content.observeAsState()

                val root = remember(content.value ?: "") {
                    val parser = Parser.builder().build()
                    val root = parser.parse(content.value ?: "") as Document
                    root
                }

                Scaffold(topBar = {
                    TopRow(model) {
                        mViewModel.changeMode(it)
                    }
                }) {
                    if (model is Editor) {
                        TextField(
                            value = content.value ?: "",
                            onValueChange = { mViewModel.content.value = it },
                            modifier = Modifier.fillMaxSize(),
                            placeholder = {
                                Text(text = "Start writing...")
                            }
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            MDDocument(root)
                        }
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

@Composable
fun TopRow(
    mode: Mode?,
    changeMode: (mode: Mode) -> Unit
) {

    val isEditor = remember(mode) {
        mode is Editor
    }

    Box(
        modifier = Modifier
            .background(Color.LightGray)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            TextButton(
                shape = MaterialTheme.shapes.small.copy(CornerSize(8.dp)),
                colors = ButtonDefaults.textButtonColors(if (isEditor) Color.White else Color.Transparent),
                modifier = Modifier
                    .weight(1f),
                onClick = { changeMode(Editor) }) {
                Text(
                    text = "Editor",
                    color = if (isEditor) MaterialTheme.colors.primary else Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            TextButton(
                shape = MaterialTheme.shapes.small.copy(CornerSize(8.dp)),
                colors = ButtonDefaults.textButtonColors(if (isEditor.not()) Color.White else Color.Transparent),
                modifier = Modifier
                    .weight(1f),
                onClick = { changeMode(Preview) }) {
                Text(
                    text = "Preview",
                    color = if (isEditor.not()) MaterialTheme.colors.primary else Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

}
