package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
@Suppress("UNREACHABLE_CODE")
class FakeDataSource(var reminders: MutableList<ReminderDTO> = mutableListOf()) :
    ReminderDataSource {

    private var _returnError = false
    fun returnError(result: Boolean) {
        _returnError = result

    }
//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        //   TODO("Not yet implemented"

        // make datasource return error even if it's not empty to test error.
        if (_returnError) {
            return Result.Error("There is Exception Error!")
        }
        /**
         * as the reviewer mentioned here when the reminders not found the reminders it will be a mutable list,
         * and i should return it*/
        if (reminders.isEmpty()) {
            return Result.Success(reminders)
        } else {
            return Result.Success(reminders)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {

        if (_returnError) {
            return Result.Error("There is Exception Error!")
        }
        reminders.find { it.id == id }?.let { return Result.Success(it) }
        return Result.Error("Not Found The Reminder Have Id = $id")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}