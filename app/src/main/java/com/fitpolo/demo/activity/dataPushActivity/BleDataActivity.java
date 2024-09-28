package com.fitpolo.demo.activity.dataPushActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.fitpolo.demo.R;
import com.fitpolo.demo.activity.BaseActivity;
import com.fitpolo.demo.service.MokoService;
import com.fitpolo.support.MokoConstants;
import com.fitpolo.support.MokoSupport;
import com.fitpolo.support.callback.MokoOrderTaskCallback;
import com.fitpolo.support.entity.OrderTaskResponse;
import com.fitpolo.support.entity.dataEntity.BloodOxygenModel;
import com.fitpolo.support.entity.dataEntity.HeartRateModel;
import com.fitpolo.support.entity.dataEntity.PaiModel;
import com.fitpolo.support.entity.dataEntity.PressureModel;
import com.fitpolo.support.entity.dataEntity.SleepModel;
import com.fitpolo.support.entity.dataEntity.SportModel;
import com.fitpolo.support.entity.dataEntity.StepsModel;
import com.fitpolo.support.entity.dataEntity.TemperatureModel;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.dataPushTask.SyncBloodOxygenTask;
import com.fitpolo.support.task.dataPushTask.SyncHeartRateTask;
import com.fitpolo.support.task.dataPushTask.SyncStepsTask;
import com.fitpolo.support.task.dataPushTask.SyncPaiTask;
import com.fitpolo.support.task.dataPushTask.SyncPressureTask;
import com.fitpolo.support.task.dataPushTask.SyncSleepTask;
import com.fitpolo.support.task.dataPushTask.SyncSportTask;
import com.fitpolo.support.task.dataPushTask.SyncTemperatureTask;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BleDataActivity extends BaseActivity{
    @BindView(R.id.data_text)
    TextView textView;
    public String title = "";
    public String type = "";
    private MokoService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_data_page);
        ButterKnife.bind(this);
        type = getIntent().getStringExtra("orderType");
        bindService(new Intent(this, MokoService.class), mServiceConnection, BIND_AUTO_CREATE);
        initPageTitle(type);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unbindService(mServiceConnection);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        }
    };

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
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private void initPageTitle(String type) {
        TextView titleText = findViewById(R.id.title_text);
        switch (type) {
            case "steps":
                title = "步数同步";
//                显示获取当前数据按钮
                showButton();
                break;
            case "heartRate":
                title = "心率同步";
                break;
            case "bloodOxygen":
                title = "血氧同步";
                break;
            case "sleep":
                title = "睡眠同步";
                break;
            case "sport":
                title = "运动同步";
                break;
            case "PAI":
                title = "PAI同步";
                break;
            case "pressure":
                title = "压力同步";
                break;
            case "temperature":
                title = "体温同步";
                break;
            default:
                title = "同步数据";
                break;
        }
        titleText.setText(title);
    }
    private void showButton() {
        Button button= findViewById(R.id.btn_sync_current_data);
        button.setVisibility(View.VISIBLE);
    }

    public void syncDataPushData(View view) {
        textView.setText("正在获取数据。。。");

        switch (type) {
            case "steps":
                syncStepsData(1);
                break;
            case "heartRate":
                syncHeartRateData();
                break;
            case "bloodOxygen":
                syncBloodOxygenData();
                break;
            case "sleep":
                syncSleepData();
                break;
            case "sport":
                syncSportData();
                break;
            case "PAI":
                syncPaiData();
                break;
            case "pressure":
                syncPressureData();
                break;
            case "temperature":
                syncTemperatureData();
                break;
            default:
                break;
        }
    }
    public void syncCurrentDataPushData(View view) {
        textView.setText("正在获取数据。。。");

        switch (type) {
            case "steps":
                syncStepsData(0);
                break;
            default:
                break;
        }
    }

    /**
     * 步数同步
     */
    public void syncStepsData(int type) {
//        int type = 1;

        LogModule.i("步数同步type==="+type);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        SyncStepsTask syncStepsTask = new SyncStepsTask(mService, year, month, day, type);
        syncStepsTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                LogModule.i("onOrderResult--onOrderResult"+response.responseObject.toString());
                TextView textView = findViewById(R.id.data_text);

                StringBuilder contentStr = new StringBuilder();
                if (type == 0) {
                    Map<String, Object> stepData = (Map<String, Object>) response.responseObject;
                    contentStr.append("当前步数数据：").append(stepData).append(" ");
                    contentStr.append("\n");
                } else {
//                List<StepsModel> stepsModelData = MokoSupport.getInstance().mStepsData;
                    List<StepsModel> stepsModelData = (List<StepsModel>) response.responseObject;
                    for(int i = 0; i< stepsModelData.size(); i++){
                        StepsModel stepsModelItem = stepsModelData.get(i);
                        contentStr.append("第").append(i + 1).append("项：").append("\n");
                        String step = String.valueOf(stepsModelItem.step);
                        String calorie = String.valueOf(stepsModelItem.calorie);
                        String distance = String.valueOf(stepsModelItem.distance);
                        String datetime = "";
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
                                    .withZone(ZoneId.systemDefault());
                            Instant instant = Instant.ofEpochSecond(stepsModelItem.datetime);
                            datetime = formatter.format(instant);
                        } else {
                            datetime = String.valueOf(stepsModelItem.datetime);
                        }
                        contentStr.append("步数：").append(step).append(" ");
                        contentStr.append("卡路里：").append(calorie).append(" ");
                        contentStr.append("距离：").append(distance).append(" ");
                        contentStr.append("时间：").append(datetime).append(" ");
                        contentStr.append("\n");
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(contentStr.toString());
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) {}
            @Override
            public void onOrderFinish() {}
        };
        MokoSupport.getInstance().sendOrder(syncStepsTask);
    }

    /**
     * 心率同步
     */
    public void syncHeartRateData() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        SyncHeartRateTask syncHeartRateTask = new SyncHeartRateTask(mService, year, month, day);
        syncHeartRateTask.callback = new MokoOrderTaskCallback(){
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                LogModule.i("onOrderResult--onOrderResult"+response);
                TextView textView = findViewById(R.id.data_text);

                List<HeartRateModel> heartRateData = (List<HeartRateModel>) response.responseObject;
                StringBuilder contentStr = new StringBuilder();
                for(int i = 0; i<heartRateData.size(); i++){
                    HeartRateModel heartRateItem = heartRateData.get(i);
                    contentStr.append("第").append(i + 1).append("项：").append("\n");
                    String heartRate = String.valueOf(heartRateItem.heartRate);
                    String datetime = "";
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                                .withZone(ZoneId.systemDefault());
                        Instant instant = Instant.ofEpochSecond(heartRateItem.datetime);
                        datetime = formatter.format(instant);
                    } else {
                        datetime = String.valueOf(heartRateItem.datetime);
                    }
                    contentStr.append("心率：").append(heartRate).append(" ");
                    contentStr.append("时间：").append(datetime).append(" ");
                    contentStr.append("\n");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(contentStr.toString());
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) {}
            @Override
            public void onOrderFinish() {}
        };
        MokoSupport.getInstance().sendOrder(syncHeartRateTask);
    }

    /**
     * 血氧同步
     */
    public void syncBloodOxygenData() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        SyncBloodOxygenTask syncBloodOxygenTask = new SyncBloodOxygenTask(mService,  year, month, day);
        syncBloodOxygenTask.callback = new MokoOrderTaskCallback(){
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                LogModule.i("onOrderResult--onOrderResult"+response);
                TextView textView = findViewById(R.id.data_text);

                StringBuilder contentStr = new StringBuilder();
                List<BloodOxygenModel> bloodOxygenData = (List<BloodOxygenModel>) response.responseObject;

                for(int i = 0; i<bloodOxygenData.size(); i++){
                    BloodOxygenModel bloodOxygenItem = bloodOxygenData.get(i);
                    contentStr.append("第").append(i + 1).append("项：").append("\n");
                    String bloodOxygen = String.valueOf(bloodOxygenItem.bloodOxygen);
                    String datetime = "";
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                                .withZone(ZoneId.systemDefault());
                        Instant instant = Instant.ofEpochSecond(bloodOxygenItem.datetime);
                        datetime = formatter.format(instant);
                    } else {
                        datetime = String.valueOf(bloodOxygenItem.datetime);
                    }
                    contentStr.append("血氧：").append(bloodOxygen).append(" ");
                    contentStr.append("时间：").append(datetime).append(" ");
                    contentStr.append("\n");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(contentStr.toString());
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) {}
            @Override
            public void onOrderFinish() {}
        };
        MokoSupport.getInstance().sendOrder(syncBloodOxygenTask);
    }
    /**
     * 睡眠同步
     */
    public void syncSleepData() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        SyncSleepTask syncSleepTask = new SyncSleepTask(mService, year, month, day);
        syncSleepTask.callback = new MokoOrderTaskCallback(){
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                LogModule.i("onOrderResult--onOrderResult"+response);
                TextView textView = findViewById(R.id.data_text);

                StringBuilder contentStr = new StringBuilder();
                List<SleepModel> sleepData = (List<SleepModel>) response.responseObject;
                for(int i = 0; i<sleepData.size(); i++){
                    SleepModel sleepItem = sleepData.get(i);
                    contentStr.append("第").append(i + 1).append("项：").append("\n");
                    String type;
                    String slice;
                    if(sleepItem.type == 0){
                        type = "夜间睡眠";
                        if(sleepItem.slice != 0) {
                            slice = String.valueOf(sleepItem.slice);
                            contentStr.append("第").append(slice).append("段夜间睡眠， ");
                        }
                    } else {
                        type = "小睡";
                        slice = String.valueOf(sleepItem.slice);
                        contentStr.append("第").append(slice).append("段小睡， ");
                    }
                    String state;
                    if(sleepItem.state == 0){
                        state = "入睡";
                    } else if(sleepItem.state == 1){
                        state = "浅睡";
                    } else if(sleepItem.state == 2){
                        state = "深睡";
                    } else if(sleepItem.state == 3){
                        state = "清醒";
                    } else if(sleepItem.state == 12){
                        state = "REM";
                    } else { // 4或14  4-夜间睡眠醒来  14-小睡醒来
                        state = "醒来";
                    }
                    String datetime = "";
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                                .withZone(ZoneId.systemDefault());
                        Instant instant = Instant.ofEpochSecond(sleepItem.datetime);
                        datetime = formatter.format(instant);
                    } else {
                        datetime = String.valueOf(sleepItem.datetime);
                    }
                    contentStr.append("类型：").append(type).append(", ");
                    contentStr.append("状态：").append(state).append(", ");
                    contentStr.append("时间：").append(datetime).append("。 ");
                    contentStr.append("\n");
                    contentStr.append("\n");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(contentStr.toString());
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) {}
            @Override
            public void onOrderFinish() {}
        };
        MokoSupport.getInstance().sendOrder(syncSleepTask);
    }
    /**
     * 运动同步
     */
    public void syncSportData() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int fileIndex = 0;
        SyncSportTask syncSportTask = new SyncSportTask(mService, fileIndex, year, month, day);
        syncSportTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                LogModule.i("onOrderResult--onOrderResult"+response);

                TextView textView = findViewById(R.id.data_text);

                StringBuilder contentStr = new StringBuilder();
//                List<SportModel> sportData = MokoSupport.getInstance().mSportData;
                List<SportModel> sportData = (List<SportModel>) response.responseObject;
                for(int i = 0; i<sportData.size(); i++){
                    SportModel sportItem = sportData.get(i);
                    contentStr.append("第").append(i + 1).append("项：").append("\n");
                    contentStr.append("运动类型：").append(sportItem.sportType).append("\n ");
                    contentStr.append("运动时间(s)：").append(sportItem.second).append("\n ");
                    contentStr.append("开始时间：").append(sportItem.start).append("\n ");
                    contentStr.append("结束时间：").append(sportItem.date).append("\n ");
                    contentStr.append("步数：").append(sportItem.total_steps).append("\n ");
                    contentStr.append("距离（米）：").append(sportItem.total_distance).append("\n ");
                    contentStr.append("卡路里(千卡)：").append(sportItem.total_calories).append("\n ");
                    contentStr.append("平均步频(步/分)：").append(sportItem.avg_step_freq).append("\n ");
                    contentStr.append("最大步频(步/分)：").append(sportItem.max_step_freq).append("\n ");
                    contentStr.append("平均步长：").append(sportItem.avg_step_len).append("\n ");
                    contentStr.append("平均速度：").append((int)Math.floor(sportItem.avg_pace / 60)).append("'").append((int)sportItem.avg_pace % 60).append("\"").append("\n ");
                    contentStr.append("最大速度：").append((int)Math.floor(sportItem.max_pace / 60)).append("'").append((int)sportItem.max_pace % 60).append("\"").append("\n ");
                    contentStr.append("平均心率：").append(sportItem.avg_heart).append("\n ");
                    contentStr.append("最大心率：").append(sportItem.max_heart).append("\n ");
                    contentStr.append("最小心率：").append(sportItem.min_heart).append("\n ");
                    contentStr.append("心率区间-热身：").append(sportItem.hr_zone1).append("\n ");
                    contentStr.append("心率区间-燃脂：").append(sportItem.hr_zone2).append("\n ");
                    contentStr.append("心率区间-有氧运动：").append(sportItem.hr_zone3).append("\n ");
                    contentStr.append("心率区间-无氧运动：").append(sportItem.hr_zone4).append("\n ");
                    contentStr.append("心率区间-极限：").append(sportItem.hr_zone5).append("\n ");
                    contentStr.append("最大摄氧量：").append(sportItem.vo2max).append("\n ");
                    contentStr.append("训练时间：").append(sportItem.training_time).append("分").append("\n ");
                    contentStr.append("步频数组：").append(sportItem.step_freqs).append("\n ");
                    contentStr.append("速度数组：").append(sportItem.speeds).append("\n ");
                    contentStr.append("心率数组：").append(sportItem.hr_values).append("\n ");
//                    contentStr.append("GPS：").append(sportItem.gpsData).append("\n ");
                    contentStr.append("跳频数组：").append(sportItem.jumpfreqs).append("\n ");
                    contentStr.append("跳频：").append(sportItem.jumpfreq).append("\n ");
                    contentStr.append("跳次：").append(sportItem.jumpCount).append("\n ");
                    contentStr.append("游泳-划水次数：").append(sportItem.strokeCount).append("\n ");
                    contentStr.append("游泳-趟次：").append(sportItem.strokeLaps).append("\n ");
                    contentStr.append("游泳-划频：").append(sportItem.strokeFreq).append("\n ");
                    contentStr.append("游泳-平均划频：").append(sportItem.strokeAvgFreq).append("\n ");
                    contentStr.append("游泳-最佳Swolf：").append(sportItem.strokeBestSwolf).append("\n ");
                    contentStr.append("游泳-泳姿：").append(sportItem.strokeType).append("\n ");
                    contentStr.append("游泳频率：").append(sportItem.swimfreqs).append("\n ");
                    contentStr.append("骑行速度：").append(sportItem.bikeSpeed).append("\n ");
//                    contentStr.append("原始数据：").append(sportItem.RawData).append("\n ");
                    contentStr.append("\n");
                    contentStr.append("\n");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(contentStr.toString());
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) {}
            @Override
            public void onOrderFinish() {}
        };
        MokoSupport.getInstance().sendOrder(syncSportTask);

    }

    /**
     * PAI同步
     */
    public void syncPaiData() {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        SyncPaiTask syncPaiTask = new SyncPaiTask(mService, year, month, day);
        syncPaiTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                LogModule.i("onOrderResult--onOrderResult"+response.responseObject.toString());
                TextView textView = findViewById(R.id.data_text);

                StringBuilder contentStr = new StringBuilder();
//                List<PaiModel> stepsModelData = MokoSupport.getInstance().mPaiData;
                List<PaiModel> paiModelData = (List<PaiModel>) response.responseObject;
                for(int i = 0; i< paiModelData.size(); i++){
                    PaiModel paiModelItem = paiModelData.get(i);
                    contentStr.append("第").append(i + 1).append("项：").append("\n ");
                    String id = String.valueOf(paiModelItem.id);
                    String year = String.valueOf(paiModelItem.year);
                    String month = String.valueOf(paiModelItem.month);
                    String day = String.valueOf(paiModelItem.day);
                    String pai = String.valueOf(paiModelItem.pai);
                    String totals = String.valueOf(paiModelItem.totals);
                    String low = String.valueOf(paiModelItem.low);
                    String lowMins = String.valueOf(paiModelItem.lowMins);
                    String medium = String.valueOf(paiModelItem.medium);
                    String mediumMins = String.valueOf(paiModelItem.mediumMins);
                    String high = String.valueOf(paiModelItem.high);
                    String highMins = String.valueOf(paiModelItem.highMins);
                    contentStr.append("时间戳：").append(id).append("\n ");
                    contentStr.append("时间：").append(year).append(month).append(day).append("\n ");
                    contentStr.append("值：").append(pai).append("\n ");
                    contentStr.append("总数：").append(totals).append("\n ");
                    contentStr.append("最低强度：").append(low).append("\n ");
                    contentStr.append("最低强度持续时间（min）：").append(lowMins).append("\n ");
                    contentStr.append("中强度：").append(medium).append("\n ");
                    contentStr.append("中强度持续时间（min）：").append(mediumMins).append("\n ");
                    contentStr.append("高强度：").append(high).append("\n ");
                    contentStr.append("最高强度持续时间（min）：").append(highMins).append("\n ");
                    contentStr.append("\n");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(contentStr.toString());
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) {}
            @Override
            public void onOrderFinish() {}
        };
        MokoSupport.getInstance().sendOrder(syncPaiTask);
    }

    /**
     * 压力同步
     */
    public void syncPressureData() {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        SyncPressureTask syncPressureTask = new SyncPressureTask(mService, year, month, day);
        syncPressureTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                LogModule.i("onOrderResult--onOrderResult"+response.responseObject.toString());
                TextView textView = findViewById(R.id.data_text);

                StringBuilder contentStr = new StringBuilder();
//                List<StepsModel> stepsModelData = MokoSupport.getInstance().mStepsData;
                List<PressureModel> pressureModelData = (List<PressureModel>) response.responseObject;
                for(int i = 0; i< pressureModelData.size(); i++){
                    PressureModel pressureModelItem = pressureModelData.get(i);
                    contentStr.append("第").append(i + 1).append("项：").append("\n ");
                    String id = String.valueOf(pressureModelItem.id);
                    String year = String.valueOf(pressureModelItem.year);
                    String month = String.valueOf(pressureModelItem.month);
                    String day = String.valueOf(pressureModelItem.day);
                    String relax = String.valueOf(pressureModelItem.relax);
                    String normal = String.valueOf(pressureModelItem.normal);
                    String strain = String.valueOf(pressureModelItem.strain);
                    String anxiety = String.valueOf(pressureModelItem.anxiety);
                    String highest = String.valueOf(pressureModelItem.highest);
                    String minimum = String.valueOf(pressureModelItem.minimum);
                    String lately = String.valueOf(pressureModelItem.lately);
//                    String pressTime = String.valueOf(pressureModelItem.pressTime);
                    contentStr.append("时间戳：").append(id).append("\n ");
                    contentStr.append("时间：").append(year).append("年").append(month).append("月").append(day).append("日").append("\n ");
                    contentStr.append("放松：").append(relax).append("\n ");
                    contentStr.append("正常：").append(normal).append("\n ");
                    contentStr.append("紧张：").append(strain).append("\n ");
                    contentStr.append("焦虑：").append(anxiety).append("\n ");
                    contentStr.append("最高：").append(highest).append("\n ");
                    contentStr.append("最低：").append(minimum).append("\n ");
                    contentStr.append("最近：").append(lately).append("\n ");
                    contentStr.append("\n");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(contentStr.toString());
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) {}
            @Override
            public void onOrderFinish() {}
        };
        MokoSupport.getInstance().sendOrder(syncPressureTask);
    }

    /**
     * 体温同步
     */
    public void syncTemperatureData() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        SyncTemperatureTask syncTemperatureTask = new SyncTemperatureTask(mService, year, month, day);
        syncTemperatureTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                LogModule.i("onOrderResult--onOrderResult"+response.responseObject.toString());
                TextView textView = findViewById(R.id.data_text);

                StringBuilder contentStr = new StringBuilder();
//                List<StepsModel> stepsModelData = MokoSupport.getInstance().mStepsData;
                List<TemperatureModel> temperatureModelData = (List<TemperatureModel>) response.responseObject;
                for(int i = 0; i< temperatureModelData.size(); i++){
                    TemperatureModel temperatureModelItem = temperatureModelData.get(i);
                    String datetime = "";
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
                                .withZone(ZoneId.systemDefault());
                        Instant instant = Instant.ofEpochSecond(temperatureModelItem.datetime);
                        datetime = formatter.format(instant);
                    } else {
                        datetime = String.valueOf(temperatureModelItem.datetime);
                    }
                    String skinTemperature = String.valueOf(temperatureModelItem.skinTemperature);
                    String bodyTemperature = String.valueOf(temperatureModelItem.bodyTemperature);
                    contentStr.append("第").append(i + 1).append("项：").append("\n ");
                    contentStr.append("时间：").append(datetime).append("\n ");
                    contentStr.append("皮肤温度：").append(skinTemperature).append("\n ");
                    contentStr.append("体温：").append(bodyTemperature).append("\n ");
                    contentStr.append("\n");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText(contentStr.toString());
                    }
                });
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) {}
            @Override
            public void onOrderFinish() {}
        };
        MokoSupport.getInstance().sendOrder(syncTemperatureTask);
    }

}
