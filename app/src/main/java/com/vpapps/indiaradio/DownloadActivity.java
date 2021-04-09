package com.vpapps.indiaradio;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vpapps.adapter.AdapterOFSongList;
import com.vpapps.interfaces.ClickListenerPlayList;
import com.vpapps.interfaces.InterAdListener;
import com.vpapps.item.ItemSong;
import com.vpapps.utils.Constant;
import com.vpapps.utils.DBHelper;
import com.vpapps.utils.Methods;
import com.vpapps.indiaradio.R;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class DownloadActivity extends BaseActivity {

    private DBHelper dbHelper;
    private Methods methods;
    private RecyclerView rv;
    private AdapterOFSongList adapter;
    private ArrayList<ItemSong> arrayList;
    private CircularProgressBar progressBar;

    private FrameLayout frameLayout;
    private String errr_msg = "";
    private SearchView searchView;
    private String addedFrom = "download";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout contentFrameLayout = findViewById(R.id.content_frame);
        getLayoutInflater().inflate(R.layout.activity_song_by_cat, contentFrameLayout);

        toolbar.setTitle(getString(R.string.downloads));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_back);

        dbHelper = new DBHelper(DownloadActivity.this);
        methods = new Methods(DownloadActivity.this, new InterAdListener() {
            @Override
            public void onClick(int position, String type) {
                Intent intent = new Intent(DownloadActivity.this, PlayerService.class);
                intent.setAction(PlayerService.ACTION_PLAY);
                startService(intent);
            }
        });
        errr_msg = getString(R.string.err_no_songs_found);

        arrayList = new ArrayList<>();

        progressBar = findViewById(R.id.pb_song_by_cat);
        frameLayout = findViewById(R.id.fl_empty);

        rv = findViewById(R.id.rv_song_by_cat);
        LinearLayoutManager llm = new LinearLayoutManager(DownloadActivity.this);
        rv.setLayoutManager(llm);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setHasFixedSize(true);

        if(checkPer()) {
            new LoadDownloadSongs().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_search);
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setOnQueryTextListener(queryTextListener);
        for (int i= 0; i < menu.size();i++){
            MenuItem menuItem = menu.getItem(i);
            SpannableString s = new SpannableString(menu.getItem(i).getTitle().toString());
            s.setSpan(new ForegroundColorSpan(Color.RED),0,s.length(),0);
            menuItem.setTitle(s);
        }
        return super.onCreateOptionsMenu(menu);
    }

    SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            if (adapter != null) {
                if (!searchView.isIconified()) {
                    adapter.getFilter().filter(s);
                    adapter.notifyDataSetChanged();
                }
            }
            return true;
        }
    };

    class LoadDownloadSongs extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            arrayList.clear();
            frameLayout.setVisibility(View.GONE);
            rv.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            loadDownloaded();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                setAdapter();
                progressBar.setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadDownloaded() {
        try {
            ArrayList<ItemSong> tempArray = dbHelper.loadDataDownload();

            File fileroot = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.app_name) + File.separator + "temp");
            File[] files = fileroot.listFiles();
            if (files != null) {
                for (File file : files) {
                    for (int j = 0; j < tempArray.size(); j++) {
                        if (new File(file.getAbsolutePath()).getName().contains(tempArray.get(j).getTempName())) {
                            ItemSong itemSong = tempArray.get(j);
                            itemSong.setUrl(file.getAbsolutePath());
                            arrayList.add(itemSong);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setAdapter() {
        adapter = new AdapterOFSongList(DownloadActivity.this, arrayList, new ClickListenerPlayList() {
            @Override
            public void onClick(int position) {
                Constant.isOnline = false;
                if (!Constant.addedFrom.equals(addedFrom)) {
                    Constant.arrayList_play.clear();
                    Constant.arrayList_play.addAll(arrayList);
                    Constant.addedFrom = addedFrom;
                    Constant.isNewAdded = true;
                }
                Constant.playPos = position;

                methods.showInterAd(position, "");
            }

            @Override
            public void onItemZero() {

            }
        }, "downloads");
        rv.setAdapter(adapter);
        setEmpty();
    }

    public void setEmpty() {
        if (arrayList.size() > 0) {
            rv.setVisibility(View.VISIBLE);
            frameLayout.setVisibility(View.GONE);
        } else {
            rv.setVisibility(View.GONE);
            frameLayout.setVisibility(View.VISIBLE);

            frameLayout.removeAllViews();
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

            View myView = inflater.inflate(R.layout.layout_err_nodata, null);

            TextView textView = myView.findViewById(R.id.tv_empty_msg);
            textView.setText(errr_msg);
            myView.findViewById(R.id.btn_empty_try).setVisibility(View.GONE);

            myView.findViewById(R.id.btn_empty_downloads).setVisibility(View.GONE);

            myView.findViewById(R.id.btn_empty_music_lib).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent_music_lib = new Intent(DownloadActivity.this, OfflineMusicActivity.class);
                    startActivity(intent_music_lib);
                }
            });


            frameLayout.addView(myView);
        }
    }

    public Boolean checkPer() {

        if ((ContextCompat.checkSelfPermission(DownloadActivity.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED)) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        boolean canUseExternalStorage = false;

        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    canUseExternalStorage = true;
                    new LoadDownloadSongs().execute();
                }

                if (!canUseExternalStorage) {
                    Toast.makeText(DownloadActivity.this, getResources().getString(R.string.err_cannot_use_features), Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

//
//    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
//    public void onEquilizerChange(ItemAlbums itemAlbums) {
//        if(adapter != null) {
//            adapter.notifyDataSetChanged();
//        }
//        GlobalBus.getBus().removeStickyEvent(itemAlbums);
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//        GlobalBus.getBus().register(this);
//    }
//
//    @Override
//    public void onStop() {
//        GlobalBus.getBus().unregister(this);
//        super.onStop();
//    }
}