
/**
 *
 * @author FurkanKarabulut
 */
package ChatProgrami;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class Client extends Thread {

    private final int login = 1, signup = 0, msgtype = 2, file = 3, activeFriendList = 4, activeAnonymousList = 5, inactiveFriends = 6;
    private final int singleActiveFriend = 7, singleAnonymous = 8, logout = 9, frequest = 10, prevMsg = 11, accepted = 12;
    private final int rejected = 13, offline = 14,fileReached = 15;
    private String host;
    private int port;
    private Socket cs;
    private ObjectInputStream sockInput;
    private ObjectOutputStream sockOutput;
    private InputStream is;
    private OutputStream os;
    ClientGUI gui1;
    ClientGUI2 gui2;

    public Client(String h, int p) {

        host = h;
        port = p;

    }

    public void startClient() throws IOException {
        cs = new Socket(host, port);
        os = cs.getOutputStream();
        sockOutput = new ObjectOutputStream(os);
        is = cs.getInputStream();
        sockInput = new ObjectInputStream(is);

        gui1 = new ClientGUI(sockInput, sockOutput);
        gui2 = new ClientGUI2(sockInput, sockOutput);
        gui2.setVisible(false);
        gui1.setVisible(true);
        gui1.setResizable(false);
        gui2.setResizable(false);
    }

    public void run() {
        ChatMessage chmsg;
        try {
            startClient();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error Connecting....Try again.", "Connection Failed!", JOptionPane.ERROR);
            return;
        }
        while (true) {
            try {
                chmsg = (ChatMessage) sockInput.readObject();
                switch (chmsg.getType()) {
                    case login:
                    case signup:
                        if (chmsg.getMessage().equals("basarılı")) {

                            String usr = new String(gui1.getUserName());
                            gui1.setVisible(false);
                            gui2.setVisible(true);
                            gui2.setUserName(usr);
                            String basarılı = (chmsg.getType() == login ? "Giriş başarılı!" : "Kayıt ve Başarılı Giriş Yapıldı!");
                            JOptionPane.showMessageDialog(gui2, basarılı);
                        } else {
                            String failed = (chmsg.getType() == login ? "Yanlış Kullanıcı Adı veya Şifre.Tekrar Deneyin." : "Kullanıcı zaten var.");
                            JOptionPane.showMessageDialog(gui1, failed);

                        }
                        gui1.resetFrame();
                        break;
                    case activeFriendList:
                    case inactiveFriends:
                    case activeAnonymousList:
                    case singleActiveFriend:
                    case singleAnonymous:
                        gui2.setFriendList(chmsg);
                        break;

                    case msgtype:
                        gui2.showNewMessage(chmsg);
                        System.out.println(chmsg.getMessage());
                        break;
                    case prevMsg:
                        gui2.showAllMessage(chmsg);
                        break;
                    case frequest:
                        gui2.showFriendRequest(chmsg);
                        break;
                    case accepted:
                    case rejected:
                        gui2.handleRequestResponse(chmsg);
                        break;
                    case file:
                        gui2.downloadFile(chmsg);
                        sockOutput.writeObject(new ChatMessage(fileReached,chmsg.getMessage(),chmsg.getDest(),chmsg.getSource()));
                        System.out.println("ack sent");
                        break;
                    case offline:
                        gui2.updateOffline(chmsg.getSource());
                        break;
                    case logout:
                        gui2.resetFrame();
                        gui2.setVisible(false);
                        gui1.setVisible(true);
                        JOptionPane.showMessageDialog(gui1, "Oturumunuz Kapatıldı.", "Çıkış Yapıldı", JOptionPane.PLAIN_MESSAGE);
                        break;
                    case fileReached:
                        System.out.println("acc rec");
                        JOptionPane.showMessageDialog(gui2, "File delivered to "+chmsg.getSource(),"File Transfer",JOptionPane.PLAIN_MESSAGE);
                        break;
                        
                }

            } catch (IOException ex) {
                System.out.println("offf");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void main(String[] args) {
        int port = 5555;
        String host = "localhost";

        if (args.length == 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }

        Client client = new Client(host, port);
        client.start();

    }

}
