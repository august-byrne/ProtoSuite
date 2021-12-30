package com.augustbyrne.tas.ui.notes

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.augustbyrne.tas.data.PreferenceManager
import com.augustbyrne.tas.data.db.entities.DataItem
import com.augustbyrne.tas.data.db.entities.NoteItem
import com.augustbyrne.tas.data.db.entities.NoteWithItems
import com.augustbyrne.tas.data.repositories.NoteRepository
import com.augustbyrne.tas.util.DarkMode
import com.augustbyrne.tas.util.SortType
import com.augustbyrne.tas.util.TimerTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repo: NoteRepository,
    private val preferences: PreferenceManager
): ViewModel() {

    /**
     * Room Local Database Here, LiveData for view access, since Flow gives me lifecycle bugs
     */
    // returns noteId. Be aware this does not update nicely with one to many relations
    suspend fun upsert(item: NoteItem): Long = repo.upsert(item)

    suspend fun updateNote(item: NoteItem) = repo.updateNote(item)

    suspend fun upsertDataItem(item: DataItem) = repo.upsertDataItem(item)

    fun upsertNoteAndData(noteItem: NoteItem, dataItems: MutableList<DataItem>) =
        CoroutineScope(Dispatchers.Main).launch {
            if (noteItem.id == 0) {
                val noteDataId = repo.upsert(noteItem).toInt()
                dataItems.replaceAll {
                    it.copy(
                        parent_id = noteDataId,
                        order = dataItems.lastIndex - dataItems.indexOf(it)
                    )
                }
            } else {
                repo.upsert(noteItem)
            }
            repo.upsertData(dataItems)
        }

    fun deleteNote(id: Int) = CoroutineScope(Dispatchers.Main).launch {
        repo.cascadeDeleteNote(id)
    }

    suspend fun deleteDataItem(id: Int) = repo.deleteDataItem(id)

    fun sortedAllNotesWithItems(sortType: SortType): LiveData<List<NoteWithItems>> =
        repo.allNotesWithItems.map { list ->
            when (sortType) {
                SortType.Creation -> {
                    list.sortedByDescending { it.note.creation_date }
                }
                SortType.LastEdited -> {
                    list.sortedByDescending { it.note.last_edited_on }
                }
                SortType.Order -> {
                    list.sortedByDescending { it.note.order }
                }
                SortType.Default -> {
                    list
                }
            }
        }.asLiveData()

    fun getNoteWithItemsById(id: Int): LiveData<NoteWithItems> =
        repo.getNoteWithItemsById(id).asLiveData()

    /**
     * PreferenceManager DataStore Preferences Here
     */
    val showAdsFlow: LiveData<Boolean> = preferences.showAdsFlow.asLiveData()
    suspend fun setShowAds(value: Boolean) = preferences.setShowAds(value)

    val isDarkThemeFlow: LiveData<DarkMode> =
        preferences.isDarkThemeFlow.map { DarkMode.getMode(it) }.asLiveData()

    suspend fun setIsDarkTheme(value: DarkMode) = preferences.setIsDarkTheme(value.mode)

    val timerThemeFlow: LiveData<TimerTheme> =
        preferences.timerThemeFlow.map { TimerTheme.getTheme(it) }.asLiveData()

    suspend fun setTimerTheme(value: TimerTheme) = preferences.setTimerTheme(value.theme)

    val sortTypeFlow: LiveData<SortType> =
        preferences.sortTypeFlow.map { SortType.getType(it) }.asLiveData()

    suspend fun setSortType(value: SortType) = preferences.setSortType(value.type)

    val lastUsedTimeUnitFlow: LiveData<Int> = preferences.lastUsedTimeUnitFlow.asLiveData()
    suspend fun setLastUsedTimeUnit(value: Int) = preferences.setLastUsedTimeUnit(value)

    /**
     * Other Cross-Composable Variables Here
     */
    var initialDialogDataItem: DataItem? by mutableStateOf(null)
    var tempSavedNote: NoteWithItems? = null
    var openSortPopup by mutableStateOf(false)
    var openEditDialog by mutableStateOf(false)
    var selectedNavBarItem by mutableStateOf(0)

    private var listState: LazyListState = LazyListState()
    fun saveListPosition(newListState: LazyListState) {
        listState = newListState
    }

    fun loadListPosition(): LazyListState = listState
}
