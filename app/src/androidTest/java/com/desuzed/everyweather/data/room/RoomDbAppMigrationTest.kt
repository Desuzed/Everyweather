package com.desuzed.everyweather.data.room

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomDbAppMigrationTest {

    @After
    fun deleteDatabase() {
        InstrumentationRegistry.getInstrumentation().targetContext.deleteDatabase(TEST_DB_NAME)
    }

    @Test
    fun migrationFromVersion1PreservesFavoriteLocationFieldsAndAddsEmptyCustomName() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.openOrCreateDatabase(TEST_DB_NAME, android.content.Context.MODE_PRIVATE, null).apply {
            execSQL(
                """
                    CREATE TABLE favorite_location_table (
                        lat_lon TEXT NOT NULL,
                        name TEXT NOT NULL,
                        region TEXT NOT NULL,
                        country TEXT NOT NULL,
                        lat TEXT NOT NULL,
                        lon TEXT NOT NULL,
                        PRIMARY KEY(lat_lon)
                    )
                """.trimIndent(),
            )
            execSQL(
                """
                    INSERT INTO favorite_location_table
                    (lat_lon, name, region, country, lat, lon)
                    VALUES (?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf("55.8,37.7", "Moscow", "Moscow", "Russia", "55.7558", "37.6173"),
            )
            version = 1
            close()
        }

        val database = Room.databaseBuilder(context, RoomDbApp::class.java, TEST_DB_NAME).build()
        try {
            val migrated = database.favoriteLocationDAO().getAlphabetizedLocations().first().single()

            assertEquals("Moscow", migrated.cityName)
            assertEquals("55.7558", migrated.lat)
            assertEquals("37.6173", migrated.lon)
            assertEquals("", migrated.customName)
        } finally {
            database.close()
        }
    }

    private companion object {
        const val TEST_DB_NAME = "room-migration-test"
    }
}
