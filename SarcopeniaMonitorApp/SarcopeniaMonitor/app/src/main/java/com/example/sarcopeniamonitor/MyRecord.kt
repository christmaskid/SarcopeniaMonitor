package com.example.sarcopeniamonitor

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.pow

data class MealImage(
    var imageUri: String? = null,  // Store only the URI
    var uploadedAt: String? = null,
    var mealType: String? = null
)

@Entity(tableName = "item")
data class UserRecord (
    @PrimaryKey
    var recordID: Int = 0, // 0 until assigned by Django
    @ColumnInfo(name = "date")
    var recordDate: String? = null,
    @ColumnInfo(name = "time")
    var recordTime: String? = null,
    @ColumnInfo(name = "height")
    var height: Double? = null,
    @ColumnInfo(name = "weight")
    var weight: Double? = null,
    @ColumnInfo(name = "bmi")
    var bmi: Double? = null,
    @ColumnInfo(name = "sbp")
    var sbp: Double? = null,
    @ColumnInfo(name = "dbp")
    var dbp: Double? = null,
    @ColumnInfo(name = "mealImages")
    var mealImages: MutableList<MealImage> = mutableListOf()  // Unified structure
)

class MyRecord(
    recordID: Int = 0,
) {
    private var _userRecord = UserRecord(
        recordID, "", "",
        null, null, null, null, null
    )
    private var editState: Boolean = false

    fun getPrivateRecord(): UserRecord {
        return _userRecord
    }

    fun setPrivateRecord(userRecord: UserRecord) {
        _userRecord = userRecord
    }

    fun update(
        recordDateString: String = "",
        recordTimeString: String = "",
        heightValue: Double? = null,
        weightValue: Double? = null,
        sbpValue: Double? = null,
        dbpValue: Double? = null,
        mealImages: List<MealImage> = emptyList()  // Unified structure
    ) {
        if (editState) {
            _userRecord.recordDate = recordDateString
            _userRecord.recordTime = recordTimeString
            _userRecord.height = heightValue
            _userRecord.weight = weightValue
            _userRecord.bmi = getBMI()
            _userRecord.sbp = sbpValue
            _userRecord.dbp = dbpValue
            Log.d("Update", mealImages.toString())

            // Merge mealImages instead of replacing
//            val existingImages = _userRecord.mealImages.associateBy { it.imageUri }
            val newImages = mealImages.associateBy { it.imageUri }
//            _userRecord.mealImages = (existingImages + newImages).values.toMutableList()
            _userRecord.mealImages = newImages.values.toMutableList()
            Log.d("Update", "Merged mealImages: ${_userRecord.mealImages}")
        }
        enableEditing(false)
    }

    fun enableEditing(enable: Boolean) {
        editState = enable
    }

    fun getRecordID(): Int {
        return _userRecord.recordID
    }
    fun getDate(): String? {
        return _userRecord.recordDate
    }
    fun getTime(): String? {
        return _userRecord.recordTime
    }
    fun clearDateTime() {
        _userRecord.recordDate = null
        _userRecord.recordTime = null
    }
    fun getHeight(): Double? {
        return _userRecord.height
    }
    fun getWeight(): Double? {
        return _userRecord.weight
    }
    fun getBMI(): Double? {
        val heightValue = _userRecord.height
        val weightValue = _userRecord.weight
        if (heightValue != null && weightValue != null && weightValue != 0.0) {
            _userRecord.bmi = (weightValue) / (heightValue / 100).pow(2.0)
        } else {
            _userRecord.bmi = null
        }
        return _userRecord.bmi
    }
    fun getSBP(): Double? {
        return _userRecord.sbp
    }
    fun getDBP(): Double? {
        return _userRecord.dbp
    }
    fun getMealImages(): MutableList<MealImage> {
        return _userRecord.mealImages
    }
}

class MyRecordList() {
    private val _recordList = mutableStateListOf<MyRecord>()
    val recordList: SnapshotStateList<MyRecord> = _recordList

    fun addElement(): MyRecord {
        val newID = if (getLastElement() == null) { 0 } else {getLastElement()!!.getRecordID()}
        val newRecord = MyRecord(newID)
        newRecord.clearDateTime()
        _recordList.add(newRecord)
        Log.d("Add a new record", newRecord.getRecordID().toString())
        return newRecord
    }

    fun getLastElement(): MyRecord? {
        if (_recordList.size > 0) {
            return _recordList.last()
        }
        return null
    }

    fun clear() {
        _recordList.clear()
    }
    fun removeElement(record: MyRecord) {
        _recordList.remove(record)
        Log.d("Remove a record", "Record with ID ${record.getRecordID()} removed.")
    }
}