import java.net.ServerSocket;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

//�X���b�h���i�e�N���C�A���g�ɉ����āj
class ClientProcThread extends Thread {
	private int number;//�����̔ԍ�
	private Socket incoming;
	private InputStreamReader myIsr;
	private BufferedReader myIn;
	private PrintWriter myOut;
	private String myName;//�ڑ��҂̖��O

	public ClientProcThread(int n, Socket i, InputStreamReader isr, BufferedReader in, PrintWriter out) {
		number = n;
		incoming = i;
		myIsr = isr;
		myIn = in;
		myOut = out;
	}

	public void run() {
		try {
			myOut.println(number);//���񂾂��Ă΂��
			
			myName = myIn.readLine();//���߂Đڑ������Ƃ��̈�s�ڂ͖��O

			while (true) {//�������[�v�ŁC�\�P�b�g�ւ̓��͂��Ď�����
				String str = myIn.readLine();
				System.out.println("Received from client No."+number+"("+myName+"), Messages: "+str);
				if (str != null) {//���̃\�P�b�g�i�o�b�t�@�j�ɓ��͂����邩���`�F�b�N
					if (str.toUpperCase().equals("BYE")) {
						myOut.println("Good bye!");
						break;
					}
					MyServerBJ.SendAll(str, myName);//�T�[�o�ɗ������b�Z�[�W�͐ڑ����Ă���N���C�A���g�S���ɔz��
				}
			}
		} catch (Exception e) {
			//�����Ƀv���O���������B����Ƃ��́C�ڑ����؂ꂽ�Ƃ�
			System.out.println("Disconnect from client No." + (number + 1) + "("+myName+")");
			MyServerBJ.SetFlag(number, false);//�ڑ����؂ꂽ�̂Ńt���O��������
      MyServerBJ.DecreaseMemberCount();
		}
	}
}

class MyServerBJ {
    private static final int maxConnections = 4;
    private static Socket[] incoming;
    private static boolean[] flag;
    private static InputStreamReader isr [];
    private static BufferedReader[] in;
    private static PrintWriter[] out;
    private static ClientProcThread[] myClientProcThread;
    private static int member = 0;

    // �S���Ƀ��b�Z�[�W�𑗂�
    public static void SendAll(String str, String myName) {
        for (int i = 1; i <= maxConnections; i++) {
            if (flag[i - 1]) {
                out[i - 1].println(str);
                out[i - 1].flush();
                System.out.println("Send messages to client No." + i);
            }
        }
    }
    
    public static void DecreaseMemberCount(){
      member--;
    }

    // �t���O�̐ݒ���s��
    public static void SetFlag(int n, boolean value) {
        flag[n] = value;
    }

    private static ArrayList<Integer> generateRandomcardList() {
        ArrayList<Integer> cardsList = new ArrayList<>(52);
        for (int i = 0; i < 52; i++) {
            cardsList.add(i);
        }
        Collections.shuffle(cardsList);
        return cardsList;
    }
    
    /*
    private static void sendTilesListToClients() {
      String cardString = "";
      ArrayList<Integer> cards0 = generateRandomcardList();
      cardString += Integer.toString(cards0.get(0));
      for(int i = 1; i < 52; i++){
        cardString += ",";
        cardString += Integer.toString(cards0.get(i));
      }
      
      for(int i = 1; i < 10; i++){
        ArrayList<Integer> cards = generateRandomcardList();
        for(int j = 0; j < 52; j++){
          cardString += ",";
          cardString += Integer.toString(cards.get(i));
        }
      }
      
      for (int i = 0; i < maxConnections; i++) {
        if (flag[i]) {
          out[i].println("CARDS " + cardString);
          out[i].flush();
        }
      }
    }*/
    
    private static void sendTilesListToClients() {
      String cardString = "";
      
      for(int i = 0; i < 10; i++){
        ArrayList<Integer> cards0 = generateRandomcardList();
        if(i == 0){
          cardString += Integer.toString(cards0.get(0));
          
          for(int j = 1; j < 52; j++){
            cardString += "," + Integer.toString(cards0.get(j));
          }
        }else{
          for(int j = 0; j < 52; j++){
            cardString += "," + Integer.toString(cards0.get(j));
          }
        }
      }
      
      for (int i = 0; i < maxConnections; i++) {
        if (flag[i]) {
          out[i].println("CARDS " + cardString);
          out[i].flush();
        }
      }
    }
    
    
    public static void main(String[] args) {
      incoming = new Socket[maxConnections];
      flag = new boolean[maxConnections];
      isr = new InputStreamReader[maxConnections];
      in = new BufferedReader[maxConnections];
      out = new PrintWriter[maxConnections];
      myClientProcThread = new ClientProcThread[maxConnections];
      
        try {
            ServerSocket server = new ServerSocket(10000);
            System.out.println("The server has launched!");

            while (true) {
                Socket socket = server.accept();

                if (member < maxConnections) {
                    member++;
                    int n = member - 1;
                    
                    incoming[n] = socket;
                    flag[n] = true;
                    System.out.println("Accept client No." + (n + 1));

                    InputStreamReader isr = new InputStreamReader(incoming[n].getInputStream());
                    in[n] = new BufferedReader(isr);
                    out[n] = new PrintWriter(incoming[n].getOutputStream(), true);

                    myClientProcThread[n] = new ClientProcThread(n, incoming[n], isr, in[n], out[n]);
                    myClientProcThread[n].start();

                    if (member == maxConnections) {
                        sendTilesListToClients();
                    }
                }else{
                  //�C����������t������
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred in the server: " + e.getMessage());
        }
    }
}