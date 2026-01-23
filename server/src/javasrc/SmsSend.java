import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SmsSend {

	private Socket sock 			 = null;
	private BufferedReader brReader  = null;
	private PrintWriter pwWriter 	 = null;
	private BufferedOutputStream bos = null;
	
    /**
     * 생성자
     */
    public SmsSend() {
    }
	
	
    /**
     * LTS에 접속하여 서버측 메시지를 수신
     *
     * @param strHost LTS가 설치된 시스템의 IP
     * @param iPort 포트
     */
    public String open(String strHost, int iPort) throws IOException {

        
        try {
            sock = new Socket(strHost, iPort);

            sock.setReceiveBufferSize(10 * 1024);
            brReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            pwWriter = new PrintWriter(sock.getOutputStream(), true);
            bos = new BufferedOutputStream(sock.getOutputStream());

            return brReader.readLine();
        } catch(IOException ioe) {
        	System.out.println("SmsSend.open(\"" + strHost + "\", " + iPort + "): " + ioe.toString());
            throw ioe;
        }
    }
	
    /**
     * AID( adapter ID를 인자로 넘겨줌 ) 현재 AID 는 NVECAREMSG테이블의 ECARE_NO를 사용
     *
     * @param sAID
     * @return 리턴 메시지
     */
    public String setAID(String sAID) throws IOException {
        try {
            pwWriter.print("AID:" + sAID);
            pwWriter.print("\r\n");
            pwWriter.flush();

            return brReader.readLine();
        } catch(IOException ioe) {
        	System.out.println("SmsSend.setAID(\"" + sAID + "\"): " + ioe.toString());
            throw ioe;
        }
    }
	
    /**
     * 인자(value값)가 단일라인로 처리되는 경우 이 메소드 사용 ex) name: 최재철 ---> setArg("name", "최재철")
     *
     * @param sKey (키값)
     * @param sVal (value값)
     * @return 리턴 메시지
     */
    public String setArg(String sKey, String sVal) throws IOException {
        try {
            pwWriter.print(sKey + ":" + sVal);
            pwWriter.print("\r\n");
            pwWriter.flush();

            return brReader.readLine();
        } catch(IOException ioe) {
        	System.out.println("SmsSend.setArg(\"" + sKey + "\", \"" + sVal + "\"): " + ioe.toString());
            throw ioe;
        }
    }
    
    /**
     * 호출을 마치고 실행함을 의미
     */
    public String commit() throws IOException {

        try {
            pwWriter.print("COMMIT");
            pwWriter.print("\r\n");
            pwWriter.flush();

            return brReader.readLine();
        } catch(IOException ioe) {
        	System.out.println("SmsSend.commit(): " + ioe.toString());
            throw ioe;
        }
    }
    
    /**
     * 접속을 끊음
     */
    public void quit() /* throws IOException */ {
        try {
            pwWriter.print("QUIT");
            pwWriter.print("\r\n");
            pwWriter.flush();

        } finally {
            try {
                pwWriter.close();
            } catch(Exception e1) {
            }
            try {
                brReader.close();
            } catch(Exception e2) {
            }
            try {
                bos.close();
            } catch(Exception e3) {
            }
            try {
                sock.close();
            } catch(Exception e3) {
            }
            sock = null;
            brReader = null;
            pwWriter = null;
            bos = null;
        }
    }
	
    public static void main(String[] args) {
		if (args.length != 8) {
			System.out.println("usage: <IP> <PORT> <REQDEPT> <REQID> <REVID> <REVNAME> <TELNO> <MESSAGE>");
			return;
		}
		
        System.out.println("START");
        try {
			
        	SmsSend tc = new SmsSend();

        	//LTS 서버 IP / PORT (해당 IP와 PORT는 투입시 전달예정)
            //tc.open("192.168.124.136", 9100);
            
           	String ipaddr = args[0].toString();
           	int portno = Integer.parseInt(args[1]);
			tc.open(ipaddr, portno);
            
			System.out.println("++++PARAM ["+ipaddr+" "+portno+" "+args[2]+" "+args[3]+" "+args[4]+" "+args[5]+" " +args[6]+"]");
			
            // 이케어 번호 (UMS 관리자 웹화면에서 생성하는 번호로 서비스 생성 요청시 이케어 생성 후 번호 전달드립니다.)
            // 아래는 생성된 이케어 번호임.
            // 1:실시간 SMS - 템플릿 없는 문구 전송
            // 2:준실시간 SMS - 템플릿 없는 문구 전송
            tc.setAID("1");
            
            //인자값 (요청부서 / 사용자아이디 셋팅 필수)
            tc.setArg("_REQ_DEPT_ID", args[2]);      //시스템 호스트명
            tc.setArg("_REQ_USER_ID", args[3]); //

            //필수 개인화 인자값
            tc.setArg("IUSERID" , args[4]); //수신자 아이디 : 은행직원은 행번, 서울시 직원은 사번
            tc.setArg("INAME"   , args[5]); //수신자 이름
            tc.setArg("IPHONE"  , args[6]); //전화번호
            
            String smsTxt = args[7].toString();
            
            smsTxt = smsTxt.replaceAll("\n", "\r");
            tc.setArg("IMESSAGE", smsTxt);
			
			System.out.println("++++RECEIVE MESSAGE ["+args[7]+"]");
			
            tc.commit();
            tc.quit();
            tc = null;

            //      }
        } catch(Exception e) {
        	System.out.println("[ERROR] " + e.toString());
        }
        System.out.println("END");
    }
}
