package com.example.notelistapp.ui

import com.example.notelistapp.data.Note
import com.example.notelistapp.data.NoteDao

class NoteRepository(private val dao: NoteDao) {
    val allNotes = dao.getAllNotes()
    suspend fun insert(note: Note) = dao.insertNote(note)
    suspend fun delete(note: Note) = dao.deleteNote(note)
    suspend fun update(note: Note) = dao.updateNote(note)
}