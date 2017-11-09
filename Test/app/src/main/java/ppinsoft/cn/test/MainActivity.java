package ppinsoft.cn.test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    WheelView wheelView;
    TextView tvNumber;
    List<String> data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        data = new ArrayList<>();
        for (int i=1;i<30;i++){
            data.add(i*1+"");
        }
        wheelView = (WheelView) findViewById(R.id.wheel);
        tvNumber = (TextView)findViewById(R.id.tv_number);
        //先设置offset再设置data，可扩展展示区域
        wheelView.setOffset(5);
        wheelView.setItems(data);
        wheelView.setSeletion(5);
        tvNumber.setText(data.get(5));
        wheelView.setOnWheelViewListener(new WheelView.OnWheelViewListener(){
            @Override
            public void onSelected(int selectedIndex, String item) {
                tvNumber.setText(item);
            }
        });
    }
}
