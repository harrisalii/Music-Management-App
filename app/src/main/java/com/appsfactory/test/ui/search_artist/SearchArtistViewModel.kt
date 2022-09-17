package com.appsfactory.test.ui.search_artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appsfactory.test.domain.artist.Artist
import com.appsfactory.test.domain.repository.LastFMRepository
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
class SearchArtistViewModel @Inject constructor(
    private val repository: LastFMRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Artist>>>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _uiEvents = Channel<SearchArtistEvent>()
    val uiEvents = _uiEvents.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.searchArtist(name = "cher").collectLatest { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.emit(UiState.Success(result.data))
                    }
                    is Result.Error -> {
                        _uiEvents.send(SearchArtistEvent.ShowError(result.error))
                    }
                }
            }
        }
    }

    fun onArtistClicked(artist: Artist) = viewModelScope.launch {
        _uiEvents.send(SearchArtistEvent.NavigateToAlbumScreen(artist))
    }

    sealed class SearchArtistEvent {
        data class NavigateToAlbumScreen(val artist: Artist) : SearchArtistEvent()
        data class ShowError(val error: String) : SearchArtistEvent()
    }
}