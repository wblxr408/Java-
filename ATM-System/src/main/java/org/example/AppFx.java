package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * JavaFX ATM with term deposit features.
 */
public final class AppFx extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        AtmService svc = new AtmService(new SimpleAccount("User", new BigDecimal("1000.00")));
        NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());

        TabPane tabPane = new TabPane();

        // Tab 1: Basic Operations
        Tab tab1 = new Tab("Basic Operations");
        tab1.setClosable(false);
        tab1.setContent(createBasicOpsPane(svc, nf));

        // Tab 2: Term Deposits
        Tab tab2 = new Tab("Term Deposits");
        tab2.setClosable(false);
        tab2.setContent(createTermDepositPane(svc));

        tabPane.getTabs().addAll(tab1, tab2);

        stage.setScene(new Scene(tabPane, 550, 400));
        stage.setTitle("ATM System");
        stage.show();
    }

    private GridPane createBasicOpsPane(AtmService svc, NumberFormat nf) {
        Label lTitle = new Label("Simple ATM");
        lTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label lAmt = new Label("Amount:");
        TextField tAmt = new TextField();
        Label lBal = new Label("Balance: " + nf.format(svc.balance()));
        Label lMsg = new Label();
        lMsg.setWrapText(true);

        Button bDep = new Button("Deposit");
        Button bWdr = new Button("Withdraw");
        Button bQry = new Button("Query Balance");

        bDep.setOnAction(e -> {
            Result r = svc.deposit(parseAmount(tAmt.getText()));
            lMsg.setText(r.message);
            lBal.setText("Balance: " + nf.format(svc.balance()));
        });

        bWdr.setOnAction(e -> {
            Result r = svc.withdraw(parseAmount(tAmt.getText()));
            lMsg.setText(r.message);
            lBal.setText("Balance: " + nf.format(svc.balance()));
        });

        bQry.setOnAction(e -> {
            lMsg.setText("Current balance: " + nf.format(svc.balance()));
            lBal.setText("Balance: " + nf.format(svc.balance()));
        });

        GridPane root = new GridPane();
        root.setPadding(new Insets(16));
        root.setHgap(10);
        root.setVgap(10);
        root.add(lTitle, 0, 0, 3, 1);
        root.add(lAmt, 0, 1);
        root.add(tAmt, 1, 1, 2, 1);
        root.add(bDep, 0, 2);
        root.add(bWdr, 1, 2);
        root.add(bQry, 2, 2);
        root.add(lBal, 0, 3, 3, 1);
        root.add(lMsg, 0, 4, 3, 1);

        return root;
    }

    private VBox createTermDepositPane(AtmService svc) {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(16));

        Label title = new Label("Term Deposit Center");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Create deposit section
        Label lAmount = new Label("Deposit Amount:");
        TextField tAmount = new TextField();

        Label lTerm = new Label("Select Term:");
        ComboBox<String> cbTerm = new ComboBox<>();
        cbTerm.getItems().addAll(
                "3 months - " + InterestCalculator.getRateDisplay(3),
                "6 months - " + InterestCalculator.getRateDisplay(6),
                "12 months - " + InterestCalculator.getRateDisplay(12),
                "24 months - " + InterestCalculator.getRateDisplay(24),
                "36 months - " + InterestCalculator.getRateDisplay(36)
        );
        cbTerm.setValue(cbTerm.getItems().getFirst());

        Button bCreate = new Button("Create Term Deposit");
        Label lResult = new Label();
        lResult.setWrapText(true);

        TextArea taDeposits = new TextArea();
        taDeposits.setEditable(false);
        taDeposits.setPrefRowCount(8);

        Button bRefresh = new Button("Refresh List");
        Button bRedeem = new Button("Redeem Selected");
        TextField tRedeemIndex = new TextField();
        tRedeemIndex.setPromptText("Enter index to redeem");
        tRedeemIndex.setPrefWidth(150);

        bCreate.setOnAction(e -> {
            BigDecimal amt = parseAmount(tAmount.getText());
            if (amt == null) {
                lResult.setText("Invalid amount.");
                return;
            }

            String termStr = cbTerm.getValue().split(" ")[0];
            int term = Integer.parseInt(termStr);

            Result r = svc.createTermDeposit(amt, term);
            lResult.setText(r.message);
            if (r.ok) {
                tAmount.clear();
                updateDepositList(svc, taDeposits);
            }
        });

        bRefresh.setOnAction(e -> updateDepositList(svc, taDeposits));

        bRedeem.setOnAction(e -> {
            try {
                int idx = Integer.parseInt(tRedeemIndex.getText().trim());
                Result r = svc.redeemTermDeposit(idx);
                lResult.setText(r.message);
                if (r.ok) {
                    tRedeemIndex.clear();
                    updateDepositList(svc, taDeposits);
                }
            } catch (NumberFormatException ex) {
                lResult.setText("Invalid index number.");
            }
        });

        GridPane createPane = new GridPane();
        createPane.setHgap(10);
        createPane.setVgap(10);
        createPane.add(lAmount, 0, 0);
        createPane.add(tAmount, 1, 0);
        createPane.add(lTerm, 0, 1);
        createPane.add(cbTerm, 1, 1);
        createPane.add(bCreate, 1, 2);

        Separator sep1 = new Separator();
        Label lList = new Label("Active Term Deposits:");
        lList.setStyle("-fx-font-weight: bold;");

        GridPane redeemPane = new GridPane();
        redeemPane.setHgap(10);
        redeemPane.setVgap(10);
        redeemPane.add(tRedeemIndex, 0, 0);
        redeemPane.add(bRedeem, 1, 0);
        redeemPane.add(bRefresh, 2, 0);

        vbox.getChildren().addAll(
                title,
                createPane,
                lResult,
                sep1,
                lList,
                taDeposits,
                redeemPane
        );

        updateDepositList(svc, taDeposits);
        return vbox;
    }

    private void updateDepositList(AtmService svc, TextArea ta) {
        List<DepositAccount> deposits = svc.getActiveTermDeposits();
        if (deposits.isEmpty()) {
            ta.setText("No active term deposits.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < deposits.size(); i++) {
            DepositAccount d = deposits.get(i);
            sb.append(String.format("[%d] %s\n", i, d.toString()));
            sb.append(String.format("    Status: %s\n\n",
                    d.isMature() ? "✓ MATURE - Ready to redeem!" : "Locked until " + d.getMaturityDate()));
        }
        ta.setText(sb.toString());
    }

    private static BigDecimal parseAmount(String s) {
        if (s == null) return null;
        String v = s.trim();
        if (!v.matches("\\d+(\\.\\d{1,2})?")) return null;
        try {
            BigDecimal x = new BigDecimal(v).setScale(2, RoundingMode.DOWN);
            return x.signum() > 0 ? x : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}