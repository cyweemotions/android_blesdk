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
import com.fitpolo.support.entity.dataEntity.Steps;
import com.fitpolo.support.task.dataPushTask.StepsTask;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BleDataActivity extends BaseActivity{
    @BindView(R.id.steps_data_text)
    TextView textView;
    private MokoService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ble_data_page);
        ButterKnife.bind(this);
        bindService(new Intent(this, MokoService.class), mServiceConnection, BIND_AUTO_CREATE);
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
    // Java 示例
    @OnClick(R.id.btn_sync_data)
    public void onMyButtonClick() {
        // 设置 TextView 的文本
        textView.setText("正在获取数据。。。");
        // 处理按钮点击事件
        int type = 1;
        MokoSupport.getInstance().sendOrder(new StepsTask(mService, type));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 这里是你想要延迟5秒后执行的代码

                // 获取 TextView
                TextView textView = findViewById(R.id.steps_data_text);

                // 定义变量
                StringBuilder contentStr = new StringBuilder();
                List<Steps> stepsData = MokoSupport.getInstance().mStepsData;
                for(int i = 0; i<stepsData.size(); i++){
                    Steps stepsItem = stepsData.get(i);
                    contentStr.append("第").append(i + 1).append("项：").append("\n");
                    String step = String.valueOf(stepsItem.step);
                    String calorie = String.valueOf(stepsItem.calorie);
                    String distance = String.valueOf(stepsItem.distance);
                    String datetime = "";
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
                                .withZone(ZoneId.systemDefault());
                        Instant instant = Instant.ofEpochSecond(stepsItem.datetime);
                        datetime = formatter.format(instant);
                    } else {
                        datetime = String.valueOf(stepsItem.datetime);
                    }
                    contentStr.append("步数：").append(step).append(" ");
                    contentStr.append("卡路里：").append(calorie).append(" ");
                    contentStr.append("距离：").append(distance).append(" ");
                    contentStr.append("时间：").append(datetime).append(" ");
                    contentStr.append("\n");
                }

                // 设置 TextView 的文本
                textView.setText(contentStr.toString());
            }
        }, 2000);
    }

    public void syncStepsData(View view) {
        // 设置 TextView 的文本
        textView.setText("正在获取数据。。。");
        // 处理按钮点击事件
        int type = 1;
        MokoSupport.getInstance().sendOrder(new StepsTask(mService, type));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 这里是你想要延迟5秒后执行的代码

                // 获取 TextView
                TextView textView = findViewById(R.id.steps_data_text);

                // 定义变量
                StringBuilder contentStr = new StringBuilder();
                List<Steps> stepsData = MokoSupport.getInstance().mStepsData;
                for(int i = 0; i<stepsData.size(); i++){
                    Steps stepsItem = stepsData.get(i);
                    contentStr.append("第").append(i + 1).append("项：").append("\n");
                    String step = String.valueOf(stepsItem.step);
                    String calorie = String.valueOf(stepsItem.calorie);
                    String distance = String.valueOf(stepsItem.distance);

//                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//                    Date date = new Date(stepsItem.datetime);
//                    String datetime = dateFormat.format(date);
                    String datetime = "";
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
                                .withZone(ZoneId.systemDefault());
                        Instant instant = Instant.ofEpochSecond(stepsItem.datetime);
                        datetime = formatter.format(instant);
                    } else {
                        datetime = String.valueOf(stepsItem.datetime);
                    }
                    contentStr.append("步数：").append(step).append(" ");
                    contentStr.append("卡路里：").append(calorie).append(" ");
                    contentStr.append("距离：").append(distance).append(" ");
                    contentStr.append("时间：").append(datetime).append(" ");
                    contentStr.append("\n");
                }

                // 设置 TextView 的文本
                textView.setText(contentStr.toString());
            }
        }, 2000);
    }
}
