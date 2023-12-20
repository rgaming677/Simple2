package com.example.Simple2.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Simple2.R;
import com.example.Simple2.database.NoteDatabase;
import com.example.Simple2.database.entities.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CreateNoteActivity extends AppCompatActivity {

    private EditText etNoteTitle, etNoteSubtitle, etNote;
    private TextView tvDateTime;
    private View vSubtitleIndicator;
    private ImageView ivNote;

    private String selectNotesColor;
    private String selectImagePath;

    private static final int REQUEST_CODE_STORANGE_PERMISION = 1;
    private boolean shouldSelectImage = false;

    private ActivityResultLauncher<String> requeatPermisionLauncher;
    private ActivityResultLauncher<Intent> selectImageLauncher;

    private void sendResultToMainActivity(String etText1, String etText2) {
        Intent intent = new Intent();
        intent.putExtra("key1", etText1);
        intent.putExtra("key2", etText2);
        setResult(RESULT_OK, intent);
        finish();
    }

    //
    public void setShouldSelectImage(boolean shouldSelectImage) {
        this.shouldSelectImage = shouldSelectImage;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        //Button back
        ImageView ivBack = findViewById(R.id.ivBack);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getOnBackPressedDispatcher().onBackPressed();
            }
        });

        //Input Data
        etNoteTitle = findViewById(R.id.etNoteTitle);
        etNoteSubtitle = findViewById(R.id.etNoteSubtitle);
        etNote = findViewById(R.id.etNote);
        tvDateTime = findViewById(R.id.tvDateTime);
        vSubtitleIndicator = findViewById(R.id.vSubtitleIndicator);
        ivNote = findViewById(R.id.ivNote);

        tvDateTime.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date())
        );

        ivNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    selectImageLauncher.launch(intent);
                }
            }
        });

        requeatPermisionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        selectImage();
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    }
                });

        selectImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleImageSelection(result.getData().getData());
                    }
                });

        if (shouldSelectImage) {
            shouldSelectImage = false;
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requeatPermisionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                selectImage();
            }
        }


        //Button done, save data to database. Share preference.
        ImageView ivDone = findViewById(R.id.ivDone);

        ivDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String etText1 = etNoteTitle.getText().toString().trim();
                String etText2 = etNoteSubtitle.getText().toString().trim();

                if (!etText1.isEmpty() && !etText2.isEmpty()) {
                    Intent intent = new Intent();
                    sendResultToMainActivity(etText1, etText2);
                    setResult(RESULT_OK, intent);
                    saveNote();
                    finish();
                } else {
                    Toast.makeText(CreateNoteActivity.this, "Text tidak boleh kosong", Toast.LENGTH_SHORT).show();
                }



            }
        });

        selectNotesColor = "#FFFFFF";
        selectImagePath = "";

        initMiscellaneous();
        setSubtitleIndicatorColor();
    }

    //Save data
    private void saveNote() {
        final Note note = new Note();
        note.setTitle(etNoteTitle.getText().toString());
        note.setSubtitle(etNoteSubtitle.getText().toString());
        note.setNoteText(etNote.getText().toString());
        note.setDateTime(tvDateTime.getText().toString());
        note.setColor(selectNotesColor);
        note.setImagePath(selectImagePath);

        //Main Treat
        Executor executor = Executors.newSingleThreadExecutor();
        CompletableFuture.runAsync(() -> {
            NoteDatabase.getInstance(getApplicationContext()).noteDao().insertNote(note);
        }, executor).thenRun(() -> {
            runOnUiThread(() -> {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            });
        });


    }

    private void initMiscellaneous() {
        final LinearLayout llMiscellaneous = findViewById(R.id.llMiscellaneous);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(llMiscellaneous);
        llMiscellaneous.findViewById(R.id.tvMiscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        final ImageView imageColor1 = llMiscellaneous.findViewById(R.id.ivColor1);
        final ImageView imageColor2 = llMiscellaneous.findViewById(R.id.ivColor2);
        final ImageView imageColor3 = llMiscellaneous.findViewById(R.id.ivColor3);
        final ImageView imageColor4 = llMiscellaneous.findViewById(R.id.ivColor4);
        final ImageView imageColor5 = llMiscellaneous.findViewById(R.id.ivColor5);

        llMiscellaneous.findViewById(R.id.vColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectNotesColor = "#FFFFFF";
                imageColor1.setImageResource(R.drawable.ic_done);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        llMiscellaneous.findViewById(R.id.vColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectNotesColor = "#EDEADE";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_done);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        llMiscellaneous.findViewById(R.id.vColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectNotesColor = "#F5F5DC";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_done);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        llMiscellaneous.findViewById(R.id.vColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectNotesColor = "#FFF8DC";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_done);
                imageColor5.setImageResource(0);
                setSubtitleIndicatorColor();
            }
        });

        llMiscellaneous.findViewById(R.id.vColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectNotesColor = "#FFFDD0";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_done);
                setSubtitleIndicatorColor();
            }
        });

        llMiscellaneous.findViewById(R.id.llAddImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORANGE_PERMISION
                    );
                } else {
                    selectImage();
                }

            }
        });
    }

    private void setSubtitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) vSubtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectNotesColor));
    }



    private void selectImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) !=null) {
            selectImageLauncher.launch(intent);
        }
    }


    private void handleImageSelection(Uri selectImageUri) {
        if (selectImageUri != null){
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                ivNote.setImageBitmap(bitmap);
                ivNote.setVisibility(View.VISIBLE);

                selectImagePath = getPathFromUri(selectImageUri);

            } catch (Exception exception) {
                Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver()
                .query(contentUri, null, null, null, null);
        if (cursor == null){
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }
}