package com.ptit.soa.vehicleshare.activity.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ptit.soa.vehicleshare.R;

import java.io.IOException;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,NavigationView.OnNavigationItemSelectedListener {

    private TextView txtName,txtEmail,txtFullname,txtUserEmail;
    private ImageView imgAvatar,imgUpload,imageView;
    private Button btnLogout, btnChooseFile,btnUpload;


    private GoogleApiClient googleApiClient;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;

    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        NavigationView signOutButton = findViewById(R.id.nav_view_signout);
        signOutButton.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                LogOut();
                drawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });

        final View viewHeaderMain = navigationView.getHeaderView(0);
        txtFullname = viewHeaderMain.findViewById(R.id.tv_fullname);
        txtUserEmail = viewHeaderMain.findViewById(R.id.tv_email);
        imageView = viewHeaderMain.findViewById(R.id.img_profile);

        AnhXa();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();

        firebaseAuth = firebaseAuth.getInstance();
        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if(user != null){
                    setUserData(user);
                    /*

                    txtFullname.setText(user.getDisplayName());
                    txtEmail.setText(user.getEmail());
                    Glide.with(getApplicationContext()).load(user.getPhotoUrl()).into(imageView);*/
                }
                else{
                    goLoginScreen();
                }
            }
        };

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogOut();
            }
        });

        btnChooseFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    private void uploadImage() {
        if(filePath != null){
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Dang Upload....");
            progressDialog.show();

            StorageReference ref = storageReference.child(user.getUid()+"/"+ UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Tai len thanh cong! URL la:" + taskSnapshot.getDownloadUrl(), Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Tai len that bai", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Da tai len: "+(int)progress+" %");
                        }
                    });


        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType(("image/*"));
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Chon Anh:"),PICK_IMAGE_REQUEST);
    }

    private void setUserData(FirebaseUser user) {
        txtName.setText(user.getDisplayName());
        txtEmail.setText(user.getEmail());
        Glide.with(this).load(user.getPhotoUrl()).into(imgAvatar);

        //set Data cho NavigationView

        txtUserEmail.setText(user.getEmail());
        txtFullname.setText(user.getDisplayName());
        Glide.with(this).load(user.getPhotoUrl()).into(imageView);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            filePath = data.getData();
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filePath);
                imgUpload.setImageBitmap(bitmap);
            }
            catch (IOException e){
                Toast.makeText(this, "Loi Chon File"+e.toString(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(firebaseAuthListener);

    }

/*    private void handleSignInResult(GoogleSignInResult result) {
        if(result.isSuccess()){

            GoogleSignInAccount account = result.getSignInAccount();

            if(account != null){
                txtName.setText(account.getDisplayName());
                txtEmail.setText(account.getEmail());
                Glide.with(getApplicationContext()).load(account.getPhotoUrl()).into(imgAvatar);
            }
            else{
                Toast.makeText(this, "User null", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            goLoginScreen();
        }
    }*/

    private void goLoginScreen() {
        Intent intent = new Intent(this,LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void LogOut(){
        firebaseAuth.signOut();

        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()){
                    goLoginScreen();
                }
                else{
                    Toast.makeText(MainActivity.this, "Dang Xuat That Bai!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void revoke(){
        firebaseAuth.signOut();

        Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()){
                    goLoginScreen();
                }
                else{
                    Toast.makeText(MainActivity.this, "Loi Revoke Login", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void AnhXa(){
        txtEmail = findViewById(R.id.txtEmail);
        txtName = findViewById(R.id.txtName);
        imgAvatar = findViewById(R.id.imgAvatar);
        btnLogout = findViewById(R.id.btnLogout);
        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnUpload = findViewById(R.id.btnUpload);
        imgUpload = findViewById(R.id.imgUpload);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseAuthListener != null){
            firebaseAuth.removeAuthStateListener(firebaseAuthListener);
        }
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}