package com.example

import android.app.Application
import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.example.model.AppLanguage
import com.example.ui.viewmodel.DominosViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `AndroidViewModelFactory instantiates DominosViewModel and MainActivity launches cleanly`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(app)
        val viewModel = factory.create(DominosViewModel::class.java)
        assertNotNull(viewModel)
        assertEquals(AppLanguage.FR, viewModel.language.value)

        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        assertNotNull(controller.get())
    }

    @Test
    fun `default French, English, and Arabic RTL string resources resolve properly`() {
        val baseContext = ApplicationProvider.getApplicationContext<Context>()

        // Default French
        val frContext = baseContext.createLocalizedContext(AppLanguage.FR)
        assertEquals("Dominos", frContext.getString(R.string.app_name))
        assertEquals("Accueil", frContext.getString(R.string.nav_home))
        assertEquals(View.LAYOUT_DIRECTION_LTR, frContext.resources.configuration.layoutDirection)

        // English
        val enContext = baseContext.createLocalizedContext(AppLanguage.EN)
        assertEquals("Home", enContext.getString(R.string.nav_home))
        assertEquals(View.LAYOUT_DIRECTION_LTR, enContext.resources.configuration.layoutDirection)

        // Arabic RTL
        val arContext = baseContext.createLocalizedContext(AppLanguage.AR)
        assertEquals("الرئيسية", arContext.getString(R.string.nav_home))
        assertEquals(View.LAYOUT_DIRECTION_RTL, arContext.resources.configuration.layoutDirection)
    }
}
