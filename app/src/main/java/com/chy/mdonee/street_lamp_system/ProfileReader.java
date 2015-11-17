package com.chy.mdonee.street_lamp_system;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chy on 2015/9/21.
 */
public class ProfileReader {
    private Context context;
    private static String xmlName ="layer_setting";
    public ProfileReader(Context context) {
        super();
        this.context = context;
    }

    public ProfileReader(Context context,String file_name){
        super();
        this.context = context;
        xmlName = file_name;
    }
    /**
     * 保存参数
     */
    public void save(String key, String value) {
        //第一个参数 指定名称 不需要写后缀名 第二个参数文件的操作模式
        SharedPreferences preferences = context.getSharedPreferences(xmlName, Context.MODE_PRIVATE);
        //取到编辑器
        SharedPreferences.Editor editor=preferences.edit();
        editor.putString(key, value);
        //把数据提交给文件中
        editor.apply();
    }
    /**
     * 获取各项配置参数
     * @return
     */
    public Map<String,?> getPreferences(){
        SharedPreferences preferences=context.getSharedPreferences(xmlName, Context.MODE_PRIVATE);
        return  preferences.getAll();
    }

    public void setXmlName(String name){
        xmlName = name;
    }

    public String getValues(String key){
        SharedPreferences preferences=context.getSharedPreferences(xmlName, Context.MODE_PRIVATE);
        return preferences.getString(key,"null");
    }
}
