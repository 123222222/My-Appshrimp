package com.dung.myapplication.mainUI.common

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun MyButon (text: String, onClick:()-> Unit) {
    Button(
        onClick = onClick,
    ) {
        Text(
            text = text,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyButtonPreview() {
    MaterialTheme {
        MyButon(
            text = "Click me",
            onClick = { /* Preview: không cần làm gì */ }
        )
    }
}