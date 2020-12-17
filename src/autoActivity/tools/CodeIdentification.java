package autoActivity.tools;

import java.awt.image.BufferedImage;

import org.json.JSONObject;

/**
 * 验证码识别工具类
 * @author xz8090
 *
 */
public class CodeIdentification {
	//是否使用超级鹰打码平台
	private static boolean isChaoJiYing = LoadPropertiesUtil.getKey("info.properties", "chaojiyingPWD")==null||LoadPropertiesUtil.getKey("info.properties", "chaojiyingPWD").isEmpty()
    		?false:true;

	public static String getCodeFromBufferedImage(BufferedImage img) {
		try {
			if(isChaoJiYing) {
				JSONObject obj=new JSONObject(ChaoJiYing.bytes2Object(ChaoJiYing.imageToBytes(img,"png")));
		        String codeResult=obj.getString("pic_str").toUpperCase();
		        return codeResult;
			}
		}catch(Exception e) {
			return "";
		}
		return "";
	}
}
