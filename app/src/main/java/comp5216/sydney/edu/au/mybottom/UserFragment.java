package comp5216.sydney.edu.au.mybottom;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


public class UserFragment extends Fragment {
    public static UserFragment newInstance(String param1) {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        args.putString("agrs1", param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_user, container, false);
        Bundle bundle = getArguments();
        String agrs1 = bundle.getString("agrs1");
        TextView tv = (TextView)view.findViewById(R.id.container);
        tv.setText("agrs1");
        ImageView imageView = view.findViewById(R.id.userpro);

        view.findViewById(R.id.aboutus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open url
                String website = getResources().getString(R.string.our_website);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(website)));
            }
        });
        view.findViewById(R.id.feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open url
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{getResources().getString(R.string.our_email)});
                intent.setType("message/rfc882");
                startActivity(Intent.createChooser(intent, "choose one email"));
            }

        });
        return view;
    }


}