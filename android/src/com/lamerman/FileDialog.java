package com.lamerman;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.geekalarm.android.R;

public class FileDialog extends ListActivity {

    private static final String ITEM_KEY = "key";
    private static final String ITEM_IMAGE = "image";

    public static final String START_URI = "START_URI";
    public static final String RESULT_URI = "RESULT_URI";

    private List<String> item = null;
    private List<String> path = null;
    private String root = "/";
    private TextView myPath;
    private ArrayList<HashMap<String, Object>> mList;

    private Button selectButton;
    private Button cancelButton;

    private LinearLayout layoutSelect;
    private InputMethodManager inputManager;
    private String parentPath;
    private String currentPath = root;

    
    private MediaPlayer player;
    private File selectedFile;
    private HashMap<String, Integer> lastPositions = new HashMap<String, Integer>();
    private Stack<String> history;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_CANCELED, getIntent());

        setContentView(R.layout.file_dialog_main);
        myPath = (TextView) findViewById(R.id.path);

        inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        selectButton = (Button) findViewById(R.id.fdButtonSelect);
        selectButton.setEnabled(false);
        selectButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (selectedFile != null) {
                    getIntent().putExtra(RESULT_URI, 
                            selectedFile.toURI().toString());
                    setResult(RESULT_OK, getIntent());
                    finish();
                }
            }
        });

        history = new Stack<String>();
        layoutSelect = (LinearLayout) findViewById(R.id.fdLinearLayoutSelect);

        cancelButton = (Button) findViewById(R.id.fdButtonCancel);
        cancelButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED, getIntent());
                finish();
            }

        });
        player = new MediaPlayer();
        String startUri = getIntent().getStringExtra(START_URI);
        if (startUri != null) {
            String path = Uri.parse(startUri).getPath();
            File file = new File(path);
            if (!file.exists()) {
                getDir(root);
            } else {
                getDir(file.getParent());
                selectFile(file);
            }
        } else {
            getDir(root);
        }
    }

    private void getDir(String dirPath) {

        boolean useAutoSelection = dirPath.length() < currentPath.length();

        Integer position = lastPositions.get(parentPath);

        history.push(dirPath);
        getDirImpl(dirPath);

        if (position != null && useAutoSelection) {
            getListView().setSelection(position);
        }

    }

    private void getDirImpl(String dirPath) {

        myPath.setText(getText(R.string.location) + ": " + dirPath);
        currentPath = dirPath;

        item = new ArrayList<String>();
        path = new ArrayList<String>();
        mList = new ArrayList<HashMap<String, Object>>();

        File f = new File(dirPath);
        File[] files = f.listFiles();

        if (!dirPath.equals(root)) {

            item.add(root);
            addItem(root, R.drawable.folder);
            path.add(root);

            item.add("../");
            addItem("../", R.drawable.folder);
            path.add(f.getParent());
            parentPath = f.getParent();

        }

        TreeMap<String, String> dirsMap = new TreeMap<String, String>();
        TreeMap<String, String> dirsPathMap = new TreeMap<String, String>();
        TreeMap<String, String> filesMap = new TreeMap<String, String>();
        TreeMap<String, String> filesPathMap = new TreeMap<String, String>();
        for (File file : files) {
            if (file.isDirectory()) {
                String dirName = file.getName();
                dirsMap.put(dirName, dirName);
                dirsPathMap.put(dirName, file.getPath());
            } else {
                filesMap.put(file.getName(), file.getName());
                filesPathMap.put(file.getName(), file.getPath());
            }
        }
        item.addAll(dirsMap.tailMap("").values());
        item.addAll(filesMap.tailMap("").values());
        path.addAll(dirsPathMap.tailMap("").values());
        path.addAll(filesPathMap.tailMap("").values());

        SimpleAdapter fileList = new FileSimpleAdapter(this, mList,
                R.layout.file_dialog_row,
                new String[] { ITEM_KEY, ITEM_IMAGE }, new int[] {
                        R.id.fdrowtext, R.id.fdrowimage });

        for (String dir : dirsMap.tailMap("").values()) {
            addItem(dir, R.drawable.folder);
        }

        for (String file : filesMap.tailMap("").values()) {
            addItem(file, R.drawable.file);
        }

        fileList.notifyDataSetChanged();

        setListAdapter(fileList);

    }

    private void addItem(String fileName, int imageId) {
        HashMap<String, Object> item = new HashMap<String, Object>();
        item.put(ITEM_KEY, fileName);
        item.put(ITEM_IMAGE, imageId);
        mList.add(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        File file = new File(path.get(position));

        if (file.isDirectory()) {
            unselect();
            if (file.canRead()) {
                lastPositions.put(currentPath, position);
                getDir(path.get(position));
            } else {
                new AlertDialog.Builder(this)
                        .setIcon(R.drawable.icon)
                        .setTitle(
                                "[" + file.getName() + "] "
                                        + getText(R.string.cant_read_folder))
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) {

                                    }
                                }).show();
            }
        } else {
            selectFile(file);
        }
    }
    
    private void selectFile(File file) {
        player.reset();
        String fileName = file.getName();
        int position = 0;
        for (Map<String, Object> curFile : mList) {
            if (curFile.get(ITEM_KEY).equals(fileName)) {
                break;
            }
            position++;
        }
        getListView().smoothScrollToPosition(position);
        try {
            Uri uri = Uri.parse(file.toURI().toString());
            player.setDataSource(FileDialog.this, uri);
            player.prepare();
            player.start();
            selectedFile = file;
            selectButton.setEnabled(true);
            getListView().invalidateViews();
        } catch (Exception e) {
            Toast.makeText(FileDialog.this, 
                           R.string.bad_file, 
                           Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        if (player.isPlaying()) {
            player.stop();
        }
        super.onDestroy();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            unselect();
            history.pop();
            if (!history.empty()) {
                getDir(history.pop());
            } else {
                return super.onKeyDown(keyCode, event);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void unselect() {
        selectButton.setEnabled(false);
        if (player.isPlaying()) {
            player.stop();
        }
    }
    
    private class FileSimpleAdapter extends SimpleAdapter {

        public FileSimpleAdapter(Context context,
                List<? extends Map<String, ?>> data, int resource,
                String[] from, int[] to) {
            super(context, data, resource, from, to);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            Map<String, Object> item = (Map)getItem(position);
            boolean selected = selectedFile != null && 
                selectedFile.getName().equals(item.get(ITEM_KEY));
            v.setBackgroundColor(!selected ? Color.BLACK : Color.BLUE);
            return v;
        }
        
    }
}