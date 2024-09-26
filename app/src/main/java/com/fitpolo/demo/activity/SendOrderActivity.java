package com.fitpolo.demo.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.fitpolo.demo.R;
import com.fitpolo.demo.activity.dataPushActivity.BleDataActivity;
import com.fitpolo.demo.service.MokoService;
import com.fitpolo.demo.utils.FileUtils;
import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.AutoLighten;
import com.fitpolo.support.entity.dataEntity.StepsModel;
import com.fitpolo.support.entity.setEntity.AddressBook;
import com.fitpolo.support.entity.setEntity.AlarmClock;
import com.fitpolo.support.entity.CustomScreen;
import com.fitpolo.support.entity.DailySleep;
import com.fitpolo.support.entity.DailyStep;
import com.fitpolo.support.entity.FindDevice;
import com.fitpolo.support.entity.HeartRate;
import com.fitpolo.support.entity.OrderEnum;
import com.fitpolo.support.entity.OrderTaskResponse;
import com.fitpolo.support.entity.SitAlert;
import com.fitpolo.support.entity.UserInfo;
import com.fitpolo.support.entity.funcEntity.MotionControl;
import com.fitpolo.support.entity.setEntity.DoNotDisturb;
import com.fitpolo.support.entity.setEntity.HeartRateMonitor;
import com.fitpolo.support.entity.setEntity.MotionTarget;
import com.fitpolo.support.entity.setEntity.NotifyType;
import com.fitpolo.support.entity.setEntity.SleepMonitor;
import com.fitpolo.support.handler.UpgradeHandler;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.authTask.DeviceBindTask;
import com.fitpolo.support.task.authTask.QueryAuthStateTask;
import com.fitpolo.support.task.dataPushTask.SyncStepsTask;
import com.fitpolo.support.task.dataPushTask.SyncWeatherTask;
import com.fitpolo.support.task.funcTask.DeviceInfoTask;
import com.fitpolo.support.task.funcTask.LanguageSupportTask;
import com.fitpolo.support.task.funcTask.MessageNotifyTask;
import com.fitpolo.support.task.funcTask.MotionControlTask;
import com.fitpolo.support.task.funcTask.PositionGPSTask;
import com.fitpolo.support.task.funcTask.RemotePhotoTask;
import com.fitpolo.support.task.funcTask.QueryInfoTask;
import com.fitpolo.support.task.funcTask.TimeAlignTask;
import com.fitpolo.support.task.funcTask.UnbindDeviceTask;
import com.fitpolo.support.task.getTask.AddressBookDataTask;
import com.fitpolo.support.task.getTask.GetDoNotDisturbTask;
import com.fitpolo.support.task.getTask.GetSitAlertSettingTask;
import com.fitpolo.support.task.getTask.GetTargetTask;
import com.fitpolo.support.task.getTask.SleepMonitorDataTask;
import com.fitpolo.support.task.setTask.AddressBookTask;
import com.fitpolo.support.task.setTask.AlarmClockTask;
import com.fitpolo.support.task.setTask.AutoLightenTask;
import com.fitpolo.support.task.funcTask.GetBatteryTask;
import com.fitpolo.support.task.funcTask.FindDeviceTask;
import com.fitpolo.support.task.setTask.CallReminderTask;
import com.fitpolo.support.task.setTask.DoNotDisturbTask;
import com.fitpolo.support.task.setTask.HeartRateMonitorTask;
import com.fitpolo.support.task.setTask.LanguageTask;
import com.fitpolo.support.task.setTask.MotionTargetTask;
import com.fitpolo.support.task.setTask.NotifyTask;
import com.fitpolo.support.task.setTask.OnScreenDurationTask;
import com.fitpolo.support.task.setTask.PowerSaveTask;
import com.fitpolo.support.task.setTask.SitLongTimeAlertTask;
import com.fitpolo.support.task.setTask.SleepMonitorTask;
import com.fitpolo.support.task.setTask.SleepTask;
import com.fitpolo.support.task.setTask.StandardAlertTask;
import com.fitpolo.support.task.setTask.TargetTask;
import com.fitpolo.support.task.setTask.TimeTask;
import com.fitpolo.support.task.setTask.UserInfoTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description
 */

public class SendOrderActivity extends BaseActivity {
    private static final String TAG = "SendOrderActivity";

//    @BindView(R.id.btn_heart_rate_interval)
//    Button btnHeartRateInterval;
//    @BindView(R.id.btn_lastest_heart_rate)
//    Button btnLastestHeartRate;
//    @BindView(R.id.btn_read_all_alarms)
//    Button btnReadAllAlarms;
//    @BindView(R.id.btn_read_sit_alert)
//    Button btnReadSitAlert;
//    @BindView(R.id.btn_read_settings)
//    Button btnReadSettings;
//    @BindView(R.id.btn_notification)
//    Button btnNotification;
//    @BindView(R.id.btn_firmware_params)
//    Button btnFirmwareParams;
    private MokoService mService;
    private String deviceMacAddress;
    private boolean mIsUpgrade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_order_layout);
        ButterKnife.bind(this);
        deviceMacAddress = getIntent().getStringExtra("deviceMacAddress");
        bindService(new Intent(this, MokoService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                        case BluetoothAdapter.STATE_OFF:
//                            SendOrderActivity.this.finish();
                            break;
                    }
                }
                if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
                    abortBroadcast();
                    if (!mIsUpgrade) {
//                        Toast.makeText(SendOrderActivity.this, "Connect failed", Toast.LENGTH_SHORT).show();
//                        SendOrderActivity.this.finish();
                    }
                }
                if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                    OrderTaskResponse response = (OrderTaskResponse) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK);
                    OrderEnum orderEnum = response.order;
                    switch (orderEnum) {
                        case getInnerVersion:

//                            btnAllHeartRate.setVisibility(MokoSupport.showHeartRate ? View.VISIBLE : View.GONE);
//                            btnHeartRateInterval.setVisibility(MokoSupport.showHeartRate ? View.VISIBLE : View.GONE);

//                            btnLastestSteps.setVisibility(MokoSupport.supportNewData ? View.VISIBLE : View.GONE);
//                            btnLastestSleeps.setVisibility(MokoSupport.supportNewData ? View.VISIBLE : View.GONE);
//                            btnLastestHeartRate.setVisibility(MokoSupport.showHeartRate && MokoSupport.supportNewData ? View.VISIBLE : View.GONE);
//
//                            btnFirmwareParams.setVisibility(MokoSupport.versionCodeLast >= 28 ? View.VISIBLE : View.GONE);
//
//                            btnReadAllAlarms.setVisibility(MokoSupport.supportNotifyAndRead ? View.VISIBLE : View.GONE);
//                            btnReadSettings.setVisibility(MokoSupport.supportNotifyAndRead ? View.VISIBLE : View.GONE);
//                            btnReadSitAlert.setVisibility(MokoSupport.supportNotifyAndRead ? View.VISIBLE : View.GONE);
//                            btnNotification.setVisibility(MokoSupport.supportNotifyAndRead ? View.VISIBLE : View.GONE);

                            LogModule.i("Support heartRate：" + MokoSupport.showHeartRate);
                            LogModule.i("Support newData：" + MokoSupport.supportNewData);
                            LogModule.i("Support notify and read：" + MokoSupport.supportNotifyAndRead);
                            LogModule.i("Version code：" + MokoSupport.versionCode);
                            LogModule.i("Should upgrade：" + MokoSupport.canUpgrade);
                            break;
                        case setSystemTime:
                            break;
                        case setUserInfo:
                            break;
                        case setAlarmClock:
                            break;
                        case setUnitType:
                            break;
                        case setTimeFormat:
                            break;
                        case setAutoLigten:
                            break;
                        case setSitLongTimeAlert:
                            break;
                        case setLastScreen:
                            break;
                        case setHeartRateMonitor:
                            break;
                        case setFunctionDisplay:
                            break;
                        case getFirmwareVersion:
                            LogModule.i("firmware version：" + MokoSupport.versionCodeShow);
                            break;
                        case getBattery:
                            LogModule.i("battery：" + MokoSupport.getInstance().getBatteryQuantity());
                            break;
                        case getSleepHeartCount:
                            break;
                        case getAllSteps:
                            LogModule.i("步数返回数据");

                            ArrayList<DailyStep> steps = MokoSupport.getInstance().getDailySteps();
                            if (steps == null || steps.isEmpty()) {
                                return;
                            }
                            for (DailyStep step : steps) {
                                LogModule.i(step.toString());
                            }
                            break;
                        case getAllSleepIndex:
                            ArrayList<DailySleep> sleeps = MokoSupport.getInstance().getDailySleeps();
                            if (sleeps == null || sleeps.isEmpty()) {
                                return;
                            }
                            for (DailySleep sleep : sleeps) {
                                LogModule.i(sleep.toString());
                            }
                            break;
                        case getAllHeartRate:
                            ArrayList<HeartRate> heartRates = MokoSupport.getInstance().getHeartRates();
                            if (heartRates == null || heartRates.isEmpty()) {
                                return;
                            }
                            for (HeartRate heartRate : heartRates) {
                                LogModule.i(heartRate.toString());
                            }
                            break;
                        case getLastestSteps:
                            ArrayList<DailyStep> lastestSteps = MokoSupport.getInstance().getDailySteps();
                            if (lastestSteps == null || lastestSteps.isEmpty()) {
                                return;
                            }
                            for (DailyStep step : lastestSteps) {
                                LogModule.i(step.toString());
                            }
                            break;
                        case getLastestSleepIndex:
                            ArrayList<DailySleep> lastestSleeps = MokoSupport.getInstance().getDailySleeps();
                            if (lastestSleeps == null || lastestSleeps.isEmpty()) {
                                return;
                            }
                            for (DailySleep sleep : lastestSleeps) {
                                LogModule.i(sleep.toString());
                            }
                            break;
                        case getLastestHeartRate:
                            ArrayList<HeartRate> lastestHeartRate = MokoSupport.getInstance().getHeartRates();
                            if (lastestHeartRate == null || lastestHeartRate.isEmpty()) {
                                return;
                            }
                            for (HeartRate heartRate : lastestHeartRate) {
                                LogModule.i(heartRate.toString());
                            }
                            break;
                        case getFirmwareParam:
                            LogModule.i("Last charge time：" + MokoSupport.getInstance().getLastChargeTime());
                            LogModule.i("Product batch：" + MokoSupport.getInstance().getProductBatch());
                            break;
                        case READ_ALARMS:
                            ArrayList<AlarmClock> bandAlarms = MokoSupport.getInstance().getAlarms();
                            for (AlarmClock bandAlarm : bandAlarms) {
                                LogModule.i(bandAlarm.toString());
                            }
                            break;
                        case READ_SETTING:
                            boolean unitType = MokoSupport.getInstance().getUnitTypeBritish();
                            int timeFormat = MokoSupport.getInstance().getTimeFormat();
                            CustomScreen customScreen = MokoSupport.getInstance().getCustomScreen();
                            boolean lastScreen = MokoSupport.getInstance().getLastScreen();
                            int interval = MokoSupport.getInstance().getHeartRateInterval();
                            AutoLighten autoLighten = MokoSupport.getInstance().getAutoLighten();
                            LogModule.i("Unit type:" + unitType);
                            LogModule.i("Time format:" + timeFormat);
                            LogModule.i("Function display:" + customScreen.toString());
                            LogModule.i("Last screen:" + lastScreen);
                            LogModule.i("HeartRate interval:" + interval);
                            LogModule.i("Auto light:" + autoLighten.toString());

                            break;
                        case READ_SIT_ALERT:
                            SitAlert sitAlert = MokoSupport.getInstance().getSitAlert();
                            LogModule.i("Sit alert:" + sitAlert.toString());
                            break;
                    }

                }
                if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
                    Toast.makeText(SendOrderActivity.this, "Timeout", Toast.LENGTH_SHORT).show();
                }
                if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                    Toast.makeText(SendOrderActivity.this, "Success", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((MokoService.LocalBinder) service).getService();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(MokoConstants.ACTION_CONN_STATUS_DISCONNECTED);
            filter.addAction(MokoConstants.ACTION_DISCOVER_TIMEOUT);
            filter.addAction(MokoConstants.ACTION_ORDER_RESULT);
            filter.addAction(MokoConstants.ACTION_ORDER_TIMEOUT);
            filter.addAction(MokoConstants.ACTION_ORDER_FINISH);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.setPriority(200);
            registerReceiver(mReceiver, filter);
            // first
//            MokoSupport.getInstance().sendOrder(new InnerVersionTask(mService));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    /********************* 鉴权 begin *****************/
    public void queryAuthState(View view) {
        QueryAuthStateTask queryAuthStateTask = new QueryAuthStateTask(mService);
        queryAuthStateTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                StringBuilder contentStr = new StringBuilder();
                int result = (int) response.responseObject;
                LogModule.i("绑定状态====" + result);
                String state = result == 1 ? "已经被绑定" : "未绑定";
                contentStr.append("绑定状态：").append(state).append("\n ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAlertDialog(String.valueOf(contentStr));
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) { }
            @Override
            public void onOrderFinish() { }
        };
        MokoSupport.getInstance().sendOrder(queryAuthStateTask);
    }
    public void bindAuth(View view) {
        DeviceBindTask deviceBindTask = new DeviceBindTask(mService);
        deviceBindTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                StringBuilder contentStr = new StringBuilder();
                int result = (int) response.responseObject;
                LogModule.i("绑定结果====" + result);
                if(result == 3) {
                    contentStr.append("绑定结果：").append("成功").append("\n ");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showAlertDialog(String.valueOf(contentStr));
                        }
                    });
                }
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) { }
            @Override
            public void onOrderFinish() { }
        };
        MokoSupport.getInstance().sendOrder(deviceBindTask);
    }
    /********************* 鉴权 end *****************/

    /********************* 功能类型 begin *****************/
    public void TimeAlign(View view){
        LogModule.i("开始校准时间====");
        MokoSupport.getInstance().sendOrder(new TimeAlignTask(mService));
    }
    public void getBattery(View view) {
        LogModule.i("开始电量获取====");
        GetBatteryTask getBatteryTask = new GetBatteryTask(mService);
        getBatteryTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                StringBuilder contentStr = new StringBuilder();
                int result = (int) response.responseObject;
                LogModule.i("电量====" + result);
                contentStr.append("电量：").append(result).append("\n ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAlertDialog(String.valueOf(contentStr));
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) { }
            @Override
            public void onOrderFinish() { }
        };
        MokoSupport.getInstance().sendOrder(getBatteryTask);
    }
    public void findDevice(View view){
        LogModule.i("开始查找设备====");
        FindDevice findDevice = new FindDevice();
        findDevice.action = 1;
        MokoSupport.getInstance().sendOrder(new FindDeviceTask(mService, findDevice));
    }
    public void unbindDevice(View view){
        LogModule.i("开始解绑设备====");
        UnbindDeviceTask unbindDeviceTask = new UnbindDeviceTask(mService);
        unbindDeviceTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                StringBuilder contentStr = new StringBuilder();
                int result = (int) response.responseObject;
                LogModule.i("开始解绑设备11====" + result);
                String onOff = result == 0 ? "成功" : "失败";
                contentStr.append("解绑设备").append("\n ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAlertDialog(String.valueOf(contentStr));
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) { }
            @Override
            public void onOrderFinish() { }
        };
        MokoSupport.getInstance().sendOrder(unbindDeviceTask);
    }
    public void languageSupport(View view){
        LogModule.i("开始语言支持====");
        MokoSupport.getInstance().sendOrder(new LanguageSupportTask(mService));
    }
    public void deviceInfo(View view){
        LogModule.i("开始设备信息====");
        DeviceInfoTask deviceInfoTask = new DeviceInfoTask(mService);
        deviceInfoTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                StringBuilder contentStr = new StringBuilder();
                String result = (String) response.responseObject;
                LogModule.i("设备信息====" + result);
                contentStr.append("解绑设备").append(result).append("\n ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAlertDialog(String.valueOf(contentStr));
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) { }
            @Override
            public void onOrderFinish() { }
        };
        MokoSupport.getInstance().sendOrder(deviceInfoTask);
    }
    public void remotePhoto(View view){
        LogModule.i("开始远程拍照====");
        showAlertDialog("暂无");
//        MokoSupport.getInstance().sendOrder(new RemotePhotoTask(mService));
    }
    public void messageNotify(View view){
        LogModule.i("开始消息通知====");
        Calendar calendar = Calendar.getInstance();
        String title = "这是标题";
        String content = "这是一段消息内容";

        int appType = 12; //app
//        String packageName = MokoConstants.wechatPName;
//        if(packageName == MokoConstants.wechatPName){
//            appType = 12;
//        }else if(packageName == MokoConstants.facebookPName){
//            appType = 1;
//        }else if(packageName == MokoConstants.instagramPName){
//            appType = 2;
//        }else if(packageName == MokoConstants.kakaotalkPName){
//            appType = 3;
//        }else if(packageName == MokoConstants.LinePName){
//            appType = 4;
//        }else if(packageName == MokoConstants.LINKEDINPName){
//            appType = 5;
//        }else if(packageName == MokoConstants.MESSAGERPName){
//            appType = 6;
//        }else if(packageName == MokoConstants.QQPName){
//            appType = 7;
//        }else if(packageName == MokoConstants.TWITTERPName){
//            appType = 8;
//        }else if(packageName == MokoConstants.VIBERPName){
//            appType = 9;
//        }else if(packageName == MokoConstants.VKONTAKETPName){
//            appType = 10;
//        }else if(packageName == MokoConstants.mms){
//            appType = 0;
//        }
        MokoSupport.getInstance().sendOrder(new MessageNotifyTask(mService, calendar, appType, title, content));
    }
    public void positionGPS(View view){
        LogModule.i("开始定位GPS====");
        showAlertDialog("暂无");
//        MokoSupport.getInstance().sendOrder(new PositionGPSTask(mService, 3, 1, 1));
    }
    public void motionControl(View view){
        LogModule.i("开始运动控制====");
        MotionControl motionControl = new MotionControl();
        motionControl.type = 1;
        motionControl.action = 1;
        MokoSupport.getInstance().sendOrder(new MotionControlTask(mService, motionControl));
    }
    public void pauseMotion(View view){
        LogModule.i("开始暂停运动====");
        MotionControl motionControl = new MotionControl();
        motionControl.type = 1;
        motionControl.action = 3;
        MokoSupport.getInstance().sendOrder(new MotionControlTask(mService, motionControl));
    }
    public void resumeMotion(View view){
        LogModule.i("开始继续运动====");
        MotionControl motionControl = new MotionControl();
        motionControl.type = 1;
        motionControl.action = 4;
        MokoSupport.getInstance().sendOrder(new MotionControlTask(mService, motionControl));
    }
    public void stopMotion(View view){
        LogModule.i("开始停止运动====");
        MotionControl motionControl = new MotionControl();
        motionControl.type = 1;
        motionControl.action = 5;
        MokoSupport.getInstance().sendOrder(new MotionControlTask(mService, motionControl));
    }
    public void queryInfo(View view){
        LogModule.i("开始查询信息====");
        MokoSupport.getInstance().sendOrder(new QueryInfoTask(mService));
    }
    /********************* 功能类型 end *****************/

    /********************* 设置类型 begin *****************/
    public void setUserInfo(View view) {
        UserInfo userInfo = new UserInfo();
        userInfo.name = "小明";
        userInfo.male = 0;
        userInfo.birth = 20001109;
        userInfo.height = 170;
        userInfo.weight = 60;
        userInfo.hand = 0;
        userInfo.MHR = 200;
        MokoSupport.getInstance().sendOrder(new UserInfoTask(mService, userInfo));
    }
    public void setTarget(View view) {
        TargetTask targetTask = new TargetTask(mService, 4, 2,4, 4);
        targetTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                int result = (int) response.responseObject;
                LogModule.i("setTarget====" + result);
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) { }
            @Override
            public void onOrderFinish() { }
        };
        MokoSupport.getInstance().sendOrder(targetTask);
    }
    public void getTarget(View view) {
        GetTargetTask getTargetTask = new GetTargetTask(mService);
        getTargetTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                StringBuilder contentStr = new StringBuilder();
                List<Integer> getSitAlertData = (List<Integer>) response.responseObject;
                String stepValue = String.valueOf((getSitAlertData.get(0) * 1000));
                String distanceValue = String.valueOf((getSitAlertData.get(1)));
                String calorieValue = String.valueOf((getSitAlertData.get(2) * 50));
                String sportValue = String.valueOf((getSitAlertData.get(3) * 15));
                contentStr.append("步数：").append(stepValue).append("步\n");
                contentStr.append("距离：").append(distanceValue).append("公里\n");
                contentStr.append("卡路里：").append(calorieValue).append("千卡\n");
                contentStr.append("运动时长：").append(sportValue).append("分钟\n");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAlertDialog(String.valueOf(contentStr));
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) { }
            @Override
            public void onOrderFinish() { }
        };
        MokoSupport.getInstance().sendOrder(getTargetTask);
    }
    public void setTimeFormat(View view) {
        TimeTask timeTask = new TimeTask(mService, 1 , 2, 8);
        timeTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                int result = (int) response.responseObject;
                LogModule.i("timeTask====" + result);
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) { }
            @Override
            public void onOrderFinish() { }
        };
        MokoSupport.getInstance().sendOrder(timeTask);
    }
    public void setSleep(View view) {
        int startTime = 8;
        int endTime = 23;
        MokoSupport.getInstance().sendOrder(new SleepTask(mService, startTime, endTime));
    }
    public void setSitAlert(View view) {
        SitAlert alert = new SitAlert();
        alert.alertSwitch = 0;
        alert.startTime = 8;
        alert.endTime = 21;
        alert.interval = 60;
        MokoSupport.getInstance().sendOrder(new SitLongTimeAlertTask(mService, alert));
    }
    public void getSitAlertSetting(View view) {
        GetSitAlertSettingTask getSitAlertSettingTask = new GetSitAlertSettingTask(mService);
        getSitAlertSettingTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                StringBuilder contentStr = new StringBuilder();
                List<Integer> getSitAlertData = (List<Integer>) response.responseObject;
                String onOff = getSitAlertData.get(0) == 0 ? "开" : "关";
                int timeValue = getSitAlertData.get(1);
                String startHour =  String.format("%02d", getSitAlertData.get(2) / 60);
                String startMinute =  String.format("%02d", getSitAlertData.get(2) % 60);
                String endHour = String.format("%02d", getSitAlertData.get(3) / 60);
                String endMinute = String.format("%02d", getSitAlertData.get(3) % 60);
                contentStr.append("开关：").append(onOff).append("\n ");
                contentStr.append("间隔：").append(timeValue).append("\n ");
                contentStr.append("开始时间：").append(startHour).append(":").append(startMinute).append("\n ");
                contentStr.append("结束时间：").append(endHour).append(":").append(endMinute).append("\n ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAlertDialog(String.valueOf(contentStr));
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) { }
            @Override
            public void onOrderFinish() { }
        };
        MokoSupport.getInstance().sendOrder(getSitAlertSettingTask);
    }
    public void setAutoLigten(View view) {
        MokoSupport.getInstance().sendOrder(new AutoLightenTask(mService));
    }
    public void setHeartRateMonitor(View view) {
        HeartRateMonitor heartRateMonitor = new HeartRateMonitor();
        heartRateMonitor.monitorSwitch = 0;
        heartRateMonitor.interval = 10;
        heartRateMonitor.alarmSwitch = 1;
        heartRateMonitor.minLimit = 50;
        heartRateMonitor.maxLimit = 190;
        MokoSupport.getInstance().sendOrder(new HeartRateMonitorTask(mService, heartRateMonitor));
    }
    public void setAlarmClock(View view) {
        showAlertDialog("暂无");
//        AlarmClock alarmClock = new AlarmClock();
//        alarmClock.action = 0;
//        alarmClock.index = 1;
//        alarmClock.toggle = 0;
//        alarmClock.mode = 2;
//        alarmClock.time = 23 * 60;
//        alarmClock.repeat = 0;
//        alarmClock.activeDay = 0;
//        MokoSupport.getInstance().sendOrder(new AlarmClockTask(mService, alarmClock));
    }
    public void setCallReminder(View view) {
        MokoSupport.getInstance().sendOrder(new CallReminderTask(mService, 1));
    }
    public void setMotionTarget(View view) {
        MotionTarget motionTarget = new MotionTarget();
        motionTarget.setType = 0;
        motionTarget.sportType = 0;
        if(motionTarget.setType == 0) {
            motionTarget.distance = 1;
            motionTarget.sportTime = 2;
            motionTarget.calorie = 3;
            motionTarget.targetType = 2;
        } else {
            motionTarget.autoPauseSwitch = 0;
        }
        MokoSupport.getInstance().sendOrder(new MotionTargetTask(mService, motionTarget));
    }
    public void setAutoPause(View view) {
        MotionTarget motionTarget = new MotionTarget();
        motionTarget.setType = 1;
        motionTarget.sportType = 0;
        motionTarget.autoPauseSwitch = 0;
        MokoSupport.getInstance().sendOrder(new MotionTargetTask(mService, motionTarget));
    }
    public void setStandardAlert(View view) {
        MokoSupport.getInstance().sendOrder(new StandardAlertTask(mService, 0));
    }
    public void setLanguage(View view) {
        MokoSupport.getInstance().sendOrder(new LanguageTask(mService, 1));
    }
    public void setNotify(View view) {
        NotifyType notifyType = new NotifyType();
        notifyType.toggle = 0;
        notifyType.common = 1;
        notifyType.facebook = 1;
        notifyType.instagram = 0;
        notifyType.kakaotalk = 1;
        notifyType.line = 1;
        notifyType.linkedin = 1;
        notifyType.SMS = 1;
        notifyType.QQ = 0;
        notifyType.twitter = 0;
        notifyType.viber = 1;
        notifyType.vkontaket = 1;
        notifyType.whatsapp = 0;
        notifyType.wechat = 1;
        notifyType.other1 = 1;
        notifyType.other2 = 1;
        notifyType.other3 = 1;
        MokoSupport.getInstance().sendOrder(new NotifyTask(mService, notifyType));
    }
    public void setOnScreenDuration(View view) {
        MokoSupport.getInstance().sendOrder(new OnScreenDurationTask(mService, 15));
    }
    public void setDoNotDisturb(View view) {
        DoNotDisturb doNotDisturb = new DoNotDisturb();
        doNotDisturb.allToggle = 0;
        doNotDisturb.partToggle = 1;
        doNotDisturb.startTime = 22*60+10;
        doNotDisturb.endTime = 8*60+20;
        MokoSupport.getInstance().sendOrder(new DoNotDisturbTask(mService, doNotDisturb));
    }
    public void getDoNotDisturb(View view) {
        GetDoNotDisturbTask getDoNotDisturbTask = new GetDoNotDisturbTask(mService);
        getDoNotDisturbTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                StringBuilder contentStr = new StringBuilder();
                List<Integer> getSitAlertData = (List<Integer>) response.responseObject;
                String onOff1 = getSitAlertData.get(0) == 0 ? "开" : "关";
                String onOff2 = getSitAlertData.get(1) == 0 ? "开" : "关";
                String startHour =  String.format("%02d", getSitAlertData.get(2) / 60);
                String startMinute =  String.format("%02d", getSitAlertData.get(2) % 60);
                String endHour = String.format("%02d", getSitAlertData.get(3) / 60);
                String endMinute = String.format("%02d", getSitAlertData.get(3) % 60);
                contentStr.append("全天开启：").append(onOff1).append("\n ");
                contentStr.append("定时开启：").append(onOff2).append("\n ");
                contentStr.append("定时开启-开始时间：").append(startHour).append(":").append(startMinute).append("\n ");
                contentStr.append("定时开启-结束时间：").append(endHour).append(":").append(endMinute).append("\n ");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAlertDialog(String.valueOf(contentStr));
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) { }
            @Override
            public void onOrderFinish() { }
        };
        MokoSupport.getInstance().sendOrder(getDoNotDisturbTask);
    }
    public void setPowerSaveMode(View view) {
        MokoSupport.getInstance().sendOrder(new PowerSaveTask(mService, 1));
    }
    public void setAddressBook(View view) {
        AddressBook addressBook = new AddressBook();
        addressBook.action = 0;
        addressBook.name = "小刚";
        addressBook.phoneNumber = "152666655550987";
        MokoSupport.getInstance().sendOrder(new AddressBookTask(mService, addressBook));
    }
    public void getAddressBook(View view) {
        AddressBookDataTask addressBookDataTask = new AddressBookDataTask(mService);
        addressBookDataTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                StringBuilder contentStr = new StringBuilder();
                List<HashMap> sleepMonitorData = (List<HashMap>) response.responseObject;
                for (HashMap<String, Object> map : sleepMonitorData) {
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        contentStr.append(entry.getKey()).append(":").append(entry.getValue()).append("  ");
                    }
                    contentStr.append(" \n");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAlertDialog(String.valueOf(contentStr));
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) { }
            @Override
            public void onOrderFinish() { }
        };
        MokoSupport.getInstance().sendOrder(addressBookDataTask);
    }
    public void setSleepMonitor(View view) {
        SleepMonitor sleepMonitor = new SleepMonitor();
        sleepMonitor.heighPrecisionDetec = 1;
        sleepMonitor.breatheQualityDetec = 1;
        MokoSupport.getInstance().sendOrder(new SleepMonitorTask(mService,sleepMonitor));
    }
    public void getSleepMonitor(View view) {
        SleepMonitorDataTask sleepMonitorDataTask = new SleepMonitorDataTask(mService);
        sleepMonitorDataTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                StringBuilder contentStr = new StringBuilder();
                List<Integer> sleepMonitorData = (List<Integer>) response.responseObject;
                String switch1 = sleepMonitorData.get(0) == 0 ? "开" : "关";
                String switch2 = sleepMonitorData.get(1) == 0 ? "开" : "关";
                contentStr.append("睡眠高精度监测：").append(switch1).append(" ");
                contentStr.append("\n");
                contentStr.append("睡眠呼吸质量监测：").append(switch2).append(" ");
                contentStr.append("\n");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showAlertDialog(String.valueOf(contentStr));
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) { }
            @Override
            public void onOrderFinish() { }
        };
        MokoSupport.getInstance().sendOrder(sleepMonitorDataTask);
    }
    /********************* 设置类型 end *****************/

    /********************* 数据交互类型 begin *****************/

    public void syncWeather(View view) {
        MokoSupport.getInstance().sendOrder(new SyncWeatherTask(mService));
    }
    public void syncSteps(View view) {
        Intent orderIntent = new Intent(SendOrderActivity.this, BleDataActivity.class);
        orderIntent.putExtra("orderType", "steps");
        startActivity(orderIntent);

//        int type = 1;
//        MokoSupport.getInstance().sendOrder(new StepTask(mService, type));
    }
    public void syncHeartRate(View view) {
        Intent orderIntent = new Intent(SendOrderActivity.this, BleDataActivity.class);
        orderIntent.putExtra("orderType", "heartRate");
        startActivity(orderIntent);
    }
    public void syncBloodOxygen(View view) {
        Intent orderIntent = new Intent(SendOrderActivity.this, BleDataActivity.class);
        orderIntent.putExtra("orderType", "bloodOxygen");
        startActivity(orderIntent);
    }
    public void syncSleep(View view) {
        Intent orderIntent = new Intent(SendOrderActivity.this, BleDataActivity.class);
        orderIntent.putExtra("orderType", "sleep");
        startActivity(orderIntent);
    }
    public void syncSport(View view) {
        Intent orderIntent = new Intent(SendOrderActivity.this, BleDataActivity.class);
        orderIntent.putExtra("orderType", "sport");
        startActivity(orderIntent);
    }
    public void syncPAI(View view) {
        Intent orderIntent = new Intent(SendOrderActivity.this, BleDataActivity.class);
        orderIntent.putExtra("orderType", "PAI");
        startActivity(orderIntent);
    }
    public void syncPressure(View view) {
        Intent orderIntent = new Intent(SendOrderActivity.this, BleDataActivity.class);
        orderIntent.putExtra("orderType", "pressure");
        startActivity(orderIntent);
    }
    public void syncTemperature(View view) {
        Intent orderIntent = new Intent(SendOrderActivity.this, BleDataActivity.class);
        orderIntent.putExtra("orderType", "temperature");
        startActivity(orderIntent);
    }
    /********************* 数据交互类型 end *****************/

    // 显示弹窗
    private void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SendOrderActivity.this);
        builder.setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户点击了“确定”按钮
                        dialog.dismiss();
                    }
                })
                .setCancelable(true); // 允许用户点击外部区域关闭对话框（可选）

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private static final int REQUEST_CODE_FILE = 2;

    //升级固件
    public void upgradeFirmware(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "select file first!"), REQUEST_CODE_FILE);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, "install file manager app", Toast.LENGTH_SHORT).show();
        }
    }

    private ProgressDialog mDialog;

    private void upgrade(String firmwarePath) {
        mIsUpgrade = true;
        if (!isFinishing()) {
            mDialog = ProgressDialog.show(this, null, "upgrade...", false, false);
        }
        UpgradeHandler upgradeHandler = new UpgradeHandler(this);
        upgradeHandler.setFilePath(firmwarePath, deviceMacAddress, new UpgradeHandler.IUpgradeCallback() {
            @Override
            public void onUpgradeError(int errorCode) {
                if (mDialog != null && mDialog.isShowing() && !isFinishing()) {
                    mDialog.dismiss();
                }
                switch (errorCode) {
                    case UpgradeHandler.EXCEPTION_FILEPATH_IS_NULL:
                        Toast.makeText(SendOrderActivity.this, "file is not exist！", Toast.LENGTH_SHORT).show();
                        break;
                    case UpgradeHandler.EXCEPTION_DEVICE_MAC_ADDRESS_IS_NULL:
                        Toast.makeText(SendOrderActivity.this, "mac address is null！", Toast.LENGTH_SHORT).show();
                        break;
                    case UpgradeHandler.EXCEPTION_UPGRADE_FAILURE:
                        Toast.makeText(SendOrderActivity.this, "upgrade failed！", Toast.LENGTH_SHORT).show();
                        back();
                        break;
                }
                mIsUpgrade = false;
            }

            @Override
            public void onProgress(int progress) {
                if (mDialog != null && mDialog.isShowing() && !isFinishing()) {
                    mDialog.setMessage("upgrade progress:" + progress + "%");
                }
            }

            @Override
            public void onUpgradeDone() {
                if (mDialog != null && mDialog.isShowing() && !isFinishing()) {
                    mDialog.dismiss();
                }
                Toast.makeText(SendOrderActivity.this, "upgrade success", Toast.LENGTH_SHORT).show();
//                SendOrderActivity.this.finish();
            }
        });
    }

    private void back() {
        if (MokoSupport.getInstance().isConnDevice(this, deviceMacAddress)) {
           mService.disConnectBle();
        }
//        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_FILE:
                    Uri uri = data.getData();
                    String path = FileUtils.getPath(this, uri);
                    upgrade(path);
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
