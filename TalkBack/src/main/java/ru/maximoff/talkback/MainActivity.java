package ru.maximoff.talkback;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;
import ru.maximoff.talkback.R;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		final TextView info = findViewById(R.id.mainTextView1);
		info.setText("TalkBack " + (isEnabled() ? "Enabled" : "Disabled"));
		Button toggle = findViewById(R.id.mainButton1);
		toggle.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View p1) {
					String message;
					try {
						if (updateTalkBackState()) {
							message = "Enabled";
						} else {
							message = "Disabled";
						}
					} catch (Exception e) {
						message = e.getMessage();
					}
					Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
					info.setText("TalkBack " + message);
				}
			});
		Button copy = findViewById(R.id.mainButton2);
		copy.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View p1) {
					setClipboard(String.format("adb shell pm grant %s android.permission.WRITE_SECURE_SETTINGS", getPackageName()));
				}
			});
    }

	private boolean updateTalkBackState() {
		if (isEnabled()) {
			disableTalkBack();
			return false;
		} else {
			enableTalkBack();
			return true;
		}
	}

	private void enableTalkBack() {
		Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.google.android.marvin.talkback/.TalkBackService");
		Settings.Secure.putString(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, "1");
	}

	private void disableTalkBack() {
		Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "");
		Settings.Secure.putString(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, "0");
	}

	private boolean isEnabled() {
		AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
		if (am != null && am.isEnabled()) {
			List<AccessibilityServiceInfo> serviceInfoList = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_SPOKEN);
			if (!serviceInfoList.isEmpty())
				return true;
		}
		return false;
	}

	public void setClipboard(String text) {
		if (text.isEmpty()) {
			return;
		}
		String message;
		try {
			ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			ClipData clip = ClipData.newPlainText("Copy", text);
			clipboard.setPrimaryClip(clip);
			message = "Copied";
		} catch (Exception e) {
			message = e.getMessage();
		}
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}
