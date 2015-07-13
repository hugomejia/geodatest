package com.arrg.android.app.geoda;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

    public ArrayList<ItemRow> files;
    public ListView lvFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        lvFiles = (ListView) findViewById(R.id.lvFiles);

        ItemRowAdapter itemRowAdapter = new ItemRowAdapter(files(), this);
        lvFiles.setAdapter(itemRowAdapter);

        lvFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*File file = new File(files.get(position).getPathOfFile());

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(file));
                intent.setType("application/json");*/

                Intent intent = new Intent(SettingsActivity.this, EditorTextActivity.class);
                intent.putExtra("name", files.get(position).getNameOfFile());
                intent.putExtra("path", files.get(position).getPathOfFile());
                try {
                    startActivity(intent);
                    overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
                } catch (Exception e) {
                    Toast.makeText(SettingsActivity.this, getString(R.string.unable_to_open_file), Toast.LENGTH_SHORT).show();
                }
                ;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
        finish();
    }

    private ArrayList<ItemRow> files() {
        files = new ArrayList<>();
        //files.add(new ItemRow(getString(R.string.header_row)));

        File[] jsonFiles = new File(Constants.APP_DATA_SDCARD).listFiles();

        for (File f : jsonFiles) {
            if (f.isFile() && f.getPath().endsWith(".json")) {
                files.add(new ItemRow(f.getName(), f.getAbsolutePath()));
            }
        }

        return files;
    }
}
