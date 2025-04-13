package top.ntutn.apadwallparper

import android.app.WallpaperManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import top.ntutn.apadwallparper.ui.theme.APadWallparperTheme

class MainActivity : ComponentActivity() {
    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // 处理结果
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            APadWallparperTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column (
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        Button(onClick = {

                        }) {
                            Text("选择横屏壁纸")
                        }
                        Button(onClick = {

                        }) {
                            Text("选择竖屏壁纸")
                        }
                        Button(onClick = {
                            val intent = Intent()
                            intent.action = WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            launcher.launch(intent)
                        }) {
                            Text("设为壁纸")
                        }
                    }
                }
            }
        }
    }
}
