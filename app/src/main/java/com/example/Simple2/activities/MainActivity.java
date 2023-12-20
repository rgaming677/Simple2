package com.example.Simple2.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.Simple2.R;
import com.example.Simple2.adapters.NoteAdapters;
import com.example.Simple2.database.NoteDatabase;
import com.example.Simple2.database.entities.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rcHome;
    private List<Note> noteList;
    private NoteAdapters noteAdapters;


    //Result data
    private ActivityResultLauncher<Intent> noteActivityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK){
                            Intent data = result.getData();
                            if (data !=null){
                                if (data.hasExtra("key1") && data.hasExtra("key2")) {
                                    String value1 = data.getStringExtra("key1");
                                    String value2 = data.getStringExtra("key2");
                                    Log.d("MainActivity", "Received data from CreateNoteActivity: " + value1 + ", " + value2);
                                    getNotes();
                                }
                            }
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Button create new notes.
        ImageView ivAddNoteMain = findViewById(R.id.ivAddNoteMain);
        ivAddNoteMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Log.d("Simple2", "Button clicked");
                    Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                    noteActivityResultLauncher.launch(intent);

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Simple2", "Exception: " + e.getMessage());
                }

            }
        });


        //Layout menu
        rcHome = findViewById(R.id.rcHome);
        rcHome.setLayoutManager(
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        );

        //List menu
        noteList = new ArrayList<>();
        noteAdapters = new NoteAdapters(noteList);
        rcHome.setAdapter(noteAdapters);

        getNotes();

    }

    //Load data to menu
    private void getNotes() {
        Executor executor = Executors.newSingleThreadExecutor();

        CompletableFuture.supplyAsync(() ->
                NoteDatabase.getInstance(getApplicationContext())
                        .noteDao().getAllnotes(),
                executor)
                .thenAccept(this::onNoteReceived);
    }


    private void onNoteReceived(List<Note> notes) {
        runOnUiThread(() -> {
            if (noteList.size() == 0) {
                noteList.addAll(notes);
                noteAdapters.notifyDataSetChanged();
            } else {
                noteList.add(0, notes.get(0));
                noteAdapters.notifyItemInserted(0);
            }
            rcHome.smoothScrollToPosition(0);
        });

    }

    public void startCreateNoteActivity() {
        Intent intent = new Intent(MainActivity.this, CreateNoteActivity.class);
        noteActivityResultLauncher.launch(intent);
    }


}