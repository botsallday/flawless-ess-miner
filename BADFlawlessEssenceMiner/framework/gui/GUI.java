package scripts.BADFlawlessEssenceMiner.framework.gui;

@SuppressWarnings("serial")
public class GUI extends javax.swing.JFrame {

    /**
     * Creates new form UI
     */
    public GUI() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {
    	System.out.println("Inside init");
        jInternalFrame1 = new javax.swing.JInternalFrame();
        combobox = new javax.swing.JComboBox<>();

        jInternalFrame1.setVisible(true);

        javax.swing.GroupLayout jInternalFrame1Layout = new javax.swing.GroupLayout(jInternalFrame1.getContentPane());
        jInternalFrame1.getContentPane().setLayout(jInternalFrame1Layout);
        jInternalFrame1Layout.setHorizontalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jInternalFrame1Layout.setVerticalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        combobox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Bronze", "Iron", "Steel", "Black", "Mithril", "Adamant", "Rune", "Dragon"}));
        combobox.setSelectedItem("Bronze");
        combobox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(168, 168, 168)
                .addComponent(combobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(173, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(101, 101, 101)
                .addComponent(combobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(177, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                        
    
    public String getPicaxeValue() {
        return combobox.getSelectedItem().toString()+" pickaxe";
    }
    
    public boolean getWaitGui() {
    	return waitGui;
    }
    
    private void comboboxActionPerformed(java.awt.event.ActionEvent evt) {                                         
        System.out.println(getPicaxeValue());
        
        if (getPicaxeValue() != null) {
        	waitGui = false;
        }
    }                                        



    // Variables declaration - do not modify                     
    private javax.swing.JComboBox<String> combobox;
    private javax.swing.JInternalFrame jInternalFrame1;
    private boolean waitGui = true;
    // End of variables declaration                   


}
