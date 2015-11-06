/* Copyright 2015 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.chy.mdonee.street_lamp_system;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer.MODE;
import com.esri.android.map.event.OnSingleTapListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.chy.mdonee.street_lamp_system.FeatureLayerUtils.FieldType;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Point;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.FeatureEditResult;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.SpatialRelationship;
import com.esri.core.tasks.ags.query.Query;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main activity class for the Attribute Editor Sample
 */
public class AttributeEditorActivity extends Activity {

  MapView mapView;

  ArcGISFeatureLayer featureLayer;
  ArcGISDynamicMapServiceLayer dmsl;

  Point pointClicked;

  LayoutInflater inflator;

  AttributeListAdapter listAdapter;
  
  Envelope initextent;

  ListView listView;

  View listLayout;

  public static TextView mTextView ;
  public static String mLampState = "this is null";
  //定时器
  public Timer mExcuteTimer;
  public Handler mHandler;
  public static final int DONE = 1;

  public static final String LAMP_STATE_ATTRIBUTE = "CS";
  public static final String LAMP_ON_VALUE = "1";

  public static final String TAG = "AttributeEditorSample";

  static final int ATTRIBUTE_EDITOR_DIALOG_ID = 1;

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);

    dmsl = new ArcGISDynamicMapServiceLayer("http://192.168.20.100:6080/arcgis/rest/services/FeatureAccess/Test_FeatureAccess//MapServer");

	featureLayer = new ArcGISFeatureLayer(
			"http://192.168.20.100:6080/arcgis/rest/services/FeatureAccess/Test_FeatureAccess/FeatureServer/0",
			MODE.ONDEMAND);

	setContentView(R.layout.main);
    mapView = (MapView)this.findViewById(R.id.map);
    mapView.addLayer(dmsl);
    mapView.addLayer(featureLayer);
    SimpleFillSymbol sfs = new SimpleFillSymbol(Color.TRANSPARENT);
    sfs.setOutline(new SimpleLineSymbol(Color.YELLOW, 3));
    featureLayer.setSelectionSymbol(sfs);
    mTextView = (TextView)findViewById(R.id.textView);
    // set up local variables
    inflator = LayoutInflater.from(getApplicationContext());
    listLayout = inflator.inflate(R.layout.list_layout, null);
    listView = (ListView) listLayout.findViewById(R.id.list_view);

    // Create a new AttributeListAdapter when the feature layer is initialized
    if (featureLayer.isInitialized()) {

      listAdapter = new AttributeListAdapter(this, featureLayer.getFields(), featureLayer.getTypes(),
          featureLayer.getTypeIdField());

    } else {

      featureLayer.setOnStatusChangedListener(new OnStatusChangedListener() {

        private static final long serialVersionUID = 1L;

        public void onStatusChanged(Object source, STATUS status) {

          if (status == STATUS.INITIALIZED) {
            listAdapter = new AttributeListAdapter(AttributeEditorActivity.this, featureLayer.getFields(), featureLayer
                .getTypes(), featureLayer.getTypeIdField());
          }
        }
      });
    }

    // Set tap listener for MapView
    mapView.setOnSingleTapListener(new OnSingleTapListener() {

      private static final long serialVersionUID = 1L;

      public void onSingleTap(float x, float y) {

        // convert event into screen click
        pointClicked = mapView.toMapPoint(x, y);

        // build a query to select the clicked feature
        Query query = new Query();
        query.setOutFields(new String[] { "*" });
        query.setSpatialRelationship(SpatialRelationship.INTERSECTS);
        query.setGeometry(pointClicked);
        query.setInSpatialReference(mapView.getSpatialReference());

        // call the select features method and implement the callbacklistener
        featureLayer.selectFeatures(query, ArcGISFeatureLayer.SELECTION_METHOD.NEW, new CallbackListener<FeatureSet>() {

          // handle any errors
          public void onError(Throwable e) {

            Log.d(TAG, "Select Features Error" + e.getLocalizedMessage());

          }

          public void onCallback(FeatureSet queryResults) {

            if (queryResults.getGraphics().length > 0) {

              Log.d(
                  TAG,
                  "Feature found id="
                      + queryResults.getGraphics()[0].getAttributeValue(featureLayer.getObjectIdField()));

              // set new data and notify adapter that data has changed
              listAdapter.setFeatureSet(queryResults);
              listAdapter.notifyDataSetChanged();

              // This callback is not run in the main UI thread. All GUI
              // related events must run in the UI thread,
              // therefore use the Activity.runOnUiThread() method. See
              // http://developer.android.com/reference/android/app/Activity.html#runOnUiThread(java.lang.Runnable)
              // for more information.
              AttributeEditorActivity.this.runOnUiThread(new Runnable() {

                public void run() {

                  // show the editor dialog.
                  showDialog(ATTRIBUTE_EDITOR_DIALOG_ID);

                }
              });
            }
          }
        });
      }
    });

    // TODO handle rotation
    mExcuteTimer = new Timer();
    //mExcuteTimer.schedule(new timerListener(),1000,2000);
    mHandler = new Handler() {
      @Override
      public void handleMessage(Message msg) {

        String str = LampStateExcute();
        mTextView.setText(str);
        Log.d(TAG, "--------------finish--------------");
      }
    };

  }

  /**
   * Overidden method from Activity class - this is the recommended way of creating dialogs
   */
  @Override
  protected Dialog onCreateDialog(int id) {

    switch (id) {

      case ATTRIBUTE_EDITOR_DIALOG_ID:

        // create the attributes dialog
        Dialog dialog = new Dialog(this);
        listView.setAdapter(listAdapter);
        dialog.setContentView(listLayout);
        dialog.setTitle("Edit Attributes");

        // set button on click listeners, setting as XML attributes doesn't work
        // due to a scope/thread issue
        Button btnEditCancel = (Button) listLayout.findViewById(R.id.btn_edit_discard);
        btnEditCancel.setOnClickListener(returnOnClickDiscardChangesListener());

        Button btnEditApply = (Button) listLayout.findViewById(R.id.btn_edit_apply);
        btnEditApply.setOnClickListener(returnOnClickApplyChangesListener());

        return dialog;
    }
    return null;
  }

  /**
   * Helper method to return an OnClickListener for the Apply button
   */
  public OnClickListener returnOnClickApplyChangesListener() {

    return new OnClickListener() {

      public void onClick(View v) {

        boolean isTypeField = false;
        boolean hasEdits = false;
        boolean updateMapLayer = false;
        Map<String, Object> attrs = new HashMap<String, Object>();

        // loop through each attribute and set the new values if they have
        // changed
        for (int i = 0; i < listAdapter.getCount(); i++) {

          AttributeItem item = (AttributeItem) listAdapter.getItem(i);
          String value = "";

          // check to see if the View has been set
          if (item.getView() != null) {

            // determine field type and therefore View type
            if (item.getField().getName().equals(featureLayer.getTypeIdField())) {
              // drop down spinner

              Spinner spinner = (Spinner) item.getView();
              // get value for the type
              String typeName = spinner.getSelectedItem().toString();
              value = FeatureLayerUtils.returnTypeIdFromTypeName(featureLayer.getTypes(), typeName);

              // update map layer as for this featurelayer the type change will
              // change the features symbol.
              isTypeField = true;

            } else if (FieldType.determineFieldType(item.getField()) == FieldType.DATE) {
              // date

              Button dateButton = (Button) item.getView();
              value = dateButton.getText().toString();

            } else {
              // edit text

              EditText editText = (EditText) item.getView();
              value = editText.getText().toString();

            }

            // try to set the attribute value on the graphic and see if it has
            // been changed
            boolean hasChanged = FeatureLayerUtils.setAttribute(attrs, listAdapter.featureSet.getGraphics()[0],
                item.getField(), value, listAdapter.formatter);

            // if a value has for this field, log this and set the hasEdits
            // boolean to true
            if (hasChanged) {

              Log.d(TAG, "Change found for field=" + item.getField().getName() + " value = " + value
                  + " applyEdits() will be called");
              hasEdits = true;

              // If the change was from a Type field then set the dynamic map
              // service to update when the edits have been applied, as the
              // renderer of the feature will likely change
              if (isTypeField) {

                updateMapLayer = true;

              }
            }

            // check if this was a type field, if so set boolean back to false
            // for next field
            if (isTypeField) {

              isTypeField = false;
            }
          }
        }

        // check there have been some edits before applying the changes
        if (hasEdits) {

          // set objectID field value from graphic held in the featureset
        	attrs.put(featureLayer.getObjectIdField(),listAdapter.featureSet.getGraphics()[0].getAttributeValue(featureLayer.getObjectIdField()));
			Graphic newGraphic = new Graphic(null, null, attrs);
            featureLayer.applyEdits(null, null, new Graphic[] { newGraphic }, createEditCallbackListener(updateMapLayer));
        }

        // close the dialog
        Message msg = new Message();
        mHandler.sendMessage(msg);
        dismissDialog(ATTRIBUTE_EDITOR_DIALOG_ID);

      }
    };

  }

  /**
   * OnClick method for the Discard button
   */
  public OnClickListener returnOnClickDiscardChangesListener() {

    return new OnClickListener() {

      public void onClick(View v) {

        // close the dialog
        dismissDialog(ATTRIBUTE_EDITOR_DIALOG_ID);

      }
    };

  }

  /**
   * Helper method to create a CallbackListener<FeatureEditResult[][]>
   * 
   * @return CallbackListener<FeatureEditResult[][]>
   */
  CallbackListener<FeatureEditResult[][]> createEditCallbackListener(final boolean updateLayer) {

    return new CallbackListener<FeatureEditResult[][]>() {

      public void onCallback(FeatureEditResult[][] result) {

        // check the response for success or failure
        if (result[2] != null && result[2][0] != null && result[2][0].isSuccess()) {

          Log.d(AttributeEditorActivity.TAG, "Success updating feature with id=" + result[2][0].getObjectId());

          // see if we want to update the dynamic layer to get new symbols for
          // updated features

          if (updateLayer) {

          dmsl.refresh();

          }
        }
      }

      public void onError(Throwable e) {

        Log.d(AttributeEditorActivity.TAG, "error updating feature: " + e.getLocalizedMessage());

      }
    };
  }
  public void onClick (View v){
    ProgressDialog dialog = new ProgressDialog(this);
    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
    dialog.setCancelable(true);// 设置是否可以通过点击Back键取消
    dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
    //dialog.setIcon(R.drawable.ic_launcher);//
    // 设置提示的title的图标，默认是没有的，如果没有设置title的话只设置Icon是不会显示图标的
    dialog.setTitle("提示");
    // dismiss监听
    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

      @Override
      public void onDismiss(DialogInterface dialog) {
        // TODO Auto-generated method stub

      }
    });
    // 监听cancel事件
    dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

      @Override
      public void onCancel(DialogInterface dialog) {
        // TODO Auto-generated method stub
        Message msg = new Message();
        mHandler.sendMessage(msg);

      }
    });
    dialog.setMessage("这是一个圆形进度条");
    dialog.show();

  }

  class timerListener extends TimerTask {
    @Override
    public void run(){

      Message msg = new Message();
      mHandler.sendMessage(msg);//用activity中的handler发送消息
      }

    }

  public String LampStateExcute(){
    Query query = new Query();
    query.setOutFields(new String[] { "*" });
    query.setSpatialRelationship(SpatialRelationship.INTERSECTS);
    query.setWhere("OBJECTID  > 0");
    query.setInSpatialReference(mapView.getSpatialReference());

    // call the select features method and implement the callbacklistener
    featureLayer.queryFeatures(query, new CallbackListener<FeatureSet>() {

      // handle any errors
      public void onError(Throwable e) {

        Log.d(TAG, "Select Features Error" + e.getLocalizedMessage());

      }

      public void onCallback(FeatureSet queryResults) {

        ProgressDialog dialog = new ProgressDialog(getApplicationContext());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);// 设置进度条的形式为圆形转动的进度条
        dialog.setCancelable(true);// 设置是否可以通过点击Back键取消
        dialog.setCanceledOnTouchOutside(false);// 设置在点击Dialog外是否取消Dialog进度条
        //dialog.setIcon(R.drawable.ic_launcher);//
        // 设置提示的title的图标，默认是没有的，如果没有设置title的话只设置Icon是不会显示图标的
        dialog.setTitle("提示");
        // dismiss监听
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

          @Override
          public void onDismiss(DialogInterface dialog) {
            // TODO Auto-generated method stub

          }
        });
        // 监听cancel事件
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

          @Override
          public void onCancel(DialogInterface dialog) {
            // TODO Auto-generated method stub
            Message msg = new Message();
            mHandler.sendMessage(msg);

          }
        });
        dialog.setMessage("这是一个圆形进度条");
        dialog.show();

        if (queryResults.getGraphics().length > 0) {

          //Log.d(TAG,"--------------Feature number is "+ queryResults.getGraphics().length+"--------------" );
          int onlight = 0;
          for (int i = 0; i < queryResults.getGraphics().length; i++) {
            Graphic gra = queryResults.getGraphics()[i];
            if ((gra.getAttributeValue(LAMP_STATE_ATTRIBUTE) != null) && (gra.getAttributeValue(LAMP_STATE_ATTRIBUTE).toString().equals(LAMP_ON_VALUE))) {
              onlight = onlight + 1;
            }

          }
          Log.d(TAG, "--------------Feature  onlight is" + onlight + "--------------");
          double rate = (onlight + 0.0) / queryResults.getGraphics().length * 100;
          mLampState = "灯亮率为" + new java.text.DecimalFormat("#.00").format(rate);

        }
        dialog.dismiss();
      }
    });
    return mLampState;
  }


}