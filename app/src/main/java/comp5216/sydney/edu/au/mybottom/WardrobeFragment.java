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
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class WardrobeFragment extends Fragment {


    public static WardrobeFragment newInstance(String param1) {
        WardrobeFragment fragment = new WardrobeFragment();
        Bundle args = new Bundle();
        args.putString("agrs1", param1);
        fragment.setArguments(args);
        return fragment;
    }

    class WardrobeAdapter extends ArrayAdapter<Type> {
        public WardrobeAdapter(Context context, ArrayList<Type> itemList) {
            super(context, 0, itemList);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Type type = getItem(i);
            if (view == null) {
                view = LayoutInflater.from(getContext()).inflate(R.layout.list_item, viewGroup, false);
            }

            TextView typeName = view.findViewById(R.id.typeName);
            GridView gridView = view.findViewById(R.id.grid);
            setGridView(type, gridView);
            typeName.setText(type.getName());
            return view;
        }

        private void setGridView(Type type, GridView gridView) {
            int size = type.getClothes().size();
            int length = 100;
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            float density = dm.density;
            int gridviewWidth = (int) (size * (length + 4) * density);
            int itemWidth = (int) (length * density);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    gridviewWidth, LinearLayout.LayoutParams.FILL_PARENT);
            gridView.setLayoutParams(params); // 设置GirdView布局参数,横向布局的关键
            gridView.setColumnWidth(itemWidth); // 设置列表项宽
            gridView.setHorizontalSpacing(5); // 设置列表项水平间距
            gridView.setStretchMode(GridView.NO_STRETCH);
            gridView.setNumColumns(size); // 设置列数量=列表集合数

            GriditemAdapter adapter = new GriditemAdapter(getActivity().getApplicationContext(),
                    type.getClothes());
            gridView.setAdapter(adapter);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Map<String, List<File>> wardrobe = new HashMap<>();
        wardrobe.put("Hats", new LinkedList<>());
        wardrobe.put("Tops", new LinkedList<>());
        wardrobe.put("Bottoms", new LinkedList<>());
        wardrobe.put("Shoes", new LinkedList<>());
        wardrobe.put("Accessories", new LinkedList<>());
        ArrayList<Type> typeList = new ArrayList<>(5);
        typeList.add(new Type("Hats", wardrobe.get("Hats")));
        typeList.add(new Type("Tops", wardrobe.get("Tops")));
        typeList.add(new Type("Bottoms", wardrobe.get("Bottoms")));
        typeList.add(new Type("Shoes", wardrobe.get("Shoes")));
        typeList.add(new Type("Accessories", wardrobe.get("Accessories")));
        View view = inflater.inflate(R.layout.fragment_wardrobe, container, false);
        Bundle bundle = getArguments();
        String agrs1 = bundle.getString("agrs1");
        WardrobeAdapter wardrobeAdapter = new WardrobeAdapter(getActivity().getApplicationContext(), typeList);
        ListView listView =  view.findViewById(R.id.list);
        listView.setAdapter(wardrobeAdapter);
        return view;
    }
}