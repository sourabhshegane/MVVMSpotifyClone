package com.sourabhcodes.spotifyclone.other

open class Event <out T>(private val data: T){
    var hasBeenEnabled = false
    private set

    fun getContentIfNotHandled(): T?{
        return if (hasBeenEnabled){
            null
        }else{
            hasBeenEnabled = true
            data
        }
    }

    fun peekContent() = data
}