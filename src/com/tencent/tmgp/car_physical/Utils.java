package com.tencent.tmgp.car_physical;

import android.widget.Toast;


import com.tencent.msdk.api.WGPlatform;
import com.tencent.msdk.consts.EPlatform;
import com.tencent.msdk.remote.api.PersonInfo;
import com.tencent.msdk.tools.Logger;
import com.unity3d.player.UnityPlayer;

public class Utils {
	public final static String title = "全名漂移";
	public final static String desc = "来这里漂移，飘到手抽筋，都不舍得放下手机";
	public final static String imgUrl = "http://ws.cdn.yaowan.com/mini/tencent-share.png";
	public final static String urlFormatOpenId = "http://gamecenter.qq.com/gcjump?appid=1000000640&pf=invite&from=androidqq&plat=qq&originuin=%s&ADTAG=gameobj.msg_heart";
	
    public static void ToastWithMsg(String vMsg) {
        String platStr = "(" + WGPlatform.WGGetVersion();
        if (MainActivity.platform == EPlatform.ePlatform_QQ) {
            platStr += "QQ游戏中心";
        } else if (MainActivity.platform == EPlatform.ePlatform_Weixin) {
            platStr += "微信";
        } else{
        	platStr += "Platform None";
        }
        
        platStr += ")" + vMsg;
        Logger.d("MsdkStat", platStr);
        ////////////////////////////////////////////////////////////////////
        if(MainActivity.isExecutedInEclipse){
        	Toast.makeText(MainActivity.Singleton, platStr, Toast.LENGTH_LONG).show();
        	if(MainActivity.m_TextView != null) MainActivity.m_TextView.setText(platStr);
        }else{
        	/*
        	final String aPlatStr = platStr;
        	Runnable r = new Runnable(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Toast.makeText(MainActivity.Singleton, aPlatStr, Toast.LENGTH_LONG).show();
				}
        	};
        	MainActivity.Singleton.runOnUiThread(r);
        	*/
            SendMsgToUnity("U3DThirdPartyCommunicate_DebugLog", platStr);
        }
        ////////////////////////////////////////////////////////////////////
    }
    
    public static void SendMsgToUnity(String vMethodName, String vMsg){
    	if(!MainActivity.isExecutedInEclipse){
    		UnityPlayer.UnitySendMessage("SDK", vMethodName, vMsg);
    	}
    }
    
	static String KSPERATOR =  "|||";
	static String KSPERATORArray = "---";
    public static void SendMsgToUnity(String vMethodName,String... vMsg){
    	String tmpStr = "";
    	for(int i = 0; i< vMsg.length; i++){
    		if(i != vMsg.length - 1)
    			tmpStr += vMsg[i] + KSPERATOR;
    		else if(i == vMsg.length - 1){
    			tmpStr += vMsg[i];
    		}
    	}
    	SendMsgToUnity(vMethodName, tmpStr);
    }
    
    public static String GetPersonInfoFormat(PersonInfo vPersonInfo, boolean debug){
        
        String out = "";
        out += /*KSPERATOR +*/ ValidateString(vPersonInfo.nickName) ;
        out += KSPERATOR + ValidateString(vPersonInfo.openId) ;
        out += KSPERATOR + ValidateString(vPersonInfo.gender) ;
        out += KSPERATOR + ValidateString(vPersonInfo.pictureSmall) ;
        out += KSPERATOR + ValidateString(vPersonInfo.pictureMiddle) ;
        out += KSPERATOR + ValidateString(vPersonInfo.pictureLarge) ;
        
        //bool
        out  = out + KSPERATOR + ((vPersonInfo.isFriend == true) ? "true" : "false") ;
        

        out = out + KSPERATOR + vPersonInfo.distance;
        
        out += KSPERATOR + ValidateString(vPersonInfo.lang) ;
        out += KSPERATOR + ValidateString(vPersonInfo.country) ;
        
        if(debug)
        	ToastWithMsg(out);

        return out;
    }

    public static String GetPersonInfoFormatArray(PersonInfo[] vPersonInfoArray,  boolean debug){
    	int size = vPersonInfoArray.length;
        String out = "";
        for (int i = 0; i < size; ++i) {
        	PersonInfo personInfo = vPersonInfoArray[i];
            if (i == (size - 1)) {
                out += GetPersonInfoFormat(personInfo, false);
            }else{
                out += GetPersonInfoFormat(personInfo, false) + KSPERATORArray;
            }
        }
        
        if (debug)
        	ToastWithMsg(out);
 
        return out;
    }

    static String ValidateString(String vStr){
        if (vStr.length() == 0) {
            return "NULL";
        } else {
            return vStr;
        }
    }

}
