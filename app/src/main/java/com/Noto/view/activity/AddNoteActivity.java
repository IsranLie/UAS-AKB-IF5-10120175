package com.Noto.view.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.Noto.R;
import com.Noto.database.DatabaseHelper;
import com.Noto.model.Note;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Date;
import java.util.Random;

public class AddNoteActivity extends AppCompatActivity { //10120175 - I Wayan Widi P - IF5 - August 2023
    ImageButton button;
    EditText editTitle;
    EditText editCategory;
    EditText editDesc;
    Button addButton;
    Button deleteButton;
    TextView titleAdd;

    private DatabaseReference databaseReference;
    Note note = null;
    String idUser = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private void sendCloudMessage(String title, String message) {
        // Replace "topic_name" with the topic to which you want to send the message
        FirebaseMessaging.getInstance().subscribeToTopic("topic_name")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Send the notification data to the subscribed topic
                        RemoteMessage.Builder builder = new RemoteMessage.Builder("843257242028@gcm.googleapis.com")
                                .setMessageId(Integer.toString(new Random().nextInt(1000)))
                                .addData("title", title)
                                .addData("body", message);

                        FirebaseMessaging.getInstance().send(builder.build());
                    } else {
                        Toast.makeText(this, "Failed to subscribe to the topic", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        getSupportActionBar().hide();
        note = (Note) getIntent().getSerializableExtra("Note");
        button = findViewById(R.id.back);
        editTitle = findViewById(R.id.title);
        editCategory = findViewById(R.id.category);
        editDesc = findViewById(R.id.txt_desc);
        addButton = findViewById(R.id.buttonAdd);
        deleteButton = findViewById(R.id.buttonDelete);
        titleAdd = findViewById(R.id.txt_add);
        databaseReference = new DatabaseHelper().getNotesReference();

        button.setOnClickListener(v -> {
            finish();
        });

        if (note == null){
            deleteButton.setVisibility(View.GONE);

            addButton.setOnClickListener(v -> {
                if (editTitle.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Note title cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (editCategory.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Note category cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (editDesc.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Note content cannot be empty!", Toast.LENGTH_SHORT).show();
                }
                Date d = new Date();
                CharSequence date = DateFormat.format("EEEE, d MMM yyyy HH:mm", d.getTime());
                String id = databaseReference.push().getKey();
                Note n = new Note(
                        id,
                        editTitle.getText().toString(),
                        editCategory.getText().toString(),
                        editDesc.getText().toString(),
                        date+""
                );

                if (id != null) {
                    databaseReference.child(idUser).child(id).setValue(n);
                    finish();
                    Toast.makeText(this, "\n" + "Note added successfully", Toast.LENGTH_SHORT).show();
//                    notification();
                    // After saving the note, send a cloud message
                    sendCloudMessage(editTitle.getText().toString(), "Note has been saved");
                    String title = "Note has been successfully saved";
                    notification(title);
                } else {
                    Toast.makeText(this, "Failed to add note: Invalid note ID", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            editTitle.setText(note.getTitle());
            editCategory.setText(note.getCategory());
            editDesc.setText(note.getDesc());
            deleteButton.setVisibility(View.VISIBLE);
            titleAdd.setText("Change note");

            addButton.setOnClickListener(v -> {
                if (editTitle.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Note title cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (editCategory.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Note category cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (editDesc.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Note content cannot be empty!", Toast.LENGTH_SHORT).show();
                }

                Date d = new Date();
                CharSequence date = DateFormat.format("EEEE, d MMMM yyyy HH:mm", d.getTime());

                note.setTitle(editTitle.getText().toString());
                note.setCategory(editCategory.getText().toString());
                note.setDesc(editDesc.getText().toString());
                note.setDate("last modified " + date + "");
                String idNote = note.getId();
                databaseReference.child(idUser).child(idNote).setValue(note);
                Toast.makeText(this, "Note successfully modified", Toast.LENGTH_SHORT).show();
                String title = "Note successfully modified";
                notification(title);
                finish();
            });
        }

        deleteButton.setOnClickListener(v-> {
            if (note != null) {
                showDeleteConfirmationDialog(note);
            } else {
                Toast.makeText(this, "Note not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete confirm");
        builder.setMessage("Are you sure you want to delete this note?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String noteId = note.getId();
                if (noteId != null) {
                    databaseReference.child(idUser).child(noteId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                finish();
                                Toast.makeText(AddNoteActivity.this, "Note successfully deleted", Toast.LENGTH_SHORT).show();
                                String title = "Note successfully deleted";
                                notification(title);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(AddNoteActivity.this, "Failed to delete note", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(AddNoteActivity.this, "Failed to delete note: invalid note ID", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing, dismiss the dialog
            }
        });
        builder.create().show();
    }

    private void notification(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel("n", "n", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "n")
                .setContentText("Note")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentText(text);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        managerCompat.notify(999, builder.build());
    }
}

