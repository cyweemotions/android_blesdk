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
import com.fitpolo.support.entity.dataEntity.HeartRateModel;
import com.fitpolo.support.entity.dataEntity.StepsModel;
import com.fitpolo.support.task.dataPushTask.HeartRateTask;
import com.fitpolo.support.task.dataPushTask.StepsTask;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
            default:
                break;
        }
    }

    /**
     * 步数同步
     */
    public void syncStepsData() {

        int type = 1;
        MokoSupport.getInstance().sendOrder(new StepsTask(mService, type));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView textView = findViewById(R.id.data_text);

                StringBuilder contentStr = new StringBuilder();
                List<StepsModel> stepsModelData = MokoSupport.getInstance().mStepsDataModels;
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

                textView.setText(contentStr.toString());
            }
        }, 1500);
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
                List<HeartRateModel> heartRateData = MokoSupport.getInstance().mHeartRateModelData;
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
}
