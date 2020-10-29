// Muhamad Elassar
// CNT 4714
// Project 1 – Spring 2020

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import javax.swing.*;
import java.util.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class BookStore extends JFrame {

    private double orderSubtotal = 0;
    private double orderTotal = 0;
    private int totalItems = 0;
    private ArrayList<String> items = new ArrayList<>(); //all confirmed items
    private StringBuilder viewOrder = new StringBuilder();
    private StringBuilder orderInvoice = new StringBuilder();
    File transactionsFile = new File("transactions.txt");
    private String itemInfo = new String();
    private ArrayList<Book> inventory;
    private Integer itemCount = 0;
    private Integer numOfItemsInOrder;
    private Integer bookID;
    private Integer quantityOfItem;

    // create text fields and their labels
    JLabel numItemsLabel = new JLabel("Enter number of items in this order:       ", JLabel.RIGHT);
    private JTextField numItemsTextField = new JTextField();

    JLabel bookIdLabel = new JLabel("Enter Book ID for Item #1:       ", JLabel.RIGHT);
    private JTextField bookIdTextField = new JTextField();

    JLabel quantityLabel = new JLabel("Enter Quantitiy for Item #1:       ", JLabel.RIGHT);
    private JTextField quantityTextField = new JTextField();

    JLabel itemInfoLabel = new JLabel("Item #1 Info:       ", JLabel.RIGHT);
    private JTextField itemInfoTextField = new JTextField();

    JLabel subtotalLabel = new JLabel("Order Subtotal for 0 item(s):       ", JLabel.RIGHT);
    private JTextField subtotalTextField = new JTextField();

    // create the buttons
    private JButton processButton = new JButton("Process Item #1");
    private JButton confirmButton = new JButton("Confirm Item #1");
    private JButton viewOrderButton = new JButton("View Order");
    private JButton finishOrderButton = new JButton("Finish Order");
    private final JButton newOrderButton = new JButton("New Order");
    private final JButton exitButton = new JButton("Exit");

    public static void main(String[] args) throws FileNotFoundException {
        BookStore frame = new BookStore();
        frame.pack(); // fit windows for screen
        frame.setTitle("Ye Olde Book Shoppe - Spring 2020");
        frame.setLocationRelativeTo(null); // center windows
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // close window
        frame.setVisible(true); // display window
    }

    public BookStore() throws FileNotFoundException {
        // get the inventory from the file and store it in arraylist so it can 
        // be accessed later
        this.parseInventory();

        // set the color of the field labels to yellow
        numItemsLabel.setForeground(Color.yellow);
        bookIdLabel.setForeground(Color.yellow);
        quantityLabel.setForeground(Color.yellow);
        itemInfoLabel.setForeground(Color.yellow);
        subtotalLabel.setForeground(Color.yellow);

        // create a jpanel for the fields and their labels and add them to the panel
        JPanel fieldPanel = new JPanel(new GridLayout(8, 3, 0, 5));
        fieldPanel.add(new JLabel(""));
        fieldPanel.add(new JLabel(""));
        fieldPanel.add(numItemsLabel);
        fieldPanel.add(numItemsTextField);
        fieldPanel.add(bookIdLabel);
        fieldPanel.add(bookIdTextField);
        fieldPanel.add(quantityLabel);
        fieldPanel.add(quantityTextField);
        fieldPanel.add(itemInfoLabel);
        fieldPanel.add(itemInfoTextField);
        fieldPanel.add(subtotalLabel);
        fieldPanel.add(subtotalTextField);
        fieldPanel.setBackground(Color.black);

        // creat a second panel just for the buttons which will go on the bottom
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonsPanel.add(processButton);
        buttonsPanel.add(confirmButton);
        buttonsPanel.add(viewOrderButton);
        buttonsPanel.add(finishOrderButton);
        buttonsPanel.add(newOrderButton);
        buttonsPanel.add(exitButton);
        buttonsPanel.setBackground(Color.BLUE);

        // the subtotal and iteminfo text fields will always be disabled
        this.subtotalTextField.setEnabled(false);
        this.itemInfoTextField.setEnabled(false);

        // initialize the confirm, viewOrder and finishOrder buttons to disabled
        this.confirmButton.setEnabled(false);
        this.viewOrderButton.setEnabled(false);
        this.finishOrderButton.setEnabled(false);

        //add the panels to the frame
        add(fieldPanel, BorderLayout.NORTH);
        add(buttonsPanel, BorderLayout.SOUTH);

        // action event on the process button
        processButton.addActionListener((ActionEvent e) -> {
            // get the info entered by the user in the text fields
            numOfItemsInOrder = Integer.parseInt(numItemsTextField.getText());
            bookID = Integer.parseInt(bookIdTextField.getText());
            quantityOfItem = Integer.parseInt(quantityTextField.getText());

            numItemsTextField.setEnabled(false);

            // search for book
            Book desiredBook = bookSearch(bookID);

            // if the book was found, get its information
            if (desiredBook != null) {
                itemCount++;

                // set the item info for transactions file
                this.itemInfo = Integer.toString(desiredBook.getBookID()) + "," + 
                        desiredBook.getTitle() + ", " + Double.toString(desiredBook.getPrice()) + 
                        ", " + Integer.toString(quantityOfItem) + ", " + 
                        Integer.toString(this.getDiscountPercentage(quantityOfItem)) + ", " + 
                        new DecimalFormat("0.00").format(this.getTotalDiscount(quantityOfItem, desiredBook.getPrice())) + ", ";

                // display book info in the text field
                String bookInfo = desiredBook.getBookID() + desiredBook.getTitle() + " $"
                        + desiredBook.getPrice() + " " + quantityOfItem + " "
                        + this.getDiscountPercentage(quantityOfItem) + "% $"
                        + new DecimalFormat("0.00").format(this.getTotalDiscount(quantityOfItem, desiredBook.getPrice()));
                itemInfoTextField.setText(bookInfo);

                // disable the process button and enable confirm button
                processButton.setEnabled(false);
                confirmButton.setEnabled(true);
                
                // add to the subtotal
                this.addToSubtotal(quantityOfItem, desiredBook.getPrice());
                
                // change the item info text
                itemInfoLabel.setText("Item #" + itemCount + " info:       ");
            } 
            else {
                JOptionPane.showMessageDialog(null, "Book ID " + bookID + " not in file.");
            }
        });

        confirmButton.addActionListener((ActionEvent e) -> {

            this.totalItems++;

            JOptionPane.showMessageDialog(null, "Item #" + this.totalItems + " accepted");

            //prepare transaction.txt line
            this.fileTransaction();

            //add item to viewOrder
            this.addToOrderList(itemInfoTextField.getText());

            //enable buttons
            processButton.setEnabled(true);
            viewOrderButton.setEnabled(true);
            finishOrderButton.setEnabled(true);
            confirmButton.setEnabled(false);
            numItemsTextField.setEnabled(false);

            // update text fields and their labels
            bookIdLabel.setText("Enter Book ID for Item #" + (itemCount + 1) + ":       ");
            bookIdTextField.setText("");
            quantityLabel.setText("Enter quantity for Item #" + (itemCount + 1) + ":       ");
            quantityTextField.setText("");
            subtotalLabel.setText("Order subtotal for " + itemCount + " item(s)       ");
            subtotalTextField.setText("$" + new DecimalFormat("0.00").format(this.orderSubtotal));

            // added new feature to increment counter on button text
            processButton.setText("Process Item #" + (this.totalItems + 1));
            confirmButton.setText("Confirm Item #" + (this.totalItems + 1));

            // if the current item is the last item in order, don't reset fields
            if (itemCount >= numOfItemsInOrder) {
                bookIdLabel.setVisible(false);
                quantityLabel.setVisible(false);
                bookIdTextField.setEnabled(false);
                quantityTextField.setEnabled(false);
                processButton.setEnabled(false);
                confirmButton.setEnabled(false);
                processButton.setText("Process Item");
                confirmButton.setText("Confirm Item");
            }
        });

        viewOrderButton.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(null, this.viewOrder.toString());
        });

        finishOrderButton.addActionListener((ActionEvent e) -> {
            //write items to transactions.txt
            try {
                // print transactions to text file and show invoice
                this.printTransactions();
                JOptionPane.showMessageDialog(null, this.orderInvoice.toString());

            } 
            catch (IOException e1) {
            }
            
            // close frame
            BookStore.super.dispose();
        });

        newOrderButton.addActionListener((ActionEvent e) -> {
            BookStore.super.dispose(); //dispose frame
            //run main
            try {
                BookStore.main(null);
            } 
            catch (FileNotFoundException e1) {
            }
        });

        exitButton.addActionListener((ActionEvent e) -> {
            // close frame
            BookStore.super.dispose(); 
        });

    }

    // search for the book and return the book if the ID is found
    public Book bookSearch(int BookID) {
        for (int i = 0; i < this.inventory.size(); i++) {
            Book currentBook = inventory.get(i);
            if (currentBook.getBookID() == BookID) {
                return currentBook;
            }
        }
        return null;
    }

    public void parseInventory() throws FileNotFoundException {
        // initialize inventory
        this.inventory = new ArrayList<>();
        File inventoryFile = new File("inventory.txt");
        Scanner textFile = new Scanner(inventoryFile);

        // loop through file and set the book information
        while (textFile.hasNextLine()) {
            String book = textFile.nextLine();
            String[] bookInfo = book.split(",");
            
            Book currentBook = new Book();
            currentBook.setBookID(Integer.parseInt(bookInfo[0]));
            currentBook.setTitle(bookInfo[1]);
            currentBook.setPrice(Double.parseDouble(bookInfo[2]));

            inventory.add(currentBook);
        }

        textFile.close();
        
    }

    public void createInvoice(String date, String time) {
        this.addSalesTax();
        this.orderInvoice.append("Date: " + date + " " + time);
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append("Number of line items: " + this.totalItems);
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append("Item# / Title / ID / Price / Qty / Disc % / Subtotal:");
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append(this.viewOrder.toString());
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append("Order subtotal:   $" + new DecimalFormat("0.00").format(this.orderSubtotal));
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append("Tax rate:     6%");
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append("Tax amount:      $" + new DecimalFormat("0.00").format(.06 * this.orderSubtotal));
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append("Order total:      $" + new DecimalFormat("0.00").format(this.orderTotal));
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append(System.getProperty("line.separator"));
        this.orderInvoice.append("Thanks for shopping at the Ye Olde Book Shoppe!");
    }

    public void addToOrderList(String order) {
        viewOrder.append(this.totalItems + ". " + order);
        viewOrder.append(System.getProperty("line.separator"));
    }

    public double getTotalDiscount(int quantity, double bookPrice) {
        return ((bookPrice * quantity) - ((double) getDiscountPercentage(quantity) / 100) * bookPrice * quantity);
    }

    public int getDiscountPercentage(int quantity) {
        //0% discount
        if (quantity >= 1 && quantity <= 4) {
            return 0;
        }
        // 10% discount
        if (quantity >= 5 && quantity <= 9) {
            return 10;
        }
        // 15% discount
        if (quantity >= 10) {
            return 15;
        }

        return 0;
    }

    public void fileTransaction() {
        String transactionItem = itemInfo;
        items.add(transactionItem);
    }

    public void printTransactions() throws IOException {
        Date orderDate = Calendar.getInstance().getTime();
        SimpleDateFormat permutation = new SimpleDateFormat("yyMMddyyHHmm");
        SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a z");
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

        this.createInvoice(dateFormat.format(orderDate), time.format(orderDate));

        // if there isn't a transactions file, create one
        if (transactionsFile.exists() == false) {
            transactionsFile.createNewFile();
        }

        // append only if file exist
        PrintWriter transactionOutput = new PrintWriter(new FileWriter("transactions.txt", true));

        // write to file
        for (int i = 0; i < items.size(); i++) {
            transactionOutput.append(permutation.format(orderDate) + ", ");
            transactionOutput.append(this.items.get(i));
            transactionOutput.append(dateFormat.format(orderDate) + ", ");
            transactionOutput.append(time.format(orderDate));
            transactionOutput.println();
        }

        transactionOutput.flush();
        transactionOutput.close();
    }

    public void addToSubtotal(int quantity, double bookPrice) {
        this.orderSubtotal = this.orderSubtotal + this.getTotalDiscount(quantity, bookPrice);
    }

    public void addSalesTax() {
        this.orderTotal = this.orderSubtotal + (.06 * this.orderSubtotal);
    }
}
