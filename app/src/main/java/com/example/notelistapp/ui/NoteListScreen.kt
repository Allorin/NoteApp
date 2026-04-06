package com.example.notelistapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notelistapp.data.Note
import com.example.notelistapp.data.NoteDatabase
import androidx.lifecycle.compose.collectAsStateWithLifecycle
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteViewModel = viewModel(
        factory = NoteViewModelFactory(
            NoteRepository(
                NoteDatabase.getInstance(LocalContext.current).noteDao()
            )
        )
    )
) {
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<Note?>(null) }

    var selectedCategory by rememberSaveable { mutableStateOf("Все") }
    var menuExpanded by rememberSaveable { mutableStateOf(false) }

    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    val categories = remember(notes) {
        listOf("Все") + notes.map { it.category }.distinct()
    }

    val filteredNotes = if (selectedCategory == "Все") notes
    else notes.filter { it.category == selectedCategory }

    val gradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2), Color(0xFFf093fb))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои заметки", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    Text(
                        text = "Категория:",
                        modifier = Modifier.padding(end = 8.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Выбрать категорию")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            categories.forEach { cat: String ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        menuExpanded = false
                                    },
                                    leadingIcon = {
                                        if (selectedCategory == cat) Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingNote = null
                showDialog = true
            }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(brush = Brush.verticalGradient(colors = gradientColors, startY = 0f, endY = 1000f))
        ) {
            if (filteredNotes.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text("Нет заметок", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Нажмите +, чтобы добавить", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.padding(top = 8.dp))
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(filteredNotes, key = { it: Note -> it.id }) { note: Note ->
                        NoteItem(
                            note = note,
                            onToggle = { viewModel.toggleComplete(note) },
                            onEdit = {
                                editingNote = note
                                showDialog = true
                            },
                            onDelete = {
                                noteToDelete = note
                                showDeleteDialog = true
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить заметку?") },
            text = { Text("Это действие нельзя отменить.") },
            confirmButton = {
                Button(
                    onClick = {
                        noteToDelete?.let { viewModel.deleteNote(it) }
                        showDeleteDialog = false
                        noteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }

    if (showDialog) {
        val currentEditingNote = editingNote
        NoteDialog(
            initialText = currentEditingNote?.text ?: "",
            initialCategory = currentEditingNote?.category ?: "Общее",
            onDismiss = { showDialog = false },
            onSave = { text: String, category: String ->
                if (currentEditingNote != null) {
                    viewModel.updateNote(currentEditingNote.copy(text = text, category = category))
                } else {
                    viewModel.addNote(text, category)
                }
                showDialog = false
            }
        )
    }
}