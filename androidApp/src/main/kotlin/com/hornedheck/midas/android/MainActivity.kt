package com.hornedheck.midas.android

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import com.hornedheck.midas.App
import com.hornedheck.midas.auth.BiometricAuthenticatorAndroid
import com.hornedheck.midas.ui.lock.LockViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.context.GlobalContext

class MainActivity : FragmentActivity() {

    private val biometric by lazy { GlobalContext.get().get<BiometricAuthenticatorAndroid>() }
    private val lockViewModel: LockViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { !lockViewModel.state.value.ready }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        window.decorView.filterTouchesWhenObscured = true
        biometric.bind(this)

        setContent {
            App()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        biometric.unbind()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
