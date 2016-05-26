package cn.z.ecash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityLoad extends Activity {
	public final static int RESULT_CODE = 1;

	EditText etInput;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load);

		Bundle tbdl = this.getIntent().getExtras();
		String param = tbdl.getString("showstring");

		TextView tvshowparm = (TextView) findViewById(R.id.tv_showdebuginfo);
		tvshowparm.setText(param);

		etInput = (EditText) findViewById(R.id.ev_credit_for_load_inputmoney);

		Button btnCreditForLoadOK = (Button) findViewById(R.id.btn_credit_for_load_ok);
		btnCreditForLoadOK.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				retMainActivity();
			}
		});

	}

	void retMainActivity() {
		// Intent tintent = new Intent(this,MainActivity.class);
		// Bundle tbdl = new Bundle();
		//
		// String strinput = etInput.getText().toString();
		//
		// tbdl.putString("inputloamoney",strinput);
		// tintent.putExtras(tbdl);
		//
		// startActivity(tintent);
		
		String strinput = etInput.getText().toString();
		Intent intent = new Intent();
		intent.putExtra("inputloamoney", strinput);//
		setResult(RESULT_CODE, intent);//
		finish();

	}

}
