
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class Principal {

    String serverAdress;
    Scanner in;
    PrintWriter out;
    JFrame frame= new JFrame("Chat");
    JTextField textField= new JTextField(50);
    JTextArea messageArea= new JTextArea(16, 50);
    
    public Principal(String serverAdress){
        this.serverAdress= serverAdress;
        textField.setEditable(false);
        messageArea.setEditable(false);
        
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();
        
        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText().trim());
                textField.setText("");
            }
        });
    }
    
    private String getNombre(){
        return JOptionPane.showInputDialog(frame, "Nombre de pantalla: ", "Selecciona nombre de pantalla", JOptionPane.PLAIN_MESSAGE);
    }
    private String getContraseña(){
        return JOptionPane.showInputDialog(frame, "Contraseña", "Escriba su contraseña", JOptionPane.PLAIN_MESSAGE);
    }
    
    private void run() throws IOException {
        try{
            Socket socket= new Socket(serverAdress, 59001);
            in= new Scanner(socket.getInputStream());
            out= new PrintWriter(socket.getOutputStream(), true);
            
            while (in.hasNextLine()) {                
                String line= in.nextLine();
                if(line.startsWith("SUBMITNAME")){
                    out.println(getNombre() + ";" + getContraseña());
                } else if(line.startsWith("NAMEACCEPTED")){
                    this.frame.setTitle("Chat - " + line.substring(13,line.indexOf(";")));
                    textField.setEditable(true);
                } else if(line.startsWith("MESSAGE")){
                    messageArea.append(line.substring(8) + "\n");
                }
            }
        } finally { 
            frame.setVisible(false);
            frame.dispose();
        }
    }    
    
    public static void main(String[] args) throws Exception{
        if(args.length != 1){
            System.err.println("La dirección IP es incorrecta");
            return;
        }
        Principal cliente= new Principal(args[0]);
        cliente.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        cliente.frame.setVisible(true);
        cliente.run();
    }
    
}
