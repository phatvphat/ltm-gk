/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package KTGK;

import DB.DBQuery;
import Models.Friend;
import Models.RecentMessage;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import org.json.JSONObject;

/**
 *
 * @author phatv
 */
public class Dashboard extends javax.swing.JFrame {

    String domain = "127.0.0.1";
    int port = 4321;
    DatagramSocket socket;

    public int user_id;
    public String username;

    AddFriends frmAddFr;

    DBQuery query = new DBQuery();
    DefaultListModel listFriends = new DefaultListModel();
    DefaultListModel listRecentMessages = new DefaultListModel();
    private String hashFriends = "";
    private String hashRecentMessages = "";

    private int receiver_id = 0;
    private boolean receiveMessage = false;

    /**
     * Creates new form Dashboard
     */
    public Dashboard() {
        initComponents();
    }

    public Dashboard(int user_id, String username) {
        this.user_id = user_id;
        this.username = username;

        initComponents();

        lblHello.setText("Xin chào, " + username.toUpperCase() + "!");
        new Thread(() -> {
            while (true) {
                ListFriends();
                ListCurrentMessages();
                try {
                    Thread.sleep(800);
                } catch (InterruptedException ex) {
                }
            }
        }).start();
        frmAddFr = new AddFriends(this);

        try {
            socket = new DatagramSocket();
        } catch (Exception e) {
            System.out.println("Error init socket: " + e.toString());
        }
    }

    private void ListFriends() {
        String hash = "";
        DefaultListModel list = new DefaultListModel();
        ResultSet rs = query.ListFriends(user_id);
        try {
            while (rs.next()) {
                Friend f = new Friend(rs.getInt("id"), rs.getInt("user_id"), rs.getInt("friend_id"), rs.getInt("status"), rs.getString("username"));
                list.addElement(f);
                hash += f.toString();
            }
        } catch (SQLException ex) {
        }
        hash = Utils.hashMD5(hash);
        if (hashFriends.equals("") || !hashFriends.equals(hash)) {
            hashFriends = hash;
            listFriends.clear();
            listFriends = list;
            lstFriends.removeAll();
            lstFriends.setModel(list);
        }
    }

    private void ListCurrentMessages() {
        String hash = "";
        DefaultListModel list = new DefaultListModel();
        ResultSet rs = query.ListRecentMessages(user_id);
        try {
            while (rs.next()) {
                RecentMessage rm = new RecentMessage(rs.getInt("id"), rs.getInt("sender_id"), rs.getInt("receiver_id"), rs.getString("message"), rs.getString("created_at"), rs.getInt("user_id"), rs.getString("username"));
                list.addElement(rm);
                hash += rm.toString();
            }
        } catch (SQLException ex) {
        }
        hash = Utils.hashMD5(hash);
        if (hashRecentMessages.equals("") || !hashRecentMessages.equals(hash)) {
            hashRecentMessages = hash;
            listRecentMessages.clear();
            listRecentMessages = list;
            lstRecentMessages.removeAll();
            lstRecentMessages.setModel(list);
        }
    }

    private void joinServer() {
        byte[] sendData;

        JSONObject jsonObj = new JSONObject();
        try {
//            socket = new DatagramSocket();
            InetAddress ipServer = InetAddress.getByName(domain);
            jsonObj.put("action", "join");
            jsonObj.put("sender_id", user_id);
            jsonObj.put("username", username);
            sendData = jsonObj.toString().getBytes();
            DatagramPacket sendPacket;
            sendPacket = new DatagramPacket(sendData, sendData.length, ipServer, port);
            socket.send(sendPacket);
//            socket.close();
        } catch (Exception e) {
            System.out.println("Error join: " + e.toString());
        }
    }

    private void openChat(int receiver_id, String receiver_username) {
        if (this.receiver_id != receiver_id) {
            this.receiver_id = receiver_id;
            receiveMessage = true;
            txtTitle.setText("Chat box: " + receiver_username);
//            txtChatBox.setText("");
            ResultSet rs = query.GetMessages(user_id, receiver_id);
            String content = "";
            try {
                while (rs.next()) {
                    String colorName = user_id == rs.getInt("sender_id") ? "<font color='red'>" + username + "</font>" : receiver_username;
                    content += "<br/><span title='" + rs.getString("created_at") + "'><b>" + colorName + ":</b> " + rs.getString("message") + "</span>";
                }

                txtChatBox.setText("");
                HTMLEditorKit kit = new HTMLEditorKit();
                HTMLDocument doc = (HTMLDocument) txtChatBox.getStyledDocument();
                txtChatBox.setEditorKit(kit);
                txtChatBox.setDocument(doc);
                try {
                    kit.insertHTML(doc, doc.getLength(), content, 0, 0, null);
                } catch (Exception ex) {
//                    Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (SQLException ex) {
//                Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
            }
            txtMessage.setEditable(true);

            new Thread(() -> {
                String receiveData = "";
                byte[] buffer = new byte[1024];
                // 1 trong 2 mới mở luồng nhận tin ???
                while (this.receiver_id > 0 || receiveMessage) {
                    try {
                        // Gói tin nhận
                        DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                        socket.receive(incoming);
                        receiveData = new String(incoming.getData(), 0, incoming.getLength());
                        // Đổi dữ liệu nhận được dạng mảng bytes ra chuỗi và in ra màn hình
                        System.out.println("Received: " + receiveData);

                        JSONObject jsonObj = new JSONObject(receiveData);
                        String action = jsonObj.getString("action");
                        if (action.equals("chat")) {
                            int sender_id = jsonObj.getInt("sender_id");
                            if (user_id == sender_id || user_id == this.receiver_id || this.receiver_id == sender_id) {
                                String sender_username = jsonObj.getString("username");
                                String message = jsonObj.getString("message");

                                HTMLEditorKit kit = new HTMLEditorKit();
                                HTMLDocument doc = (HTMLDocument) txtChatBox.getStyledDocument();
                                txtChatBox.setEditorKit(kit);
                                txtChatBox.setDocument(doc);
                                String colorName = username.equals(sender_username) ? "<font color='red'>" + sender_username + "</font>" : sender_username;
                                kit.insertHTML(doc, doc.getLength(), "<b>" + colorName + ":</b> " + message, 0, 0, null);

//                            Rectangle r = modelToView(doc.getLength());
//                            if (r != null) {
//                                scrollRectToVisible(r);
//                            }
                                txtMessage.setText("");

                                if (message.toLowerCase().contains("buzz")) {
                                    Utils.vibrate(this);
                                }
                            }
                        }

                    } catch (Exception e) {
                        System.out.println("Error receiving: " + e.toString());
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Dashboard.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }).
                    start();
        }

        joinServer();
    }

    private void sendChat() {
        joinServer();
        if (receiver_id > 0) {
            new Thread(() -> {
                byte[] sendData;
                byte[] buffer = new byte[1024];
//                DatagramSocket socket;

                String stSend = txtMessage.getText().trim();
                JSONObject jsonObj = new JSONObject();
                if (stSend.length() > 0) {
                    // Limit text
                    if (stSend.length() >= 500) {
                        stSend = stSend.substring(0, 500);
                    }
                    try {
//                        socket = new DatagramSocket();
                        InetAddress ipServer = InetAddress.getByName(domain);
//                        sendData = stSend.getBytes();
//                    jsonObj.put("address", socket.getInetAddress());
//                    jsonObj.put("port", socket.getPort());
                        jsonObj.put("action", "chat");
                        jsonObj.put("username", username);
                        jsonObj.put("sender_id", user_id);
                        jsonObj.put("receiver_id", receiver_id);
                        jsonObj.put("message", stSend);
                        sendData = jsonObj.toString().getBytes();
                        DatagramPacket sendPacket;
                        sendPacket = new DatagramPacket(sendData, sendData.length, ipServer, port);
                        socket.send(sendPacket);
                    } catch (Exception e) {
                        System.out.println("Error send: " + e.toString());
                    }
                }
            }).start();
        }
    }

    private void closeChat() {
        receiver_id = 0;
        receiveMessage = false;
        txtTitle.setText("Chat box: ");
        txtChatBox.setText("");
        txtMessage.setEditable(false);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblHello = new javax.swing.JLabel();
        btnAddFr = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstRecentMessages = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        lstFriends = new javax.swing.JList<>();
        txtTitle = new javax.swing.JLabel();
        txtMessage = new javax.swing.JTextField();
        btnSendMessage = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        btnExitChat = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtChatBox = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        lblHello.setText("Xin chào,");

        btnAddFr.setText("Tìm & Kết bạn");
        btnAddFr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFrActionPerformed(evt);
            }
        });

        lstRecentMessages.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstRecentMessagesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(lstRecentMessages);

        jLabel1.setText("Tin nhắn gần đây:");

        jLabel2.setText("Danh sách bạn bè:");

        lstFriends.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstFriendsMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(lstFriends);

        txtTitle.setText("Chat box:");

        txtMessage.setEditable(false);
        txtMessage.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtMessageKeyPressed(evt);
            }
        });

        btnSendMessage.setText("Gửi");
        btnSendMessage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendMessageActionPerformed(evt);
            }
        });

        btnExit.setText("Thoát");
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });

        btnLogout.setText("Đăng xuất");
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        btnExitChat.setText("Thoát chat");
        btnExitChat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitChatActionPerformed(evt);
            }
        });

        txtChatBox.setEditable(false);
        txtChatBox.setContentType("text/html"); // NOI18N
        jScrollPane3.setViewportView(txtChatBox);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lblHello)
                                .addGap(5, 5, 5))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(btnAddFr)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnExitChat))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                                        .addGap(18, 18, 18)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(txtTitle)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(txtMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnSendMessage))
                                            .addComponent(jScrollPane3))))
                                .addGap(18, 18, 18)))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnLogout)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnExit))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblHello)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAddFr)
                    .addComponent(btnExit)
                    .addComponent(btnLogout)
                    .addComponent(btnExitChat))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1)
                    .addComponent(txtTitle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
                    .addComponent(jScrollPane2)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSendMessage))))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_btnExitActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        frmAddFr.dispose();
        dispose();
        Login frmLogin = new Login();
        frmLogin.setLocationRelativeTo(null);
        frmLogin.setVisible(true);
    }//GEN-LAST:event_btnLogoutActionPerformed

    private void btnAddFrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFrActionPerformed
//        frmAddFr.setLocationRelativeTo(null);
        frmAddFr.setVisible(true);
    }//GEN-LAST:event_btnAddFrActionPerformed

    private void lstFriendsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstFriendsMouseClicked
        // Double-click detected
        if (evt.getClickCount() == 2) {
            int i = lstFriends.getSelectedIndex();
            if (i >= 0) {
                Friend f = (Friend) listFriends.getElementAt(i);
                openChat(user_id == f.user_id ? f.friend_id : f.user_id, f.username);
            }
        }
    }//GEN-LAST:event_lstFriendsMouseClicked

    private void btnSendMessageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendMessageActionPerformed
        sendChat();
    }//GEN-LAST:event_btnSendMessageActionPerformed

    private void lstRecentMessagesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lstRecentMessagesMouseClicked
        // Double-click detected
        if (evt.getClickCount() == 2) {
            int i = lstRecentMessages.getSelectedIndex();
            if (i >= 0) {
                RecentMessage rm = (RecentMessage) listRecentMessages.getElementAt(i);
                openChat(user_id == rm.sender_id ? rm.receiver_id : rm.sender_id, rm.username);
            }
        }
    }//GEN-LAST:event_lstRecentMessagesMouseClicked

    private void btnExitChatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExitChatActionPerformed
        closeChat();
    }//GEN-LAST:event_btnExitChatActionPerformed

    private void txtMessageKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtMessageKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            sendChat();
        }
    }//GEN-LAST:event_txtMessageKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;

                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);

        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class
                    .getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Dashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddFr;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnExitChat;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnSendMessage;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel lblHello;
    private javax.swing.JList<String> lstFriends;
    private javax.swing.JList<String> lstRecentMessages;
    private javax.swing.JTextPane txtChatBox;
    private javax.swing.JTextField txtMessage;
    private javax.swing.JLabel txtTitle;
    // End of variables declaration//GEN-END:variables
}
