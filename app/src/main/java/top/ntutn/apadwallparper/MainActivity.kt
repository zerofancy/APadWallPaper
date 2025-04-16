package top.ntutn.apadwallparper

import android.app.WallpaperManager
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import top.ntutn.apadwallparper.ui.theme.APadWallparperTheme

class MainActivity : ComponentActivity() {
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            // 处理结果
        }
    }

    private val selectWallHLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { result ->
        if (result == null) {
            Toast.makeText(applicationContext, "No selected image", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        contentResolver.takePersistableUriPermission(result, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        lifecycleScope.launch {
            WallPaperPreferences.updateUriH(this@MainActivity, result)
        }
    }

    private val selectWallVLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { result ->
        if (result == null) {
            Toast.makeText(applicationContext, "No selected image", Toast.LENGTH_SHORT).show()
            return@registerForActivityResult
        }
        contentResolver.takePersistableUriPermission(result, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        lifecycleScope.launch {
            WallPaperPreferences.updateUriV(this@MainActivity, result)
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
                            selectWallHLauncher.launch(arrayOf("image/*"))
                        }) {
                            Text("选择横屏壁纸")
                        }
                        Button(onClick = {
                            selectWallVLauncher.launch(arrayOf("image/*"))
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
