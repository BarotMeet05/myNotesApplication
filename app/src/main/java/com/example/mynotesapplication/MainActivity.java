package com.example.mynotesapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    EditText editTextNote;
    Button buttonAddNote;
    ListView listViewNotes;
    ArrayList<String> notesList = new ArrayList<>();
    ArrayAdapter<String> notesAdapter;
    // Database Helper
    NotesDBHelper dbHelper;

    private void editNote(final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Note");

        final EditText input = new EditText(this);
        input.setText(notesList.get(position));
        builder.setView(input);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String updatedNote = input.getText().toString();
                notesList.set(position, updatedNote);
                updateNoteInDatabase(position, updatedNote); // Update note in the database
                notesAdapter.notifyDataSetChanged();
            }
        });
        builder.show();
    }

    private void deleteNote(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete this note?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteNoteFromDatabase(position); // Delete note from the database
                notesList.remove(position);
                notesAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextNote = findViewById(R.id.editTextNote);
        buttonAddNote = findViewById(R.id.buttonAddNote);
        listViewNotes = findViewById(R.id.listViewNotes);

        // Initialize the database helper
        dbHelper = new NotesDBHelper(this);

        // Load notes from the database
        notesList = dbHelper.getAllNotes();

        // Create a custom adapter to handle the list items
        notesAdapter = new ArrayAdapter<String>(this, R.layout.list_item_note, R.id.textViewNote, notesList) {
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                final Button editButton = view.findViewById(R.id.buttonEdit);
                final Button deleteButton = view.findViewById(R.id.buttonDelete);

                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        editNote(position);
                    }
                });

                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteNote(position);
                    }
                });

                return view;
            }
        };

        listViewNotes.setAdapter(notesAdapter);

        buttonAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String noteText = editTextNote.getText().toString();
                if (!noteText.isEmpty()) {
                    long noteId = addNoteToDatabase(noteText); // Add note to the database
                    if (noteId != -1) {
                        notesList.add(noteText);
                        notesAdapter.notifyDataSetChanged();
                        editTextNote.setText("");
                    }
                }
            }
        });
    }

    private long addNoteToDatabase(String note) {
        return dbHelper.insertNote(note);
    }

    private void updateNoteInDatabase(int position, String updatedNote) {
        int noteId = dbHelper.getNoteId(notesList.get(position));
        dbHelper.updateNote(noteId, updatedNote);
    }

    private void deleteNoteFromDatabase(int position) {
        int noteId = dbHelper.getNoteId(notesList.get(position));
        dbHelper.deleteNote(noteId);
    }
}
