package com.Noto.database;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseHelper { //10120175 - I Wayan Widi P - IF5 - August 2023

    private static final String NOTES_PATH = "notes";
    private DatabaseReference databaseReference;

    public DatabaseHelper() {
        databaseReference = FirebaseDatabase.getInstance().getReference(NOTES_PATH);
    }

    public DatabaseReference getNotesReference() {
        return databaseReference;
    }
}

