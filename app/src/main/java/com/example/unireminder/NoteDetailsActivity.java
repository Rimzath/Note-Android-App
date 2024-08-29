package com.example.unireminder;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

public class NoteDetailsActivity extends AppCompatActivity {

    EditText titleEditText, contentEditText;
    ImageButton saveNoteBtn, addPhotoBtn, removePhotoBtn;
    TextView pageTitleTextView;
    ImageView photoView;
    String title, content, docId, photoPath;
    boolean isEditMode = false;
    TextView deleteNoteTextViewBtn;

    static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        titleEditText = findViewById(R.id.notes_title_text);
        contentEditText = findViewById(R.id.notes_content_text);
        saveNoteBtn = findViewById(R.id.save_note_btn);
        pageTitleTextView = findViewById(R.id.page_title);
        deleteNoteTextViewBtn = findViewById(R.id.delete_note_text_view_btn);
        addPhotoBtn = findViewById(R.id.add_photo_btn);
        removePhotoBtn = findViewById(R.id.remove_photo_btn);
        photoView = findViewById(R.id.photo_view);

        // Receive data
        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        photoPath = getIntent().getStringExtra("photoPath");
        docId = getIntent().getStringExtra("docId");

        if (docId != null && !docId.isEmpty()) {
            isEditMode = true;
        }

        titleEditText.setText(title);
        contentEditText.setText(content);

        // Display photo if available
        if (photoPath != null && !photoPath.isEmpty()) {
            photoView.setVisibility(View.VISIBLE);
            removePhotoBtn.setVisibility(View.VISIBLE);
            photoView.setImageURI(Uri.parse(photoPath));
        }

        if (isEditMode) {
            pageTitleTextView.setText("Edit your note");
            deleteNoteTextViewBtn.setVisibility(View.VISIBLE);
        }

        saveNoteBtn.setOnClickListener((v) -> saveNote());

        deleteNoteTextViewBtn.setOnClickListener((v) -> deleteNoteFromFirebase());

        addPhotoBtn.setOnClickListener((v) -> openGallery());

        removePhotoBtn.setOnClickListener((v) -> removePhoto());
    }

    void saveNote() {
        String noteTitle = titleEditText.getText().toString();
        String noteContent = contentEditText.getText().toString();
        if (noteTitle == null || noteTitle.isEmpty()) {
            titleEditText.setError("Title is required");
            return;
        }

        Note note = new Note();
        note.setTitle(noteTitle);
        note.setContent(noteContent);
        note.setTimestamp(Timestamp.now());
        note.setPhotoPath(photoPath);

        saveNoteToFirebase(note);
    }

    void saveNoteToFirebase(Note note) {
        DocumentReference documentReference;
        if (isEditMode) {
            documentReference = Utility.getCollectionReferenceForNotes().document(docId);
        } else {
            documentReference = Utility.getCollectionReferenceForNotes().document();
        }

        documentReference.set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Utility.showToast(NoteDetailsActivity.this, "Note added successfully!");
                    finish();
                } else {
                    Utility.showToast(NoteDetailsActivity.this, "Failed while adding note!");
                }
            }
        });
    }

    void deleteNoteFromFirebase() {
        DocumentReference documentReference;
        documentReference = Utility.getCollectionReferenceForNotes().document(docId);
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Utility.showToast(NoteDetailsActivity.this, "Note deleted successfully!");
                    finish();
                } else {
                    Utility.showToast(NoteDetailsActivity.this, "Failed while deleting note!");
                }
            }
        });
    }

    void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    void removePhoto() {
        photoPath = null;
        photoView.setVisibility(View.GONE);
        removePhotoBtn.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                photoPath = selectedImageUri.toString();
                photoView.setVisibility(View.VISIBLE);
                removePhotoBtn.setVisibility(View.VISIBLE);
                photoView.setImageURI(selectedImageUri);
            }
        }
    }
}
