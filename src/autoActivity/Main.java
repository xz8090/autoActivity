package autoActivity;

import com.machinepublishers.jbrowserdriver.JBrowserDriver;
import com.machinepublishers.jbrowserdriver.Settings;
import com.machinepublishers.jbrowserdriver.Timezone;

import autoActivity.tools.ChaoJiYing;
import autoActivity.tools.CodeIdentification;
import autoActivity.tools.LoadPropertiesUtil;
import autoActivity.tools.MailUtils;

import org.json.JSONObject;
import org.openqa.selenium.*;

import javax.imageio.ImageIO;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

public class Main {

    private static JBrowserDriver driver = new JBrowserDriver(Settings.builder().javascript(true).ignoreDialogs(false).timezone(Timezone.AMERICA_NEWYORK).headless(true).loggerLevel(Level.OFF).build());
    private static String userId = LoadPropertiesUtil.getKey("info.properties", "userId");
    private static String userPass = LoadPropertiesUtil.getKey("info.properties", "userPass");
    private static String pwd = LoadPropertiesUtil.getKey("info.properties", "VPNPWD")==null||LoadPropertiesUtil.getKey("info.properties", "VPNPWD").isEmpty()
    		?"":LoadPropertiesUtil.getKey("info.properties", "VPNPWD");//VPN密码
    private static String email = LoadPropertiesUtil.getKey("info.properties", "email")==null||LoadPropertiesUtil.getKey("info.properties", "email").isEmpty()
    		?"":LoadPropertiesUtil.getKey("info.properties", "email");//接受报告的邮件
    
    private static Long count = 0L;
    public static SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static int loginMax = 3;//登录失败尝试登录次数
    

    public static void loginWebVPN() {
    	String logoutHttp = "https://w.buct.edu.cn/logout";
        driver.get(logoutHttp);
    	System.out.println("webvpn");
    	String loginHttp = "https://w.buct.edu.cn/login";//登录webvpn
        driver.get(loginHttp);
        //输入账号
        driver.findElementByXPath("//*[@id=\"user_name\"]").clear();
        driver.findElementByXPath("//*[@id=\"user_name\"]").sendKeys(userId);
        //输入密码
        driver.findElementByXPath("//*[@id=\"form\"]/div[3]/div/input").clear();
        driver.findElementByXPath("//*[@id=\"form\"]/div[3]/div/input").sendKeys(pwd);
        //点击登录
        driver.findElementByXPath("//*[@id=\"login\"]").click();
        driver.pageWait();
    }
    
    //n为登录次数
    public static boolean login(int n) throws Exception {
    	if(!pwd.isEmpty()) loginWebVPN();
        System.out.println("开始登录"+n);
        String loginHttp = "";//登录信息管理平台的地址
        if(!pwd.isEmpty()) {
        	//WebVPN的后台登录地址
        	long nowTime = Calendar.getInstance().getTimeInMillis();
        	loginHttp = "https://w.buct.edu.cn/http-8080/77726476706e69737468656265737421e9fd528569327d536a468ca88d1b203b/pyxx/login.aspx?wrdrecordvisit="+nowTime;
        }else {
        	loginHttp = "http://yjsy.buct.edu.cn:8080/pyxx/login.aspx";
        }
        driver.get(loginHttp);
        driver.pageWait();
        File f = null;
        Rectangle s = null;
        try {
	        //获得验证码的图片位置
	        WebElement webElement = driver.findElementByXPath("//*[@id=\"aspnetForm\"]/table/tbody/tr[3]/td[2]/img");
	        s=webElement.getRect();
	        f = driver.getScreenshotAs(OutputType.FILE);
        }catch (Exception e1) {
            e1.printStackTrace();
            return false;//表示登录网址不可用
        }
        //String pic_id = "";
        //先获得屏幕截屏再根据验证码位置切割图片
        try {
            BufferedImage img = ImageIO.read(f).getSubimage(s.x+1, s.y+1, s.width-3, s.height-3);
            String codeResult=CodeIdentification.getCodeFromBufferedImage(img);
            //pic_id = obj.getString("pic_id");//记录识别错误的验证码id
            //输入账号
            driver.findElementByXPath("//*[@id=\"_ctl0_txtusername\"]").clear();
            driver.findElementByXPath("//*[@id=\"_ctl0_txtusername\"]").sendKeys(userId);
            //输入密码
            driver.findElementByXPath("//*[@id=\"_ctl0_txtpassword\"]").clear();
            driver.findElementByXPath("//*[@id=\"_ctl0_txtpassword\"]").sendKeys(userPass);
            //输入验证码
            driver.findElementByXPath("//*[@id=\"_ctl0_txtyzm\"]").clear();
            driver.findElementByXPath("//*[@id=\"_ctl0_txtyzm\"]").sendKeys(codeResult);
            //登录
            driver.findElementByXPath("//*[@id=\"_ctl0_ImageButton1\"]").click();
            //driver.pageWait();
            System.out.println("超过5秒未出现个人信息请按Ctrl+C中断重新运行。");
            //进入个人信息页面
            driver.get("http://yjsy.buct.edu.cn:8080/pyxx/loging.aspx");
            String stuInfo = driver.findElementByXPath("//*[@id=\"form1\"]/div[1]/table/tbody/tr/td[2]/div[1]/div[1]/p/span").getText();
            System.out.println("请核对以下账号信息：");
            System.out.println(stuInfo);
            if(stuInfo.contains(userId)) return true;
            else return false;
        } catch (IOException e1) {
            e1.printStackTrace();
            driver.get(loginHttp);
            if(n<=loginMax) {
            	//验证码识别错误
				//if(isChaoJiYing&&!pic_id.isEmpty()) { ChaoJiYing.ReportNowError(pic_id); }
				 
            	login(n+1);//登录不成功
            	
            }else {
            	return false;//多次尝试无果则退出登录
            }
        }
        
        return false;
    }

    public static boolean getActivities() throws IOException, AddressException, MessagingException {
        //进入活动页面
        //driver.get("https://w.buct.edu.cn/http-8080/77726476706e69737468656265737421e9fd528569327d536a468ca88d1b203b/pyxx/txhdgl/hdlist.aspx?xh="+userId);
    	driver.get("http://yjsy.buct.edu.cn:8080/pyxx/txhdgl/hdlist.aspx?xh="+userId+"&n="+count);
    	((JavascriptExecutor) driver).executeScript("window.scrollTo(0,document.body.scrollHeight)");
    	List<WebElement> webElementList = driver.findElementsByXPath("//*[@id=\"dgData00\"]/tbody/tr");
        int n = webElementList.size() - 1;//活动数量
        for(int i=1;i<=n;i++){
            WebElement ele = webElementList.get(i);//获取这一行所有元素
            WebElement opeBtn=driver.findElementByXPath("//*[@id=\"dgData00__ctl"+(i+1)+"_Linkbutton3\"]");//报名按钮
            String text = ele.getText();
            String activities[]=text.split("	");
            int capacity = Integer.parseInt(activities[6]);//容量
            int nowcount = Integer.parseInt(activities[7]);//已报
            System.out.println(activities[1]+",容量："+capacity+",现有："+nowcount);
            if(nowcount<capacity){
            	//String pic_id = "";//记录识别错误的验证码id
                //活动验证码
                WebElement txtyzm=driver.findElementByXPath("//*[@id=\"txtyzm\"]");//验证码输入框
                WebElement webElement = driver.findElementByXPath("//*[@id=\"Table2\"]/tbody/tr[2]/td/p/img");//验证码图片
                Rectangle rectangle=webElement.getRect();
                File f = driver.getScreenshotAs(OutputType.FILE);
                BufferedImage img = ImageIO.read(f).getSubimage(rectangle.x+1, rectangle.y+1, rectangle.width-3, rectangle.height-3);
                String codeResult=CodeIdentification.getCodeFromBufferedImage(img);
                txtyzm.clear();
                txtyzm.sendKeys(codeResult);//输入验证码
                opeBtn.click();//提交
                System.out.println("已提交报名申请");
                try {
                	String honest = driver.switchTo().alert().getText();
                	System.out.println(honest);//第一个弹窗内容，提示诚信有关内容
                	driver.switchTo().alert().accept();//接受弹窗，【关键】，决定是否报名成功
                	String successOrfail = driver.switchTo().alert().getText();
                	System.out.println(successOrfail);//提示报名是否成功，提示内容有三种：您已成功申请（成功）、报名人数已满（失败）、你输入的验证码错误（失败）
                	driver.switchTo().alert().accept();//接受弹窗
                	if(successOrfail.contains("成功申请")) {
                		System.out.println("报名成功！");
                		if(!email.isEmpty()) MailUtils.sendMail(email,"【活动报告报名提醒】","您申请的活动报告【"+activities[1]+"】报名成功！请及时登录平台处理！活动开始时间："+activities[3]
                				+ "系统操作时间 ："+dateFormat.format(new Date()));
                		return true;
                	}else if(successOrfail.contains("验证码错误")) {
						//if(!pic_id.isEmpty()) { ChaoJiYing.ReportNowError(pic_id); }//超级鹰识别错误报告
						 
                	}
                	return false;
                }catch(Exception e) {
                	return false;
                }
            }
        }
        return false;
    }
    
    public static void refreshActivity() {
    	boolean login = false;//是否成功登录
    	try {
        	login = login(1);//1表示首次登录
        }catch (Exception e){
            //String logoutHttp = "https://w.buct.edu.cn/logout";
        	String logoutHttp = "http://yjsy.buct.edu.cn:8080/pyxx/login.aspx";
            driver.get(logoutHttp);
        }
    	Scanner input = new Scanner(System.in);
    	System.out.println("信息正确按任意键继续，否则Ctrl+C中断重新运行。");
    	String a = input.nextLine();
        boolean flag = false;//是否选到
        if(login) {
            System.out.println("进入活动页面");
            while(!flag) {
            	count++;//监听次数
                System.out.println("正在监听："+count);
	            try{
	                flag = getActivities();//获取活动列表
	            }catch (Exception e){
	            	//String logoutHttp = "https://w.buct.edu.cn/logout";
	            	String logoutHttp = "http://yjsy.buct.edu.cn:8080/pyxx/login.aspx";
	                driver.get(logoutHttp);
	                flag = true;//活动页面异常则退出监听
	                System.out.println("活动页面异常");
	            }
            }
            System.out.println("活动报名完成，需要监听请重启程序");
        }else {
        	System.out.println("登录失败");
        }
        driver.quit();
    	driver.close();
        return;
    }
    public static void main(String[] args) {
    	if(userId==null||userPass==null) {
    		System.out.println("配置文件不存在");
    		driver.quit();
        	driver.close();
    		return;
    	}else if(userId.isEmpty()||userPass.isEmpty()) {
    		System.out.println("学号密码字段为空，请重新输入");
    		driver.quit();
        	driver.close();
    		return;
    	}else refreshActivity();
    }
}