package com.sourabhcodes.spotifyclone.ui.viewmodels

import android.media.browse.MediaBrowser
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sourabhcodes.spotifyclone.data.entities.Song
import com.sourabhcodes.spotifyclone.exoplayer.MusicServiceConnection
import com.sourabhcodes.spotifyclone.exoplayer.isPlayEnabled
import com.sourabhcodes.spotifyclone.exoplayer.isPlaying
import com.sourabhcodes.spotifyclone.exoplayer.isPrepared
import com.sourabhcodes.spotifyclone.other.Constants.MEDIA_ROOT_ID
import com.sourabhcodes.spotifyclone.other.Resource

class MainViewModel @ViewModelInject constructor(
    private val musicServiceConnection: MusicServiceConnection
): ViewModel(){
    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    private val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currentlyPlayingSong = musicServiceConnection.currentPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback(){
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>,
                options: Bundle
            ) {
                super.onChildrenLoaded(parentId, children, options)
                val items = children.map {
                    Song(
                        it.mediaId!!,
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString()
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })
    }

    fun skipToNextSong(){
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong(){
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(position: Long){
        musicServiceConnection.transportControls.seekTo(position)
    }

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false

        if(isPrepared && mediaItem.mediaid == currentlyPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)){
            playbackState.value?.let { playbackState ->
                when{
                    playbackState.isPlaying -> if(toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        }else{
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaid, null)
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback(){

        })
    }
}