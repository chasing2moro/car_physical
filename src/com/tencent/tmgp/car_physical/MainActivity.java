package com.tencent.tmgp.car_physical;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import com.tencent.midas.api.APMidasPayAPI;
import com.tencent.midas.api.APMidasResponse;
import com.tencent.midas.api.IAPMidasNetCallBack;
import com.tencent.midas.api.IAPMidasPayCallBack;
import com.tencent.midas.api.request.APMidasGameRequest;
import com.tencent.midas.api.request.APMidasNetRequest;
import com.tencent.msdk.api.CallbackRet;
import com.tencent.msdk.api.LocationRet;
import com.tencent.msdk.api.LoginRet;
import com.tencent.msdk.api.MsdkBaseInfo;
import com.tencent.msdk.api.ShareRet;
import com.tencent.msdk.api.TokenRet;
import com.tencent.msdk.api.WGPlatform;
import com.tencent.msdk.api.WGPlatformObserver;
import com.tencent.msdk.api.WGQZonePermissions;
import com.tencent.msdk.api.WakeupRet;
import com.tencent.msdk.api.eQQScene;
import com.tencent.msdk.api.eWechatScene;
import com.tencent.msdk.consts.CallbackFlag;
import com.tencent.msdk.consts.EPlatform;
import com.tencent.msdk.consts.TokenType;
import com.tencent.msdk.qq.ApiName;
import com.tencent.msdk.remote.api.PersonInfo;
import com.tencent.msdk.remote.api.RelationRet;
import com.tencent.msdk.tools.Logger;

import com.tencent.tmgp.car_physical.R;
import com.unity3d.player.UnityPlayerActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends UnityPlayerActivity implements  IAPMidasPayCallBack{
public static boolean isExecutedInEclipse = false;
//public class MainActivity extends Activity implements  IAPMidasPayCallBack {
//	public static boolean isExecutedInEclipse = true;

	//private static String LANG = "java";// 开发语言 java cpp
	//private static String GAME = "demo";// 游戏包 demo peng
	//private ProgressDialog mAutoLoginWaitingDlg;
  //  private Handler mhandler;
   // private String TAG = "WeGame";
   // private ProgressBar mMyappDldProcess;
  //  private RelativeLayout mDldMyApp;
   // private ProgressBar mGameUpdateProcess;
    //private RelativeLayout mUpdateGame;
    //private Dialog mUpdateDlg;
    
    private long pauseTime = System.currentTimeMillis() / 10000;
    private boolean m_LauchSDK = false;
    public static EPlatform platform = EPlatform.ePlatform_None;
    public static MainActivity Singleton;
    
    public String openId ;
    public String pf;
    public String pfKey;
    public String accessToken;
    public String payToken;
    public String refreshToken;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Singleton = this;
		
/////////////////////////////////////////////////////////////////////
		if(isExecutedInEclipse){
			setContentView(R.layout.activity_main);
			if (savedInstanceState == null) {
				getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
			}
			this.initListener();
		}

/////////////////////////////////////////////////////////////////////
		//Keep Screen On
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	     // MSDKSample用代码, 游戏需自行检测自身是否重复, 检测到吃重复的Activity则要把自己finish掉
        if (WGPlatform.IsDifferentActivity(this)) {
        	WGPlatform.DestroyActivity();//yw add
        	Utils.ToastWithMsg("Warning!Reduplicate game activity was detected.Activity will finish immediately.");
           // this.finish();//yw comment
          //  return;//yw comment
        }
  
      //  Looper looper = Looper.myLooper();
      //  mhandler = new Handler(looper);
      //  Logger.d("onCreate");
      //  mhandler.post(new Runnable() {
      //     @Override
      //    public void run() {
      //     	//do your cost time action
      //     }
      //  });


        /***********************************************************
         *  TODO GAME 接入必须要看， baseInfo值因游戏而异，填写请注意以下说明：  *
         *  baseInfo值游戏填写错误将导致 QQ、微信的分享，登录失败 ，切记 ！！！        *
         * 	只接单一平台的游戏请勿随意填写其余平台的信息，否则会导致部分公告获取失败  *
         ***********************************************************/
        MsdkBaseInfo baseInfo = new MsdkBaseInfo();
        baseInfo.qqAppId = "1104219288";
        baseInfo.qqAppKey = "KJfmi8nItb8z7o7O";
        baseInfo.wxAppId = "wx9cde061332c0a7ba";
        baseInfo.wxAppKey = "efd7e42ed79b1116fb9ddc29243a1ad5";
        //订阅型测试用offerId
        baseInfo.offerId = "1104219288"; 
        //开通包月测试用offerId
        // baseInfo.offerId = "1104219288"; 
        
                                      
		WGPlatform.Initialized(this, baseInfo);
		WGPlatform.WGSetPermission(WGQZonePermissions.eOPEN_ALL); // 设置拉起QQ时候需要用户授权的项
        WGPlatform.WGSetObserver(new MsdkCallback());
        
		if (WGPlatform.wakeUpFromHall(this.getIntent())) {
			Logger.d("LoginPlatform is Hall");
			// 拉起平台为大厅
		} else {
			// 拉起平台不是大厅
			Logger.d("LoginPlatform is not Hall");
		}

		WGPlatform.handleCallback(this.getIntent());
		
		InitMidas();
	}
	
    // TODO GAME 游戏需要集成此方法并调用WGPlatform.onPause()
    @Override
    protected void onPause() {
        super.onPause();
        WGPlatform.onPause();
        this.pauseTime = System.currentTimeMillis() / 1000;
    }

    // TODO GAME 游戏需要集成此方法并调用WGPlatform.onResume()
    @Override
    protected void onResume() {
        super.onResume();
        WGPlatform.onResume();

        if(m_LauchSDK){
	        // 游戏根据自身逻辑判断决定是否要重新验证票据, 由于onResume会被频繁的调用到, 这里的时间间隔由游戏根据自身情况确定
	        if (pauseTime != 0 && System.currentTimeMillis() / 1000 - pauseTime > 1800) {
	            Logger.d("MsdkStat", "start auto login");
	            // 模拟游戏自动登录 START
	            WGPlatform.WGLoginWithLocalInfo();
	            // 模拟游戏自动登录 END
	        } else {
	            Logger.d("MsdkStat", "do not start auto login");
	        }
        }
    }

    // TODO GAME 游戏需要集成此方法并调用WGPlatform.onDestory()
    @Override
    protected void onDestroy() {
        super.onDestroy();
        WGPlatform.onDestory(this);

        Logger.d("onDestroy");
    }

    // TODO GAME 在onNewIntent中需要调用handleCallback将平台带来的数据交给MSDK处理
    @Override
    protected void onNewIntent(Intent intent) {
        Logger.d("onNewIntent");
        super.onNewIntent(intent);
        if (WGPlatform.wakeUpFromHall(intent)) {
        	// 拉起平台是大厅
            Logger.d("MsdkStat", "LoginPlatform is Hall");
        } else {
			// 拉起平台不是大厅
			Logger.d("MsdkStat", "LoginPlatform is not Hall");
        }
        WGPlatform.handleCallback(intent);
    }
    
	// 游戏不需要这个, 这是MSDKSample自用的
	static {
		System.loadLibrary("NativeRQD"); // 游戏需要加载此动态库, 数据上报用
	}
	
    public static TextView m_TextView = null;
/////////////////////////////////////////////////////////////////////
    
    Button login_by_qq_btn;
    Button login_by_wx_btn;
    Button logout_btn;
    Button login_by_local_btn;
    Button get_login_info_btn;
    Button invite_btn;
    Button invite_zoon_btn;
    Button query_friends_btn;
    Button query_me_btn;
    Button pay_btn;
	private void initListener() {
    	m_TextView = (TextView) findViewById(R.id.textView1);
    	
        // QQ登陆按钮, 点击以后调用WGLogin进行QQ登陆
    	login_by_qq_btn = (Button) findViewById(R.id.button_qq_login);
        login_by_qq_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	QQLogin();
            }
        });

        // 微信登陆按钮, 点击以后调用WGLogin进行QQ登陆
        login_by_wx_btn = (Button) findViewById(R.id.button_weixin_login);
        login_by_wx_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	WeiXinLogin();
            }
        });
        
        // 注销
        logout_btn = (Button) findViewById(R.id.button_logout);
        logout_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	Logout();
            }
        });
        
        //login with local info
        login_by_local_btn = (Button) findViewById(R.id.button_login_with_local);
        login_by_local_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	LoginWithLocalInfo();
            }
        });
        
        //login with local info
        get_login_info_btn = (Button) findViewById(R.id.button_get_login_info);
        get_login_info_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	GetLoginInfo();
            }
        });
        
        invite_btn= (Button) findViewById(R.id.button_invite);
        invite_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	InviteToFriend();
            }
        });
        
        invite_zoon_btn = (Button) findViewById(R.id.button_invite_zone);
        invite_zoon_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	InviteToZone();
            }
        });
        
        query_friends_btn = (Button) findViewById(R.id.button_query_friends);
        query_friends_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	QueryFriends();
            }
        });
        
        query_me_btn = (Button) findViewById(R.id.button_query_me);
        query_me_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	QueryMe();
            }
        });
        
        pay_btn = (Button) findViewById(R.id.button_pay);
        pay_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	PayGameWithSaveNumber(16);
            }
        });
    }
    
/////////////////////////////////////////////////////////////////////
    
    //QQ登陆
	public void QQLogin(){
		WGPlatform.WGLogin(EPlatform.ePlatform_QQ);
		m_LauchSDK = true;
	}
	
	//微信登陆
	public void WeiXinLogin(){
		WGPlatform.WGLogin(EPlatform.ePlatform_Weixin);
		m_LauchSDK = true;
	}
	
	//QQ、微信  用LocalInfo 登陆
	public void LoginWithLocalInfo(){
		Logger.d("MsdkStat", "WGLoginWithLocalInfo");
		WGPlatform.WGLoginWithLocalInfo();
		m_LauchSDK = true;
	}
	
	public void Login(){
		WGPlatform.WGLogin(MainActivity.platform);
	}
	//Logout
	//Boolean is class。  boolean 32bit built-in type
	public boolean Logout(){
		boolean success = WGPlatform.WGLogout();
		if(success) Utils.ToastWithMsg("Logout");
		return success;
	}
	
	//获取 本地信息
	public LoginRet GetLoginInfo(){
		LoginRet ret = new LoginRet();
		WGPlatform.WGGetLoginRecord(ret);

		EPlatform aPlatform = EPlatform.values()[ret.platform];
		String tmp = "GetLoginInfo:" + aPlatform + " flag:" + ret.flag + " des:" + ret.desc;
		Utils.ToastWithMsg(tmp);
		return ret;
	}
	public int GetLastPlatform(){
		LoginRet ret = GetLoginInfo();
		return ret.platform;
	}
	
	//去微信邀请
	public void InviteToFriend() {
		final String title = Utils.title;
		final String desc = Utils.desc;
		
		final String imgUrl = Utils.imgUrl;
	    if (platform == EPlatform.ePlatform_QQ){
			String url = String.format(Utils.urlFormatOpenId, openId);
		    int imgUrlLen = imgUrl.length();
	    	WGPlatform.WGSendToQQ(eQQScene.QQScene_Session,
	    			title,
	    			desc,
	    			url,
	    			imgUrl,
	    			imgUrlLen);
	    } else {
	    	byte[] thumbImgData = null;
	    	try {
				thumbImgData = new DownloadFileAsyncTask().execute(imgUrl).get();
			} catch (InterruptedException e) {e.printStackTrace();} catch (ExecutionException e) {e.printStackTrace();}
	    	final int maxLen = 3000;
	    	byte[] TrueThumbImgData = null;
	    	if(thumbImgData.length > maxLen){
	    		TrueThumbImgData = new byte[maxLen];
	    		for(int i = 0; i < maxLen; i++){
	    			TrueThumbImgData[i] = thumbImgData[i];
	    		}
	    	}else{
	    		TrueThumbImgData = thumbImgData;
	    	}
	    	//Utils.ToastWithMsg("origin length:" + thumbImgData.length + " clamp length" + TrueThumbImgData.length);
  	    	WGPlatform.WGSendToWeixin(title, 
	    			desc,
	    			"MSG_INVITE",
	    			TrueThumbImgData, 
	    			TrueThumbImgData.length, 
	    			"messageExt");
	    }
	   
	}
	
	//去空间邀请
	public void InviteToZone() {
		final String title = Utils.title;
		final String desc = Utils.desc;
		
		final String imgUrl = Utils.imgUrl;
	    if (platform == EPlatform.ePlatform_QQ){
			String url = String.format(Utils.urlFormatOpenId, openId);
		    int imgUrlLen = imgUrl.length();
	    	WGPlatform.WGSendToQQ(eQQScene.QQScene_QZone,
	    			title,
	    			desc,
	    			url,
	    			imgUrl,
	    			imgUrlLen);
	    } else {
	    	byte[] thumbImgData = null;
	    	try {
				thumbImgData = new DownloadFileAsyncTask().execute(imgUrl).get();
			} catch (InterruptedException e) {e.printStackTrace();} catch (ExecutionException e) {e.printStackTrace();}

	    	//Utils.ToastWithMsg("origin length:" + thumbImgData.length);
  	    	WGPlatform.WGSendToWeixinWithPhoto(eWechatScene.WechatScene_Timeline,
  	    			"MSG_INVITE",
  	    			thumbImgData, 
  	    			thumbImgData.length,
  	    			"ExtMsg",
  	    			"WECHAT_SNS_JUMP_APP");
	    }
	}
	
	public void QueryFriends(){
	    if (platform == EPlatform.ePlatform_QQ){
	    	WGPlatform.WGQueryQQGameFriendsInfo();
	    } else {
	    	WGPlatform.WGQueryWXGameFriendsInfo();
	    }
	}
	
	public void QueryMe(){
	    if (platform == EPlatform.ePlatform_QQ){
	    	WGPlatform.WGQueryQQMyInfo();
	    } else {
	    	WGPlatform.WGQueryWXMyInfo();
	    }
	}
	

	 class MsdkCallback implements WGPlatformObserver { // 游戏需要根据自己的逻辑实现自己的MsdkCallback对象
	        @SuppressWarnings("unused")
	        public void OnLoginNotify(LoginRet ret) {
	            // game todo
	            //toastCallbackInfo(ret.platform, "登录", ret.flag, ret.desc);
	        	MainActivity.platform = EPlatform.values()[ret.platform];
	        	String tmp = "OnLoginNotify:" + MainActivity.platform + " CallbackFlag:" + ret.flag + " des:" + ret.desc;
	            

	             openId = "NULL";
	             pf = "NULL";
	             pfKey = "NULL";
	             accessToken = "NULL";
	             payToken = "NULL";
	             refreshToken = "NULL";
	            switch (ret.flag) {
	                case CallbackFlag.eFlag_Succ:
	                    //stopWaiting();
	                    // 登陆成功, 读取各种票据
	                    openId = ret.open_id;
	                    pf = ret.pf;
	                    pfKey = ret.pf_key;
	                    long wxAccessTokenExpire = 0;
	                    long wxRefreshTokenExpire = 0;
	                    for (TokenRet tr : ret.token) {
	                    	switch (tr.type) {
	                    	case TokenType.eToken_QQ_Access:
	                    		accessToken = tr.value;
	                    		break;
	                    	case TokenType.eToken_QQ_Pay:
	                    		payToken = tr.value;
	                    		break;
	                    	case TokenType.eToken_WX_Access:
	                    		accessToken = tr.value;
	                    		wxAccessTokenExpire = tr.expiration;
	                    		break;
	                    	case TokenType.eToken_WX_Refresh:
	                    		refreshToken = tr.value;
	                    		wxRefreshTokenExpire = tr.expiration;
	                    		break;
	                    	default:
	                    		break;
	                    	}
	                    }
	                  //  letUserLogin();
	                    break;
	                case CallbackFlag.eFlag_WX_NotInstall:
	                	
	                	break;
	                case CallbackFlag.eFlag_WX_NotSupportApi:
	                	
	                	break;
	                case CallbackFlag.eFlag_WX_UserCancel:
	                case CallbackFlag.eFlag_WX_LoginFail:
	                    // 登陆失败处理
	                    break;
	                case CallbackFlag.eFlag_Local_Invalid:
	                    // Login with local info fail
	                	Login();
	                default:
	                    break;
	            }
	            Utils.ToastWithMsg(tmp + " refreshToken:" + refreshToken);
	            Utils.SendMsgToUnity("U3DTencentSDKAppCommunicate_ReturnLoginResult",
	            		String.valueOf(ret.platform),
	            		String.valueOf(ret.flag),
	            		ret.desc,
	    	            openId,
	    	            pf,
	    	            pfKey,
	    	            accessToken,
	    	            payToken,
	    	            refreshToken
	            		);
	        }

	        public void OnShareNotify(ShareRet ret) {
	            // game todo
	        	MainActivity.platform = EPlatform.values()[ret.platform];
	           Utils.ToastWithMsg(platform + " 分享" + " flag:" +  ret.flag + " desc:" + ret.desc);
	            Utils.SendMsgToUnity("U3DTencentSDKAppCommunicate_ReturnShareResult",
	            		String.valueOf(ret.platform),
	            		String.valueOf(ret.flag),
	            		ret.desc);
	            switch (ret.flag) {
	                case CallbackFlag.eFlag_Succ:
	                    // 分享成功
	                    MainActivity.platform = EPlatform.values()[ret.platform];
	                    break;
	                case CallbackFlag.eFlag_QQ_UserCancel:
	                case CallbackFlag.eFlag_QQ_NetworkErr:
	                    // 分享失败处理
	                    Logger.d(ret.desc);
	                    break;
	                case CallbackFlag.eFlag_WX_UserCancel:
	                case CallbackFlag.eFlag_WX_NotInstall:
	                case CallbackFlag.eFlag_WX_NotSupportApi:
	                    // 分享失败处理
	                    Logger.d(ret.desc);
	                    break;
	                default:
	                    break;
	            }
	        }

	        //被其他应用拉起
	        public void OnWakeupNotify(WakeupRet ret) {
	            // game todo
	          //  toastCallbackInfo(ret.platform, "拉起", ret.flag, ret.desc);

	        	Logger.d("OnWakeupNotify flag:" + ret.flag + " des:" + ret.desc);
	            MainActivity.platform = EPlatform.values()[ret.platform];
	         // TODO GAME 这里增加处理异账号的逻辑
	            if (CallbackFlag.eFlag_Succ == ret.flag
	             || CallbackFlag.eFlag_UrlLogin == ret.flag
	             || CallbackFlag.eFlag_AccountRefresh == ret.flag) {
	                Utils.ToastWithMsg("拉起成功");
	          //      runOnUiThread(new Runnable() {
	          //          @Override
	          //          public void run() {
	             //       }
	            //    });
	            } else if (ret.flag == CallbackFlag.eFlag_NeedSelectAccount) {
	                Logger.d("diff account");
	                Utils.ToastWithMsg("拉起失败，账号不一致");
	            } else if (ret.flag == CallbackFlag.eFlag_NeedLogin) {
	            	Utils.ToastWithMsg("拉起失败，没有有效的票据，需要重新登录");
	               // letUserLogout();
	            } else {
	            	Utils.ToastWithMsg("拉起失败，最好注销");
	            }
	        }

	        


	        @Override
	        public void OnRelationNotify(RelationRet relationRet) {
	            int count = relationRet.persons.size();
	          //  Utils.ToastWithMsg("OnRelationNotify Size：" + count + " relationRet.flag:" + relationRet.flag);
	            String outPut;
	            if (count == 1) {
	               PersonInfo logInfo = relationRet.persons.get(0);
	                outPut = Utils.GetPersonInfoFormat(logInfo, true);
	            }else if(count > 1){
	                outPut = Utils.GetPersonInfoFormatArray((PersonInfo[])relationRet.persons.toArray(new PersonInfo[1]), true);
	            }else{
	            	outPut = "No Friend";
	            	Utils.ToastWithMsg("OnRelationNotify " + outPut );
	            	return;
	            }
	            
	           // Utils.ToastWithMsg("OnRelationNotify " + outPut );
	            Utils.SendMsgToUnity("U3DTencentSDKAppCommunicate_ReturnUserInfo", outPut);
	        }

	        @Override
	        public void OnLocationNotify(RelationRet relationRet) {
	            Logger.d(relationRet);
	        }

	        @Override
	        public void OnFeedbackNotify(int flag, String desc) {
	            Logger.d(String.format(Locale.CHINA, "flag: %d; desc: %s;", flag, desc));
	        }

	        @Override
	        public String OnCrashExtMessageNotify() {
	            // 此处游戏补充crash时上报的额外信息
	            Logger.d(String.format(Locale.CHINA, "OnCrashExtMessageNotify called"));
	            Date nowTime = new Date();
	            SimpleDateFormat time = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	            return "new Upload extra crashing message for rqd1.7.8 on " + time.format(nowTime);
	        }

			@Override
			public void OnLocationGotNotify(LocationRet arg0) {
				// TODO Auto-generated method stub
				
			}
	    }
	 
////////////////////////////////////////////////////////
	 //Midas
	 public String env = "";
	 void InitMidas(){
			//APMidasPayAPI.ENV_RELEASE为现网环境
			//APMidasPayAPI.ENV_TEST为test环境
			env = APMidasPayAPI.ENV_TEST;

			//初始化，
			APMidasPayAPI.init(this);
			APMidasPayAPI.setEnv(env);
			APMidasPayAPI.setLogEnable(true);
			Logger.d("Midas", "Midas Version:" + APMidasPayAPI.getMidasPluginVersion());
			
	 }
	 
	 //@vOpenKey QQ:payToken WX:accessToken
	 public void PayGameWithSaveNumberAndOthers(int vNum, String vOpenId, String vOpenKey, String vPf, String vPfKey){
			final APMidasGameRequest request = new APMidasGameRequest();
			request.offerId = "1104219288";
			request.openId = vOpenId;
			
			switch(platform){
			case ePlatform_QQ:
				request.openKey = vOpenKey;
				request.sessionId = "openid";
				request.sessionType = "kp_actoken";
				break;
			case ePlatform_Weixin:
				request.openKey = vOpenKey;
				request.sessionId = "hy_gameid";
				request.sessionType = "wc_actoken";
				break;
				default:
					break;
			}

			request.zoneId = "1";
			request.pf = vPf;
			request.pfKey = vPfKey;
			request.acctType = APMidasPayAPI.ACCOUNT_TYPE_COMMON;
			request.saveValue = String.valueOf(vNum);
			request.isCanChange = false;
			request.resId = R.drawable.sample_yuanbao;
			
        	

        	Runnable r = new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					APMidasPayAPI.launchPay(MainActivity.this, request, MainActivity.this);
				}
        	};
        	runOnUiThread(r);
        	
			
			//APMidasPayAPI.launchPay(this, request, this);
	 }
	 
	 public void PayGameWithSaveNumber(int vNum){
		 String tmpOpenKey;
			switch(platform){
			case ePlatform_QQ:
				tmpOpenKey = payToken;
				break;
			case ePlatform_Weixin:
				tmpOpenKey = accessToken;
				break;
				default:
				tmpOpenKey = payToken;
				break;
			}
			PayGameWithSaveNumberAndOthers(vNum,
				 openId,
				 tmpOpenKey,
				 pf,
				 pfKey);
 			/*APMidasGameRequest request = new APMidasGameRequest();
			request.offerId = "1104219288";
			request.openId = openId;
			
			switch(platform){
			case ePlatform_QQ:
				request.openKey = payToken;
				request.sessionId = "openid";
				request.sessionType = "kp_actoken";
				break;
			case ePlatform_Weixin:
				request.openKey = accessToken;
				request.sessionId = "hy_gameid";
				request.sessionType = "wc_actoken";
				break;
				default:
					break;
			}

			request.zoneId = "1";
			request.pf = pf;
			request.pfKey = pfKey;
			request.acctType = APMidasPayAPI.ACCOUNT_TYPE_COMMON;
			request.saveValue = String.valueOf(vNum);
			request.isCanChange = false;
			request.resId = R.drawable.sample_yuanbao;
			
			APMidasPayAPI.launchPay(this, request, this);*/
	 }
	 
		@Override
		public void MidasPayCallBack(APMidasResponse response) 
		{
			Log. d("MSDK_PAY", "支付sdk回调应用" );
			Utils.ToastWithMsg("支付sdk回调应用 resultCode:" + response.resultCode 
					+ " Msg:" + response.resultMsg 
					+ " payChannel:" + response.payChannel
					+ " payState:" + response.payState
					+ " provideState:" + response.provideState
					+ " saveNum:" + response.realSaveNum
					+ " response.resultInerCode:" + response.resultInerCode
					+ " extendInfo:" + response.extendInfo);
		}

		@Override
		public void MidasPayNeedLogin() 
		{
			 Log. d("MSDK_PAY", "登录票据过期，请重新登录" );
			 Utils.ToastWithMsg("登录票据过期，请重新登录");
		}
///////////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.button_invite_zone) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	 // A placeholder fragment containing a simple view.

	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}
	

}
