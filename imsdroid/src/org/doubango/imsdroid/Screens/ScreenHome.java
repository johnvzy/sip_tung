/* Copyright (C) 2010-2011, Mamadou Diop.
*  Copyright (C) 2011, Doubango Telecom.
*
* Contact: Mamadou Diop <diopmamadou(at)doubango(dot)org>
*	
* This file is part of imsdroid Project (http://code.google.com/p/imsdroid)
*
* imsdroid is free software: you can redistribute it and/or modify it under the terms of 
* the GNU General Public License as published by the Free Software Foundation, either version 3 
* of the License, or (at your option) any later version.
*	
* imsdroid is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
* See the GNU General Public License for more details.
*	
* You should have received a copy of the GNU General Public License along 
* with this program; if not, write to the Free Software Foundation, Inc., 
* 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package org.doubango.imsdroid.Screens;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.doubango.imsdroid.Engine;
import org.doubango.imsdroid.Main;
import org.doubango.imsdroid.R;
import org.doubango.ngn.events.NgnEventArgs;
import org.doubango.ngn.events.NgnRegistrationEventArgs;
import org.doubango.ngn.media.NgnMediaType;
import org.doubango.ngn.services.INgnConfigurationService;
import org.doubango.ngn.services.INgnSipService;
import org.doubango.ngn.sip.NgnSipSession.ConnectionState;
import org.doubango.ngn.utils.NgnConfigurationEntry;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class ScreenHome extends BaseScreen {
    private static String TAG = ScreenHome.class.getCanonicalName();

    private static final int MENU_EXIT = 0;
    private static final int MENU_SETTINGS = 1;

    private GridView mGridView;

    private final INgnSipService mSipService;

    private BroadcastReceiver mSipBroadCastRecv;

    private boolean passSystem;
    protected static final int REFRESH_DATA = 0x00000001;
    private String NursePhoneCode="";//主護理師的號碼
    private String NurseName="";


    //動態改變SIP Server Config
    public String CONFIG_SIP_SERVER_RELAMIP="";
    public String CONFIG_SIP_SERVER_IP="122.117.67.226";
    public String CONFIG_SIP_SERVER_PORT="25060";
    public String CONFIG_SIP_SERVER_STUNIP="";
    public String CONFIG_SIP_SERVER_STUNPORT="";
    //最後註冊的BedId
    public static String LastRegBedId="";
    public Boolean IsGetSipConfig=false;


    public ScreenHome() {
        super(SCREEN_TYPE.HOME_T, TAG);

        mSipService = getEngine().getSipService();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mSipService.isRegistered() == false) {
            mSipService.register(ScreenHome.this);
        }else{
            if (Main.callNurse==true)
            {
                Main.callNurse=false;
                final Engine mEngine;
                final INgnConfigurationService mConfigurationService;
                mEngine = (Engine) Engine.getInstance();
                mConfigurationService = mEngine.getConfigurationService();
                String paraBedId=mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI,"");
                //showAlert("讀取護理師資訊",paraBedId);
                NursePhoneCode="";
                NurseName="";
                new LongOperation().execute(paraBedId);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_home);

        //
        if (mSipService.isRegistered() == false) {
            //mSipService.register(ScreenHome.this);
            new ConfigSipServer().execute("-1");

            //Query Dick Setting
            //Thread t = new Thread(new sendPostRunnable(""));
            //t.start();
            //newSettingCommit();
            String pBedId=readFromFile();
            newSettingCommit(pBedId);
        }

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                showDialog("綁定床號", "請輸入指令綁定SIP床號");
                return true;
            }
        });
        /*
        Button btnCallNurse1 = (Button) findViewById(R.id.BtnCallNurse);
        Button btnCallNurse2 = (Button) findViewById(R.id.BtnCallNurseBackup);
        Button btnCallBCService = (Button) findViewById(R.id.BtnCallBCService);

        btnCallNurse1.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSipService.isRegistered()) {
                    ScreenAV.makeCall("9991", NgnMediaType.AudioVideo);
                }
            }
        });

        btnCallNurse2.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSipService.isRegistered()) {
                    ScreenAV.makeCall("9992", NgnMediaType.AudioVideo);
                }
            }
        });

        btnCallBCService.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSipService.isRegistered()) {
                    ScreenAV.makeCall("888", NgnMediaType.AudioVideo);
                }
            }
        });


        //btnCallNurse1.setEnabled(false);
        //btnCallNurse2.setEnabled(false);
        //btnCallBCService.setEnabled(false);
        */


        passSystem=false;

        mGridView = (GridView) findViewById(R.id.screen_home_gridview);
        mGridView.setAdapter(new ScreenHomeAdapter(this));
        mGridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ScreenHomeItem item = (ScreenHomeItem) parent.getItemAtPosition(position);
                if (item != null) {
                    if (position == ScreenHomeItem.ITEM_SIGNIN_SIGNOUT_POS) {
                        if (mSipService.getRegistrationState() == ConnectionState.CONNECTING || mSipService.getRegistrationState() == ConnectionState.TERMINATING) {
                            mSipService.stopStack();
                        } else if (mSipService.isRegistered()) {
                            passSystem=false;
                            showDialog("登出帳號", "請輸入密碼來登出");
                            if (passSystem==true) {
                                mSipService.unRegister();
                            }
                        } else {
                            mSipService.register(ScreenHome.this);
                        }
                    } else if (position == ScreenHomeItem.ITEM_EXIT_POS) {
                        ((Main) (getEngine().getMainActivity())).exit();
                    } else if (position == ScreenHomeItem.ITEM_Call1_POS) {

                        /*
                        if (mSipService.isRegistered()) {
                            //ScreenAV.makeCall("301", NgnMediaType.Audio);
                            final Engine mEngine;
                            final INgnConfigurationService mConfigurationService;
                            mEngine = (Engine) Engine.getInstance();
                            mConfigurationService = mEngine.getConfigurationService();
                            String paraBedId=mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI,"");
                            //showAlert("讀取護理師資訊",paraBedId);
                            NursePhoneCode="";
                            NurseName="";
                            new LongOperation().execute(paraBedId);
                        }
                        */

                        //2016-07-28 改成點滴滴空
                        if (LastRegBedId.trim()!=""){
                            new ddOperation().execute(LastRegBedId);
                        }

                    } else if (position == ScreenHomeItem.ITEM_Call2_POS) {
                        if (mSipService.isRegistered()) {
                            ScreenAV.makeCall("302", NgnMediaType.Audio);
                        }
                    } else if (position == ScreenHomeItem.ITEM_CallLucky_POS) {
                        if (mSipService.isRegistered()) {
                            ScreenAV.makeCall("303", NgnMediaType.Audio);
                        }
                    } else if (position == ScreenHomeItem.ITEM_Call3_POS) {
                        if (mSipService.isRegistered()) {
                            ScreenAV.makeCall("888", NgnMediaType.Audio);
                        }
                    } else if (position == ScreenHomeItem.ITEM_SIGNIN_SETTING_POS) {
                        showDialog("綁定床號", "請輸入指令綁定SIP床號");
                    } else {
                        mScreenService.show(item.mClass, item.mClass.getCanonicalName());
                    }
                }
            }
        });

        mSipBroadCastRecv = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                // Registration Event
                if (NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT.equals(action)) {
                    NgnRegistrationEventArgs args = intent.getParcelableExtra(NgnEventArgs.EXTRA_EMBEDDED);
                    if (args == null) {
                        Log.e(TAG, "Invalid event args");
                        return;
                    }
                    switch (args.getEventType()) {
                        case REGISTRATION_NOK:
                        case UNREGISTRATION_OK:
                        case REGISTRATION_OK:
                        case REGISTRATION_INPROGRESS:
                        case UNREGISTRATION_INPROGRESS:
                        case UNREGISTRATION_NOK:
                        default:
                            ((ScreenHomeAdapter) mGridView.getAdapter()).refresh();
                            break;
                    }
                }
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NgnRegistrationEventArgs.ACTION_REGISTRATION_EVENT);
        registerReceiver(mSipBroadCastRecv, intentFilter);
    }


    Handler mHandler = new Handler()

    {

        @Override

        public void handleMessage(Message msg)

        {

            switch (msg.what)

            {

                // 顯示網路上抓取的資料

                case REFRESH_DATA:

                    String result = null;

                    if (msg.obj instanceof String)

                        result = (String) msg.obj;

                    if (result != null)

                        // 印出網路回傳的文字
                        Toast.makeText(ScreenHome.this
                                , result, Toast.LENGTH_LONG).show();
                    if (mSipService.isRegistered() == false) {
                    if (result.equals("")) {
                        showAlert("綁定床號失敗","無法存取Android Id");
                    }else {
                        showAlert("綁定床號成功","床號：" + result);
                        newSettingCommit(result);
                    }}
                    this.removeMessages(0);
                    //mHandler.removeMessages(0);
                    break;

            }

        }

    };

    public String readFromFile() {

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard,"bedlog.txt");

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                //text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }

        return text.toString();
    }

    private String sendPostDataToInternet(String strTxt)
    {

        String result="";
        String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        HttpClient client = new DefaultHttpClient();

        //android_id="19fd402896c86565";
        HttpGet get = new HttpGet("http://192.168.254.5/api/profile/" + android_id);
        HttpResponse response = null;
        try {
            response = client.execute(get);
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpEntity resEntity = response.getEntity();

        try {
            result = EntityUtils.toString(resEntity);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            JSONObject json = new JSONObject(result);
            result=json.getString("bedName");
            result=result.replace("-","");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    class sendPostRunnable implements Runnable
    {

        String strTxt = null;
        // 建構子，設定要傳的字串

        public sendPostRunnable(String strTxt)

        {

            this.strTxt = strTxt;

        }



        @Override

        public void run()

        {

            String result = sendPostDataToInternet(strTxt);

            mHandler.obtainMessage(REFRESH_DATA, result).sendToTarget();

        }

    }


    @Override
    protected void onDestroy() {
        if (mSipBroadCastRecv != null) {
            unregisterReceiver(mSipBroadCastRecv);
            mSipBroadCastRecv = null;
        }

        /*
        //2016新增
        if (mSipService.isRegistered() == true) {
            mSipService.stopStack();
            mSipService.unRegister();
        }
*/
        super.onDestroy();
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public boolean createOptionsMenu(Menu menu) {
        //menu.add(0, ScreenHome.MENU_SETTINGS, 0, "Settings");
        /*MenuItem itemExit =*/
        //menu.add(0, ScreenHome.MENU_EXIT, 0, "Exit");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		/*
		switch(item.getItemId()){
			case ScreenHome.MENU_EXIT:
				((Main)getEngine().getMainActivity()).exit();
				break;
			case ScreenHome.MENU_SETTINGS:
				mScreenService.show(ScreenSettings.class);
				break;
		}*/
        //showDialog("綁定床號","請輸入指令綁定SIP床號");

        return true;
    }

    private void showAlert(String pTitle, String pMessage) {
        AlertDialog.Builder MyAlertDialog = new AlertDialog.Builder(this);
        MyAlertDialog.setTitle(pTitle);
        MyAlertDialog.setMessage(pMessage);
        MyAlertDialog.show();
    }


    private void showDialog(String pTitle, String pMessage) {
        final EditText input = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle(pTitle)
                .setMessage(pMessage)
                .setView(input)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // 在此處理 input
                        String strResult = input.getText().toString();
                        if (strResult.trim().length() > 0) {
                            String[] cmdArray = strResult.split(":");
                            if (cmdArray.length > 1) {
                                if (cmdArray[0].toString().toLowerCase().equals("bind")) {
                                    if (cmdArray[1].toString().trim().equals("")) {
                                        showAlert("錯誤", "參數有誤或為空值");
                                    } else {
                                        newSettingCommit(cmdArray[1].toString());
                                    }
                                } else if (cmdArray[0].toString().toLowerCase().equals("logout") &&
                                           cmdArray[1].toString().toLowerCase().equals("bcadmin")) {
                                    passSystem = true;
                                    mSipService.unRegister();
                                } else if (cmdArray[0].toString().toLowerCase().equals("setting") &&
                                           cmdArray[1].toString().toLowerCase().equals("bcadmin")) {
                                    mScreenService.show(ScreenIdentity.class, ScreenIdentity.class.getCanonicalName());
                                } else if (cmdArray[0].toString().toLowerCase().equals("network") &&
                                        cmdArray[1].toString().toLowerCase().equals("bcadmin")) {
                                    mScreenService.show(ScreenNetwork.class, ScreenNetwork.class.getCanonicalName());
                                }else if (cmdArray[0].toString().toLowerCase().equals("sipserver") ) {
                                        String tmpPara=cmdArray[1].toString();
                                        new ConfigSipServer().execute(tmpPara);
                                }else {
                                    showAlert("錯誤", "輸入的指令錯誤");
                                }
                            } else {
                                showAlert("錯誤", "輸入錯誤或過多的:");
                            }
                        } else {
                            showAlert("錯誤", "輸入的指令錯誤或空白");
                        }
                    }
                })
                .show();
    }


    private void newSettingCommit(String newBedId) {
        final Engine mEngine;
        final INgnConfigurationService mConfigurationService;
        mEngine = (Engine) Engine.getInstance();
        mConfigurationService = mEngine.getConfigurationService();

        mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
                newBedId);


        if (IsGetSipConfig==true)
        {
            mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU,
                    "sip:" + newBedId + "@" + CONFIG_SIP_SERVER_IP);
            mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI,
                    newBedId);
            mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD,
                    "bcadmin");
            mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM,
                    "sip:" + CONFIG_SIP_SERVER_RELAMIP);

            mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST,
                    CONFIG_SIP_SERVER_IP);

            Log.v("新 SIP SERVER:",CONFIG_SIP_SERVER_IP);

        }else {

            //Default Setting
            mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU,
                    "sip:" + newBedId + "@140.115.191.37");
            mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI,
                    newBedId);
            mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD,
                    "bcadmin");
            mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM,
                    "sip:140.115.191.37");

            mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST,
                    "140.115.191.37");

            Log.v("預設的中央設定", "140.115.191.37");

        }



        mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_EARLY_IMS,
                true);

        mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_WIFI,
                true);
        mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_3G,
                true);
        // Compute
        if(!mConfigurationService.commit()){
            //Log.e(TAG, "Failed to Commit() configuration");
        }

        //mConfigurationService.commit();


        //showAlert("設定成功", "綁定床號 [ " + newBedId + "] 成功");

        LastRegBedId=newBedId;

        if (mSipService.isRegistered() == true) {
            mSipService.stopStack();
            mSipService.unRegister();
            mSipService.register(ScreenHome.this);
        }else{
            mSipService.register(ScreenHome.this);
        }
    }



/*
    private void newSettingCommit(String newBedId) {
        final Engine mEngine;
        final INgnConfigurationService mConfigurationService;
        mEngine = (Engine) Engine.getInstance();
        mConfigurationService = mEngine.getConfigurationService();

        mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_DISPLAY_NAME,
                newBedId);
        mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPU,
                "sip:" + newBedId + "@122.117.67.226:5788");
        mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_IMPI,
                newBedId);
        mConfigurationService.putString(NgnConfigurationEntry.IDENTITY_PASSWORD,
                newBedId);
        mConfigurationService.putString(NgnConfigurationEntry.NETWORK_REALM,
                "sip:122.117.67.226:5788");
        mConfigurationService.putString(NgnConfigurationEntry.NETWORK_PCSCF_HOST,
                "122.117.67.226");
        mConfigurationService.putInt(NgnConfigurationEntry.NETWORK_PCSCF_PORT,
                5788);
        mConfigurationService.putBoolean(NgnConfigurationEntry.NETWORK_USE_EARLY_IMS,
                true);
        // Compute
        if (!mConfigurationService.commit()) {
            Log.e(TAG, "Failed to Commit() configuration");
        }
        mConfigurationService.commit();
        showAlert("設定成功", "綁定床號 [ " + newBedId + "] 成功");

        if (mSipService.isRegistered() == true) {
            mSipService.stopStack();
            mSipService.unRegister();
            mSipService.register(ScreenHome.this);
        }
    }
*/

    /**
     * ScreenHomeItem
     */
    static class ScreenHomeItem {
        static final int ITEM_SIGNIN_SIGNOUT_POS = -2;
        static final int ITEM_SIGNIN_SETTING_POS = -1;
        static final int ITEM_Call1_POS = 0;
        static final int ITEM_Call2_POS = 1;
        static final int ITEM_CallLucky_POS = 4;
        static final int ITEM_Call3_POS = 3;

        static final int ITEM_EXIT_POS = 999;//BC-Custom
        final int mIconResId;
        final String mText;
        final Class<? extends Activity> mClass;

        private ScreenHomeItem(int iconResId, String text, Class<? extends Activity> _class) {
            mIconResId = iconResId;
            mText = text;
            mClass = _class;
        }
    }

    /**
     * ScreenHomeAdapter
     */
    static class ScreenHomeAdapter extends BaseAdapter {
        static final int ALWAYS_VISIBLE_ITEMS_COUNT = 6;//BC-Custom
        static final ScreenHomeItem[] sItems = new ScreenHomeItem[]{
                /*
                // always visible
                new ScreenHomeItem(R.drawable.sign_in_48, "登入SIP Server", null),
                //new ScreenHomeItem(R.drawable.exit_48, "離開", null),
                new ScreenHomeItem(R.drawable.options_48, "設定", null),
                //new ScreenHomeItem(R.drawable.about_48, "About", ScreenAbout.class),
                // visible only if connected
                new ScreenHomeItem(R.drawable.dialer_48, "撥號2", ScreenTabDialer.class),
                //new ScreenHomeItem(R.drawable.eab2_48, "Address Book", ScreenTabContacts.class),
                new ScreenHomeItem(R.drawable.history_48, "歷史記錄", ScreenTabHistory.class),
                //new ScreenHomeItem(R.drawable.history_48, "歷史記錄", ScreenSettings.class),
                //new ScreenHomeItem(R.drawable.chat_48, "Messages", ScreenTabMessages.class),
               */
                /*
                new ScreenHomeItem(R.drawable.sign_in_48, "", null),
                new ScreenHomeItem(R.drawable.options_48, "", null),
                new ScreenHomeItem(R.drawable.dialer_48, "", ScreenTabDialer.class),
                new ScreenHomeItem(R.drawable.history_48, "", ScreenTabHistory.class),

                new ScreenHomeItem(R.drawable.call1, "", null),
                new ScreenHomeItem(R.drawable.call2, "", null),
                new ScreenHomeItem(R.drawable.calllucky, "", null),//303
                new ScreenHomeItem(R.drawable.call3, "", null),
                */
                //new ScreenHomeItem(R.drawable.sign_in_48, "", null),
                //new ScreenHomeItem(R.drawable.options_48, "", null),

                new ScreenHomeItem(R.drawable.call1, "", null),
                new ScreenHomeItem(R.drawable.call2, "", null),
                new ScreenHomeItem(R.drawable.dialer_48, "", ScreenTabDialer.class),
                new ScreenHomeItem(R.drawable.call3, "", null),
                new ScreenHomeItem(R.drawable.calllucky, "", null),//303
                new ScreenHomeItem(R.drawable.history_48, "", ScreenTabHistory.class),


        };

        private final LayoutInflater mInflater;
        private final ScreenHome mBaseScreen;

        ScreenHomeAdapter(ScreenHome baseScreen) {
            mInflater = LayoutInflater.from(baseScreen);
            mBaseScreen = baseScreen;
        }

        void refresh() {
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mBaseScreen.mSipService.isRegistered() ? sItems.length : ALWAYS_VISIBLE_ITEMS_COUNT;
        }

        @Override
        public Object getItem(int position) {
            return sItems[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            final ScreenHomeItem item = (ScreenHomeItem) getItem(position);

            if (item == null) {
                return null;
            }

            if (view == null) {
                view = mInflater.inflate(R.layout.screen_home_item, null);
            }

            if (position == ScreenHomeItem.ITEM_SIGNIN_SIGNOUT_POS) {
                if (mBaseScreen.mSipService.getRegistrationState() == ConnectionState.CONNECTING || mBaseScreen.mSipService.getRegistrationState() == ConnectionState.TERMINATING) {
                    //((TextView) view.findViewById(R.id.screen_home_item_text)).setText("取消動作");
                    ((TextView) view.findViewById(R.id.screen_home_item_text)).setText("");
                    ((ImageView) view.findViewById(R.id.screen_home_item_icon)).setImageResource(R.drawable.sign_inprogress_48);
                    //((ImageView) view .findViewById(R.id.screen_home_item_icon)).setVisibility(View.INVISIBLE);
                } else {
                    if (mBaseScreen.mSipService.isRegistered()) {
                       // ((TextView) view.findViewById(R.id.screen_home_item_text)).setText("登出");
                        ((TextView) view.findViewById(R.id.screen_home_item_text)).setText("");
                        ((ImageView) view.findViewById(R.id.screen_home_item_icon)).setImageResource(R.drawable.sign_out_48);
                        //((ImageView) view .findViewById(R.id.screen_home_item_icon)).setVisibility(View.INVISIBLE);

                    } else {
                        //((TextView) view.findViewById(R.id.screen_home_item_text)).setText("登入");
                        ((TextView) view.findViewById(R.id.screen_home_item_text)).setText("");
                        ((ImageView) view.findViewById(R.id.screen_home_item_icon)).setImageResource(R.drawable.sign_in_48);
                        //((ImageView) view .findViewById(R.id.screen_home_item_icon)).setVisibility(View.INVISIBLE);

                    }
                }
            } else {
                ((TextView) view.findViewById(R.id.screen_home_item_text)).setText(item.mText);
                ((ImageView) view.findViewById(R.id.screen_home_item_icon)).setImageResource(item.mIconResId);
            }

            return view;
        }

    }

    private class LongOperation  extends AsyncTask<String, Void, Void> {

        private final HttpClient Client = new DefaultHttpClient();
        private String Content;
        private String Error = null;

        protected void onPreExecute() {

        }

        protected Void doInBackground(String... urls) {
            try {

                HttpGet httpget = new HttpGet("http://122.117.67.226:5288/BedQuery.aspx?Olive=dd&QueryId=" + urls[0]);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                Content = Client.execute(httpget, responseHandler);
                /*
                * [0]->BedId
                * [1]->主護名稱
                * [2]->主護號碼
                */
                NursePhoneCode=Content.split("\\$")[2].toString();
                NurseName=Content.split("\\$")[1].toString();
                Log.v("NursePhoneCode=>", NursePhoneCode);


            } catch (ClientProtocolException e) {
                Error = e.getMessage();
                cancel(true);
            } catch (IOException e) {
                Error = e.getMessage();
                cancel(true);
            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            showAlert("呼叫服務","撥號給「主護」：" + NurseName);
            ScreenAV.makeCall(NursePhoneCode, NgnMediaType.Audio);
        }
    }



    private class ddOperation  extends AsyncTask<String, Void, Void> {

        private final HttpClient Client = new DefaultHttpClient();
        private String Content;
        private String Error = null;
        private Boolean isPass=false;

        protected Void doInBackground(String... urls) {
            try {
                HttpGet httpget = new HttpGet("http://122.117.67.226:5388/newNurseCall.aspx?BedId=" + urls[0]);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                Content = Client.execute(httpget, responseHandler);
                if (Content.trim()!="" && Content.equals("Complete")){
                    isPass=true;
                }

            } catch (ClientProtocolException e) {
                Error = e.getMessage();
                cancel(true);
            } catch (IOException e) {
                Error = e.getMessage();
                cancel(true);
            }

            return null;
        }

        protected void onPostExecute(Void unused) {
            if (isPass==true)
            {
                showAlert("點滴滴空服務","您的點滴滴空服務，已經通知護理師，請稍後...");
            }
        }
    }

    private class ConfigSipServer  extends AsyncTask<String, Void, Void> {

        private final HttpClient Client = new DefaultHttpClient();
        private String Content;
        private String Error = null;

        protected void onPreExecute() {
                IsGetSipConfig=false;
        }

        protected Void doInBackground(String... urls) {
            try {

                HttpGet httpget = new HttpGet("http://122.117.67.226:5388/SipServerQuery.aspx?Id=" + urls[0]);
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                Content = Client.execute(httpget, responseHandler);
                /*
                * [0]->Sip Relam Ip
                * [1]->Sip Server Ip
                * [2]->Sip Server Port
                * [3]->Sip Stun Ip
                * [4]->Sip Stun Port
                */
                CONFIG_SIP_SERVER_RELAMIP=Content.split(",")[0].toString();
                //CONFIG_SIP_SERVER_IP=Content.split(",")[1].toString();
                //CONFIG_SIP_SERVER_PORT=Content.split(",")[2].toString();
                CONFIG_SIP_SERVER_STUNIP=Content.split(",")[3].toString();
                CONFIG_SIP_SERVER_STUNPORT=Content.split(",")[4].toString();

                Log.v("Config Relam", CONFIG_SIP_SERVER_RELAMIP);
                Log.v("Config Server", CONFIG_SIP_SERVER_IP);
                Log.v("Config Port", CONFIG_SIP_SERVER_PORT);
                Log.v("Config Stun", CONFIG_SIP_SERVER_STUNIP);
                Log.v("Config Stun Port", CONFIG_SIP_SERVER_STUNPORT);


            } catch (ClientProtocolException e) {
                Error = e.getMessage();
                cancel(true);
            } catch (IOException e) {
                Error = e.getMessage();
                cancel(true);
            }

            return null;
        }

        protected void onPostExecute(Void unused) {
                IsGetSipConfig=true;
            if (LastRegBedId.equals("")==false){
                Log.v("向新的Server註冊:",LastRegBedId);
                newSettingCommit(LastRegBedId);
            }else{
                final Engine mEngine;
                final INgnConfigurationService mConfigurationService;
                mEngine = (Engine) Engine.getInstance();
                mConfigurationService = mEngine.getConfigurationService();
                String oldBed=mConfigurationService.getString(NgnConfigurationEntry.IDENTITY_IMPI,"123");
                newSettingCommit(oldBed);
            }
        }
    }
}
