package com.davecoss.android.Temperature;

import java.text.DecimalFormat;

import com.davecoss.android.Temperature.R;
import com.davecoss.android.lib.Notifier;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class TemperatureApp extends IOIOActivity {
	private TextView textView_;
	private ToggleButton toggleButton_;
	public static final int TEMPERATURE_PIN = 42;
	
	public enum Units {UNITS_FAHRENHEIT,UNITS_CELCIUS,UNITS_KELVIN};
	protected Units units;
	private Notifier notifier;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        notifier = new Notifier(getApplicationContext());
        units = Units.UNITS_FAHRENHEIT;

        textView_ = (TextView)findViewById(R.id.TextView);
        toggleButton_ = (ToggleButton)findViewById(R.id.ToggleButton);

        enableUi(false);
    }
	
    public void radio_btn_click(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        
        // Which?
        switch(view.getId()) {
            case R.id.btn_celcius:
                if(checked)
                {
                	units = Units.UNITS_CELCIUS;
                	notifier.toast_message("Units set to Celcius");
                }
                break;
            case R.id.btn_fahrenheit:
            	if(checked)
            	{
            		units = Units.UNITS_FAHRENHEIT;
                	notifier.toast_message("Units set to Fahrenheit");
            	}
            	break;
            case R.id.btn_kelvin:
            	if(checked)
            	{
            		units = Units.UNITS_KELVIN;
                	notifier.toast_message("Units set to Kelvin");
            	}
            	break;
        }
    }
    
	class Looper extends BaseIOIOLooper {
		private AnalogInput input_;
		private DigitalOutput led_;

		
		@Override
		public void setup() throws ConnectionLostException {
			try {
				input_ = ioio_.openAnalogInput(TEMPERATURE_PIN);
				led_ = ioio_.openDigitalOutput(IOIO.LED_PIN, true);
				enableUi(true);
			} catch (ConnectionLostException e) {
				enableUi(false);
				throw e;
			}
		}
		
		@Override
		public void loop() throws ConnectionLostException {
			try {
				final float reading = input_.read();
				double temperature = (reading*3300-400)/19.5;
				
				if(units == Units.UNITS_FAHRENHEIT)
					temperature = (temperature*9)/5 + 32;
				else if(units == Units.UNITS_KELVIN)
					temperature += 273;
				DecimalFormat df = new DecimalFormat("#.#");
	    		setText(df.format(temperature));
				led_.write(!toggleButton_.isChecked());
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				ioio_.disconnect();
			} catch (ConnectionLostException e) {
				enableUi(false);
				throw e;
			}
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	private void enableUi(final boolean enable) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				toggleButton_.setEnabled(enable);
			}
		});
	}
	
	private void setText(final String str) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				textView_.setText(str);
			}
		});
	}
}