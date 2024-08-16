package com.fitpolo.demo.activity;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fitpolo.demo.AppConstants;
import com.fitpolo.demo.R;
import com.fitpolo.demo.activity.dataPushActivity.BleDataActivity;
import com.fitpolo.demo.service.MokoService;
import com.fitpolo.demo.utils.FileUtils;
import com.fitpolo.demo.utils.Utils;
import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.entity.AutoLighten;
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
import com.fitpolo.support.task.dataPushTask.WeatherTask;
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
import com.fitpolo.support.task.getTask.SleepMonitorDataTask;
import com.fitpolo.support.task.setTask.AddressBookTask;
import com.fitpolo.support.task.setTask.AlarmClockTask;
import com.fitpolo.support.task.dataPushTask.AllHeartRateTask;
import com.fitpolo.support.task.dataPushTask.AllSleepIndexTask;
import com.fitpolo.support.task.dataPushTask.AllStepsTask;
import com.fitpolo.support.task.setTask.AutoLightenTask;
import com.fitpolo.support.task.funcTask.GetBatteryTask;
import com.fitpolo.support.task.funcTask.FindDeviceTask;
import com.fitpolo.support.task.funcTask.FirmwareParamTask;
import com.fitpolo.support.task.FunctionDisplayTask;
import com.fitpolo.support.task.setTask.CallReminderTask;
import com.fitpolo.support.task.setTask.DoNotDisturbTask;
import com.fitpolo.support.task.setTask.HeartRateMonitorTask;
import com.fitpolo.support.task.InnerVersionTask;
import com.fitpolo.support.task.LastScreenTask;
import com.fitpolo.support.task.dataPushTask.LastestHeartRateTask;
import com.fitpolo.support.task.dataPushTask.LastestSleepIndexTask;
import com.fitpolo.support.task.dataPushTask.LastestStepsTask;
import com.fitpolo.support.task.NotifyPhoneTask;
import com.fitpolo.support.task.NotifySmsTask;
import com.fitpolo.support.task.OrderTask;
import com.fitpolo.support.task.ReadAlarmsTask;
import com.fitpolo.support.task.ReadSettingTask;
import com.fitpolo.support.task.ReadSitAlertTask;
import com.fitpolo.support.task.ShakeBandTask;
import com.fitpolo.support.task.setTask.LanguageTask;
import com.fitpolo.support.task.setTask.MotionTargetTask;
import com.fitpolo.support.task.setTask.NotifyTask;
import com.fitpolo.support.task.setTask.OnScreenDurationTask;
import com.fitpolo.support.task.setTask.PowerSaveTask;
import com.fitpolo.support.task.setTask.SitLongTimeAlertTask;
import com.fitpolo.support.task.SleepHeartCountTask;
import com.fitpolo.support.task.UnitTypeTask;
import com.fitpolo.support.task.setTask.SleepMonitorTask;
import com.fitpolo.support.task.setTask.SleepTask;
import com.fitpolo.support.task.setTask.StandardAlertTask;
import com.fitpolo.support.task.setTask.TargetTask;
import com.fitpolo.support.task.setTask.TimeTask;
import com.fitpolo.support.task.setTask.UserInfoTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description
 */

public class SendOrderActivity extends BaseActivity {
    private static final String TAG = "SendOrderActivity";

    @BindView(R.id.btn_heart_rate_interval)
    Button btnHeartRateInterval;
    @BindView(R.id.btn_lastest_steps)
    Button btnLastestSteps;
    @BindView(R.id.btn_lastest_sleeps)
    Button btnLastestSleeps;
    @BindView(R.id.btn_lastest_heart_rate)
    Button btnLastestHeartRate;
    @BindView(R.id.btn_all_heart_rate)
    Button btnAllHeartRate;
    @BindView(R.id.btn_read_all_alarms)
    Button btnReadAllAlarms;
    @BindView(R.id.btn_read_sit_alert)
    Button btnReadSitAlert;
    @BindView(R.id.btn_read_settings)
    Button btnReadSettings;
    @BindView(R.id.btn_notification)
    Button btnNotification;
    @BindView(R.id.btn_firmware_params)
    Button btnFirmwareParams;
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

                            btnAllHeartRate.setVisibility(MokoSupport.showHeartRate ? View.VISIBLE : View.GONE);
                            btnHeartRateInterval.setVisibility(MokoSupport.showHeartRate ? View.VISIBLE : View.GONE);

                            btnLastestSteps.setVisibility(MokoSupport.supportNewData ? View.VISIBLE : View.GONE);
                            btnLastestSleeps.setVisibility(MokoSupport.supportNewData ? View.VISIBLE : View.GONE);
                            btnLastestHeartRate.setVisibility(MokoSupport.showHeartRate && MokoSupport.supportNewData ? View.VISIBLE : View.GONE);

                            btnFirmwareParams.setVisibility(MokoSupport.versionCodeLast >= 28 ? View.VISIBLE : View.GONE);

                            btnReadAllAlarms.setVisibility(MokoSupport.supportNotifyAndRead ? View.VISIBLE : View.GONE);
                            btnReadSettings.setVisibility(MokoSupport.supportNotifyAndRead ? View.VISIBLE : View.GONE);
                            btnReadSitAlert.setVisibility(MokoSupport.supportNotifyAndRead ? View.VISIBLE : View.GONE);
                            btnNotification.setVisibility(MokoSupport.supportNotifyAndRead ? View.VISIBLE : View.GONE);

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
            MokoSupport.getInstance().sendOrder(new InnerVersionTask(mService));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    public void getInnerVersion(View view) {
        MokoSupport.getInstance().sendOrder(new InnerVersionTask(mService));
    }

    public void setSystemTime(View view) {
        Toast.makeText(this, "Connect successp-[1", Toast.LENGTH_SHORT).show();
//        MokoSupport.getInstance().sendOrder(new SystemTimeTask(mService));
        MokoSupport.getInstance().sendOrder(new ReadSettingTask(mService));
    }

    public void setUnitType(View view) {
        MokoSupport.getInstance().sendOrder(new UnitTypeTask(mService, 0));
    }



    public void setLastScreen(View view) {
        MokoSupport.getInstance().sendOrder(new LastScreenTask(mService, 1));
    }

    public void setFunctionDisplay(View view) {
        CustomScreen customScreen = new CustomScreen(true, true, true, true, true);
        MokoSupport.getInstance().sendOrder(new FunctionDisplayTask(mService, customScreen));
    }


    public void getSleepHeartCount(View view) {
        MokoSupport.getInstance().sendOrder(new SleepHeartCountTask(mService));
    }

    public void getAllSteps(View view) {
//        if (MokoSupport.getInstance().getDailyStepCount() == 0) {
//            Toast.makeText(this, "Get step count first", Toast.LENGTH_SHORT).show();
//            return;
//        }
        MokoSupport.getInstance().sendOrder(new AllStepsTask(mService));
    }

    public void getAllSleeps(View view) {
        if (MokoSupport.getInstance().getSleepIndexCount() == 0) {
            Toast.makeText(this, "Get sleep count first", Toast.LENGTH_SHORT).show();
            return;
        }
        MokoSupport.getInstance().sendOrder(new AllSleepIndexTask(mService));
    }

    public void getAllHeartRate(View view) {
        if (MokoSupport.getInstance().getHeartRateCount() == 0) {
            Toast.makeText(this, "Get heartrate count first", Toast.LENGTH_SHORT).show();
            return;
        }
        MokoSupport.getInstance().sendOrder(new AllHeartRateTask(mService));
    }

    public void shakeBand(View view) {
        MokoSupport.getInstance().sendDirectOrder(new ShakeBandTask(mService));
    }

    public void setPhoneNotify(View view) {
        OrderTask shakeBandTask = new NotifyPhoneTask(mService, "1234567", true);
        MokoSupport.getInstance().sendDirectOrder(shakeBandTask);
    }

    public void setSmsNotify(View view) {
        OrderTask shakeBandTask = new NotifySmsTask(mService, "abcdef", false);
        MokoSupport.getInstance().sendDirectOrder(shakeBandTask);
    }

    public void getLastestSteps(View view) {
        Calendar lastSyncTime = Utils.strDate2Calendar("2018-06-01 00:00", AppConstants.PATTERN_YYYY_MM_DD_HH_MM);
        OrderTask stepsTask = new LastestStepsTask(mService, lastSyncTime);
        MokoSupport.getInstance().sendOrder(stepsTask);
    }

    public void getLastestSleeps(View view) {
        Calendar lastSyncTime = Utils.strDate2Calendar("2018-06-01 00:00", AppConstants.PATTERN_YYYY_MM_DD_HH_MM);
        OrderTask sleepGeneral = new LastestSleepIndexTask(mService, lastSyncTime);
        MokoSupport.getInstance().sendOrder(sleepGeneral);
    }

    public void getLastestHeartRate(View view) {
        Calendar lastSyncTime = Utils.strDate2Calendar("2018-06-01 00:00", AppConstants.PATTERN_YYYY_MM_DD_HH_MM);
        OrderTask heartRateTask = new LastestHeartRateTask(mService, lastSyncTime);
        MokoSupport.getInstance().sendOrder(heartRateTask);
    }


    public void getFirmwareParams(View view) {
        MokoSupport.getInstance().sendOrder(new FirmwareParamTask(mService));
    }


    public void readAllAlarms(View view) {
        MokoSupport.getInstance().sendOrder(new ReadAlarmsTask(mService));
    }

    public void readSitAlert(View view) {
        MokoSupport.getInstance().sendOrder(new ReadSitAlertTask(mService));
    }

    public void readSettings(View view) {
        MokoSupport.getInstance().sendOrder(new ReadSettingTask(mService));
    }

    public void notification(View view) {
        startActivity(new Intent(this, MessageNotificationActivity.class));
    }

    /********************* 鉴权 begin *****************/
    public void queryAuthState(View view) {
        MokoSupport.getInstance().sendOrder(new QueryAuthStateTask(mService));
    }
    public void bindAuth(View view) {
        List<Byte> dataList = new ArrayList<>();
        dataList.add((byte) 1);
        dataList.add((byte) 0);
        dataList.add((byte) 0);
        dataList.add((byte) 0);
        dataList.add((byte) 1);
        MokoSupport.getInstance().sendOrder(new DeviceBindTask(mService, 1, 0, dataList));
    }
    /********************* 鉴权 end *****************/

    /********************* 功能类型 begin *****************/
    public void TimeAlign(View view){
        LogModule.i("开始校准时间====");
        MokoSupport.getInstance().sendOrder(new TimeAlignTask(mService));
    }
    public void getBattery(View view) {
        MokoSupport.getInstance().sendOrder(new GetBatteryTask(mService));
    }
    public void findDevice(View view){
        LogModule.i("开始查找设备====");
        FindDevice findDevice = new FindDevice();
        findDevice.action = 1;
        MokoSupport.getInstance().sendOrder(new FindDeviceTask(mService, findDevice));
    }
    public void unbindDevice(View view){
        LogModule.i("开始解绑设备====");
        MokoSupport.getInstance().sendOrder(new UnbindDeviceTask(mService));
    }
    public void languageSupport(View view){
        LogModule.i("开始语言支持====");
        MokoSupport.getInstance().sendOrder(new LanguageSupportTask(mService));
    }
    public void deviceInfo(View view){
        LogModule.i("开始设备信息====");
        MokoSupport.getInstance().sendOrder(new DeviceInfoTask(mService));
    }
    public void remotePhoto(View view){
        LogModule.i("开始远程拍照====");
        MokoSupport.getInstance().sendOrder(new RemotePhotoTask(mService));
    }
    public void messageNotify(View view){
        LogModule.i("开始消息通知====");
        MokoSupport.getInstance().sendOrder(new MessageNotifyTask(mService));
    }
    public void positionGPS(View view){
        LogModule.i("开始定位GPS====");
        MokoSupport.getInstance().sendOrder(new PositionGPSTask(mService));
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
        MokoSupport.getInstance().sendOrder(new TargetTask(mService));
    }
    public void setTimeFormat(View view) {
        MokoSupport.getInstance().sendOrder(new TimeTask(mService));
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
        AlarmClock alarmClock = new AlarmClock();
        alarmClock.action = 0;
        alarmClock.index = 1;
        alarmClock.toggle = 0;
        alarmClock.mode = 2;
        alarmClock.time = 23 * 60;
        alarmClock.repeat = 0;
        alarmClock.activeDay = 0;
        MokoSupport.getInstance().sendOrder(new AlarmClockTask(mService, alarmClock));
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
    public void setPowerSaveMode(View view) {
        MokoSupport.getInstance().sendOrder(new PowerSaveTask(mService, 1));
    }
    public void setAddressBook(View view) {
        AddressBook addressBook = new AddressBook();
        addressBook.action = 1;
        addressBook.name = "小刚";
        addressBook.phoneNumber = "152666655550987";
        MokoSupport.getInstance().sendOrder(new AddressBookTask(mService, addressBook));
    }
    public void getAddressBook(View view) {
        MokoSupport.getInstance().sendOrder(new AddressBookDataTask(mService));
    }
    public void setSleepMonitor(View view) {
        SleepMonitor sleepMonitor = new SleepMonitor();
        sleepMonitor.heighPrecisionDetec = 1;
        sleepMonitor.breatheQualityDetec = 1;
        MokoSupport.getInstance().sendOrder(new SleepMonitorTask(mService,sleepMonitor));
    }
    public void getSleepMonitor(View view) {
        MokoSupport.getInstance().sendOrder(new SleepMonitorDataTask(mService));
    }
    /********************* 设置类型 end *****************/

    /********************* 数据交互类型 begin *****************/

    public void syncWeather(View view) {
        MokoSupport.getInstance().sendOrder(new WeatherTask(mService));
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
    /********************* 数据交互类型 end *****************/

    private static final int REQUEST_CODE_FILE = 2;

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
