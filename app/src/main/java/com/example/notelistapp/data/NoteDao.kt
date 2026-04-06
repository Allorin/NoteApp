package com.example.notelistapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Query("SELECT DISTINCT category FROM notes")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT * FROM notes WHERE category = :category ORDER BY id DESC")
    fun getNotesByCategory(category: String): Flow<List<Note>>
}