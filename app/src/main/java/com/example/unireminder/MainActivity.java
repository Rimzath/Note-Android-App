package com.example.unireminder;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;

    FloatingActionButton addNoteBtn;
    RecyclerView recyclerView;
    ImageButton menuBtn;
    NoteAdapter noteAdapter;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addNoteBtn = findViewById(R.id.add_note_btn);
        recyclerView = findViewById(R.id.recyler_view);
        menuBtn = findViewById(R.id.menu_btn);
        searchView = findViewById(R.id.search_view);

        // Check for storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, proceed with accessing the storage
            proceedWithStorageAccess();
        }

        addNoteBtn.setOnClickListener((v) -> startActivity(new Intent(MainActivity.this, NoteDetailsActivity.class)));
        menuBtn.setOnClickListener((v) -> showMenu());
        setupRecyclerView("");

        // Adding search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                setupRecyclerView(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                setupRecyclerView(newText);
                return false;
            }
        });
    }

    void showMenu() {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, menuBtn);
        popupMenu.getMenu().add("Logout");
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getTitle().equals("Logout")) {
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    void setupRecyclerView(String searchText) {
        Query query;

        if (searchText.isEmpty()) {
            query = Utility.getCollectionReferenceForNotes().orderBy("timestamp", Query.Direction.DESCENDING);
        } else {
            query = Utility.getCollectionReferenceForNotes()
                    .orderBy("title")
                    .startAt(searchText)
                    .endAt(searchText + "\uf8ff");
        }

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class).build();

        // Use GridLayoutManager instead of LinearLayoutManager
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));


        noteAdapter = new NoteAdapter(options, this);
        recyclerView.setAdapter(noteAdapter);
        noteAdapter.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        noteAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        noteAdapter.notifyDataSetChanged();
    }

    // Handle the permission request response
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted, proceed with accessing the storage
                proceedWithStorageAccess();
            } else {
                // Permission was denied, show a message to the user
                Toast.makeText(this, "Permission denied. Cannot access storage.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Your method to proceed with accessing storage
    private void proceedWithStorageAccess() {
        // Here you can access the external storage, load images, etc.
        // For example, you might want to load an image from storage.
        // This is a placeholder for your storage access logic.
        Toast.makeText(this, "Permission granted! Now you can access storage.", Toast.LENGTH_SHORT).show();
    }
}
