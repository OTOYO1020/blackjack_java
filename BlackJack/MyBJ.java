import java.net.*;
import java.io.*;
import javax.swing.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.awt.image.*;//画像処理に必要
import java.awt.geom.*;//画像処理に必要

public class MyBJ extends JFrame implements MouseListener,MouseMotionListener {
	private ArrayList<Integer> cardList = new ArrayList<Integer>(520);
  
  //プレイヤー毎のカード置き場の作成
  private JButton P0_Button [] = new JButton[5];
  private JButton P1_Button [] = new JButton[5];
  private JButton P2_Button [] = new JButton[5];
  private JButton P3_Button [] = new JButton[5];
  private ImageIcon backgroundBlue, hit, stand, go, BET;
  
  private ImageIcon heart[] = new ImageIcon[13];
  private ImageIcon diamond[] = new ImageIcon[13];
  private ImageIcon clover[] = new ImageIcon[13];
  private ImageIcon spade[] = new ImageIcon[13];
  private ImageIcon back;
  
  private JButton Hit, Stand, Go;
  
  private JLabel P0_score_Label, P1_score_Label, P2_score_Label, P3_score_Label;
  private int P0_cards[] = new int [5];
  private int P1_cards[] = new int [5];
  private int P2_cards[] = new int [5];
  private int P3_cards[] = new int [5];
  private int myCardNum = 2;
  
  private int parent_money = 0;
  private int child_money [] = new int [3];
  private int posession [] = new int [3];
  private int earn = 0;
  
  private int gameSizeWidth, gameSizeHeight;
  private int width = 30, height = 42;
  private int myNumber;
  private int cardNumber = 0, nowTurn = 1;
  
  private JLabel P0_Point;
  private JLabel P1_Point;
  private JLabel P2_Point;
  private JLabel P3_Point;
  private JLabel Parent_Point;
  
  private JLabel BET_LABEL;
  private JTextField BET_INPUT;
  private JButton BET_PUSH;
  private boolean BET_STATUS = false;
  private int BET_Money[] = new int [3];
  
  private boolean judgement_game = true;
  
  private int completed_playerNumber = 0;
  private int gameCount = 0;
  
  private int gameResult[] = new int [4];
  private int childRank[] = new int [3];
  private Container c;
	PrintWriter out;//出力用のライター

	public MyBJ() {
		//名前の入力ダイアログを開く
		String myName = JOptionPane.showInputDialog(null,"名前を入力してください","名前の入力",JOptionPane.QUESTION_MESSAGE);
		if(myName.equals("")){
			myName = "No Name";//名前がないときは，"No Name"とする
		}
    
    String myIP = JOptionPane.showInputDialog(null,"IPアドレスを入力してください","名前の入力",JOptionPane.QUESTION_MESSAGE);
		if(myIP.equals("")){
			myIP = "localhost";//nullの場合、localhostとする
		}
    
		//ウィンドウを作成する
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じるときに，正しく閉じるように設定する
		setTitle("MyBJ");//ウィンドウのタイトルを設定する
    settingImageIcon();
    
		//サーバに接続する
		Socket socket = null;
		try {
			//"localhost"は，自分内部への接続．localhostを接続先のIP Address（"133.42.155.201"形式）に設定すると他のPCのサーバと通信できる
			//10000はポート番号．IP Addressで接続するPCを決めて，ポート番号でそのPC上動作するプログラムを特定する
			socket = new Socket("localhost", 10000);
		} catch (UnknownHostException e) {
			System.err.println("ホストの IP アドレスが判定できません: " + e);
		} catch (IOException e) {
			 System.err.println("エラーが発生しました: " + e);
		}
		
		MesgRecvThread mrt = new MesgRecvThread(socket, myName);//受信用のスレッドを作成する
		mrt.start();//スレッドを動かす（Runが動く）
	}
		
	//メッセージ受信のためのスレッド
	public class MesgRecvThread extends Thread {
		Socket socket;
		String myName;
		
		public MesgRecvThread(Socket s, String n){
			socket = s;
			myName = n;
		}
		
		//通信状況を監視し，受信データによって動作する
		public void run() {
			try{
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);
				out = new PrintWriter(socket.getOutputStream(), true);
				out.println(myName);//接続の最初に名前を送る
        
        String myNumberStr = br.readLine();
        int myNumberInt = Integer.parseInt(myNumberStr);
        myNumber= myNumberInt % 4;
        System.out.println(myNumber);
        
        
        //ウィンドウを作成する
        if(myNumber == 0){
          gameSizeWidth = 15 * width + 220;
        }else{
          gameSizeWidth = 17 * width + 330;
        }
        gameSizeHeight = 2 * height + 400;
        setSize(gameSizeWidth, gameSizeHeight);//ウィンドウのサイズを設定する
        c = getContentPane();
        c.setLayout(null);//自動レイアウトの設定を行わない
        
        
        PlaceButtons(myNumber);
        PlaceLabel(myNumber);
        PlaceTextField(myNumber);
        
        for(int i = 0; i < 5; i++){
            P0_cards[i] = -1;
            P1_cards[i] = -1;
            P2_cards[i] = -1;
            P3_cards[i] = -1;
        }
        
        for(int i = 0; i < 3; i++){
          BET_Money[i] = 0;
          posession[i] = 300;
          childRank[i] = -1;
        }
        
        for(int i = 0; i < 4; i++){
          gameResult[i] = -1;
        }
        
				while(true) {
					String inputLine = br.readLine();//データを一行分だけ読み込んでみる
					if (inputLine != null) {//読み込んだときにデータが読み込まれたかどうかをチェックする
						//System.out.println(inputLine);//デバッグ（動作確認用）にコンソールに出力する
						String[] inputTokens = inputLine.split(" ");	//入力データを解析するために、スペースで切り分ける
						String cmd = inputTokens[0];//コマンドの取り出し．１つ目の要素を取り出す
            if(cmd.equals("CARDS")){
              String cardString = inputTokens[1];
              //System.out.println(cardString);
              String cardNumbers [] = cardString.split(",");
              for(String number: cardNumbers){  //拡張for文に気づいた天才と呼んでくれ
                cardList.add(Integer.parseInt(number));  //ここで鯖からもらってリストを紐づける
              }
              
              //初めに4人にカードを配る
              P0_cards[0] = cardList.get(0);
              P1_cards[0] = cardList.get(1);
              P2_cards[0] = cardList.get(2);
              P3_cards[0] = cardList.get(3);
              P0_cards[1] = cardList.get(4);
              P1_cards[1] = cardList.get(5);
              P2_cards[1] = cardList.get(6);
              P3_cards[1] = cardList.get(7);
              
              showIcon();
              
            }
            
            if(cmd.equals("HIT")){
              int tmpCard = Integer.parseInt(inputTokens[1]);
              int tmpNumber = Integer.parseInt(inputTokens[2]);
              cardNumber++;
              
              if(nowTurn == 0){
                P0_cards[tmpNumber - 1] = tmpCard;
              }else if(nowTurn == 1){
                P1_cards[tmpNumber - 1] = tmpCard;
              }else if(nowTurn == 2){
                P2_cards[tmpNumber - 1] = tmpCard;
              }else if(nowTurn == 3){
                P3_cards[tmpNumber - 1] = tmpCard;
              }
              showLabel();
              showIcon2();
            }
            
            if(cmd.equals("NEXT")){  //次のプレイヤーに制御を移す処理
              nowTurn = (nowTurn + 1) % 4;
            }
            
            if(cmd.equals("MONEY")){
              int tmp_posession = Integer.parseInt(inputTokens[1]);
              int tmpNumber = Integer.parseInt(inputTokens[2]);
              int tmpBET_Money = Integer.parseInt(inputTokens[3]);
              
              posession[tmpNumber] = tmp_posession;  //ここで全体へ賭けた後の所持金の紐づけを行う
              BET_Money[tmpNumber] = tmpBET_Money;
              String tmp_posession_string = Integer.toString(tmp_posession);
              completed_playerNumber += tmpNumber + 1;
              //表示する
              if(tmpNumber == 0){  //playersNumber == 1
                P1_score_Label.setText(tmp_posession_string);
              }else if(tmpNumber == 1){  //playersNumber == 2
                P2_score_Label.setText(tmp_posession_string);
              }else if(tmpNumber == 2){  //playersNumber == 3
                P3_score_Label.setText(tmp_posession_string);
              }
              
              if(completed_playerNumber == 6){
                P1_Button[1].setIcon(linkCards(P1_cards[1]));
                P2_Button[1].setIcon(linkCards(P2_cards[1]));
                P3_Button[1].setIcon(linkCards(P3_cards[1]));
              }
            }
            
            if(cmd.equals("GO")){  //勝負!!
              //開示
              for(int i = 0; i < 5; i++){
                P0_Button[i].setIcon(linkCards(P0_cards[i]));
              }
              int parent_Score = calcBJ(P0_cards);
              int child_Score [] = new int [3];
              child_Score[0] = calcBJ(P1_cards);
              child_Score[1] = calcBJ(P2_cards);
              child_Score[2] = calcBJ(P3_cards);
              
              Parent_Point.setText(Integer.toString(parent_Score));
              
              if(parent_Score >= 22){  //親がバーストしたとき
                for(int i = 0; i < 3; i++){
                  if(child_Score[i] >= 22){  //子もバーストしたとき
                    posession[i] += BET_Money[i];
                  }else{  //子がバーストしなかったとき
                    posession[i] += 2 * BET_Money[i];
                  }
                }
              }else{  //親がバーストしていない時
                for(int i = 0; i < 3; i++){
                  if(child_Score[i] >= 22){  //子がバーストしたとき
                    earn += BET_Money[i];
                  }else{
                    if(child_Score[i] < parent_Score){  //両方バーストせずに、親の勝ち
                      earn += BET_Money[i];
                    }else if(child_Score[i] == parent_Score){  //引き分け
                      posession[i] += BET_Money[i];
                    }else{  //子の勝ち
                      posession[i] += 2 * BET_Money[i];
                    }
                  }
                  //System.out.println(earn);
                }
              }
              
              //ここで反映し直す
              P0_score_Label.setText(Integer.toString(earn));
              P1_score_Label.setText(Integer.toString(posession[0]));
              P2_score_Label.setText(Integer.toString(posession[1]));
              P3_score_Label.setText(Integer.toString(posession[2]));
              
              gameCount++;
              judgement_game = judgement();
              
              if(gameCount == 10 || !judgement_game){
                if(myNumber == 0){
                  out.println("FINISH");
                  out.flush();
                }
              }else if(gameCount < 10){
                try{
                  Thread.sleep(10000);
                  if(myNumber == 0){
                    out.println("ONE_MORE");
                    out.flush();
                  }
                }catch(InterruptedException ev){
                  ev.printStackTrace();
                }
              }
            }
            
            if(cmd.equals("ONE_MORE")){
              BET_STATUS = false;
              
              for(int i = 0; i < 3; i++){
                BET_Money[i] = 0;
              }
              
              if(myNumber != 0){
                BET_INPUT.setText("");
              }
              
              completed_playerNumber = 0;
              
              for(int i = 0; i < 5; i++){
                P0_cards[i] = -1;
                P1_cards[i] = -1;
                P2_cards[i] = -1;
                P3_cards[i] = -1;
              }
              
              P0_cards[0] = cardList.get(cardNumber + 1);
              P1_cards[0] = cardList.get(cardNumber + 2);
              P2_cards[0] = cardList.get(cardNumber + 3);
              P3_cards[0] = cardList.get(cardNumber + 4);
              P0_cards[1] = cardList.get(cardNumber + 5);
              P1_cards[1] = cardList.get(cardNumber + 6);
              P2_cards[1] = cardList.get(cardNumber + 7);
              P3_cards[1] = cardList.get(cardNumber + 8);
              
              cardNumber += 8;
              
              showLabel();
              showIcon();
              
              nowTurn = 1;
              myCardNum = 2;
            }
            
            if(cmd.equals("FINISH")){
              if(gameCount < 10){  //子が死んだ場合
                gameResult[0] = 0;
                calcChildRank(posession, childRank);  //子の順位の処理を行う
              }else{  //子が死んでいない場合
                if(earn >= 1000){
                  gameResult[0] = 0;
                  calcChildRank(posession, childRank);  //子の順位の処理を行う
                }else{
                  gameResult[0] = 3;
                  calcChildRank(posession, childRank);
                  for(int i = 0; i < 3; i++){
                    if(childRank[i] != 4){
                      childRank[i] -= 1;
                    }
                  }
                }
              }
              
              //gameResultに順位が格納されている  0~3が順位、4は死んだ場合
              for(int i = 0; i < 3; i++){
                gameResult[i + 1] = childRank[i];
              }
              
              if(gameResult[myNumber] == 0){
                JOptionPane.showMessageDialog(null, "Congratulations!!");
              }else if(gameResult[myNumber] != 4){
                String MESSAGE = "あなたの順位は、" + (gameResult[myNumber] + 1) + "位でした。";
                JOptionPane.showMessageDialog(null, MESSAGE);
              }else{
                JOptionPane.showMessageDialog(null, "YOU LOSE...");
              }
            }
          }else{  //何も命令がない時
            break;
          }
				
				}
				socket.close();
			} catch (IOException e) {
				System.err.println("エラーが発生しました: " + e);
			}
		}
	}

	public static void main(String[] args) {
		MyBJ net = new MyBJ();
		net.setVisible(true);
	}
  	
	public void mouseClicked(MouseEvent e) {//ボタンをクリックしたときの処理
    JButton theButton = (JButton)e.getComponent();//クリックしたオブジェクトを得る．型が違うのでキャストする
    String theArrayIndex = theButton.getActionCommand();//ボタンの配列の番号を取り出す
    
    //ヒットボタンが押された場合の処理
    if(Integer.parseInt(theArrayIndex) == 100 && completed_playerNumber == 6){
      if(nowTurn == myNumber && myCardNum < 5){
        int tmpCard = cardList.get(cardNumber);
        myCardNum++;
        
        out.println("HIT " + tmpCard + " " + myCardNum);
        out.flush();
      }
    }else if(Integer.parseInt(theArrayIndex) == 200){  //スタンドボタンが押された場合の処理
      if(nowTurn == myNumber){
        out.println("NEXT");
        out.flush();
      }
    }else if(Integer.parseInt(theArrayIndex) == 300){  //GOボタンが押された場合の処理（開示と勝敗の決定）
      if(nowTurn == myNumber){
        out.println("GO");
        out.flush();
      }
    }else if(Integer.parseInt(theArrayIndex) == 400 && !BET_STATUS){
      //入力した数字を読み取る、数字以外・マイナス・持ち金よりも大きい場合はメッセージを表示してやり直させる。
      String BP_string = BET_INPUT.getText();
      //System.out.println(BP_string);
      
      try{
        //整数型に変更とかの諸々の処理
        int BP = Integer.parseInt(BP_string);
        if(BP < 10){
          JOptionPane.showMessageDialog(null, "10以上の整数を入力してください。");
        }else if(posession[myNumber - 1] < BP){
          JOptionPane.showMessageDialog(null, "所持金より小さい金額を入力してください。");
        }else{
          BET_Money[myNumber - 1] = BP;
          posession[myNumber - 1] -= BP;
          BET_STATUS = true;
          //それぞれの所持金(posession)を全体に送信 + 表示する
          out.println("MONEY " + posession[myNumber - 1] + " " + (myNumber - 1) + " " + BP);
          out.flush();
        }
      }catch(NumberFormatException ev){
        JOptionPane.showMessageDialog(null, "整数を入力してください。");
      }
    }
	}
	
	public void mouseEntered(MouseEvent e) {//マウスがオブジェクトに入ったときの処理
	}
	
	public void mouseExited(MouseEvent e) {//マウスがオブジェクトから出たときの処理
	}
	
	public void mousePressed(MouseEvent e) {//マウスでオブジェクトを押したときの処理（クリックとの違いに注意）
	}
	
	public void mouseReleased(MouseEvent e) {//マウスで押していたオブジェクトを離したときの処理
	}
	
	public void mouseDragged(MouseEvent e) {//マウスでオブジェクトとをドラッグしているときの処理
	}

	public void mouseMoved(MouseEvent e) {//マウスがオブジェクト上で移動したときの処理
	}
  
  public void settingImageIcon(){
    heart[0] = new ImageIcon("./trump/heart1.png");
    heart[1] = new ImageIcon("./trump/heart2.png");
    heart[2] = new ImageIcon("./trump/heart3.png");
    heart[3] = new ImageIcon("./trump/heart4.png");
    heart[4] = new ImageIcon("./trump/heart5.png");
    heart[5] = new ImageIcon("./trump/heart6.png");
    heart[6] = new ImageIcon("./trump/heart7.png");
    heart[7] = new ImageIcon("./trump/heart8.png");
    heart[8] = new ImageIcon("./trump/heart9.png");
    heart[9] = new ImageIcon("./trump/heart10.png");
    heart[10] = new ImageIcon("./trump/heart11.png");
    heart[11] = new ImageIcon("./trump/heart12.png");
    heart[12] = new ImageIcon("./trump/heart13.png");
    
    diamond[0] = new ImageIcon("./trump/diamond1.png");
    diamond[1] = new ImageIcon("./trump/diamond2.png");
    diamond[2] = new ImageIcon("./trump/diamond3.png");
    diamond[3] = new ImageIcon("./trump/diamond4.png");
    diamond[4] = new ImageIcon("./trump/diamond5.png");
    diamond[5] = new ImageIcon("./trump/diamond6.png");
    diamond[6] = new ImageIcon("./trump/diamond7.png");
    diamond[7] = new ImageIcon("./trump/diamond8.png");
    diamond[8] = new ImageIcon("./trump/diamond9.png");
    diamond[9] = new ImageIcon("./trump/diamond10.png");
    diamond[10] = new ImageIcon("./trump/diamond11.png");
    diamond[11] = new ImageIcon("./trump/diamond12.png");
    diamond[12] = new ImageIcon("./trump/diamond13.png");
    
    spade[0] = new ImageIcon("./trump/spade1.png");
    spade[1] = new ImageIcon("./trump/spade2.png");
    spade[2] = new ImageIcon("./trump/spade3.png");
    spade[3] = new ImageIcon("./trump/spade4.png");
    spade[4] = new ImageIcon("./trump/spade5.png");
    spade[5] = new ImageIcon("./trump/spade6.png");
    spade[6] = new ImageIcon("./trump/spade7.png");
    spade[7] = new ImageIcon("./trump/spade8.png");
    spade[8] = new ImageIcon("./trump/spade9.png");
    spade[9] = new ImageIcon("./trump/spade10.png");
    spade[10] = new ImageIcon("./trump/spade11.png");
    spade[11] = new ImageIcon("./trump/spade12.png");
    spade[12] = new ImageIcon("./trump/spade13.png");
    
    clover[0] = new ImageIcon("./trump/clover1.png");
    clover[1] = new ImageIcon("./trump/clover2.png");
    clover[2] = new ImageIcon("./trump/clover3.png");
    clover[3] = new ImageIcon("./trump/clover4.png");
    clover[4] = new ImageIcon("./trump/clover5.png");
    clover[5] = new ImageIcon("./trump/clover6.png");
    clover[6] = new ImageIcon("./trump/clover7.png");
    clover[7] = new ImageIcon("./trump/clover8.png");
    clover[8] = new ImageIcon("./trump/clover9.png");
    clover[9] = new ImageIcon("./trump/clover10.png");
    clover[10] = new ImageIcon("./trump/clover11.png");
    clover[11] = new ImageIcon("./trump/clover12.png");
    clover[12] = new ImageIcon("./trump/clover13.png");
    
    back = new ImageIcon("./trump/back.png");
    
    backgroundBlue = new ImageIcon("./others/background.png");
    hit = new ImageIcon("./others/hit.png");
    stand = new ImageIcon("./others/stand.png");
    go = new ImageIcon("./others/go.png");
    
    BET = new ImageIcon("./others/BET.png");
  }
  
  public void PlaceButtons(int playersNumber){
    Hit = new JButton(hit);
    c.add(Hit);
    Hit.setBounds(95 + 7 * width - 70, gameSizeHeight - width - 50, height ,width);
    Hit.addMouseListener(this);
    Hit.addMouseMotionListener(this);
    Hit.setActionCommand(Integer.toString(100));
    
    if(playersNumber != 0){
      Stand = new JButton(stand);
      c.add(Stand);
      Stand.setBounds(95 + 7 * width + 70, gameSizeHeight - width - 50, height ,width);
      Stand.addMouseListener(this);
      Stand.addMouseMotionListener(this);
      Stand.setActionCommand(Integer.toString(200));
    }else{
      Go = new JButton(go);
      c.add(Go);
      Go.setBounds(95 + 7 * width + 70, gameSizeHeight - width - 50, height ,width);
      Go.addMouseListener(this);
      Go.addMouseMotionListener(this);
      Go.setActionCommand(Integer.toString(300));
    }
    
    if(playersNumber != 0){
      for(int i = 0; i < 5; i++){
        P0_Button[i] = new JButton();
        c.add(P0_Button[i]);
        P0_Button[i].setBounds(100 + (9 - i) * width, 80, width, height);
        P0_Button[i].addMouseListener(this);
        P0_Button[i].addMouseMotionListener(this);
        P0_Button[i].setActionCommand(Integer.toString(i));
      }
      
      for(int i = 0; i < 5; i++){
        P1_Button[i] = new JButton();
        c.add(P1_Button[i]);
        P1_Button[i].setBounds(20 + i * width, gameSizeHeight - 150 - height, width, height);
        P1_Button[i].addMouseListener(this);
        P1_Button[i].addMouseMotionListener(this);
        P1_Button[i].setActionCommand(Integer.toString(10 + i));
      }
      
      for(int i = 0; i < 5; i++){
        P2_Button[i] = new JButton();
        c.add(P2_Button[i]);
        P2_Button[i].setBounds(100 + (i + 5) * width, gameSizeHeight - 150 - height, width, height);
        P2_Button[i].addMouseListener(this);
        P2_Button[i].addMouseMotionListener(this);
        P2_Button[i].setActionCommand(Integer.toString(20 + i));
      }
      
      for(int i = 0; i < 5; i++){
        P3_Button[i] = new JButton();
        c.add(P3_Button[i]);
        P3_Button[i].setBounds(180 + (i + 10) * width, gameSizeHeight - 150 - height, width, height);
        P3_Button[i].addMouseListener(this);
        P3_Button[i].addMouseMotionListener(this);
        P3_Button[i].setActionCommand(Integer.toString(30 + i));
      }
      
      P0_score_Label = new JLabel("0");
      c.add(P0_score_Label);
      P0_score_Label.setBounds(95 + 7 * width, 45, height ,width);
      
      P1_score_Label = new JLabel("300");
      c.add(P1_score_Label);
      P1_score_Label.setBounds(15 + 2 * width, gameSizeHeight - 185 - height, height ,width);
      
      P2_score_Label = new JLabel("300");
      c.add(P2_score_Label);
      P2_score_Label.setBounds(95 + 7 * width, gameSizeHeight - 185 - height, height ,width);
      
      P3_score_Label = new JLabel("300");
      c.add(P3_score_Label);
      P3_score_Label.setBounds(175 + 12 * width, gameSizeHeight - 185 - height, height ,width);
    }else if(playersNumber == 0){
      for(int i = 0; i < 5; i++){
        P0_Button[i] = new JButton();
        c.add(P0_Button[i]);
        P0_Button[i].setBounds(100 + (5 + i) * width, gameSizeHeight - 150, width, height);
        P0_Button[i].addMouseListener(this);
        P0_Button[i].addMouseMotionListener(this);
        P0_Button[i].setActionCommand(Integer.toString(i));
      }
      
      for(int i = 0; i < 5; i++){
        P1_Button[i] = new JButton();
        c.add(P1_Button[i]);
        P1_Button[i].setBounds(180 + (14 - i) * width, 80, width, height);
        P1_Button[i].addMouseListener(this);
        P1_Button[i].addMouseMotionListener(this);
        P1_Button[i].setActionCommand(Integer.toString(10 + i));
      }
      
      for(int i = 0; i < 5; i++){
        P2_Button[i] = new JButton();
        c.add(P2_Button[i]);
        P2_Button[i].setBounds(100 + (9 - i) * width, 80, width, height);
        P2_Button[i].addMouseListener(this);
        P2_Button[i].addMouseMotionListener(this);
        P2_Button[i].setActionCommand(Integer.toString(20 + i));
      }
      
      for(int i = 0; i < 5; i++){
        P3_Button[i] = new JButton();
        c.add(P3_Button[i]);
        P3_Button[i].setBounds(20 + (4 - i) * width, 80, width, height);
        P3_Button[i].addMouseListener(this);
        P3_Button[i].addMouseMotionListener(this);
        P3_Button[i].setActionCommand(Integer.toString(30 + i));
      }
      
      P0_score_Label = new JLabel("0");
      c.add(P0_score_Label);
      P0_score_Label.setBounds(95 + 7 * width, gameSizeHeight - 160 - width, height ,width);
      
      P1_score_Label = new JLabel("300");
      c.add(P1_score_Label);
      P1_score_Label.setBounds(175 + 12 * width, 70 - width, height ,width);
      
      P2_score_Label = new JLabel("300");
      c.add(P2_score_Label);
      P2_score_Label.setBounds(95 + 7 * width, 70 - width, height ,width);
      
      P3_score_Label = new JLabel("300");
      c.add(P3_score_Label);
      P3_score_Label.setBounds(15 + 2 * width, 70 - width, height ,width);
    }
  }
  
  public void PlaceLabel(int playersNumer){
    if(playersNumer == 0){
      P0_Point = new JLabel("00");
      c.add(P0_Point);
      P0_Point.setBounds(95 + 9 * width, gameSizeHeight - 160 - width, height ,width);
      P0_Point.setForeground(Color.BLUE);
      
      P1_Point = new JLabel("11");
      c.add(P1_Point);
      P1_Point.setBounds(187 + 12 * width, 85 + height, height ,width);
      P1_Point.setForeground(Color.BLUE);
      
      P2_Point = new JLabel("22");
      c.add(P2_Point);
      P2_Point.setBounds(107 + 7 * width, 85 + height, height ,width);
      P2_Point.setForeground(Color.BLUE);
      
      P3_Point = new JLabel("33");
      c.add(P3_Point);
      P3_Point.setBounds(27 + 2 * width, 85 + height, height ,width);
      P3_Point.setForeground(Color.BLUE);
      
      Parent_Point = new JLabel("");
      c.add(Parent_Point);
      Parent_Point.setBounds(107 + 7 * width, 60 + width + height, height ,width);
      Parent_Point.setForeground(Color.BLUE);
      
    }else{
      P0_Point = new JLabel("00");
      c.add(P0_Point);
      P0_Point.setBounds(95 + 8 * width, gameSizeHeight - 160 - width, height ,width);
      P0_Point.setForeground(Color.BLUE);
      
      P1_Point = new JLabel("11");
      c.add(P1_Point);
      P1_Point.setBounds(27 + 2 * width, gameSizeHeight - 170 + width, height ,width);
      P1_Point.setForeground(Color.BLUE);
      
      P2_Point = new JLabel("22");
      c.add(P2_Point);
      P2_Point.setBounds(107 + 7 * width, gameSizeHeight - 170 + width, height ,width);
      P2_Point.setForeground(Color.BLUE);
      
      P3_Point = new JLabel("33");
      c.add(P3_Point);
      P3_Point.setBounds(187 + 12 * width, gameSizeHeight - 170 + width, height ,width);
      P3_Point.setForeground(Color.BLUE);
      
      Parent_Point = new JLabel("99");
      c.add(Parent_Point);
      Parent_Point.setBounds(107 + 7 * width, 60 + width + height, height ,width);
      Parent_Point.setForeground(Color.BLUE);
    }
    
  }
  
  public void PlaceTextField(int playersNumber){  //boolean bet_statusとかを設定してその辺をうまくやる
    if(playersNumber != 0 && !BET_STATUS){
      BET_LABEL = new JLabel("掛け金を入力してください");
      c.add(BET_LABEL);
      BET_LABEL.setBounds(650, 280, 150, 25);
      BET_LABEL.addMouseListener(this);
      BET_LABEL.setForeground(Color.BLACK);
      BET_LABEL.setBackground(Color.WHITE);
      BET_LABEL.setOpaque(true);
      
      BET_INPUT = new JTextField();
      c.add(BET_INPUT);
      BET_INPUT.setBounds(650, 305, 150, 50);
      
      BET_PUSH = new JButton(BET);
      c.add(BET_PUSH);
      BET_PUSH.setBounds(14 * width + 290, gameSizeHeight - 2 * width - 45, height, width);
      BET_PUSH.addMouseListener(this);
      BET_PUSH.addMouseMotionListener(this);
      BET_PUSH.setActionCommand("400");
    }
  }
  
  public ImageIcon linkCards(int num){
    int mark = num / 13;
    int cardNumber = num % 13;
    ImageIcon resultImageIcon = backgroundBlue;
    
    if(num >= 0){
      if(mark == 0){
        resultImageIcon = heart[cardNumber];
      }else if(mark == 1){
        resultImageIcon = diamond[cardNumber];
      }else if(mark == 2){
        resultImageIcon = spade[cardNumber];
      }else if(mark == 3){
        resultImageIcon = clover[cardNumber];
      }
    }
    return resultImageIcon;
  }
  
  public void showIcon(){
    for(int i = 0; i < 5; i++){
      P0_Button[i].setIcon(linkCards(P0_cards[i]));
      P1_Button[i].setIcon(linkCards(P1_cards[i]));
      P2_Button[i].setIcon(linkCards(P2_cards[i]));
      P3_Button[i].setIcon(linkCards(P3_cards[i]));
    }
    
    if(myNumber != 0){
      P0_Button[0].setIcon(back);
    }
    
    P1_Button[1].setIcon(back);
    P2_Button[1].setIcon(back);
    P3_Button[1].setIcon(back);
  }
  
  public void showIcon2(){
    for(int i = 0; i < 5; i++){
      P0_Button[i].setIcon(linkCards(P0_cards[i]));
      P1_Button[i].setIcon(linkCards(P1_cards[i]));
      P2_Button[i].setIcon(linkCards(P2_cards[i]));
      P3_Button[i].setIcon(linkCards(P3_cards[i]));
    }
    
    if(myNumber != 0){
      P0_Button[0].setIcon(back);
    }
  }
  
  public void showLabel(){
    P0_Point.setText(Integer.toString(calcBJ(P0_cards)));
    P1_Point.setText(Integer.toString(calcBJ(P1_cards)));
    P2_Point.setText(Integer.toString(calcBJ(P2_cards)));
    P3_Point.setText(Integer.toString(calcBJ(P3_cards)));
  }
  
  public int calcBJ(int arr[]){
    int total = 0;
    int numAce = 0;
    
    int arr2 [] = new int [5];
    for(int i = 0; i < 5; i++){
      if(arr[i] == -1){
        arr2[i] = 0;
      }else{
        arr2[i] = arr[i] % 13 + 1;
      }
    }
    
    for(int i = 0; i < 5; i++){
      if(arr2[i] == 1){
        numAce++;
      }else if(10 <= arr2[i] && arr2[i] <= 13){
        total += 10;
      }else{
        total += arr2[i];
      }
    }
    
    for(int i = 0; i < numAce; i++){
      if(total + 11 <= 21){
        total += 11;
      }else{
        total += 1;
      }
    }
    
    return total;
  }
  
  public boolean judgement(){
    boolean result = true;
    for(int i = 0; i < 3; i++){
      if(posession[i] == 0){
        result = false;
      }
    }
    return result;
  }
  
  public void calcChildRank(int [] array, int [] ranking){
    int [] sortedArray = Arrays.copyOf(array, 3);
    Arrays.sort(sortedArray);
    
    for(int i = 0; i < 3; i++){
      int index = Arrays.binarySearch(sortedArray, array[i]);
      ranking[i] = 3 - index;
    }
    
    for(int i = 0; i < 3; i++){
      if(array[i] == 0){
        ranking[i] = 4;
      }
    }
  }
  
}