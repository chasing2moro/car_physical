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

	//private static String LANG = "java";// �������� java cpp
	//private static String GAME = "demo";// ��Ϸ�� demo peng
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
	     // MSDKSample�ô���, ��Ϸ�����м�������Ƿ��ظ�, ��⵽���ظ���Activity��Ҫ���Լ�finish��
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
         *  TODO GAME �������Ҫ���� baseInfoֵ����Ϸ���죬��д��ע������˵����  *
         *  baseInfoֵ��Ϸ��д���󽫵��� QQ��΢�ŵķ�����¼ʧ�� ���м� ������        *
         * 	ֻ�ӵ�һƽ̨����Ϸ����������д����ƽ̨����Ϣ������ᵼ�²��ֹ����ȡʧ��  *
         ***********************************************************/
        MsdkBaseInfo baseInfo = new MsdkBaseInfo();
        baseInfo.qqAppId = "1104219288";
        baseInfo.qqAppKey = "KJfmi8nItb8z7o7O";
        baseInfo.wxAppId = "wx9cde061332c0a7ba";
        baseInfo.wxAppKey = "efd7e42ed79b1116fb9ddc29243a1ad5";
        //�����Ͳ�����offerId
        baseInfo.offerId = "1104219288"; 
        //��ͨ���²�����offerId
        // baseInfo.offerId = "1104219288"; 
        
                                      
		WGPlatform.Initialized(this, baseInfo);
		WGPlatform.WGSetPermission(WGQZonePermissions.eOPEN_ALL); // ��������QQʱ����Ҫ�û���Ȩ����
        WGPlatform.WGSetObserver(new MsdkCallback());
        
		if (WGPlatform.wakeUpFromHall(this.getIntent())) {
			Logger.d("LoginPlatform is Hall");
			// ����ƽ̨Ϊ����
		} else {
			// ����ƽ̨���Ǵ���
			Logger.d("LoginPlatform is not Hall");
		}

		WGPlatform.handleCallback(this.getIntent());
		
		InitMidas();
	}
	
    // TODO GAME ��Ϸ��Ҫ���ɴ˷���������WGPlatform.onPause()
    @Override
    protected void onPause() {
        super.onPause();
        WGPlatform.onPause();
        this.pauseTime = System.currentTimeMillis() / 1000;
    }

    // TODO GAME ��Ϸ��Ҫ���ɴ˷���������WGPlatform.onResume()
    @Override
    protected void onResume() {
        super.onResume();
        WGPlatform.onResume();

        if(m_LauchSDK){
	        // ��Ϸ���������߼��жϾ����Ƿ�Ҫ������֤Ʊ��, ����onResume�ᱻƵ���ĵ��õ�, �����ʱ��������Ϸ�����������ȷ��
	        if (pauseTime != 0 && System.currentTimeMillis() / 1000 - pauseTime > 1800) {
	            Logger.d("MsdkStat", "start auto login");
	            // ģ����Ϸ�Զ���¼ START
	            WGPlatform.WGLoginWithLocalInfo();
	            // ģ����Ϸ�Զ���¼ END
	        } else {
	            Logger.d("MsdkStat", "do not start auto login");
	        }
        }
    }

    // TODO GAME ��Ϸ��Ҫ���ɴ˷���������WGPlatform.onDestory()
    @Override
    protected void onDestroy() {
        super.onDestroy();
        WGPlatform.onDestory(this);

        Logger.d("onDestroy");
    }

    // TODO GAME ��onNewIntent����Ҫ����handleCallback��ƽ̨���������ݽ���MSDK����
    @Override
    protected void onNewIntent(Intent intent) {
        Logger.d("onNewIntent");
        super.onNewIntent(intent);
        if (WGPlatform.wakeUpFromHall(intent)) {
        	// ����ƽ̨�Ǵ���
            Logger.d("MsdkStat", "LoginPlatform is Hall");
        } else {
			// ����ƽ̨���Ǵ���
			Logger.d("MsdkStat", "LoginPlatform is not Hall");
        }
        WGPlatform.handleCallback(intent);
    }
    
	// ��Ϸ����Ҫ���, ����MSDKSample���õ�
	static {
		System.loadLibrary("NativeRQD"); // ��Ϸ��Ҫ���ش˶�̬��, �����ϱ���
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
    	
        // QQ��½��ť, ����Ժ����WGLogin����QQ��½
    	login_by_qq_btn = (Button) findViewById(R.id.button_qq_login);
        login_by_qq_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	QQLogin();
            }
        });

        // ΢�ŵ�½��ť, ����Ժ����WGLogin����QQ��½
        login_by_wx_btn = (Button) findViewById(R.id.button_weixin_login);
        login_by_wx_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	WeiXinLogin();
            }
        });
        
        // ע��
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
    
    //QQ��½
	public void QQLogin(){
		WGPlatform.WGLogin(EPlatform.ePlatform_QQ);
		m_LauchSDK = true;
	}
	
	//΢�ŵ�½
	public void WeiXinLogin(){
		WGPlatform.WGLogin(EPlatform.ePlatform_Weixin);
		m_LauchSDK = true;
	}
	
	//QQ��΢��  ��LocalInfo ��½
	public void LoginWithLocalInfo(){
		Logger.d("MsdkStat", "WGLoginWithLocalInfo");
		WGPlatform.WGLoginWithLocalInfo();
		m_LauchSDK = true;
	}
	
	public void Login(){
		WGPlatform.WGLogin(MainActivity.platform);
	}
	//Logout
	//Boolean is class��  boolean 32bit built-in type
	public boolean Logout(){
		boolean success = WGPlatform.WGLogout();
		if(success) Utils.ToastWithMsg("Logout");
		return success;
	}
	
	//��ȡ ������Ϣ
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
	
	//ȥ΢������
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
	
	//ȥ�ռ�����
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
	

	 class MsdkCallback implements WGPlatformObserver { // ��Ϸ��Ҫ�����Լ����߼�ʵ���Լ���MsdkCallback����
	        @SuppressWarnings("unused")
	        public void OnLoginNotify(LoginRet ret) {
	            // game todo
	            //toastCallbackInfo(ret.platform, "��¼", ret.flag, ret.desc);
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
	                    // ��½�ɹ�, ��ȡ����Ʊ��
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
	                    // ��½ʧ�ܴ���
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
	           Utils.ToastWithMsg(platform + " ����" + " flag:" +  ret.flag + " desc:" + ret.desc);
	            Utils.SendMsgToUnity("U3DTencentSDKAppCommunicate_ReturnShareResult",
	            		String.valueOf(ret.platform),
	            		String.valueOf(ret.flag),
	            		ret.desc);
	            switch (ret.flag) {
	                case CallbackFlag.eFlag_Succ:
	                    // ����ɹ�
	                    MainActivity.platform = EPlatform.values()[ret.platform];
	                    break;
	                case CallbackFlag.eFlag_QQ_UserCancel:
	                case CallbackFlag.eFlag_QQ_NetworkErr:
	                    // ����ʧ�ܴ���
	                    Logger.d(ret.desc);
	                    break;
	                case CallbackFlag.eFlag_WX_UserCancel:
	                case CallbackFlag.eFlag_WX_NotInstall:
	                case CallbackFlag.eFlag_WX_NotSupportApi:
	                    // ����ʧ�ܴ���
	                    Logger.d(ret.desc);
	                    break;
	                default:
	                    break;
	            }
	        }

	        //������Ӧ������
	        public void OnWakeupNotify(WakeupRet ret) {
	            // game todo
	          //  toastCallbackInfo(ret.platform, "����", ret.flag, ret.desc);

	        	Logger.d("OnWakeupNotify flag:" + ret.flag + " des:" + ret.desc);
	            MainActivity.platform = EPlatform.values()[ret.platform];
	         // TODO GAME �������Ӵ������˺ŵ��߼�
	            if (CallbackFlag.eFlag_Succ == ret.flag
	             || CallbackFlag.eFlag_UrlLogin == ret.flag
	             || CallbackFlag.eFlag_AccountRefresh == ret.flag) {
	                Utils.ToastWithMsg("����ɹ�");
	          //      runOnUiThread(new Runnable() {
	          //          @Override
	          //          public void run() {
	             //       }
	            //    });
	            } else if (ret.flag == CallbackFlag.eFlag_NeedSelectAccount) {
	                Logger.d("diff account");
	                Utils.ToastWithMsg("����ʧ�ܣ��˺Ų�һ��");
	            } else if (ret.flag == CallbackFlag.eFlag_NeedLogin) {
	            	Utils.ToastWithMsg("����ʧ�ܣ�û����Ч��Ʊ�ݣ���Ҫ���µ�¼");
	               // letUserLogout();
	            } else {
	            	Utils.ToastWithMsg("����ʧ�ܣ����ע��");
	            }
	        }

	        


	        @Override
	        public void OnRelationNotify(RelationRet relationRet) {
	            int count = relationRet.persons.size();
	          //  Utils.ToastWithMsg("OnRelationNotify Size��" + count + " relationRet.flag:" + relationRet.flag);
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
	            // �˴���Ϸ����crashʱ�ϱ��Ķ�����Ϣ
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
			//APMidasPayAPI.ENV_RELEASEΪ��������
			//APMidasPayAPI.ENV_TESTΪtest����
			env = APMidasPayAPI.ENV_TEST;

			//��ʼ����
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
			Log. d("MSDK_PAY", "֧��sdk�ص�Ӧ��" );
			Utils.ToastWithMsg("֧��sdk�ص�Ӧ�� resultCode:" + response.resultCode 
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
			 Log. d("MSDK_PAY", "��¼Ʊ�ݹ��ڣ������µ�¼" );
			 Utils.ToastWithMsg("��¼Ʊ�ݹ��ڣ������µ�¼");
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
