package com.hoofit.app

import com.hoofit.app.data.Coordinate
import com.hoofit.app.data.Reserve
import com.hoofit.app.editInfo.EditTrailFragment


import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE)
@RunWith(AndroidJUnit4::class)
class EditTrailFragmentTest1 {


    @Test
    fun inputValidTest() {
        val fragment = EditTrailFragment()
        val reserve = Reserve()
        fragment.reserve = reserve
        assertFalse(fragment.isValidInput("Artem", "New trail", "medium", "12 km", "6 h", mutableListOf()))
    }

    @Test
    fun inputValidTest2() {
        val fragment = EditTrailFragment()
        val coordinates = mutableListOf(
            Coordinate(12.4, 34.5),
            Coordinate(1212.4, 3394.531)
        )
        assertTrue(fragment.isValidInput("Artem", "New trail", "medium", "12 km", "6 h", coordinates))
    }
}
