package com.sse;

import com.dcits.sc.MailUtil;
import com.dcits.sc.MessageSender;
import com.sun.org.apache.xml.internal.security.Init;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.mail.Message;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import static com.dcits.sc.MailUtil.createImageMail;
import static javafx.scene.input.KeyCode.F;

/**
 * Created by lp860 on 2017/6/27.
 */
@RequestMapping(value = "/initController")
@Controller("initController")
public class InitController {

    @Value("${spring.mail.host}")
    private String mailHost ;
    @Value("${spring.mail.username}")
    private String mailUser;
    @Value("${spring.mail.password}")
    private String mailPwd;

    private PrintStream ps;
    private Logger log = LoggerFactory.getLogger(InitController.class );

    private  ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024);

    @RequestMapping(value = "/sseRequest",produces = "text/event-stream")
    public  void sseRequest(HttpServletResponse response)throws Exception{
        response.setContentType("text/event-stream");//设置事件流
        response.setCharacterEncoding("UTF-8");//设置编码
        //获取最新时间并返回
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String string = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

//        System.out.println(string);
//        // send SSE
//        while(true){
//            writer.write("data: " + string + "\n\n");
//            writer.flush();
//            try {   //设置间隔时间
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//        }



//        while (true){
//            //string += "重新打开日志文件";
//            log.info(string);
//            writer.print("data:"+string + "\n\n");
//            writer.flush();
//            Thread.sleep(500);
//        }




        try {
            File logfile = new File("c:/log/sendMail.log");
            Long filePointer = 0L;
            RandomAccessFile file = new RandomAccessFile(logfile, "r");
            while (true) {
                long fileLength = logfile.length();
                if (fileLength < filePointer) {
                    file = new RandomAccessFile(logfile, "r");
                    filePointer = 0L;
                }
                if (fileLength > filePointer) {
                    file.seek(filePointer);
                    String line = new String(file.readLine().getBytes("iso8859-1"), "utf-8");
                    if(line != null){
                        System.out.println(line);
                        writer.print("data:"+line + "\n\n");//eventsource 固定的格式
                        writer.flush();
                        filePointer = file.getFilePointer();
                    }
                }
                if(fileLength == filePointer){
                    break;
                }
                Thread.sleep(500);
            }

        } catch (IOException e) {
            e.printStackTrace();

        } catch (InterruptedException e) {
            e.printStackTrace();

        }

    }

    @RequestMapping(value="/sseIOStream",produces = "text/event-stream")
    public void sseIOStream(HttpServletResponse response) throws InterruptedException, IOException {
        response.setContentType("text/event-stream");//设置事件流
        response.setCharacterEncoding("UTF-8");//设置编码
        //获取最新时间并返回
        PrintWriter writer = null;
        try {
            writer = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
        }
        PrintStream cacheStream = new PrintStream(outStream);
        PrintStream oldStream = System.out;
        System.setOut(cacheStream);//不打印到控制台
        System.out.println("System.out.println:test");
        while(outStream != null){
            String  outLine = outStream.toString();
            outStream.flush();
            outStream.reset();
            if(!StringUtils.isEmpty(outLine)){
                writer.print("data:"+outLine + "\n\n");//eventsource 固定的格式
                writer.flush();
            }
//            Thread.sleep(1000);
        }
    }
    @RequestMapping(value="/sseSendMailSteam",produces = "text/event-stream")
    public void sseSendMailSteam(HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream");//设置事件流
        response.setCharacterEncoding("UTF-8");//设置编码
        PrintWriter writer = response.getWriter();
        while(outStream != null){
            String  outLine = outStream.toString();
            outStream.flush();
            outStream.reset();
            if(!StringUtils.isEmpty(outLine)){
                writer.print("data:"+outLine + "\n\n");//eventsource 固定的格式
                writer.flush();
            }
//            Thread.sleep(1000);
        }

    }

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(InitController.class );
    @Autowired
    private JavaMailSender mailSender;

    @RequestMapping("/sendMsg")
    public void sendMsg(){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("lp8608@126.com");
        message.setTo("lp8608@163.com");
        message.setSubject("主题：简单邮件");
        message.setText("测试邮件内容");
        mailSender.send(message);
    }
    @RequestMapping("/sendMail")
    public void sendMsg2(String rcpt,String cc,String title,String mailContent) throws Exception {
        Properties prop = new Properties();
        prop.setProperty("mail.host", mailHost);
        prop.setProperty("mail.transport.protocol", "smtp");
        prop.setProperty("mail.smtp.auth", "true");

        //使用JavaMail发送邮件的5个步骤
        //1、创建session
        Session session = Session.getInstance(prop);
        //开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
        session.setDebug(true);
        PrintStream ps = new PrintStream(outStream);
        session.setDebugOut(ps);
        //2、通过session得到transport对象
        Transport ts = session.getTransport();
        //3、连上邮件服务器，需要发件人提供邮箱的用户名和密码进行验证
        ts.connect(mailHost, mailUser , mailPwd);
        //4、创建邮件
        Message message = MailUtil.createSimpleMail(session,rcpt,cc,title,mailContent);
//        message.set
        //5、发送邮件
        ts.sendMessage(message, message.getAllRecipients());
        ts.close();
        ps.close();
        //response.getWriter().print("发送成功！");
    }
    @RequestMapping("/writerLog")
    public void writerLogFile(String log)throws Exception{
        PrintStream ps = new PrintStream(new FileOutputStream("c:/log/sendMail.log"));
        ps.print(log);
        ps.close();

    }


    @RequestMapping("/")
    public String index(){
        return "index";
    }
    @RequestMapping("/index1")
    public String index1(){
        return "index1";
    }

}
