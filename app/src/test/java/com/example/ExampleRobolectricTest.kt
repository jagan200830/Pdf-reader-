package com.example

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DocMaster", appName)
  }

  @Test
  fun `test sample documents generation`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val docs = com.example.engine.FileUtils.loadAllAvailableDocuments(context)
    println("Loaded docs size: ${docs.size}")
    for (d in docs) {
      println("Doc: ${d.name}, size: ${d.sizeBytes}, mime: ${d.mimeType}")
    }
    org.junit.Assert.assertTrue(docs.isNotEmpty())
  }

  @Test
  fun `launch MainActivity`() {
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    scenario.onActivity { activity ->
      assertNotNull(activity)
    }
  }
}
