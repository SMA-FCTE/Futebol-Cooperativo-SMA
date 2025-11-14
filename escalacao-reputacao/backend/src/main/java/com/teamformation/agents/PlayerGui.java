package com.teamformation.agents;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PlayerGui extends JFrame {

    private PlayerAgent myAgent;
    private JTextField positionField, reputationField;

    PlayerGui(PlayerAgent a) {
        super(a.getLocalName());
        myAgent = a;

        JPanel p = new JPanel(new GridLayout(2, 2));

        p.add(new JLabel("Posição:"));
        positionField = new JTextField(15);
        p.add(positionField);

        p.add(new JLabel("Reputação:"));
        reputationField = new JTextField(15);
        p.add(reputationField);

        getContentPane().add(p, BorderLayout.CENTER);

        JButton saveButton = new JButton("Salvar");

        saveButton.addActionListener(e -> {
            try {
                String posicao = positionField.getText().trim();
                String reputacao = reputationField.getText().trim();

                myAgent.updateAttributes(posicao, reputacao);

                positionField.setText("");
                reputationField.setText("");

                JOptionPane.showMessageDialog(this, "Atributos atualizados!",
                        "Confirmação", JOptionPane.INFORMATION_MESSAGE);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel p2 = new JPanel();
        p2.add(saveButton);
        getContentPane().add(p2, BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                myAgent.doDelete();
            }
        });

        setResizable(true);
    }

    public void showGui() {
        pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(d.width / 2 - getWidth() / 2, d.height / 2 - getHeight() / 2);
        setVisible(true);
    }
}
