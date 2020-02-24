package com.example.profchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ListView messageListView;
    private ProfMessageAdapter profMessageAdapter;
    private ProgressBar progressBar;
    private ImageButton sendImageButton;
    private Button sendMessageButton;
    private EditText messageEditText;
    private String userName;
    private String recipientUserId;

    private static final int RC_IMAGE_PICKER = 123;

    private FirebaseAuth auth;

    private String recipientUserName;

    private FirebaseDatabase database;
    private DatabaseReference messagesDatabaseReference;
    private ChildEventListener messagesChildEventListener;

    private DatabaseReference usersDatabaseReference;
    private ChildEventListener usersChildEventListener;

    private FirebaseStorage storage;
    private StorageReference chatImagesStorageReferenc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        if (intent != null) {
            userName = intent.getStringExtra("userName");
            recipientUserId = intent.getStringExtra("recipientUserId");
            recipientUserName = intent.getStringExtra("recipientUserName");
        }

        setTitle(recipientUserName);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        messagesDatabaseReference = database.getReference().child("messages");
        usersDatabaseReference = database.getReference().child("users");
        chatImagesStorageReferenc = storage.getReference().child("chat_images");


        progressBar = findViewById(R.id.progressBar);
        sendImageButton = findViewById(R.id.sendPhotoButton);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messageEditText = findViewById(R.id.messageEditText);
        messageListView = findViewById(R.id.messageListView);
        List<ProfMessage> profMessages = new ArrayList<>();
        profMessageAdapter = new ProfMessageAdapter(this, R.layout.message_item,
                profMessages);
        messageListView.setAdapter(profMessageAdapter);

        //делаем изначально прогрессбар невидимым
        progressBar.setVisibility(ProgressBar.INVISIBLE);

        //метод проверки на наличие символов в EditText. И делаем кнопку Send активной
        messageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    sendMessageButton.setEnabled(true);
                } else {
                    sendMessageButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //ограничеваем количество вводимых символов до 500
        messageEditText.setFilters(new InputFilter[]
                {new InputFilter.LengthFilter(500)});

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ProfMessage profMessage = new ProfMessage();
                profMessage.setText(messageEditText.getText().toString());
                profMessage.setName(userName);
                profMessage.setSender(auth.getCurrentUser().getUid());
                profMessage.setRecipient(recipientUserId);
                profMessage.setImageUrl(null);

                messagesDatabaseReference.push().setValue(profMessage);
                //после отправки смс очищаем EditText с текстом
                messageEditText.setText("");
            }
        });

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                intent1.setType("image/*");
                intent1.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent1, "Choose an image"),
                        RC_IMAGE_PICKER);
            }
        });

        usersChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                User user = dataSnapshot.getValue(User.class);
                if (user.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    userName = user.getName();
                }
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        usersDatabaseReference.addChildEventListener(usersChildEventListener);

        messagesChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ProfMessage profMessage = dataSnapshot.getValue(ProfMessage.class);

                if (profMessage.getSender().equals(auth.getCurrentUser().getUid()) &&
                        profMessage.getRecipient().equals(recipientUserId)) {
                    profMessage.setMine(true);
                    profMessageAdapter.add(profMessage);
                } else if (profMessage.getRecipient().equals(auth.getCurrentUser().getUid()) &&
                        profMessage.getSender().equals(recipientUserId)) {
                    profMessage.setMine(false);
                    profMessageAdapter.add(profMessage);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        messagesDatabaseReference.addChildEventListener(messagesChildEventListener);
    }

    //реализация кнопки меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            //реализация кнопки меню выхода из аккаунта
            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ChatActivity.this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_IMAGE_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            final StorageReference imageReference = chatImagesStorageReferenc
                    .child(selectedImageUri.getLastPathSegment());
            UploadTask uploadTask = imageReference.putFile(selectedImageUri);

            uploadTask = imageReference.putFile(selectedImageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        ProfMessage message = new ProfMessage();
                        message.setImageUrl(downloadUri.toString());
                        message.setName(userName);
                        message.setSender(auth.getCurrentUser().getUid());
                        message.setRecipient(recipientUserId);
                        messagesDatabaseReference.push().setValue(message);
                    } else {
                    }
                }
            });
        }
    }
}
