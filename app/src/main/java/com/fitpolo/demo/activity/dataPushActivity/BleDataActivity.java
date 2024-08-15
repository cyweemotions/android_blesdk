package com.fitpolo.demo.activity.dataPushActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
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
import com.fitpolo.support.entity.dataEntity.SleepModel;
import com.fitpolo.support.entity.dataEntity.SportModel;
import com.fitpolo.support.entity.dataEntity.StepsModel;
import com.fitpolo.support.log.LogModule;
import com.fitpolo.support.task.dataPushTask.BloodOxygenTask;
import com.fitpolo.support.task.dataPushTask.HeartRateTask;
import com.fitpolo.support.task.dataPushTask.StepsTask;
import com.fitpolo.support.task.dataPushTask.SyncSleepTask;
import com.fitpolo.support.task.dataPushTask.SyncSportTask;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
            default:
                title = "同步数据";
                break;
        }
        titleText.setText(title);
    }

    public void syncDataPushData(View view) {
        textView.setText("正在获取数据。。。");

        switch (type) {
            case "steps":
                syncStepsData();
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
            default:
                break;
        }
    }

    /**
     * 步数同步
     */
    public void syncStepsData() {
        int type = 1;

        StepsTask stepsTask = new StepsTask(mService, type);
        stepsTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                LogModule.i("onOrderResult--onOrderResult"+response.responseObject.toString());
                TextView textView = findViewById(R.id.data_text);

                StringBuilder contentStr = new StringBuilder();
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
        MokoSupport.getInstance().sendOrder(stepsTask);
    }

    /**
     * 心率同步
     */
    public void syncHeartRateData() {
        Calendar calendar = Calendar.getInstance();
        int type = 1;
        MokoSupport.getInstance().sendOrder(new HeartRateTask(mService, calendar, type));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(R.id.data_text);

                StringBuilder contentStr = new StringBuilder();
                List<HeartRateModel> heartRateData = MokoSupport.getInstance().mHeartRateData;
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

                textView.setText(contentStr.toString());
            }
        }, 2000);
    }

    /**
     * 血氧同步
     */
    public void syncBloodOxygenData() {
        Calendar calendar = Calendar.getInstance();
        int type = 1;
        MokoSupport.getInstance().sendOrder(new BloodOxygenTask(mService, calendar, type));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(R.id.data_text);

                StringBuilder contentStr = new StringBuilder();
                List<BloodOxygenModel> bloodOxygenData = MokoSupport.getInstance().mBloodOxygenData;
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

                textView.setText(contentStr.toString());
            }
        }, 2000);
    }
    /**
     * 睡眠同步
     */
    public void syncSleepData() {
        Calendar calendar = Calendar.getInstance();
        int type = 1;
        MokoSupport.getInstance().sendOrder(new SyncSleepTask(mService, calendar, type));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(R.id.data_text);

                StringBuilder contentStr = new StringBuilder();
                List<SleepModel> sleepData = MokoSupport.getInstance().mSleepData;
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

                textView.setText(contentStr.toString());
            }
        }, 2000);
    }
    /**
     * 运动同步
     */
    public void syncSportData() {
        Calendar calendar = Calendar.getInstance();
        int type = 1;
        int fileIndex = 0;
        SyncSportTask syncSportTask = new SyncSportTask(mService, calendar, fileIndex, type);
        syncSportTask.callback = new MokoOrderTaskCallback() {
            @Override
            public void onOrderResult(OrderTaskResponse response) {
                LogModule.i("onOrderResult--onOrderResult"+response);
            }
            @Override
            public void onOrderTimeout(OrderTaskResponse response) {}
            @Override
            public void onOrderFinish() {}
        };
        MokoSupport.getInstance().sendOrder(syncSportTask);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(R.id.data_text);

                StringBuilder contentStr = new StringBuilder();
                List<SportModel> sportData = MokoSupport.getInstance().mSportData;
                for(int i = 0; i<sportData.size(); i++){
                    SportModel sportItem = sportData.get(i);
                    contentStr.append("第").append(i + 1).append("项：").append("\n");
                    contentStr.append("运动类型：").append(sportItem.sportType).append("\n ");
                    contentStr.append("second：").append(sportItem.second).append("\n ");
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
        }, 2000);
    }
}
