package autoActivity.tools;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Properties;

public class LoadPropertiesUtil {

    /**
     * 加载文件
     * @param fileName 为项目根路径下路径
     * @return
     */
    public static Properties loadProperties(String fileName) {
        Properties properties = new Properties();
        InputStream is = null;
        try {
            String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
            String url = path + fileName;
            url = URLDecoder.decode(url, "UTF-8");
            System.out.print("正在读取："+url);
            is = new FileInputStream(url);
            properties.load(is);
        } catch (IOException e) {
        }finally {
            if(is != null){
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return properties;
    }

    public static String getKey(String fileName, String key){
        String result = null;
        try {
            Properties properties = loadProperties(fileName);
            System.out.println(" 字段："+key);
            result = (String) properties.get(key);
        } catch (Exception e) {
        	System.out.println("配置文件错误");
        }
        return result;
    }

}
