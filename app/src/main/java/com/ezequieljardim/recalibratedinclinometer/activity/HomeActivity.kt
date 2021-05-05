package com.ezequieljardim.recalibratedinclinometer.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ezequieljardim.recalibratedinclinometer.R

/**
 * A class that provides a navigation menu to the features of Acceleration
 * Explorer.
 *
 * @author Kaleb
 */
class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_home)
    }

    companion object {
        private val tag = HomeActivity::class.java.simpleName
    }
}