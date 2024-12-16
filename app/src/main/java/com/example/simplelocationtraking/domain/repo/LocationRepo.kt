package com.example.simplelocationtraking.domain.repo

import android.location.Location
import com.example.simplelocationtraking.data.error.HandlePermission
import com.example.simplelocationtraking.data.error.Result
import kotlinx.coroutines.flow.Flow





interface LocationRepo {

    fun getTrackingLocation(interval:Long) : Flow<Result<Location,HandlePermission>>

}