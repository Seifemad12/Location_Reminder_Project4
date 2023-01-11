package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Suppress("DEPRECATION")
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    private lateinit var database: RemindersDatabase
    private val rem1 = ReminderDTO(
        "rem1", "Desc1",
        "Loc1", (-360..360).random().toDouble(),
        (-360..360).random().toDouble(), "id_1"
    )
    private val rem2 = ReminderDTO(
        "rem2", "Desc2",
        "Loc2", (-360..360).random().toDouble(),
        (-360..360).random().toDouble(), "id_2"
    )
    private val rem3 = ReminderDTO(
        "rem3", "Desc3",
        "Loc3", (-360..360).random().toDouble(),
        (-360..360).random().toDouble(), "id_3"
    )
    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

    }

    @After
    fun closeDb() = database.close()

    // test getReminders
    @Test
    fun insertReminderAndGetAll() = runBlockingTest {
        // GIVEN -> Insert  reminders.
        database.reminderDao().saveReminder(rem1)
        database.reminderDao().saveReminder(rem2)
        database.reminderDao().saveReminder(rem3)

        // WHEN -> Get the reminders from database
        val reminders = database.reminderDao().getReminders()

        // THEN -> The loaded data contains the expected values.
        assertThat(reminders[0].id, `is`(rem1.id))
        assertThat(reminders[0].title, `is`(rem1.title))
        assertThat(reminders[0].latitude, `is`(rem1.latitude))
        assertThat(reminders[0].description, `is`(rem1.description))

        assertThat(reminders[1].id, `is`(rem2.id))
        assertThat(reminders[1].title, `is`(rem2.title))
        assertThat(reminders[1].latitude, `is`(rem2.latitude))
        assertThat(reminders[1].description, `is`(rem2.description))

        assertThat(reminders[2].id, `is`(rem3.id))
        assertThat(reminders[2].title, `is`(rem3.title))
        assertThat(reminders[2].latitude, `is`(rem3.latitude))
        assertThat(reminders[2].description, `is`(rem3.description))



        //check for the size
        assertThat(reminders.size, `is`(3))

    }

    // test getReminderById
    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        // GIVEN -> Insert a reminder.
        database.reminderDao().saveReminder(rem1)
        // WHEN -> Get the task by id from the database.
        val reminder = database.reminderDao().getReminderById(rem1.id)

        // THEN -> the result is expected date.
        assertThat(reminder as ReminderDTO, notNullValue())
        assertThat(reminder.id, `is`(rem1.id))
        assertThat(reminder.title, `is`(rem1.title))
        assertThat(reminder.description, `is`(rem1.description))
        assertThat(reminder.longitude, `is`(rem1.longitude))
        assertThat(reminder.latitude, `is`(rem1.latitude))
        assertThat(reminder.location, `is`(rem1.location))

    }

    @Test
    fun saveReminder_ThenDeleteAllOfThem() = runBlockingTest {
        // GIVEN -> Save  reminders.
        database.reminderDao().saveReminder(rem1)
        database.reminderDao().saveReminder(rem2)
        database.reminderDao().saveReminder(rem3)



        // WHEN -> Delete all of reminder
        database.reminderDao().deleteAllReminders()


        // THEN -> the expected the size should be equal Zero
        val reminders = database.reminderDao().getReminders()
        assertThat(reminders.size, `is`(0))
    }

    @Test
    fun getReminder_returnError() = runBlocking {
        //GIVEN -> start with data base is empty
        database.reminderDao().deleteAllReminders()
        //WHEN -> get reminder1
        val value = database.reminderDao().getReminderById(rem1.id)
        // THEN -> check value equal null
        assertThat(value, `is`(nullValue()))
    }

}