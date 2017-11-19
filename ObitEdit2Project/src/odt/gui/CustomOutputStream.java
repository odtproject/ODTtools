package odt.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.awt.Font;
import java.awt.TextArea;

public class CustomOutputStream extends OutputStream  {
	private TextArea textArea;
    
    public CustomOutputStream(TextArea textArea) {
        this.textArea = textArea;
    }
     
    @Override
    public void write(int b) throws IOException {
        // redirects data to the text area
        textArea.append(String.valueOf((char)b));
        // scrolls the text area to the end of data
        textArea.setCaretPosition(textArea.getText().length());
        
        textArea.setFont(new Font("Lucida Console", Font.PLAIN, 12));
    }
}
