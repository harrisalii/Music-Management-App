package com.appsfactory.test.ui.album_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appsfactory.test.domain.album.Album
import com.appsfactory.test.domain.repository.LastFMRepository
import com.appsfactory.test.domain.track.Track
import com.appsfactory.test.domain.util.Result
import com.appsfactory.test.domain.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    stateHandle: SavedStateHandle,
    private val repository: LastFMRepository
) : ViewModel() {

    private val _album = stateHandle.get<Album>("album")!!
    val album = MutableStateFlow(_album).asStateFlow()

    private val _uiState = MutableStateFlow<UiState<List<Track>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEvents = Channel<AlbumDetailEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()

    init {
        getTracks(_album)
    }

    private fun getTracks(album: Album) = viewModelScope.launch {
        repository.getTracks(
            artist = album.artist.name,
            album = album.name
        ).collectLatest { result ->
            when (result) {
                is Result.Success -> {
                    _uiState.emit(UiState.Success(result.data))
                }
                is Result.Error -> {
                    _uiEvents.send(AlbumDetailEvent.ShowError(result.error))
                }
            }
        }
    }

    fun onTrackClicked(track: Track) = viewModelScope.launch {

    }

    sealed class AlbumDetailEvent {
        data class ShowError(val msg: String) : AlbumDetailEvent()
    }
}