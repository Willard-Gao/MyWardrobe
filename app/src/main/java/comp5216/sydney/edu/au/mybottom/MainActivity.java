package comp5216.sydney.edu.au.mybottom;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity{
    UserFragment mUserFragment;
    WardrobeFragment mWardrobeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationBar bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);

        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.armoire, "Home"))
                .addItem(new BottomNavigationItem(R.drawable.search, "Search"))
                .addItem(new BottomNavigationItem(R.drawable.photo, "Camera"))
                .addItem(new BottomNavigationItem(R.drawable.style, "Style"))
                .addItem(new BottomNavigationItem(R.drawable.user, "Style"))
                .setFirstSelectedPosition(0)
                .initialise();
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_SHIFTING)
                .setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);

        bottomNavigationBar //值得一提，模式跟背景的设置都要在添加tab前面，不然不会有效果。
                .setActiveColor(R.color.design_default_color_primary_dark)//选中颜色 图标和文字
                .setInActiveColor("#8e8e8e")//默认未选择颜色
                .setBarBackgroundColor("#8e8e8e");//默认背景色
//        setDefaultFragment();

        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener(){
            @Override
            public void onTabSelected(int position) {
                Log.d("TAG", "onTabSelected() called with: " + "position = [" + position + "]");
                FragmentManager fm = getFragmentManager();
                //开启事务
                FragmentTransaction transaction = fm.beginTransaction();
                switch (position) {
                    case 0:
                        if (mWardrobeFragment == null) {
                            mWardrobeFragment = WardrobeFragment.newInstance("Wardrobe");
                        }
                        transaction.replace(R.id.fragment_view, mWardrobeFragment);
                        break;
//                    case 1:
//                        if (mScanFragment == null) {
//                            mScanFragment = ScanFragment.newInstance("扫一扫");
//                        }
//                        transaction.replace(R.id.tb, mScanFragment);
//                        break;
//                    case 2:
//                        if (mMyFragment == null) {
//                            mMyFragment = MyFragment.newInstance("个人中心");
//                        }
//                        transaction.replace(R.id.tb, mMyFragment);
//                        break;
//                    case 3:
//                        if (mMyFragment == null) {
//                            mMyFragment = MyFragment.newInstance("个人中心");
//                        }
//                        transaction.replace(R.id.tb, mMyFragment);
//                        break;
                    case 4:
                        if (mUserFragment == null) {
                            mUserFragment = UserFragment.newInstance("个人中心");
                        }
                        transaction.replace(R.id.fragment_view, mUserFragment);
                        break;

                    default:
                        break;
                }

                transaction.commit();// 事务提交
            }
            @Override
            public void onTabUnselected(int position) {
            }
            @Override
            public void onTabReselected(int position) {
            }
        });
    }

    private Boolean isSignedIn = false;
//    FirebaseStorage storage = FirebaseStorage.getInstance();
    Long currentLevel = 0L;
    Long uploadThreshold = 0L;
    ListView listView;
    ArrayList<File> allFile = new ArrayList<>();
    Map<String, List<File>> wardrobe = new HashMap<>();
    WardrobeFragment.WardrobeAdapter wardrobeAdapter;
    List<MyUploadTask> uploadQueue = new LinkedList();


    public Boolean shouldStartSignIn() {
        return (!isSignedIn && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    public void findAllImage(File mediaStorageDir) {
        try {
            String[] file01 = mediaStorageDir.list();
            for (int i = 0; i < file01.length; i++) {
                File file02 = new File(mediaStorageDir + "/" + file01[i]);

                if (file02.isDirectory()) {
                    //Iteration
                    findAllImage(file02);
                } else if (file02.getName().endsWith(".jpg")) {
                    allFile.add(file02);
                }
            }
        } catch (Exception e) {
        }
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        startUploadThread();
//        // Start sign in if necessary
//        if (shouldStartSignIn()) {
//            startSignIn();
//            return;
//        }
//    }

    private final ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    onSignInResult(result);
                }
            }
    );

    private void startSignIn() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build());

// Create and launch sign-in intent
        Intent signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build();
        signInLauncher.launch(signInIntent);
    }


    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK) {
            // Successfully signed in
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            isSignedIn = true;
            String username = String.valueOf(user.getDisplayName());
            Log.i("USER", username);
            Toast.makeText(getApplicationContext(),
                    "Log in success, username: " + username,
                    Toast.LENGTH_SHORT).show();
            // set up username
//            TextView textview = (TextView) findViewById(R.id.username);
//            textview.setText(username);
            // ...
        } else {
            if (response != null){
                Toast.makeText(getApplicationContext(),
                        "Log in failed, error message: " + response.getError(),
                        Toast.LENGTH_LONG).show();
            }
            else{
                Toast.makeText(getApplicationContext(),
                        "User stopped logging in", Toast.LENGTH_SHORT).show();
                startSignIn();
            }
        }
    }

    private Uri getPhotoUri(String absolutePath) {
        Uri photoUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                values.put(MediaStore.Images.Media.DATA, absolutePath);
                photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } catch (Exception e) {
                Log.e("PHOTO URI", e.getMessage());
                e.printStackTrace();
            }
        } else {
            photoUri = Uri.fromFile(new File(absolutePath));
        }
        return photoUri;
    }



    // sharing to instagram
    public void OnShare(View view) {
        String type = "image/*";
//        File mediaStorageDir = ".";
        String filename = "./tmp.jpg";
        String mediaPath = filename;
        createInstagramIntent(type, mediaPath);
    }
    private void createInstagramIntent(String type, String mediaPath) {
        // Create the new Intent using the 'Send' action.
        Intent share = new Intent(Intent.ACTION_SEND);
        // Set the MIME type
        share.setType(type);
        // Create the URI from the media
        File media = new File(mediaPath);
        Uri uri = getPhotoUri(mediaPath);
        // Add the URI to the Intent.
        share.putExtra(Intent.EXTRA_STREAM, uri);
        // Broadcast the Intent.
        startActivity(Intent.createChooser(share, "Share to"));
    }




//    public void doUploadTask(int type) {
//        List tmpFileList = new ArrayList();
//        try {
//            new Thread() {
//                @Override
//                public void run() {
//                    File imgDir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).
//                            getAbsolutePath() + File.separator + "images");
//                    File[] imgFiles = imgDir.listFiles();
//                    StorageReference listRef = storage.getReference().child("images");
//                    Long start = System.currentTimeMillis();
//                    Long finalStart = start;
//                    listRef.listAll()
//                            .addOnSuccessListener(new OnSuccessListener<ListResult>() {
//                                @Override
//                                public void onSuccess(ListResult listResult) {
//                                    for (StorageReference item : listResult.getItems()) {
//                                        tmpFileList.add(item.getName());
//                                    }
//                                    Long end = System.currentTimeMillis();
//                                    System.out.println("list bucket files, time spent " + (end - finalStart) + " ms");
//
//                                    if (type == 0) {
//                                        while (!isWifiConnect()) {
//                                            try {
//                                                sleep(2000);
//                                            } catch (InterruptedException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                    }
//
//                                    for (File tmp : imgFiles) {
//                                        if (tmpFileList.contains(tmp.getName())) {
//                                            continue;
//                                        }
//                                        while (currentLevel > uploadThreshold) {
//                                            try {
//                                                System.out.println( String.format("current traffic %s is bigger than " +
//                                                        "upload threshold %s, wait a moment", currentLevel, uploadThreshold));
//                                                sleep(2000);
//                                            } catch (InterruptedException e) {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                        upLoadByUser(tmp);
//                                    }
//                                }
//                            })
//                            .addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                }
//                            });
//                    super.run();
//                }
//            }.start();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public boolean isWifiConnect() {
//        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo mWifiInfo = connManager.getActiveNetworkInfo();
//        if (mWifiInfo.isConnected() && mWifiInfo.getType() == ConnectivityManager.TYPE_WIFI) {
//            return true;
//        }
//        return false;
//    }
//
//    public void upLoadByUser(File uploadFile) {
//        try {
//            StorageReference storageRef = storage.getReference();
//            Uri fileUri = Uri.fromFile(uploadFile);
//            StorageReference fileRef;
//            fileRef = storageRef.child("images/" + fileUri.getLastPathSegment());
//            UploadTask uploadTask = null;
//            try {
//                uploadTask = fileRef.putStream(new FileInputStream(uploadFile));
//                currentLevel += uploadFile.length();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            uploadTask.addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception exception) {
//                    currentLevel -= uploadFile.length();
//                    Toast.makeText(getApplicationContext(), "Upload file " +
//                            uploadFile.getAbsolutePath() + " Failed, " +
//                            exception.getMessage(), Toast.LENGTH_LONG).show();
//                }
//            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                @Override
//                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    currentLevel -= uploadFile.length();
//                    System.out.println("current traffic : " + currentLevel);
//                }
//            });
//        } catch (Exception e) {
//            currentLevel -= uploadFile.length();
//            e.printStackTrace();
//        }
//    }

    private void setDefaultFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        mUserFragment = UserFragment.newInstance("User Page");
        transaction.replace(R.id.fragment_view, mUserFragment);
        transaction.commit();
    }
}