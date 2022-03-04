package com.albar.runningtracker.repositories

import com.albar.runningtracker.db.Run
import com.albar.runningtracker.db.RunDAO
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val runDao: RunDAO
) {
    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun deleteRun(run: Run) = runDao.deleteRun(run)

    // no need to use suspend function as livedata has it by default
    fun getAllRunsSortedByDate() = runDao.getAllRunSortedByDate()

    fun getAllRunsSortedByDistance() = runDao.getAllRunSortedByDistance()

    fun getAllRunsSortedByTimeInMillis() = runDao.getAllRunSortedByTimeInMillis()

    fun getAllRunsSortedByAverageSpeed() = runDao.getAllRunSortedByAvgSpeed()

    fun getAllRunsSortedByCalories() = runDao.getAllRunSortedByCaloriesBurned()

    fun getTotalAverageSpeed() = runDao.getTotalAvgSpeed()

    fun getTotalDistance() = runDao.getTotalDistance()

    fun getTotalCaloriesBurned() = runDao.getTotalCaloriesBurned()

    fun getTotalInMillis() = runDao.getTotalInMillis()
}