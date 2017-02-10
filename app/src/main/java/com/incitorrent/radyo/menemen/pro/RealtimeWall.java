package com.incitorrent.radyo.menemen.pro;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.incitorrent.radyo.menemen.pro.utils.Menemen;

public class RealtimeWall extends AppCompatActivity implements View.OnLongClickListener{
    FirebaseDatabase database;
    DatabaseReference dbRef;
    TextView nick,post;
    Menemen m;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comm_wall);
        context = RealtimeWall.this;
        m = new Menemen(context);
        nick = (TextView) findViewById(R.id.nick);
        post = (TextView) findViewById(R.id.post);
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("Wall");
        setTitle(getString(R.string.leave_a_note));
        post.setOnLongClickListener(this);

    }

    @Override
    protected void onStart() {
        dbRef.addValueEventListener(valueEventListener);
        super.onStart();
    }

    @Override
    protected void onStop() {
        dbRef.removeEventListener(valueEventListener);
        super.onStop();
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            try {
                nick.setText(dataSnapshot.child("nick").getValue(String.class));
               if(dataSnapshot.child("post").getValue(String.class).trim().length()>0 || dataSnapshot.child("post").getValue(String.class).trim().length()<21)
                   post.setText(dataSnapshot.child("post").getValue(String.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    public boolean onLongClick(View view) {
        if(view == post){
            if(!m.isLoggedIn()){
                Toast.makeText(context, R.string.toast_you_must_be_logged_in, Toast.LENGTH_SHORT).show();
                return false;
            }
            editText();
        }
        return false;
    }

    private void editText() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this,R.style.alertDialogTheme));
        builder.setTitle(getString(R.string.hint_write_something));
        // Set up the input
        final EditText input = new EditText(this);
        input.setHint(R.string.ET_hint_write_something);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(16, 16, 16, 16);
        input.setLayoutParams(layoutParams);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(input.getText().toString().trim().length()<1 || input.getText().toString().trim().length()>20)
                    return;
                post.setText(input.getText().toString());
                dbRef.child("post").setValue(input.getText().toString());
                dbRef.child("nick").setValue(m.getUsername());
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}
