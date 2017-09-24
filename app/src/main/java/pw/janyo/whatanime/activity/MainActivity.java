package pw.janyo.whatanime.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.File;
import java.util.ArrayList;

import dmax.dialog.SpotsDialog;
import pw.janyo.whatanime.R;
import pw.janyo.whatanime.adapter.AnimationAdapter;
import pw.janyo.whatanime.classes.Error;
import pw.janyo.whatanime.handler.AnalyzeHandler;
import pw.janyo.whatanime.listener.WhatAnimeBuildListener;
import pw.janyo.whatanime.util.Base64;
import pw.janyo.whatanime.util.Base64DecoderException;

import android.support.design.widget.FloatingActionButton;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import pw.janyo.whatanime.util.Settings;
import pw.janyo.whatanime.util.whatanime.WhatAnimeBuilder;
import vip.mystery0.tools.CrashHandler.CatchExceptionListener;
import vip.mystery0.tools.CrashHandler.CrashHandler;
import vip.mystery0.tools.FileUtil.FileUtil;

/**
 * Created by mystery0.
 */

public class MainActivity extends AppCompatActivity
{
	private static final String TAG = "MainActivity";
	private final static int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 233;
	private final static int REQUEST_CODE = 322;
	private Settings settings;
	private AnalyzeHandler analyzeHandler = new AnalyzeHandler();
	private SpotsDialog progressDialog;
	private FloatingActionButton main_fab_upload;
	private AnimationAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestPermission();
		initialization();
		monitor();
	}

	private void initialization()
	{
		settings = Settings.getInstance(this);
		setContentView(R.layout.activity_main);

		main_fab_upload = findViewById(R.id.main_fab_upload);
		Toolbar toolbar = findViewById(R.id.toolbar);
		RecyclerView recyclerView = findViewById(R.id.recyclerView);
		recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
		analyzeHandler.list = new ArrayList<>();
		adapter = new AnimationAdapter(MainActivity.this, analyzeHandler.list);
		recyclerView.setAdapter(adapter);
		analyzeHandler.adapter = adapter;
		analyzeHandler.context = MainActivity.this;
		analyzeHandler.context = MainActivity.this;

		progressDialog = new SpotsDialog(MainActivity.this, R.style.SpotsDialog);
		progressDialog.setMessage("搜索中……");
		progressDialog.setCancelable(false);
		analyzeHandler.progressDialog = progressDialog;

		setToolbar(toolbar);

		CrashHandler.getInstance(MainActivity.this)
				.sendException(new CatchExceptionListener()
				{
					@Override
					public void onException(String date, File file, String appVersionName, int appVersionCode, String androidVersion, int sdk, String vendor, String model, Throwable throwable)
					{
						Error error = new Error(date, appVersionName, appVersionCode, androidVersion, sdk, vendor, model, throwable);
						Bundle bundle = new Bundle();
						bundle.putSerializable("file", file);
						bundle.putSerializable("error", error);
						Intent intent = new Intent(MainActivity.this, ErrorActivity.class);
						intent.putExtra("error", bundle);
						startActivity(intent);
					}
				});

		showcase();
	}

	private void showcase()
	{
		if (settings.isFirstRun())
		{
			TapTargetView.showFor(this,
					TapTarget.forView(main_fab_upload, "点击这个按钮上传动漫截图。")
							.tintTarget(false),
					new TapTargetView.Listener()
					{
						@Override
						public void onTargetDismissed(TapTargetView view, boolean userInitiated)
						{
							settings.setFirstRun();
						}
					});
		}
	}

	private void monitor()
	{
		main_fab_upload.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View p1)
			{
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(intent, REQUEST_CODE);
			}
		});
	}

	private void requestPermission()
	{
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED)
		{
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
					WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
		}
	}

	private void setToolbar(Toolbar toolbar)
	{
		toolbar.setTitle(getTitle());
		toolbar.inflateMenu(R.menu.menu_main);
		toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener()
		{
			@Override
			public boolean onMenuItemClick(MenuItem item)
			{
				switch (item.getItemId())
				{
					case R.id.action_history:
						startActivity(new Intent(MainActivity.this, HistoryActivity.class));
						break;
					case R.id.action_settings:
						startActivity(new Intent(MainActivity.this, SettingsActivity.class));
						break;
				}
				return true;
			}
		});
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED)
		{
			Log.i(TAG, "onRequestPermissionsResult: 获得权限");
		} else
		{
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		if (REQUEST_CODE == requestCode && RESULT_OK == resultCode)
		{
			final Uri uri = data.getData();
			progressDialog.show();
			final String path = FileUtil.getPath(MainActivity.this, uri);
			adapter.setImgPath(path);
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					String url = "";
					try
					{
						url = new String(Base64.decode(getString(R.string.token)));
					} catch (Base64DecoderException e)
					{
						e.printStackTrace();
					}
					if (url.equals(""))
					{
						analyzeHandler.sendEmptyMessage(0);
						return;
					}
					WhatAnimeBuilder builder = new WhatAnimeBuilder();
					builder.setImgFile(path);
					builder.build(MainActivity.this, getString(R.string.requestUrl, url), analyzeHandler.list, new WhatAnimeBuildListener()
					{
						@Override
						public void done()
						{
							analyzeHandler.sendEmptyMessage(0);
						}

						@Override
						public void error(Exception e)
						{
							Log.wtf(TAG, "error: ", e);
							analyzeHandler.sendEmptyMessage(1);
						}
					});
				}
			}).start();
		}
	}
}
