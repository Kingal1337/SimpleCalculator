/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Alan Tsui
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 */
package simplecalculator;

import alanutilites.math.Calculator;
import alanutilites.math.Calculator.CalculatorResult;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.text.DefaultEditorKit;

/**
 *
 * @author Alan Tsui
 * @version 1.0
 * @since 1.0
 */
public class UI extends JPanel implements ComponentListener{
    private StringBuilder currentEquation;
    private JTextField equation;
    private JTextField equationLine;
    private String[][] buttonStrings;
    private JButton[][] buttons;
    public UI(){
        init();
        
        KeyboardFocusManager manager
                = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new KeyDispatcher());
    }
    
    private void initButtons(){
        
        
        buttonStrings = new String[][]{
            new String[]{"C","<-"},
            new String[]{"(",")","^","%"},
            new String[]{"7","8","9","÷"},
            new String[]{"4","5","6","x"},
            new String[]{"1","2","3","-"},
            new String[]{".","0","=","+"}
        };
        
        buttons = new JButton[6][];
    
        GridBagConstraints c = new GridBagConstraints();   
        
        for(int i=0;i<buttonStrings.length;i++){
            buttons[i] = new JButton[buttonStrings[i].length]; 
            for(int j=0;j<buttonStrings[i].length;j++){
                JButton button = new JButton(buttonStrings[i][j]);
                button.setFocusPainted(false);
                buttons[i][j] = button;
                c.weightx = 0.5;
                c.weighty = 2;
                c.fill = GridBagConstraints.BOTH;
                c.gridx = j*(4/buttonStrings[i].length);
                c.gridwidth = 4/buttonStrings[i].length;
                c.gridy = i+1;
                add(buttons[i][j],c);
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String text = ((JButton)e.getSource()).getText();
                        addText(text);
                        equationLine.requestFocus();
                    }
                });
            }
        }
    }
    
    private void init(){
        currentEquation = new StringBuilder();
        
        setLayout(new GridBagLayout()); 
        
        initButtons(); 
        
        equationLine = new JTextField();
        equationLine.getActionMap().get(DefaultEditorKit.deletePrevCharAction).setEnabled(false);        
        equationLine.setEditable(false);
        equationLine.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        equationLine.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                equationLine.getCaret().setVisible(true);
            }

            @Override
            public void focusGained(FocusEvent e) {
                equationLine.getCaret().setVisible(true);
            }
        });
        
        GridBagConstraints c = new GridBagConstraints();   
        c.weightx = 0;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 4;
        add(equationLine,c);   
        
        addComponentListener(this);
    }
    
    private void addText(String text){        
        int position = equationLine.getCaret().getMark();
        if(currentEquation.toString().equals("Error")){
            currentEquation.delete(0, currentEquation.length());
            position = 0;
        }
        switch (text) {
            case "<-":
                if(currentEquation.length() != 0 && position > 0){
                    currentEquation.deleteCharAt(position-1);
                }   position = position-2 < 0 ? -1 : position-2;
                break;
            case "C":
                currentEquation.delete(0, currentEquation.length());
                break;
            case "=":
                String equation = currentEquation.toString();
                currentEquation.delete(0, currentEquation.length());
                String string = calculate(equation);
                if(string.endsWith(".0")){
                    string = string.substring(0, string.length()-2);
                }
                currentEquation.append(string);
                position = string.length();
                break;
            default:
                currentEquation.insert(position, text);
                break;
        }
        String currentEquationString = currentEquation.toString();        
        equationLine.setText(currentEquationString);
        
        position = (position+1 >= currentEquationString.length() ? equationLine.getCaret().getMark() : position+1);        
        equationLine.setCaretPosition(position);
    }
    
    private String calculate(String equation){
        equation = equation.replaceAll("x", "*").replaceAll("X", "*").replaceAll("÷", "/");  
        CalculatorResult answer = Calculator.evaluate(equation);
        return (answer.text.equals("Success") ? answer.answer+"" : "Error");
        
    }
    
    @Override
    public void componentResized(ComponentEvent e) {
        Dimension dimension = e.getComponent().getSize();
        
        int fontSize = ((dimension.width+dimension.height)/2)/18;
        
        Font font = new Font("Comic Sans MS",0,fontSize);
        equationLine.setFont(font);
        for (JButton[] buttonArray : buttons) {
            for (JButton button : buttonArray) {
                button.setFont(font);
            }
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {}
    @Override
    public void componentShown(ComponentEvent e) {}
    @Override
    public void componentHidden(ComponentEvent e) {}
    
    private class KeyDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_TYPED) {
                String key = e.getKeyChar()+"";
                int keyButton = e.getKeyChar();
                if(keyButton == KeyEvent.VK_ESCAPE){
                    addText("C");
                }
                else if(keyButton == KeyEvent.VK_ENTER){
                    addText("=");
                }
                else if(keyButton == KeyEvent.VK_BACK_SPACE){
                    addText("<-");
                }
                else if("1234567890*xX/-+%^()^.".contains(key)){
                    addText(key);
                }
            }
            return false;
        }
    }
}
