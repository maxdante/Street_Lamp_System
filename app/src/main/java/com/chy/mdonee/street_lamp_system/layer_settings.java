package com.chy.mdonee.street_lamp_system;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class layer_settings extends AppCompatActivity {
    public ProfileReader settingReader;
    public EditText mDynamicLayer;
    public EditText mFeatureLayer;
    public static String mDLayer = "DynamicLayer";
    public static String mFLayer = "FeatureLayer";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layer_settings);
        settingReader = new ProfileReader(this);
        mDynamicLayer = (EditText)findViewById(R.id.dynamicLayer);
        mFeatureLayer = (EditText)findViewById(R.id.featurelyer);
        mDynamicLayer.setText(settingReader.getValues(mDLayer));
        mFeatureLayer.setText(settingReader.getValues(mFLayer));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_layer_settings, menu);
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



    public void onSave(View v){
        Save();
        Intent intent = new Intent();
        //intent.putExtra("name","LeiPei");
        /* 指定intent要启动的类 */
        intent.setClass(this,AttributeEditorActivity.class);
        /* 启动一个新的Activity */
        layer_settings.this.startActivity(intent);
        /* 关闭当前的Activity */
        layer_settings.this.finish();
    }

    public void onReturn(View v){
        Intent intent = new Intent();
        //intent.putExtra("name","LeiPei");
        /* 指定intent要启动的类 */
        intent.setClass(this,AttributeEditorActivity.class);
        /* 启动一个新的Activity */
        layer_settings.this.startActivity(intent);
        /* 关闭当前的Activity */
        layer_settings.this.finish();
    }
    public void Save(){
        settingReader.save(mDLayer,mDynamicLayer.getText().toString());
        settingReader.save(mFLayer,mFeatureLayer.getText().toString());
    }
}
