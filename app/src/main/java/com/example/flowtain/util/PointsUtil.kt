package com.example.flowtain.util

import android.content.Context
import android.util.Log

class PointsUtil {
    companion object {

        fun addPoints(increment: Long, context: Context) {
            //Log.i("PointsUtil", "$increment")
            PrefUtil.setPoints(PrefUtil.getPoints(context) + increment, context)
        }

        fun subtractPoints(decrement: Int, context: Context) {
            //Log.i("PointsUtil", "$decrement")
            PrefUtil.setPoints(PrefUtil.getPoints(context) + decrement, context)
        }
    }
}