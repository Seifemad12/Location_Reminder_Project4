package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@Suppress("DEPRECATION")
@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    private lateinit var reminderListViewModel: RemindersListViewModel
    // use fakeData Source to be injected in view model
    private lateinit var fakeDataSource: FakeDataSource

    private val reminder = ReminderDTO(
        "Reminder", "Description",
        "Location", (-360..360).random().toDouble(),
        (-360..360).random().toDouble(), "id"
    )

    // executes each  task synchronously using architecture components
    @get:Rule
    var instantExecuteRule = InstantTaskExecutorRule()

    // set main coroutine dispatchers for unit testing
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUpViewModel() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        reminderListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(), fakeDataSource
        )
    }

    @After
    fun clearDataSource() = runBlockingTest {
        fakeDataSource.deleteAllReminders()
    }

    @Test
    fun invalidateShowNoData_noSavedData_true() = runBlockingTest {

        //GIVEN -> empty data source
        fakeDataSource.deleteAllReminders()

        //WHEN -> loading reminders
        reminderListViewModel.loadReminders()

        //THEN -> check the size of the list is 0 and show no data
        assertThat(reminderListViewModel.remindersList.getOrAwaitValue()?.size, `is`(0))
        assertThat(reminderListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadReminders_DataSource_Error() = runBlockingTest {
        // GIVEN -> the DataSource return errors.
        fakeDataSource.returnError(true)
        fakeDataSource.saveReminder(
            ReminderDTO(
                "title",
                "description",
                "location",
                380.00,
                350.00
            )
        )
        // WHEN -> loading the reminders
        reminderListViewModel.loadReminders()

        // THEN -> Show error message
        assertThat(reminderListViewModel.showSnackBar.getOrAwaitValue(), `is`("There is Exception Error!"))
    }

    @Test
    fun loadReminders_checkLoading() = mainCoroutineRule.runBlockingTest {
        // Pause dispatcher to verify initial values
        mainCoroutineRule.pauseDispatcher()
        //GIVEN ->  Save reminder
        fakeDataSource.deleteAllReminders()
        fakeDataSource.saveReminder(reminder)

        //WHEN -> load Reminders
        reminderListViewModel.loadReminders()
        //THEN -> loading indicator is display
        assertThat(reminderListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        // Execute pending coroutines actions
        mainCoroutineRule.resumeDispatcher()
        // THEN -> loading indicator is disappear
        assertThat(reminderListViewModel.showLoading.getOrAwaitValue(), `is`(false))

    }
}